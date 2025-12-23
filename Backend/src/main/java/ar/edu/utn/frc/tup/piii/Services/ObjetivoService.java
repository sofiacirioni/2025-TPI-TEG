package ar.edu.utn.frc.tup.piii.Services;

import ar.edu.utn.frc.tup.piii.Dtos.ObjetivoDto;
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

}
