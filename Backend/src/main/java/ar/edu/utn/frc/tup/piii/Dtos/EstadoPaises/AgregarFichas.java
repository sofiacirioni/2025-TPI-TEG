package ar.edu.utn.frc.tup.piii.Dtos.EstadoPaises;


import ar.edu.utn.frc.tup.piii.Dtos.EstadoPaises.EstadoPaisFicha;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AgregarFichas {
    private Long idJugador;
    private List<EstadoPaisFicha> paisesFichas;
}
