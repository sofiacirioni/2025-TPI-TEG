package ar.edu.utn.frc.tup.piii.Dtos;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class EstadisticaDto {
    private Long id;
    private String colorJugador;
    private Long idPartida;
    private LocalDate fecha;
    private int paisesConquistados;
    private int paisesPerdidos;
    private double porcentajeMundo;
    private boolean ganador;
}
