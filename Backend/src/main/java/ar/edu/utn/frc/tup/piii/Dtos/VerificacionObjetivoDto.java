package ar.edu.utn.frc.tup.piii.Dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VerificacionObjetivoDto {

    private boolean gano;
    private String objetivoCumplido;
}
