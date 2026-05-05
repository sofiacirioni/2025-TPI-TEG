package ar.edu.utn.frc.tup.piii.Dtos;

import ar.edu.utn.frc.tup.piii.models.Color;
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
    /** Presente cuando el objetivo es de tipo ELIMINAR_JUGADOR. */
    private Color colorEnemigo;
    private Integer cantidadPaisesObjetivo;
    private Integer africa;
    private Integer asia;
    private Integer europa;
    private Integer americaNorte;
    private Integer americaSur;
    private Integer oceania;
}
