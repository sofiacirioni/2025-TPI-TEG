package ar.edu.utn.frc.tup.piii.Services;
import ar.edu.utn.frc.tup.piii.Dtos.EstadoPaisDto;

import ar.edu.utn.frc.tup.piii.Dtos.EstadoPaises.AgregarFichas;
import ar.edu.utn.frc.tup.piii.Dtos.EstadoPaises.MoverFichas;
import ar.edu.utn.frc.tup.piii.Entities.EstadoPaisEntity;


import java.util.List;


public interface EstadoPaisService {

    EstadoPaisDto getEstadoPais(Long id);

    EstadoPaisDto createEstadoPais(EstadoPaisDto estadoPais);

    EstadoPaisDto updateEstadoPais(EstadoPaisDto estadoPais);

    void inicializarEstadosPaises(Long partidaId);

    void repartirPaises(Long partidaId);

/*    List<EstadoPaisDto> fichasInicialesEstadosPaises(AgregarFichas agregarFichas);*/

    boolean agregarFichasEstadosPaises(AgregarFichas agregarFichas);

    boolean agrupacionFichas(MoverFichas moverFichas);

    boolean sonLimitrofes(Long idPais1, Long idPais2);

    void cambiarPropietario(Long idEstadoPaisObtenido, Long idEstadoPaisAtacante, Long idNuevoPropietario );

    List<EstadoPaisDto> getLimitesEstadoPais(Long idEstadoPais);

    List<EstadoPaisEntity> getLimitesEstadoPaisEntity(Long idEstadoPais);

    /*
    *
    *
    * */
}