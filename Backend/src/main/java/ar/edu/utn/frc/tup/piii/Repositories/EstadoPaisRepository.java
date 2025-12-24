package ar.edu.utn.frc.tup.piii.Repositories;

import ar.edu.utn.frc.tup.piii.Entities.EstadoPaisEntity;
import ar.edu.utn.frc.tup.piii.Entities.JugadorEntity;
import ar.edu.utn.frc.tup.piii.models.Jugador;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface EstadoPaisRepository extends JpaRepository<EstadoPaisEntity, Long> {
    // Consulta para las estadisticas
    @Query("""
                SELECT e FROM EstadoPaisEntity e
                WHERE e.idEstadoPais IN (
                    SELECT MAX(ep.idEstadoPais)
                    FROM EstadoPaisEntity ep
                    GROUP BY ep.pais
                )
                AND e.jugador = :jugador
            """)
    List<EstadoPaisEntity> findPaisesConquistados(Jugador jugador);

    @Query("""
                SELECT e FROM EstadoPaisEntity e
                WHERE e.idEstadoPais IN (
                    SELECT MAX(ep.idEstadoPais)
                    FROM EstadoPaisEntity ep
                    GROUP BY ep.pais
                )
                AND e.pais IN (
                    SELECT ep2.pais FROM EstadoPaisEntity ep2
                    WHERE ep2.jugador = :jugador
                )
                AND e.jugador <> :jugador
            """)
    List<EstadoPaisEntity> findPaisesPerdidos(Jugador jugador);

    Optional<EstadoPaisEntity> findByPaisIdPaisAndJugadorIdJugador(Long idPais, Long idJugador);

    List<EstadoPaisEntity> findByJugador_IdJugador(Long idJugador);

    List<EstadoPaisEntity> findEstadoPaisEntitiesByJugador_IdJugador(Long jugador_idJugador);

    Optional<EstadoPaisEntity> findByPais_IdPaisAndJugador_IdJugador(Long idPais, Long idJugador);

    Optional<EstadoPaisEntity> findByPais_IdPaisAndPartida_IdPartida(Long idPais, Long idPartida);

    @Query("SELECT COUNT(e) FROM EstadoPaisEntity e WHERE e.jugador = :jugador")
    long countByJugador(@Param("jugador") JugadorEntity jugador);

    List<EstadoPaisEntity> findAllByPais_IdPaisInAndPartida_IdPartida(Set<Long> idsPaisesLimite, Long idPartida);
}
