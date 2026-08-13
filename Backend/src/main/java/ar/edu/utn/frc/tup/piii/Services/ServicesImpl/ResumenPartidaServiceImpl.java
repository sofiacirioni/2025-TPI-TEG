package ar.edu.utn.frc.tup.piii.Services.ServicesImpl;

import ar.edu.utn.frc.tup.piii.Dtos.ResumenPartidaDto;
import ar.edu.utn.frc.tup.piii.Entities.JugadorEntity;
import ar.edu.utn.frc.tup.piii.Entities.PactoEntity;
import ar.edu.utn.frc.tup.piii.Entities.PartidaEntity;
import ar.edu.utn.frc.tup.piii.Repositories.EstadoPaisRepository;
import ar.edu.utn.frc.tup.piii.Repositories.PactoRepository;
import ar.edu.utn.frc.tup.piii.Repositories.PartidaRepository;
import ar.edu.utn.frc.tup.piii.Services.ResumenPartidaService;
import ar.edu.utn.frc.tup.piii.models.EstadoPacto;
import ar.edu.utn.frc.tup.piii.models.EstadoPartida;
import ar.edu.utn.frc.tup.piii.models.TipoJugador;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ResumenPartidaServiceImpl implements ResumenPartidaService {

    /** Total de países del tablero, para el porcentaje del mapa. */
    private static final int PAISES_DEL_MAPA = 50;

    private final PartidaRepository partidaRepository;
    private final EstadoPaisRepository estadoPaisRepository;
    private final PactoRepository pactoRepository;

    @Override
    @Transactional(readOnly = true)
    public ResumenPartidaDto obtenerResumen(Long idPartida) {
        PartidaEntity partida = partidaRepository.findById(idPartida)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Partida no encontrada."));

        JugadorEntity ganador = partida.getGanador();

        List<ResumenPartidaDto.ComandanteResumenDto> comandantes = partida.getJugadores().stream()
                .map(j -> aComandante(j, ganador))
                // Ordenado por territorio: el que domina el mapa encabeza la pizarra.
                .sorted(Comparator
                        .comparing(ResumenPartidaDto.ComandanteResumenDto::isGanador).reversed()
                        .thenComparing(Comparator.comparingInt(
                                ResumenPartidaDto.ComandanteResumenDto::getPaises).reversed()))
                .toList();

        List<PactoEntity> pactos = pactoRepository.findByPartida_IdPartida(idPartida);
        // Un tratado cuenta como firmado si llegó a regir: sigue activo, se rompió
        // después o expiró con la partida. Los propuestos y rechazados no cuentan.
        int firmados = (int) pactos.stream()
                .filter(p -> p.getEstado() != EstadoPacto.PROPUESTO
                        && p.getEstado() != EstadoPacto.RECHAZADO)
                .count();
        int rotos = (int) pactos.stream()
                .filter(p -> p.getEstado() == EstadoPacto.ROTO_VOLUNTARIO
                        || p.getEstado() == EstadoPacto.ROTO_AUTOMATICO)
                .count();

        return ResumenPartidaDto.builder()
                .idPartida(partida.getIdPartida())
                .fecha(partida.getFechaInicio())
                .turnosJugados(partida.getTurnoActual() != null ? partida.getTurnoActual() : 0)
                .terminada(partida.getEstadoPartida() == EstadoPartida.TERMINADA)
                .ganadorNombre(ganador != null ? ganador.getNombre() : null)
                .ganadorColor(ganador != null && ganador.getColor() != null ? ganador.getColor().name() : null)
                .objetivoCumplido(ganador != null && ganador.getObjetivo() != null
                        ? ganador.getObjetivo().getDescripcion() : null)
                .tratadosFirmados(firmados)
                .tratadosRotos(rotos)
                .comandantes(comandantes)
                .build();
    }

    private ResumenPartidaDto.ComandanteResumenDto aComandante(JugadorEntity j, JugadorEntity ganador) {
        int paises = estadoPaisRepository.findEstadoPaisEntitiesByJugador_IdJugador(j.getIdJugador()).size();
        int ataques = valor(j.getAtaquesLanzados());
        int conquistas = valor(j.getConquistas());
        int abatidas = valor(j.getTropasAbatidas());
        int perdidas = valor(j.getTropasPerdidas());

        boolean esGanador = ganador != null && ganador.getIdJugador().equals(j.getIdJugador());

        return ResumenPartidaDto.ComandanteResumenDto.builder()
                .idJugador(j.getIdJugador())
                .nombre(j.getNombre())
                .color(j.getColor() != null ? j.getColor().name() : null)
                .avatarUrl(j.avatarUrl())
                .esBot(j.getTipoJugador() == TipoJugador.BOT)
                .paises(paises)
                .ejercitos(j.getEjercito() != null ? j.getEjercito() : 0)
                .porcentajeMapa(redondear((double) paises * 100 / PAISES_DEL_MAPA))
                .eliminado(j.isPerdio())
                .eliminadoPor(j.getEliminadoPorColor() != null ? j.getEliminadoPorColor().name() : null)
                .ganador(esGanador)
                .ataquesLanzados(ataques)
                .conquistas(conquistas)
                .defensasResistidas(valor(j.getDefensasResistidas()))
                .tropasAbatidas(abatidas)
                .tropasPerdidas(perdidas)
                .canjesRealizados(valor(j.getCanjesRealizados()))
                .efectividad(ataques > 0 ? redondear((double) conquistas * 100 / ataques) : 0)
                .ratioBajas(perdidas > 0 ? redondear((double) abatidas / perdidas) : 0)
                .build();
    }

    /** Las partidas anteriores a la migración V4 tienen los contadores en null. */
    private int valor(Integer n) {
        return n != null ? n : 0;
    }

    private double redondear(double d) {
        return Math.round(d * 10) / 10.0;
    }
}
