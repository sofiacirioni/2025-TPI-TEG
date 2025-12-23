package ar.edu.utn.frc.tup.piii.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Estadistica {

    private Long idEstadistica;
    private Jugador jugador;
    private Partida partida;
    private int paisesConquistados;
    private int paisesPerdidos;
    private boolean ganador;
    private String fechaPartida;
    private String evento;

}