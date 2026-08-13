package ar.edu.utn.frc.tup.piii.Repositories;

import ar.edu.utn.frc.tup.piii.Entities.EstadisticaEntity;
import ar.edu.utn.frc.tup.piii.Entities.JugadorEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EstadisticaRepository extends JpaRepository<EstadisticaEntity, Long> {

    List<EstadisticaEntity> findByJugador(JugadorEntity jugador);
}
