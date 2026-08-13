package ar.edu.utn.frc.tup.piii.Services.ServicesImpl;

import ar.edu.utn.frc.tup.piii.Dtos.EnviarChatDto;
import ar.edu.utn.frc.tup.piii.Dtos.PartidaEventDto;
import ar.edu.utn.frc.tup.piii.Entities.JugadorEntity;
import ar.edu.utn.frc.tup.piii.Repositories.JugadorRepository;
import ar.edu.utn.frc.tup.piii.Services.ChatService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    /** Igual al límite del input en el frontend (tablero.component.ts: chatMaxLen). */
    private static final int TEXTO_MAX_LEN = 120;

    private final JugadorRepository jugadorRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public void enviar(EnviarChatDto dto) {
        String texto = dto.getTexto() == null ? "" : dto.getTexto().trim();
        if (texto.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El mensaje no puede estar vacío.");
        }
        if (texto.length() > TEXTO_MAX_LEN) {
            texto = texto.substring(0, TEXTO_MAX_LEN);
        }

        JugadorEntity jugador = jugadorRepository.findById(dto.getIdJugador())
                .orElseThrow(() -> new EntityNotFoundException("Jugador no encontrado."));

        if (jugador.getPartida() == null || !jugador.getPartida().getIdPartida().equals(dto.getIdPartida())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El jugador no pertenece a esta partida.");
        }

        PartidaEventDto evento = new PartidaEventDto();
        evento.setTipo("CHAT");
        evento.setJugadorNombre(jugador.getNombre());
        evento.setJugadorColor(jugador.getColor() != null ? jugador.getColor().name() : "");
        evento.setDescripcion(texto);
        evento.setIdPartida(dto.getIdPartida());

        messagingTemplate.convertAndSend("/topic/partida." + dto.getIdPartida() + ".evento", evento);
    }
}
