package ar.edu.utn.frc.tup.piii.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EstadoTarjeta {
    private Long idEstadoTarjeta;
    private Tarjeta tarjeta;
    private Jugador jugador;
    private Turno turno;


}