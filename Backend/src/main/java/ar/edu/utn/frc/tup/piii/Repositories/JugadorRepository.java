package ar.edu.utn.frc.tup.piii.Repositories;

import ar.edu.utn.frc.tup.piii.Entities.JugadorEntity;
import ar.edu.utn.frc.tup.piii.models.Color;
import ar.edu.utn.frc.tup.piii.models.Jugador;
import ar.edu.utn.frc.tup.piii.models.Usuario;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import ar.edu.utn.frc.tup.piii.Entities.PartidaEntity;
import ar.edu.utn.frc.tup.piii.models.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JugadorRepository extends JpaRepository<JugadorEntity, Long> {
    Optional<JugadorEntity> findByColorAndSala_IdSala(Color color, Long idSala);
    Optional<JugadorEntity> findByNombreAndSala_IdSala(String nombre, Long idSala);
    @Query(value = "SELECT * FROM jugadores WHERE id_sala = :idSala", nativeQuery = true)
    List<JugadorEntity> findBySalaIdNative(@Param("idSala") Long idSala);
    List<JugadorEntity> findBySala_IdSala(Long idSala);
    JugadorEntity findByUsuario_IdUsuario(Long idUsuario);
    Optional<JugadorEntity> findByIdJugador(Long idUsuario);
    List<JugadorEntity> findByPartidaAndEstadoJugador(PartidaEntity partida, EstadoJugador estadoJugador);
    @Query("SELECT j FROM JugadorEntity j WHERE j.partida.idPartida = :partidaId AND j.tipoJugador = 'HUMANO'")
    List<JugadorEntity> findHumanosByPartidaId(@Param("partidaId") Long partidaId);
@Query("SELECT j FROM JugadorEntity j WHERE j.partida.idPartida = :partidaId AND j.tipoJugador = 'BOT'")
List<JugadorEntity> findBotByPartidaId(@Param("partidaId") Long partidaId);
}