package ar.edu.utn.frc.tup.piii.Services.ServicesImpl;

import ar.edu.utn.frc.tup.piii.Dtos.HistorialComandanteDto;
import ar.edu.utn.frc.tup.piii.Dtos.ResumenPartidaDto;
import ar.edu.utn.frc.tup.piii.Entities.JugadorEntity;
import ar.edu.utn.frc.tup.piii.Entities.PactoEntity;
import ar.edu.utn.frc.tup.piii.Entities.PartidaEntity;
import ar.edu.utn.frc.tup.piii.Entities.UsuarioEntity;
import ar.edu.utn.frc.tup.piii.Repositories.JugadorRepository;
import ar.edu.utn.frc.tup.piii.Repositories.PactoRepository;
import ar.edu.utn.frc.tup.piii.Repositories.UsuarioRepository;
import ar.edu.utn.frc.tup.piii.Services.HistorialComandanteService;
import ar.edu.utn.frc.tup.piii.Services.ResumenPartidaService;
import ar.edu.utn.frc.tup.piii.models.EstadoPacto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Arma la hoja de servicios de un usuario sumando sus participaciones.
 *
 * <p>Cada fila de {@code jugadores} es una participación en una partida y ya
 * lleva sus contadores de combate (migración V4), así que el histórico sale de
 * sumarlas: no hace falta ninguna tabla de estadísticas aparte.
 *
 * <p>El puesto de cada campaña lo resuelve {@link ResumenPartidaService}, el
 * mismo servicio que alimenta la pizarra. Cuesta unas consultas de más, pero
 * garantiza que la medalla del perfil y el parte de campaña nunca se
 * contradigan: hay una sola definición de "quién salió segundo".
 */
@Service
@RequiredArgsConstructor
public class HistorialComandanteServiceImpl implements HistorialComandanteService {

    /**
     * La hoja de servicios es una hoja: lista las campañas más recientes, no
     * las cientos que puede acumular una cuenta vieja. Las cifras de arriba sí
     * cuentan todas.
     */
    private static final int LIMITE_REGISTRO = 10;

    private final UsuarioRepository usuarioRepository;
    private final JugadorRepository jugadorRepository;
    private final PactoRepository pactoRepository;
    private final ResumenPartidaService resumenPartidaService;

    @Override
    @Transactional(readOnly = true)
    public HistorialComandanteDto obtenerHistorial(Long idUsuario) {
        UsuarioEntity usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado."));

        List<JugadorEntity> participaciones = jugadorRepository.findParticipacionesHumanas(idUsuario).stream()
                .filter(j -> j.getPartida() != null)
                // De la campaña más reciente a la más vieja.
                .sorted(Comparator.comparing(this::fechaDeOrden).reversed())
                .toList();

        List<HistorialComandanteDto.CampaniaDto> campanias = new ArrayList<>();
        Set<Long> partidasConcluidas = new HashSet<>();

        int libradas = 0, victorias = 0, segundos = 0, terceros = 0;
        int ataques = 0, conquistas = 0, defensas = 0;
        int abatidas = 0, perdidas = 0, canjes = 0, comandantesAbatidos = 0;

        for (JugadorEntity participacion : participaciones) {
            PartidaEntity partida = participacion.getPartida();
            ResumenPartidaDto resumen = resumenPartidaService.obtenerResumen(partida.getIdPartida());
            List<ResumenPartidaDto.ComandanteResumenDto> clasificacion = resumen.getComandantes();

            int puesto = puestoEn(clasificacion, participacion.getIdJugador());
            if (puesto == 0) {
                // La partida no reconoce esta participación: no hay nada que contar.
                continue;
            }
            ResumenPartidaDto.ComandanteResumenDto yo = clasificacion.get(puesto - 1);
            boolean terminada = resumen.isTerminada();

            campanias.add(HistorialComandanteDto.CampaniaDto.builder()
                    .idPartida(partida.getIdPartida())
                    .fecha(resumen.getFecha())
                    .terminada(terminada)
                    .estado(partida.getEstadoPartida() != null
                            ? partida.getEstadoPartida().name() : null)
                    .puesto(terminada ? puesto : 0)
                    .comandantes(clasificacion.size())
                    .turnosJugados(resumen.getTurnosJugados())
                    .color(yo.getColor())
                    .ganador(yo.isGanador())
                    .eliminado(yo.isEliminado())
                    .ganadorNombre(resumen.getGanadorNombre())
                    .build());

            // Una campaña a medio jugar figura en el registro pero no suma al
            // historial: sus cifras todavía pueden cambiar.
            if (!terminada) {
                continue;
            }
            partidasConcluidas.add(partida.getIdPartida());

            libradas++;
            if (puesto == 1)      victorias++;
            else if (puesto == 2) segundos++;
            else if (puesto == 3) terceros++;

            ataques += yo.getAtaquesLanzados();
            conquistas += yo.getConquistas();
            defensas += yo.getDefensasResistidas();
            abatidas += yo.getTropasAbatidas();
            perdidas += yo.getTropasPerdidas();
            canjes += yo.getCanjesRealizados();
            comandantesAbatidos += abatidosPor(clasificacion, yo);
        }

        List<Long> idsParticipacion = participaciones.stream().map(JugadorEntity::getIdJugador).toList();
        List<PactoEntity> pactos = idsParticipacion.isEmpty()
                ? List.of()
                : pactoRepository.findByJugadorIdIn(idsParticipacion).stream()
                        .filter(p -> p.getPartida() != null
                                && partidasConcluidas.contains(p.getPartida().getIdPartida()))
                        .toList();

        // Firmado = llegó a regir. Los propuestos y los rechazados nunca lo hicieron.
        int firmados = (int) pactos.stream()
                .filter(p -> p.getEstado() != EstadoPacto.PROPUESTO
                        && p.getEstado() != EstadoPacto.RECHAZADO)
                .count();
        // Acá "roto" es más estricto que en la pizarra: son los que rompió este
        // usuario, no los que se rompieron durante la partida.
        int rotos = (int) pactos.stream()
                .filter(p -> p.getTurnoRupturaJugador() != null
                        && idsParticipacion.contains(p.getTurnoRupturaJugador()))
                .count();

        return HistorialComandanteDto.builder()
                .idUsuario(usuario.getIdUsuario())
                .usuario(usuario.getUsuario())
                .correo(usuario.getCorreo())
                .imagen(usuario.getImagen())
                .fechaAlta(usuario.getFechaAlta())
                // Se calcula sobre todas las participaciones, no sobre el
                // registro recortado que viaja en el DTO.
                .divisaHabitual(divisaHabitual(participaciones))
                .campaniasLibradas(libradas)
                .victorias(victorias)
                .segundosPuestos(segundos)
                .terceroPuestos(terceros)
                .tasaVictoria(porcentaje(victorias, libradas))
                .ataquesLanzados(ataques)
                .conquistas(conquistas)
                .efectividad(porcentaje(conquistas, ataques))
                .defensasResistidas(defensas)
                .tropasAbatidas(abatidas)
                .tropasPerdidas(perdidas)
                .ratioBajas(perdidas > 0 ? redondear((double) abatidas / perdidas) : 0)
                .canjesRealizados(canjes)
                .comandantesAbatidos(comandantesAbatidos)
                .tratadosFirmados(firmados)
                .tratadosRotos(rotos)
                .campanias(campanias.stream().limit(LIMITE_REGISTRO).toList())
                .build();
    }

    /** El color con el que más veces se presentó a jugar. */
    private String divisaHabitual(List<JugadorEntity> participaciones) {
        Map<String, Integer> conteo = new LinkedHashMap<>();
        for (JugadorEntity p : participaciones) {
            if (p.getColor() != null) {
                conteo.merge(p.getColor().name(), 1, Integer::sum);
            }
        }
        return conteo.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);
    }

    /** Posición en la clasificación (1 = vencedor); 0 si el jugador no está. */
    private int puestoEn(List<ResumenPartidaDto.ComandanteResumenDto> clasificacion, Long idJugador) {
        for (int i = 0; i < clasificacion.size(); i++) {
            if (Objects.equals(clasificacion.get(i).getIdJugador(), idJugador)) {
                return i + 1;
            }
        }
        return 0;
    }

    /** Rivales que quedaron eliminados por el color de este comandante. */
    private int abatidosPor(List<ResumenPartidaDto.ComandanteResumenDto> clasificacion,
                            ResumenPartidaDto.ComandanteResumenDto yo) {
        if (yo.getColor() == null) {
            return 0;
        }
        return (int) clasificacion.stream()
                .filter(otro -> !Objects.equals(otro.getIdJugador(), yo.getIdJugador()))
                .filter(otro -> yo.getColor().equals(otro.getEliminadoPor()))
                .count();
    }

    /**
     * Fecha con la que se ordena el registro. Desde la migración V5 toda partida
     * tiene fecha de alta con hora, que es lo que permite ordenar dos campañas
     * del mismo día; el resto de los casos son partidas anteriores a esa
     * migración o entidades armadas a mano en los tests.
     */
    private LocalDateTime fechaDeOrden(JugadorEntity participacion) {
        PartidaEntity partida = participacion.getPartida();
        if (partida.getFechaAlta() != null) {
            return partida.getFechaAlta();
        }
        if (partida.getFechaInicio() != null) {
            return partida.getFechaInicio().atStartOfDay();
        }
        return LocalDateTime.MIN;
    }

    private double porcentaje(int parte, int total) {
        return total > 0 ? redondear((double) parte * 100 / total) : 0;
    }

    private double redondear(double d) {
        return Math.round(d * 10) / 10.0;
    }
}
