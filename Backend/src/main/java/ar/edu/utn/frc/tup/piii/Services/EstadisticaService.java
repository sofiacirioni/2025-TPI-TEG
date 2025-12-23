package ar.edu.utn.frc.tup.piii.Services;

import ar.edu.utn.frc.tup.piii.Dtos.EstadisticaDto;
import ar.edu.utn.frc.tup.piii.models.Jugador;
import ar.edu.utn.frc.tup.piii.models.Partida;


import java.util.List;


public interface EstadisticaService {
    void saveEstadisticas(Jugador jugador);
    void allSaveEstadisticas(List<Jugador> jugadores);
    List<EstadisticaDto> getEstadisticas(Long idUsuario);

    void registrarEvento(String evento, Jugador jugador, Partida partida);
    //    void exportarJSON();
}
