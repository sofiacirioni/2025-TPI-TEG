package ar.edu.utn.frc.tup.piii.Repositories;

import ar.edu.utn.frc.tup.piii.Entities.TarjetaEntity;
import ar.edu.utn.frc.tup.piii.models.Tarjeta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TarjetaRepository extends JpaRepository<TarjetaEntity, Long> {
}
