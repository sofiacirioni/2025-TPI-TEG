package ar.edu.utn.frc.tup.piii.Services;

import ar.edu.utn.frc.tup.piii.Dtos.ResumenPartidaDto;

public interface ResumenPartidaService {

    /** Parte de campaña de una partida: estado final de cada comandante + su combate. */
    ResumenPartidaDto obtenerResumen(Long idPartida);
}
