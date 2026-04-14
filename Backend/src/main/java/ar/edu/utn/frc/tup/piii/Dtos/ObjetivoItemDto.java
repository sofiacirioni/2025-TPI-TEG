package ar.edu.utn.frc.tup.piii.Dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ObjetivoItemDto {
    private String descripcion;
    private int valorActual;
    private int valorObjetivo;
    private boolean completado;
}
