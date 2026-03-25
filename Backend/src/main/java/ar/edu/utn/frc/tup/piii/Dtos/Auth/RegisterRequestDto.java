package ar.edu.utn.frc.tup.piii.Dtos.Auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequestDto {

    @NotBlank(message = "Debes ingresar un nombre de usuario")
    @Size(max = 50, message = "El usuario no puede superar los 50 caracteres")
    @Pattern(regexp = "^[a-zA-ZáéíóúÁÉÍÓÚñÑ0-9 _-]+$",
             message = "El usuario solo puede contener letras, números, espacios, guiones y guiones bajos")
    private String usuario;

    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "El correo no tiene un formato válido")
    @Size(max = 100, message = "El correo no puede superar los 100 caracteres")
    private String correo;

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 8, max = 100, message = "La contraseña debe tener entre 8 y 100 caracteres")
    @Pattern(
            regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@$!%?&])[A-Za-z\\d@$!%?&]{8,}$",
            message = "La contraseña debe tener al menos una mayúscula, una minúscula, un número y un carácter especial")
    private String contrasenia;

    @NotBlank(message = "Debe seleccionar una imagen")
    @Size(max = 200, message = "La ruta de imagen no puede superar los 200 caracteres")
    private String imagen;
}
