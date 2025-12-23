package ar.edu.utn.frc.tup.piii.Repositories;

import ar.edu.utn.frc.tup.piii.Entities.JugadorEntity;
import ar.edu.utn.frc.tup.piii.Entities.TurnoEntity;
import ar.edu.utn.frc.tup.piii.models.FaseTurno;
import ar.edu.utn.frc.tup.piii.models.Jugador;
import ar.edu.utn.frc.tup.piii.models.Turno;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface TurnoRepository extends JpaRepository<TurnoEntity, Long> {
    TurnoEntity findByNroTurno(int nroTurno);
    TurnoEntity findByJugador(JugadorEntity jugador);
    Optional<TurnoEntity> findByNroTurnoAndPartida_IdPartida(int turnoActual, Long idPartida) ;

}
