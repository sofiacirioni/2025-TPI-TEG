package ar.edu.utn.frc.tup.piii.Services;

import ar.edu.utn.frc.tup.piii.Dtos.FinPartidaDto;
import ar.edu.utn.frc.tup.piii.Dtos.ObjetivoDto;
import ar.edu.utn.frc.tup.piii.Dtos.ObjetivoProgresoDto;
import ar.edu.utn.frc.tup.piii.Dtos.VerificacionObjetivoDto;
import ar.edu.utn.frc.tup.piii.Entities.JugadorEntity;


import java.util.List;


public interface ObjetivoService {
    //mostrar objetivos
    List<ObjetivoDto> obtenerObjetivosSecretos();
    ObjetivoDto obtenerObjetivoGral();
    ObjetivoDto obtenerObjetivoById(Long id);

    //asignar objetivos
    void asignarObjetivosSecretos(List<JugadorEntity> jugadores);

    //validar objetivos
    VerificacionObjetivoDto verificarObjetivos(Long idJugador);

    //progreso en tiempo real
    ObjetivoProgresoDto calcularProgreso(Long idJugador);

    /** Arma el DTO completo de fin de partida (ganador + clasificación de los demás) cuando ya se sabe quién ganó. */
    FinPartidaDto construirFinPartida(Long idJugadorGanador);

}
