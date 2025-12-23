package ar.edu.utn.frc.tup.piii.Repositories;

import ar.edu.utn.frc.tup.piii.Entities.SalaEntity;
import ar.edu.utn.frc.tup.piii.Entities.UsuarioEntity;
import ar.edu.utn.frc.tup.piii.models.EstadoSala;
import ar.edu.utn.frc.tup.piii.models.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SalaRepository extends JpaRepository<SalaEntity, Long> {

    Optional<SalaEntity> findByCreadorIdUsuario(Long idUsuario);

    @Query("SELECT s FROM SalaEntity s LEFT JOIN FETCH s.jugadores WHERE s.idSala = :id")
    Optional<SalaEntity> findByIdSalaWithJugadores(@Param("id") Long idSala);
    @Query("SELECT s FROM SalaEntity s LEFT JOIN FETCH s.jugadores WHERE s.url = :url")
    Optional<SalaEntity> findByUrl(@Param("url") String url);
}
