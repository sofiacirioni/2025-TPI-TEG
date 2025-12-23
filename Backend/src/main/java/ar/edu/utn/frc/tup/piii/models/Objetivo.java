package ar.edu.utn.frc.tup.piii.models;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Objetivo {
    private Long idObjetivo;
    private String descripcion;
    private TipoObjetivo tipoObjetivo;

}
