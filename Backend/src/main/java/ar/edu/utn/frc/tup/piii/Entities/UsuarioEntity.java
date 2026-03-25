package ar.edu.utn.frc.tup.piii.Entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "usuarios")
public class UsuarioEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idUsuario;
    @NotBlank(message = "Debes ingresar un nombre de usuario")
    @Size(max = 50, message = "El usuario no puede superar los 50 caracteres")
    @Pattern(regexp = "^[a-zA-ZáéíóúÁÉÍÓÚñÑ0-9 _-]+$", message = "El usuario solo puede contener letras, números, espacios, guiones y guiones bajos")
    private String usuario;

    @Email(message = "El correo no tiene un formato válido")
    @Size(max = 100, message = "El correo no puede superar los 100 caracteres")
    private String correo;

    @NotBlank(message = "Debes rellenar el campo contraseña")
    @Size(max = 100, message = "La contraseña no puede superar los 100 caracteres")
    @Pattern(
            regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@$!%?&])[A-Za-z\\d@$!%?&]{8,}$",
            message = "La contraseña debe tener al menos una mayúscula, una minúscula, un número, un carácter especial y mínimo 8 caracteres")
    private String contrasenia;

    @NotBlank(message = "Debe seleccionar una imagen")
    @Size(max = 200, message = "La ruta de imagen no puede superar los 200 caracteres")
    private String imagen;

}
