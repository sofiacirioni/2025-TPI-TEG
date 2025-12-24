package ar.edu.utn.frc.tup.piii.Dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class JuegoDto {
    private String faseActual;
    private String jugadorEnTurno;
    private List<EstadoPaisDto> estadosPais;
    private String ganador;
}
