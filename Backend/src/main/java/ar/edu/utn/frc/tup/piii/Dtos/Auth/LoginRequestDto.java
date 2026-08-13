package ar.edu.utn.frc.tup.piii.Dtos.Auth;

import lombok.Data;

@Data
public class LoginRequestDto {
    private String correo;
    private String contrasenia;
}
