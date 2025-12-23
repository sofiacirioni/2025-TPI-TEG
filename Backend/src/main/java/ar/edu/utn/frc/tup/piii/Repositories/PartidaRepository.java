package ar.edu.utn.frc.tup.piii.Repositories;

import ar.edu.utn.frc.tup.piii.Entities.PartidaEntity;
import ar.edu.utn.frc.tup.piii.Entities.SalaEntity;
import ar.edu.utn.frc.tup.piii.models.EstadoPartida;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PartidaRepository extends JpaRepository<PartidaEntity, Long> {
    List<PartidaEntity> findByEstadoPartida(EstadoPartida estadoPartida);
    Optional<PartidaEntity> findByConfiguracion_IdSala(Long idSala);


}
