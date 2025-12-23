package ar.edu.utn.frc.tup.piii.Services;

import ar.edu.utn.frc.tup.piii.Entities.EstadoTarjetaEntity;
import ar.edu.utn.frc.tup.piii.Entities.JugadorEntity;

public interface BotService {
    boolean turnoBot(Long idJugador);
    boolean faseDefensa(JugadorEntity botEntity);
    boolean faseAtaque(JugadorEntity botEntity);
    boolean faseReagrupar(JugadorEntity botEntity);
    boolean pedirCarta(JugadorEntity botEntity);
    boolean usarCarta(JugadorEntity botEntity, EstadoTarjetaEntity estadoTarjetaEntity);
    boolean canjearCarta(JugadorEntity botEntity);
}
