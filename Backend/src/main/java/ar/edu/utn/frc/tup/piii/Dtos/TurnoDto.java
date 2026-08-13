package ar.edu.utn.frc.tup.piii.Dtos;

import ar.edu.utn.frc.tup.piii.models.FaseTurno;
import lombok.*;

@Data
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
public class TurnoDto {
    private Long idTurno;
    private int nroTurno;
    private FaseTurno fase;
    private Long idJugador;
    private Long idPartida;
}
