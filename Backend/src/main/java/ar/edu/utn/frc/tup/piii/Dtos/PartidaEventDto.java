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
    /** ATAQUE_INICIADO | ATAQUE | CONQUISTA | FIN_TURNO | INCORPORACION | REAGRUPAMIENTO | TARJETA_OBTENIDA | TARJETA_CANJEADA | FIN_PARTIDA | PACTO_* | CHAT */
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
    private String jugadorDefensor;
    private String jugadorColorDefensor;

    // ── Campos específicos de ATAQUE_INICIADO ─────────────────────────
    /** Cantidad de dados que eligió el atacante (solo en ATAQUE_INICIADO). */
    private Integer cantDadosAtacante;
    /** Máximo de dados que puede usar el defensor (solo en ATAQUE_INICIADO). */
    private Integer maxDadosDefensor;
    /** Segundos disponibles para que el defensor elija antes del timeout. */
    private Integer timerSegundos;
    /** IDs útiles para que el frontend identifique rol sin depender de nombres. */
    private Long idAtacante;
    private Long idDefensor;

    /** Payload solo para tipo = FIN_PARTIDA. */
    private FinPartidaDto finPartida;

    /** Payload para tipos PACTO_PROPUESTO / PACTO_ACEPTADO / PACTO_RECHAZADO / PACTO_ROTO. */
    private PactoDto pacto;
}
