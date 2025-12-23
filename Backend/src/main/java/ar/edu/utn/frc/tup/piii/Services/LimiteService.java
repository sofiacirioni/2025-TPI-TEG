package ar.edu.utn.frc.tup.piii.Services;

import ar.edu.utn.frc.tup.piii.Entities.LimiteEntity;
import ar.edu.utn.frc.tup.piii.Entities.PaisEntity;


import java.util.List;


public interface LimiteService {
    List<LimiteEntity> obtenerTodos();

    List<PaisEntity> obtenerVecinos(PaisEntity pais);
}
