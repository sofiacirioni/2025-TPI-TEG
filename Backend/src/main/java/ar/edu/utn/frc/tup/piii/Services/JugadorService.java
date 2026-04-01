package ar.edu.utn.frc.tup.piii.Services;

import ar.edu.utn.frc.tup.piii.Dtos.JugadorDto;
import ar.edu.utn.frc.tup.piii.models.*;

import java.util.List;

public interface JugadorService {
    Jugador crearJugador(Jugador jugador, Usuario usuario, Sala sala);

    void eliminarJugadorDeSala(Long idJugador, Usuario solicitante);

    Jugador crearBot(Sala sala, Usuario usuarioActual);

    void votarReanudar(Long idJugador);

    void votarPausa(Long idJugador);

    void finalizarPartida(Long idJugador);

    List<JugadorDto> obtenerJugadoresPorSala(Long idSala);
}
