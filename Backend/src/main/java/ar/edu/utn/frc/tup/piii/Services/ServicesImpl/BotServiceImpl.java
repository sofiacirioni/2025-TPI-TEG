package ar.edu.utn.frc.tup.piii.Services.ServicesImpl;

import ar.edu.utn.frc.tup.piii.Dtos.EstadoPaises.AgregarFichas;
import ar.edu.utn.frc.tup.piii.Dtos.EstadoPaises.Ataque;
import ar.edu.utn.frc.tup.piii.Dtos.EstadoPaises.EstadoPaisFicha;
import ar.edu.utn.frc.tup.piii.Dtos.UsarTarjetaEnPaisDto;
import ar.edu.utn.frc.tup.piii.Entities.*;
import ar.edu.utn.frc.tup.piii.Repositories.EstadoPaisRepository;
import ar.edu.utn.frc.tup.piii.Repositories.EstadoTarjetaRepository;
import ar.edu.utn.frc.tup.piii.Repositories.JugadorRepository;
import ar.edu.utn.frc.tup.piii.Services.BotService;
import ar.edu.utn.frc.tup.piii.Services.EstadoPaisService;
import ar.edu.utn.frc.tup.piii.Services.TurnoService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import java.util.Objects;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class BotServiceImpl implements BotService {

    @Autowired
    private JugadorRepository jugadorRepository;

    @Autowired
    private EstadoPaisRepository estadoPaisRepository;

    @Autowired
    private EstadoPaisService estadoPaisService;

    @Autowired
    private TurnoService turnoService;

    @Autowired
    private EstadoTarjetaRepository estadoTarjetaRepository;

    @Override
    @Transactional
    public boolean turnoBot(Long idJugador) {
        JugadorEntity botEntity = jugadorRepository.findByIdJugador(idJugador).orElseThrow(
                () -> new IllegalArgumentException("Jugador no encontrado con ID: " + idJugador));

        if (botEntity.getEjercito() > 0) {
            faseDefensa(botEntity);
        }

        if (botEntity.getPartida().getHostilidad()) {
            if (faseAtaque(botEntity)) {
                pedirCarta(botEntity);
                if (botEntity.getTarjetas().size() == 5) {
                    canjearCarta(botEntity);
                }
                faseReagrupar(botEntity);
            }
        }

        return true;
    }

    @Override
    @Transactional
    public boolean faseDefensa(JugadorEntity botEntity) {
        Random random = new Random();

        List<EstadoPaisEntity> paises = estadoPaisRepository.findByJugador_IdJugador(botEntity.getIdJugador());

        while (botEntity.getEjercito() > 0) {
            EstadoPaisEntity pais = paises.get(random.nextInt(paises.size()));
            int cantidad = random.nextInt(botEntity.getEjercito());

            EstadoPaisFicha estadoPaisFicha = EstadoPaisFicha.builder()
                    .idPais(pais.getIdEstadoPais())
                    .cantidadFichas((long) cantidad)
                    .build();

            AgregarFichas agregarFichas = AgregarFichas.builder()
                    .idJugador(botEntity.getIdJugador())
                    .paisesFichas(List.of(estadoPaisFicha))
                    .build();

            turnoService.agregarFichas(agregarFichas);

        }

        estadoPaisRepository.saveAll(paises);
        jugadorRepository.save(botEntity);

        turnoService.cambiarFaseTurno(botEntity.getPartida().getIdPartida());
        return true;
    }

    @Override
    @Transactional
    public boolean faseAtaque(JugadorEntity botEntity) {
        boolean ataqueExitoso = false;

        List<EstadoPaisEntity> paises = estadoPaisRepository.findByJugador_IdJugador(botEntity.getIdJugador());

        // Recorre todos los paises del bot
        for (EstadoPaisEntity pais : paises) {
            // Mientras tenga tropas para atacar (> 1)
            while (pais.getCantidadTropas() > 1) {
                List<EstadoPaisEntity> paisesLimites = estadoPaisService
                        .getLimitesEstadoPaisEntity(pais.getIdEstadoPais());

                // Filtra quellos paises limitrofes que si puede atacar
                List<EstadoPaisEntity> listaPaisesAtacables = new ArrayList<>(paisesLimites);
                listaPaisesAtacables.removeIf(
                        paisLimite -> Objects.equals(paisLimite.getJugador().getIdJugador(), botEntity.getIdJugador()));

                if (listaPaisesAtacables.isEmpty()) {
                    break; // No hay a quien atacar desde este pais
                }

                // Elige un objetivo (el primero o aleatorio es mejor, pero mantenemos simple
                // por ahora)
                // Mejora: Atacar al que tenga menos tropas para maximizar chance
                EstadoPaisEntity objetivo = listaPaisesAtacables.stream()
                        .min((p1, p2) -> Integer.compare(p1.getCantidadTropas(), p2.getCantidadTropas()))
                        .orElse(listaPaisesAtacables.get(0));

                Ataque ataque = new Ataque(botEntity.getIdJugador(), pais.getPais().getIdPais(),
                        objetivo.getPais().getIdPais(), null);

                var resultado = turnoService.ataque(ataque);
                ataqueExitoso = resultado.isAtaqueExitoso() || ataqueExitoso;

                // Si el pais origen se queda sin tropas para seguir atacando
                if (pais.getCantidadTropas() <= 1)
                    break;

                // Si conquisto el objetivo, ya no es atacable (es propio), bucle continua y
                // elegira otro o parara
                // Pero si conquisto, el objetivo ahora es del bot.
                // Refrescamos estado del pais origen? JPA deberia manejarlo, pero `pais` es una
                // entidad cargada.
                // Al atacar, TurnoService modifica la entidad en DB. `pais` en memoria podria
                // estar desactualizado?
                // TurnoService modifica las entidades gestionadas. Si estamos en la misma
                // transaccion, deberia verse.
                // Pero `pais` fue leido al inicio.

                // Refrescamos o confiamos en que TurnoService actualiza las referencias?
                // En JPA, si TurnoService carga y guarda, nuestra instancia `pais` podria
                // quedar "stale" si no es la misma instancia gestionada.
                // Dado que estamos en @Transactional, el entity manager es el mismo.
                // Sin embargo, TurnoService hace `estadoPaisRepository.findBy...` lo que puede
                // traer otra instancia si no esta en cache L1?
                // Mejor recargar el pais origen dsp del ataque para asegurar troops count
                // correctos.

                pais = estadoPaisRepository.findById(pais.getIdEstadoPais()).orElseThrow();
            }
        }

        turnoService.cambiarFaseTurno(botEntity.getPartida().getIdPartida());
        return ataqueExitoso;
    }

    @Override
    @Transactional
    public boolean faseReagrupar(JugadorEntity botEntity) {

        turnoService.cambiarFaseTurno(botEntity.getPartida().getIdPartida());
        return false;
    }

    @Override
    @Transactional
    public boolean pedirCarta(JugadorEntity botEntity) {
        turnoService.entregarTarjetaSiCorresponde(botEntity.getIdJugador(), botEntity.getPartida().getIdPartida());

        List<EstadoPaisEntity> ePaisesE = estadoPaisRepository.findByJugador_IdJugador(botEntity.getIdJugador());

        List<PaisEntity> paises = ePaisesE.stream()
                .map(EstadoPaisEntity::getPais)
                .toList();

        List<EstadoTarjetaEntity> tarjetas = estadoTarjetaRepository.findByJugador_IdJugador(botEntity.getIdJugador());

        List<EstadoTarjetaEntity> tarjetasSinUsar = tarjetas.stream()
                .filter(t -> !t.isUsada() && paises.contains(t.getTarjeta().getPais())).toList();

        for (EstadoTarjetaEntity t : tarjetasSinUsar) {
            usarCarta(botEntity, t);
        }

        return true;
    }

    @Override
    @Transactional
    public boolean usarCarta(JugadorEntity botEntity, EstadoTarjetaEntity estadoTarjetaEntity) {

        UsarTarjetaEnPaisDto dto = UsarTarjetaEnPaisDto.builder()
                .idJugador(botEntity.getIdJugador())
                .idTarjeta(estadoTarjetaEntity.getIdEstadoTarjeta())
                .build();

        turnoService.validarUsarTarjetaEnPais(dto);
        return false;
    }

    @Override
    @Transactional
    public boolean canjearCarta(JugadorEntity botEntity) {
        /*
         * List<EstadoTarjetaEntity> tarjetasIguales =
         * botEntity.getTarjetas().stream().filter()
         */

        return false;
    }

    /*
     * private calcularCombinacionesCanje(cartas: EstadoTarjetaDto[]): void {
     * this.puedeCanjear = false;
     * this.combinacionesPosibles = [];
     * 
     * if (!cartas || cartas.length < 3) return;
     * 
     * const disponibles = cartas.filter(c => !c.canjeada);
     * 
     * // Generar todas las combinaciones posibles de 3 cartas
     * for (let i = 0; i < disponibles.length - 2; i++) {
     * for (let j = i + 1; j < disponibles.length - 1; j++) {
     * for (let k = j + 1; k < disponibles.length; k++) {
     * const combo = [disponibles[i], disponibles[j], disponibles[k]];
     * const simbolos = combo.map(c => c.tarjeta?.simbolo).filter(Boolean);
     * 
     * if (simbolos.length === 3) {
     * const set = new Set(simbolos);
     * if (set.size === 1 || set.size === 3) {
     * this.combinacionesPosibles.push(combo);
     * }
     * }
     * }
     * }
     * }
     * 
     * this.puedeCanjear = this.combinacionesPosibles.length > 0;
     * }
     */
}
