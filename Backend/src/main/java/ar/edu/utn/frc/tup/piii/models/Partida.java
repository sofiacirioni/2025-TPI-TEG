package ar.edu.utn.frc.tup.piii.models;

import ar.edu.utn.frc.tup.piii.Dtos.JugadorDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Partida {
    private Long idPartida;
    private LocalDate fechaInicio;
    private Turno turno;
    private Integer turnoActual;
    private Long idSala;
    private List<JugadorDto> jugadores;
    private List<EstadoPais> estadoPaises;
    private EstadoPartida estadoPartida;

}