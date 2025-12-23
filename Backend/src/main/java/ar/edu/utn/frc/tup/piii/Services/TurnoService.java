package ar.edu.utn.frc.tup.piii.Services;

import ar.edu.utn.frc.tup.piii.Dtos.CanjeTarjetasDto;
import ar.edu.utn.frc.tup.piii.Dtos.EstadoPaises.AgregarFichas;
import ar.edu.utn.frc.tup.piii.Dtos.EstadoPaises.Ataque;
import ar.edu.utn.frc.tup.piii.Dtos.EstadoPaises.AtaqueResponseDto;
import ar.edu.utn.frc.tup.piii.Dtos.EstadoPaises.MoverFichas;

import ar.edu.utn.frc.tup.piii.Dtos.TarjetaDto;

import ar.edu.utn.frc.tup.piii.Dtos.UsarTarjetaEnPaisDto;
import ar.edu.utn.frc.tup.piii.Dtos.VerificacionObjetivoDto;
import ar.edu.utn.frc.tup.piii.Entities.JugadorEntity;
import ar.edu.utn.frc.tup.piii.Entities.PartidaEntity;
import ar.edu.utn.frc.tup.piii.Entities.TurnoEntity;
import ar.edu.utn.frc.tup.piii.models.*;


import java.util.List;



public interface TurnoService {
    Turno obtenerTurno(Long idTurno);
    boolean cambiarFaseTurno(Long idPartida);
    TurnoEntity crearTurno(JugadorEntity jugador,int nroTurno,Long idPartida);
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
    TarjetaDto entregarTarjetaSiCorresponde(Long idJugador, Long idPartida);
    void validarUsarTarjetaEnPais(UsarTarjetaEnPaisDto usarTarjetaEnPaisDto);
}