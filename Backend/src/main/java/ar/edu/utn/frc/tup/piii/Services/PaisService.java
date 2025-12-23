package ar.edu.utn.frc.tup.piii.Services;

import ar.edu.utn.frc.tup.piii.Entities.PaisEntity;

import java.util.List;
import java.util.Optional;

public interface PaisService {
    List<PaisEntity> obtenerTodos();
    Optional<PaisEntity> buscarPorId(Long id);
}