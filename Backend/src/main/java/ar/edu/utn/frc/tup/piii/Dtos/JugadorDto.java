package ar.edu.utn.frc.tup.piii.Dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class JugadorDto {
    private Long idJugador;
    private String nombre;
    private String color;
    private String tipoJugador;
    private Long idUsuario;
    private Long idSala;
    private ObjetivoDto objetivo;
    private String url;
    private Integer ejercito;
    private boolean consquisto;
    private Integer cantidadTarjetas;

}
