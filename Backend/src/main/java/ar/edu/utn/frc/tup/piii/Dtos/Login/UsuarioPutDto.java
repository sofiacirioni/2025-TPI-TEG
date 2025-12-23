package ar.edu.utn.frc.tup.piii.Dtos.Login;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor

public class UsuarioPutDto {
    private String correo;
    private String contraseniaActual;
    private String nuevaContrasenia;
    private String imagen;

}
