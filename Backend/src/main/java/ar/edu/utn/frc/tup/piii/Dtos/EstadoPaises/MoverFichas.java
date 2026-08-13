package ar.edu.utn.frc.tup.piii.Dtos.EstadoPaises;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MoverFichas {
    private Long idJugador;
    private Long idPaisOrigen;
    private Long idPaisDestino;
    private Long cantidadFichas;
}
