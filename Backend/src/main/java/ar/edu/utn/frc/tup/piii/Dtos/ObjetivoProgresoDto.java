package ar.edu.utn.frc.tup.piii.Dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ObjetivoProgresoDto {
    private String descripcion;
    private List<ObjetivoItemDto> items;
    private boolean completado;
    /** true cuando el objetivo de eliminación original fue convertido al fallback de 30 países */
    private boolean objetivoConvertido;
}
