package ar.edu.utn.frc.tup.piii.Repositories;

import ar.edu.utn.frc.tup.piii.Entities.ObjetivoEntity;
import ar.edu.utn.frc.tup.piii.models.Objetivo;
import ar.edu.utn.frc.tup.piii.models.TipoObjetivo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface ObjetivoRepository extends JpaRepository<ObjetivoEntity, Long> {

    //lista de obj secretos
    List<ObjetivoEntity> findByTipoObjetivo(TipoObjetivo tipoObjetivo);
    //traer unico obj general
    Optional<ObjetivoEntity> findFirstByTipoObjetivo(TipoObjetivo tipoObjetivo);


}
