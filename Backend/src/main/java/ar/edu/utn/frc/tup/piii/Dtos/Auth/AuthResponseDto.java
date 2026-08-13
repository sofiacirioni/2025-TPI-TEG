package ar.edu.utn.frc.tup.piii.Dtos.Auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponseDto {
    private String accessToken;
    private Long idUsuario;
    private String usuario;
    private String correo;
    private String imagen;
}
