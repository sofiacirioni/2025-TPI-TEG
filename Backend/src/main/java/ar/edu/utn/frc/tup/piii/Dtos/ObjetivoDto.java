package ar.edu.utn.frc.tup.piii.Dtos;

import ar.edu.utn.frc.tup.piii.models.TipoObjetivo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ObjetivoDto {
    private Long id;
    private String descripcion;
    private TipoObjetivo tipoObjetivo;
}
