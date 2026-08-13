package ar.edu.utn.frc.tup.piii.Repositories;

import ar.edu.utn.frc.tup.piii.Entities.PactoEntity;
import ar.edu.utn.frc.tup.piii.models.EstadoPacto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PactoRepository extends JpaRepository<PactoEntity, Long> {

    List<PactoEntity> findByPartida_IdPartidaAndEstado(Long idPartida, EstadoPacto estado);

    List<PactoEntity> findByPartida_IdPartida(Long idPartida);

    @Query("SELECT p FROM PactoEntity p WHERE p.partida.idPartida = :idPartida " +
            "AND ((p.jugadorA.idJugador = :idA AND p.jugadorB.idJugador = :idB) " +
            "  OR (p.jugadorA.idJugador = :idB AND p.jugadorB.idJugador = :idA)) " +
            "AND p.estado IN (ar.edu.utn.frc.tup.piii.models.EstadoPacto.ACTIVO, " +
            "                 ar.edu.utn.frc.tup.piii.models.EstadoPacto.ROTO_VOLUNTARIO)")
    List<PactoEntity> findActivosEntreJugadores(@Param("idA") Long idA,
                                                 @Param("idB") Long idB,
                                                 @Param("idPartida") Long idPartida);

    /**
     * Todos los pactos en los que intervino alguna de estas participaciones.
     * Se recibe la lista completa de un usuario para resolver su diplomacia
     * acumulada en una sola consulta.
     */
    @Query("SELECT p FROM PactoEntity p " +
            "WHERE p.jugadorA.idJugador IN :idsJugador OR p.jugadorB.idJugador IN :idsJugador")
    List<PactoEntity> findByJugadorIdIn(@Param("idsJugador") List<Long> idsJugador);

    @Query("SELECT p FROM PactoEntity p WHERE p.partida.idPartida = :idPartida " +
            "AND (p.jugadorA.idJugador = :idJugador OR p.jugadorB.idJugador = :idJugador) " +
            "AND p.estado IN (ar.edu.utn.frc.tup.piii.models.EstadoPacto.ACTIVO, " +
            "                 ar.edu.utn.frc.tup.piii.models.EstadoPacto.ROTO_VOLUNTARIO)")
    List<PactoEntity> findActivosDelJugador(@Param("idJugador") Long idJugador,
                                             @Param("idPartida") Long idPartida);
}
