package ar.edu.utn.frc.tup.piii.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EstadoPais {
    private Long idEstadoPais;
    private Pais pais;
    private Jugador jugador;
    //private Estadistica estadistica;
    private int cantidadTropas;
}