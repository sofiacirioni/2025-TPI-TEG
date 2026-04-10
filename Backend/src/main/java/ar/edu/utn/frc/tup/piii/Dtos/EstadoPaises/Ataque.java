package ar.edu.utn.frc.tup.piii.Dtos.EstadoPaises;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Ataque {
    private Long idJugador;
    private Long idPaisOrigen;
    private Long idPaisDestino;
    /** Cantidad de dados a usar (1-3). Si es null se usa el máximo permitido. */
    private Integer cantDadosAtacante;
}
