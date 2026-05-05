package ar.edu.utn.frc.tup.piii.Dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class JugadorResultadoDto {
    private Long id;
    private String nombre;
    private String color;
    private String avatarUrl;
    private int cantidadPaises;
    private int cantidadEjercitos;
    private boolean eliminado;
}
