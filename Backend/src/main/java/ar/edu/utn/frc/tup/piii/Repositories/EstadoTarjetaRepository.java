package ar.edu.utn.frc.tup.piii.Repositories;

import ar.edu.utn.frc.tup.piii.Entities.EstadoTarjetaEntity;
import ar.edu.utn.frc.tup.piii.Entities.JugadorEntity;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EstadoTarjetaRepository extends JpaRepository<EstadoTarjetaEntity, Long> {
    @Query("SELECT e FROM EstadoTarjetaEntity e WHERE e.jugador.idJugador = :jugadorId")
    List<EstadoTarjetaEntity> findByJugadorId(@Param("jugadorId") Long jugadorId);

    @Query("SELECT e FROM EstadoTarjetaEntity e WHERE e.turno.idTurno = :turnoId")
    List<EstadoTarjetaEntity> findByTurnoId(@Param("turnoId") Long turnoId);

    @Query("SELECT e FROM EstadoTarjetaEntity e WHERE e.tarjeta.idTarjeta = :tarjetaId")
    List<EstadoTarjetaEntity> findByTarjetaId(@Param("tarjetaId") Long tarjetaId);

    List<EstadoTarjetaEntity> findByPartida_IdPartidaAndJugadorIsNull(Long idPartida);

    List<EstadoTarjetaEntity> findAllByTarjeta_IdTarjeta(Long idTarjeta);

    List<EstadoTarjetaEntity> findAllByPartida_IdPartida(Long idPartida);

    List<EstadoTarjetaEntity> findByJugador_IdJugador(Long idJugador);
}
