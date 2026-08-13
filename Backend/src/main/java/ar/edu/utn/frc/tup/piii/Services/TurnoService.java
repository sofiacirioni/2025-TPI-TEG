package ar.edu.utn.frc.tup.piii.Services;

import ar.edu.utn.frc.tup.piii.Dtos.CanjeTarjetasDto;
import ar.edu.utn.frc.tup.piii.Dtos.EstadoPaisDto;
import ar.edu.utn.frc.tup.piii.Dtos.EstadoPaises.AgregarFichas;
import ar.edu.utn.frc.tup.piii.Dtos.EstadoPaises.Ataque;
import ar.edu.utn.frc.tup.piii.Dtos.EstadoPaises.AtaqueDefender;
import ar.edu.utn.frc.tup.piii.Dtos.EstadoPaises.AtaqueResponseDto;
import ar.edu.utn.frc.tup.piii.Dtos.EstadoPaises.MoverFichas;
import java.util.List;

import ar.edu.utn.frc.tup.piii.Dtos.TarjetaDto;

import ar.edu.utn.frc.tup.piii.Dtos.UsarTarjetaEnPaisDto;
import ar.edu.utn.frc.tup.piii.Dtos.VerificacionObjetivoDto;
import ar.edu.utn.frc.tup.piii.Entities.JugadorEntity;
import ar.edu.utn.frc.tup.piii.Entities.TurnoEntity;
import ar.edu.utn.frc.tup.piii.models.*;

public interface TurnoService {
    Turno obtenerTurno(Long idTurno);

    boolean cambiarFaseTurno(Long idPartida);

    TurnoEntity crearTurno(JugadorEntity jugador, int nroTurno, Long idPartida);

    void establecerOrdenJugadores(Long idPartida);

    Long pasarTurno(Long idPartida);

    boolean validarAtaque(Pais origen, Pais destino, Jugador jugador);

    boolean validarMovimiento(Pais origen, Pais destino, Jugador jugador);

    boolean verificarOrdenAcciones(Jugador jugador, String accion);

    void gestionarTimeoutTurno(Long idTurno);

    boolean agregarFichas(AgregarFichas agregarFichas);

    Integer validarCanjeTarjetas(CanjeTarjetasDto canjeTarjetasDto);

    VerificacionObjetivoDto verificarGanador(Long idJugador);

    Boolean moverFichas(MoverFichas moverFichas);

    AtaqueResponseDto ataque(Ataque ataque);

    /**
     * Paso 1 del flujo dual: el atacante registra su intención. Valida, calcula el rango
     * de dados del defensor, almacena un AtaquePendiente y emite WS ATAQUE_INICIADO para
     * que el defensor humano elija sus dados. Si el defensor es BOT, resuelve en el acto.
     */
    AtaqueResponseDto iniciarAtaque(Ataque ataque);

    /**
     * Paso 2 del flujo dual: el defensor confirma la cantidad de dados y dispara la
     * resolución del ataque pendiente de su partida. Emite WS ATAQUE / CONQUISTA.
     */
    AtaqueResponseDto resolverAtaque(AtaqueDefender ataqueDefender);

    TarjetaDto entregarTarjetaSiCorresponde(Long idJugador, Long idPartida);

    void validarUsarTarjetaEnPais(UsarTarjetaEnPaisDto usarTarjetaEnPaisDto);

    List<EstadoPaisDto> getDestinosReagrupamiento(Long idPaisOrigen, Long idJugador, Long idPartida);
}