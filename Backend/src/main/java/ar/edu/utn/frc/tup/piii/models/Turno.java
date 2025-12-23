package ar.edu.utn.frc.tup.piii.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Turno {

    private Long idTurno;
    private int nroTurno;
    private Jugador jugador;
    private FaseTurno fase;
    private LocalDateTime inicio;
    private Long idPartida;

}