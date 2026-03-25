package ar.edu.utn.frc.tup.piii.Dtos.Login;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UsuarioDto {
    private Long idUsuario;
    private String usuario;
    private String correo;
    private String imagen;
}
