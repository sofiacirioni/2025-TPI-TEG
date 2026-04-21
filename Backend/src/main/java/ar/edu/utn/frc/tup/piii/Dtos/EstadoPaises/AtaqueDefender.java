package ar.edu.utn.frc.tup.piii.Dtos.EstadoPaises;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Payload enviado por el defensor tras confirmar la cantidad de dados a usar
 * en un ataque previamente iniciado. Dispara la resolución del ataque pendiente
 * asociado a la partida.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AtaqueDefender {
    private Long idPartida;
    private Long idJugador;
    /** Cantidad de dados a usar (1-3). Si es null se usa el máximo permitido. */
    private Integer cantDadosDefensor;
}
