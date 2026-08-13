package ar.edu.utn.frc.tup.piii.Services.ServicesImpl;

import ar.edu.utn.frc.tup.piii.Dtos.CanjeTarjetasDto;
import ar.edu.utn.frc.tup.piii.Dtos.EstadoPaises.AgregarFichas;
import ar.edu.utn.frc.tup.piii.Dtos.EstadoPaises.Ataque;
import ar.edu.utn.frc.tup.piii.Dtos.EstadoPaises.EstadoPaisFicha;
import ar.edu.utn.frc.tup.piii.Entities.EstadoPaisEntity;
import ar.edu.utn.frc.tup.piii.Entities.EstadoTarjetaEntity;
import ar.edu.utn.frc.tup.piii.Entities.JugadorEntity;
import ar.edu.utn.frc.tup.piii.Entities.PactoEntity;
import ar.edu.utn.frc.tup.piii.Repositories.EstadoPaisRepository;
import ar.edu.utn.frc.tup.piii.Repositories.EstadoTarjetaRepository;
import ar.edu.utn.frc.tup.piii.Repositories.JugadorRepository;
import ar.edu.utn.frc.tup.piii.Repositories.PactoRepository;
import ar.edu.utn.frc.tup.piii.Services.BotService;
import ar.edu.utn.frc.tup.piii.Services.EstadoPaisService;
import ar.edu.utn.frc.tup.piii.Services.PactoService;
import ar.edu.utn.frc.tup.piii.Services.TurnoService;
import ar.edu.utn.frc.tup.piii.models.EstadoPacto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Lógica de juego para jugadores bot (dificultad básica).
 * Cada turno se ejecuta de forma asíncrona con 3 s de cooldown entre fases,
 * para que el frontend pueda percibir las acciones del bot en tiempo real.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BotServiceImpl implements BotService {

    private final JugadorRepository jugadorRepository;
    private final EstadoPaisRepository estadoPaisRepository;
    private final EstadoTarjetaRepository estadoTarjetaRepository;
    private final PactoRepository pactoRepository;
    private final EstadoPaisService estadoPaisService;
    private final PactoService pactoService;

    /** @Lazy rompe la dependencia circular con TurnoServiceImpl */
    @Lazy
    @Autowired
    private TurnoService turnoService;

    /** Pausa entre fases del bot (ms). Package-private para poder sobreescribirlo en tests. */
    int cooldownMs = 1500;

    /**
     * Pausa entre acciones sueltas dentro de una misma fase (ms).
     *
     * <p>Sin esto el bot colocaba todos sus ejércitos y lanzaba todos sus
     * ataques en el mismo instante: en pantalla era un parpadeo del que no se
     * alcanzaba a leer nada. Ahora cada acción se ve por separado.
     */
    int pausaEntreAccionesMs = 2000;

    /**
     * Tropas que el bot conserva en el país desde el que ataca.
     *
     * <p>Sin este piso el bot atacaba hasta quedarse con una sola tropa en
     * todos sus países, que es exactamente el estado en el que cualquiera se
     * lo lleva puesto en el turno siguiente. Seguía siendo fácil de ganar,
     * pero además se suicidaba solo.
     */
    private static final int TROPAS_MINIMAS_EN_ORIGEN = 3;

    /**
     * Diferencia de países a partir de la cual el bot desconfía del proponente
     * y le rechaza el pacto. Por debajo de eso firma sin hacer preguntas.
     */
    private static final int VENTAJA_QUE_INCOMODA = 3;

    /**
     * Ejecuta el turno completo del bot de forma asíncrona:
     *   1. Fase INCORPORACION: distribuye ejércitos aleatoriamente.
     *   2. Avanza a ATAQUE (cooldown).
     *   3. Fase ATAQUE: ataca vecinos con menos tropas que el origen.
     *   4. Avanza a REAGRUPACION (cooldown).
     *   5. Avanza desde REAGRUPACION → pasa al siguiente jugador.
     */
    @Override
    @Async
    public void executeTurnAsync(Long idJugador) {
        Long partidaId = null;
        try {
            JugadorEntity bot = jugadorRepository.findByIdJugador(idJugador)
                    .orElseThrow(() -> new IllegalArgumentException("Bot no encontrado: " + idJugador));
            partidaId = bot.getPartida().getIdPartida();

            // --- DIPLOMACIA ---
            // Antes de jugar, contesta las propuestas que le hayan quedado pendientes.
            responderPactosPendientes(idJugador, partidaId);

            // --- INCORPORACION ---
            realizarIncorporacion(idJugador);
            Thread.sleep(cooldownMs);

            // Avanzar INCORPORACION → ATAQUE
            turnoService.cambiarFaseTurno(partidaId);
            Thread.sleep(cooldownMs);

            // --- ATAQUE ---
            realizarAtaque(idJugador);
            Thread.sleep(cooldownMs);

            // Avanzar ATAQUE → REAGRUPACION
            turnoService.cambiarFaseTurno(partidaId);
            Thread.sleep(cooldownMs);

            // --- REAGRUPACION → siguiente jugador ---
            turnoService.cambiarFaseTurno(partidaId);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            log.error("Error en turno del bot {}: {}", idJugador, e.getMessage());
            // Recuperación: intentar avanzar las fases restantes para desbloquear el juego
            if (partidaId != null) {
                for (int i = 0; i < 3; i++) {
                    try {
                        turnoService.cambiarFaseTurno(partidaId);
                        Thread.sleep(200);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    } catch (Exception ignored) {
                        // Fase ya avanzada o partida terminada
                    }
                }
            }
        }
    }

    /**
     * Contesta las propuestas de pacto que tenga pendientes, una por vez.
     *
     * <p>Sin esto las propuestas al bot quedaban en PROPUESTO para siempre: el
     * humano proponía y no pasaba nada nunca, ni aceptación ni rechazo.
     *
     * <p>El criterio es a propósito de una sola línea, como el resto del bot:
     * firma con cualquiera, salvo con quien ya le saca {@value #VENTAJA_QUE_INCOMODA}
     * países de ventaja. Un bot no le regala inmunidad al que va ganando.
     */
    private void responderPactosPendientes(Long idJugador, Long partidaId) throws InterruptedException {
        List<PactoEntity> pendientes;
        try {
            pendientes = pactoRepository.findByPartida_IdPartidaAndEstado(partidaId, EstadoPacto.PROPUESTO)
                    .stream()
                    .filter(p -> p.getJugadorB() != null
                            && idJugador.equals(p.getJugadorB().getIdJugador()))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.warn("Bot {}: no se pudieron leer los pactos pendientes: {}", idJugador, e.getMessage());
            return;
        }

        int misPaises = estadoPaisRepository.findByJugador_IdJugador(idJugador).size();

        for (PactoEntity pacto : pendientes) {
            Thread.sleep(pausaEntreAccionesMs);
            try {
                int paisesDelProponente = pacto.getJugadorA() == null ? 0
                        : estadoPaisRepository
                                .findByJugador_IdJugador(pacto.getJugadorA().getIdJugador()).size();

                if (paisesDelProponente > misPaises + VENTAJA_QUE_INCOMODA) {
                    pactoService.rechazar(pacto.getId(), idJugador);
                    log.info("Bot {} rechazó el pacto {}", idJugador, pacto.getId());
                } else {
                    pactoService.aceptar(pacto.getId(), idJugador);
                    log.info("Bot {} aceptó el pacto {}", idJugador, pacto.getId());
                }
            } catch (Exception e) {
                // Una propuesta ya resuelta o inválida no puede frenar el turno.
                log.warn("Bot {}: no pudo responder el pacto {}: {}",
                        idJugador, pacto.getId(), e.getMessage());
            }
        }
    }

    /**
     * Distribuye todos los ejércitos disponibles del bot en sus países de forma aleatoria.
     * Primero canjea tarjetas si tiene 5 o más (canje obligatorio según reglas TEG).
     * Cada llamada a agregarFichas tiene su propia transacción a través del proxy de TurnoService.
     */
    private void realizarIncorporacion(Long idJugador) throws InterruptedException {
        realizarCanjeSiNecesario(idJugador);
        JugadorEntity bot = jugadorRepository.findByIdJugador(idJugador)
                .orElseThrow(() -> new IllegalArgumentException("Bot no encontrado: " + idJugador));
        int ejercitosRestantes = bot.getEjercito();
        if (ejercitosRestantes <= 0) return;

        List<EstadoPaisEntity> misPaises = estadoPaisRepository.findByJugador_IdJugador(idJugador);
        if (misPaises.isEmpty()) return;

        Random random = new Random();
        boolean primera = true;
        while (ejercitosRestantes > 0) {
            // La pausa va antes de cada colocación menos la primera: así se ve
            // llegar refuerzo por refuerzo en vez de todos de golpe.
            if (!primera) {
                Thread.sleep(pausaEntreAccionesMs);
            }
            primera = false;

            EstadoPaisEntity pais = misPaises.get(random.nextInt(misPaises.size()));
            // Asignar entre 1 y todos los ejércitos restantes
            int cantidad = ejercitosRestantes == 1 ? 1 : (random.nextInt(ejercitosRestantes) + 1);

            turnoService.agregarFichas(AgregarFichas.builder()
                    .idJugador(idJugador)
                    .paisesFichas(List.of(EstadoPaisFicha.builder()
                            .idPais(pais.getPais().getIdPais())
                            .cantidadFichas((long) cantidad)
                            .build()))
                    .build());

            ejercitosRestantes -= cantidad;
        }
    }

    /**
     * Canjea tarjetas si el bot tiene 5 o más (canje obligatorio en TEG).
     * Busca la primera combinación válida: 3 del mismo símbolo o 3 símbolos distintos.
     *
     * <p>Nunca deja escapar una excepción: si el canje falla, el bot igual tiene
     * que jugar su turno. Un canje roto lo dejaba mudo el resto de la partida.
     */
    private void realizarCanjeSiNecesario(Long idJugador) {
        try {
            buscarYCanjear(idJugador);
        } catch (Exception e) {
            log.warn("Bot {}: no se pudo evaluar el canje, sigue el turno: {}", idJugador, e.getMessage());
        }
    }

    private void buscarYCanjear(Long idJugador) {
        List<EstadoTarjetaEntity> disponibles = estadoTarjetaRepository.findByJugadorId(idJugador)
                .stream().filter(c -> !c.isCanjeada()).collect(Collectors.toList());
        if (disponibles.size() < 5) return;

        // Buscar la primera combinación válida
        for (int i = 0; i < disponibles.size() - 2; i++) {
            for (int j = i + 1; j < disponibles.size() - 1; j++) {
                for (int k = j + 1; k < disponibles.size(); k++) {
                    String s1 = disponibles.get(i).getTarjeta().getSimbolo().name();
                    String s2 = disponibles.get(j).getTarjeta().getSimbolo().name();
                    String s3 = disponibles.get(k).getTarjeta().getSimbolo().name();
                    // HashSet y no Set.of: Set.of revienta con elementos repetidos,
                    // y dos tarjetas del mismo símbolo son justamente lo que hay que
                    // contar acá. Eso rompía el turno entero del bot.
                    Set<String> simbolos = new HashSet<>(List.of(s1, s2, s3));
                    if (simbolos.size() == 1 || simbolos.size() == 3) {
                        CanjeTarjetasDto dto = new CanjeTarjetasDto(
                                List.of(disponibles.get(i).getIdEstadoTarjeta(),
                                        disponibles.get(j).getIdEstadoTarjeta(),
                                        disponibles.get(k).getIdEstadoTarjeta()),
                                idJugador);
                        try {
                            turnoService.validarCanjeTarjetas(dto);
                            log.info("Bot {} canjeó tarjetas obligatoriamente", idJugador);
                        } catch (Exception e) {
                            log.warn("Bot {}: error en canje obligatorio: {}", idJugador, e.getMessage());
                        }
                        return; // Solo un canje por turno
                    }
                }
            }
        }
    }

    /**
     * Ataca países vecinos con menos tropas que el atacante, conservando
     * siempre {@value #TROPAS_MINIMAS_EN_ORIGEN} tropas en el país de origen.
     * Lee entidades frescas antes de cada ataque para evitar referencias obsoletas
     * (conquistas previas en el mismo turno podrían cambiar la propiedad de los países).
     */
    private void realizarAtaque(Long idJugador) throws InterruptedException {
        List<EstadoPaisEntity> misPaises = estadoPaisRepository.findByJugador_IdJugador(idJugador);
        boolean primerAtaque = true;

        for (EstadoPaisEntity pais : misPaises) {
            // Lectura fresca para obtener tropas actualizadas
            EstadoPaisEntity origen = estadoPaisRepository.findById(pais.getIdEstadoPais()).orElse(null);
            if (origen == null || origen.getCantidadTropas() < TROPAS_MINIMAS_EN_ORIGEN) continue;

            // Vecinos enemigos ordenados por menos tropas (más fáciles primero)
            List<EstadoPaisEntity> atacables = estadoPaisService
                    .getLimitesEstadoPaisEntity(origen.getIdEstadoPais())
                    .stream()
                    .filter(v -> !v.getJugador().getIdJugador().equals(idJugador))
                    .sorted(Comparator.comparingInt(EstadoPaisEntity::getCantidadTropas))
                    .collect(Collectors.toList());

            for (EstadoPaisEntity objetivo : atacables) {
                // Re-leer origen para tropas actuales tras ataques previos
                EstadoPaisEntity origenActual = estadoPaisRepository.findById(origen.getIdEstadoPais()).orElse(null);
                if (origenActual == null || origenActual.getCantidadTropas() < TROPAS_MINIMAS_EN_ORIGEN) break;

                // Re-leer objetivo para verificar que sigue siendo enemigo
                EstadoPaisEntity destActual = estadoPaisRepository.findById(objetivo.getIdEstadoPais()).orElse(null);
                if (destActual == null || destActual.getJugador().getIdJugador().equals(idJugador)) continue;

                // Sólo ataca cuando tiene ventaja. No es estrategia: es no
                // regalar tropas en un ataque que ya sabe perdido.
                if (origenActual.getCantidadTropas() <= destActual.getCantidadTropas()) continue;

                // Un ataque cada dos segundos: encadenados sin pausa, el humano
                // no llega a ver de dónde a dónde fue cada uno.
                if (!primerAtaque) {
                    Thread.sleep(pausaEntreAccionesMs);
                }
                primerAtaque = false;

                try {
                    turnoService.ataque(new Ataque(
                            idJugador,
                            origenActual.getPais().getIdPais(),
                            destActual.getPais().getIdPais(),
                            null));
                } catch (Exception e) {
                    // Ataque inválido (p. ej. territorio conquistado justo antes): continuar
                    log.debug("Ataque de bot {} fallido: {}", idJugador, e.getMessage());
                }
            }
        }
    }
}
