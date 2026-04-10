package ar.edu.utn.frc.tup.piii.Dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO para broadcast de eventos de juego via WebSocket.
 * Topic: /topic/partida.{idPartida}.evento
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PartidaEventDto {
    /** ATAQUE | CONQUISTA | FIN_TURNO | INCORPORACION | REAGRUPAMIENTO | TARJETA_OBTENIDA | TARJETA_CANJEADA */
    private String tipo;
    private String jugadorNombre;
    private String jugadorColor;
    private String descripcion;
    private String paisOrigen;
    private String paisDestino;
    private List<Integer> dadosAtaque;
    private List<Integer> dadosDefensor;
    private boolean conquista;
    private int perdidasAtacante;
    private int perdidasDefensor;
    private Long idPartida;
}
