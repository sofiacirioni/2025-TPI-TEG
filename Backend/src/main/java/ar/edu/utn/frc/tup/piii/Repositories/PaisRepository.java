package ar.edu.utn.frc.tup.piii.Repositories;

import ar.edu.utn.frc.tup.piii.Entities.ContinenteEntity;
import ar.edu.utn.frc.tup.piii.Entities.PaisEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaisRepository extends JpaRepository<PaisEntity, Long> {
    List<PaisEntity> findByContinente_IdContinente(Long idCcontinente);
}
