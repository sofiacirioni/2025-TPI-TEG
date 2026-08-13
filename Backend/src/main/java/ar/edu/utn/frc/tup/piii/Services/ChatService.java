package ar.edu.utn.frc.tup.piii.Services;

import ar.edu.utn.frc.tup.piii.Dtos.EnviarChatDto;

public interface ChatService {

    /** Valida al jugador y la partida, y difunde el mensaje por WS a todos los jugadores. */
    void enviar(EnviarChatDto dto);
}
