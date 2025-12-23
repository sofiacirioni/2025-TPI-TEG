package ar.edu.utn.frc.tup.piii.Repositories;

import ar.edu.utn.frc.tup.piii.Entities.ContinenteEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ContinenteRepository extends JpaRepository<ContinenteEntity, Long> {
}
