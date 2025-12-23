package ar.edu.utn.frc.tup.piii.Services.ServicesImpl;

import ar.edu.utn.frc.tup.piii.Dtos.EstadisticaDto;
import ar.edu.utn.frc.tup.piii.Entities.EstadisticaEntity;
import ar.edu.utn.frc.tup.piii.Entities.JugadorEntity;
import ar.edu.utn.frc.tup.piii.Entities.PartidaEntity;
import ar.edu.utn.frc.tup.piii.Repositories.EstadisticaRepository;
import ar.edu.utn.frc.tup.piii.Repositories.EstadoPaisRepository;
import ar.edu.utn.frc.tup.piii.Repositories.JugadorRepository;
import ar.edu.utn.frc.tup.piii.Services.EstadisticaService;
import ar.edu.utn.frc.tup.piii.models.Estadistica;
import ar.edu.utn.frc.tup.piii.models.Jugador;
import ar.edu.utn.frc.tup.piii.models.*;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EstadisticaServiceImpl implements EstadisticaService {

    private final EstadoPaisRepository estadoPaisRepository;
    private final EstadisticaRepository estadisticaRepository;
    private final JugadorRepository jugadorRepository;
    private final ModelMapper modelMapper;

    @Override
    public void saveEstadisticas(Jugador jugador) {
        Estadistica estadistica = new Estadistica();

        estadistica.setJugador(jugador);
        estadistica.setFechaPartida(String.valueOf(jugador.getPartida().getFechaInicio()));
        estadistica.setPaisesConquistados(estadoPaisRepository.findPaisesConquistados(jugador).size());
        estadistica.setPaisesPerdidos(estadoPaisRepository.findPaisesPerdidos(jugador).size());
        estadistica.setGanador(!jugador.isPerdio());

        EstadisticaEntity estadisticaEntity = modelMapper.map(estadistica, EstadisticaEntity.class);

        estadisticaRepository.save(estadisticaEntity);
    }

    @Override
    public void allSaveEstadisticas(List<Jugador> jugadores) {
        jugadores.forEach(this::saveEstadisticas);
    }

    @Override
    public List<EstadisticaDto> getEstadisticas(Long idUsuario) {

        JugadorEntity jugador = jugadorRepository.findByUsuario_IdUsuario(idUsuario);

        List<EstadisticaEntity> lstEstadistica = estadisticaRepository.findByJugador(jugador);

        return lstEstadistica.stream().map(this::convertirEstadistica).toList();
    }

    public EstadisticaDto convertirEstadistica(EstadisticaEntity estadistica) {
        return EstadisticaDto.builder()
                .id(estadistica.getIdEstadistica())
                .colorJugador(String.valueOf(estadistica.getJugador().getColor()))
                .idPartida(estadistica.getPartida().getIdPartida())
                .fecha(LocalDate.parse(estadistica.getFechaPartida()))
                .paisesConquistados(estadistica.getPaisesConquistados())
                .paisesPerdidos(estadistica.getPaisesPerdidos())
                .ganador(estadistica.isGanador())
                .porcentajeMundo(calcularPorcentajeMundo(estadistica.getPaisesConquistados()))
                .build();
    }

    public double calcularPorcentajeMundo(int paisesConquistados){
        return (double) (paisesConquistados * 100) / 50;
    }


    public void registrarEvento(String evento, Jugador jugador, Partida partida) {
        EstadisticaEntity estadistica = new EstadisticaEntity();
        estadistica.setJugador(modelMapper.map(jugador, JugadorEntity.class));
        estadistica.setPartida(modelMapper.map(partida, PartidaEntity.class));
        estadistica.setEvento(evento);
        estadistica.setFechaPartida(LocalDate.now().toString());

        estadisticaRepository.save(estadistica);
    }
}
