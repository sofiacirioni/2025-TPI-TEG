package ar.edu.utn.frc.tup.piii.Services;

import ar.edu.utn.frc.tup.piii.Dtos.HistorialComandanteDto;

public interface HistorialComandanteService {

    /** Hoja de servicios acumulada de un usuario. */
    HistorialComandanteDto obtenerHistorial(Long idUsuario);
}
