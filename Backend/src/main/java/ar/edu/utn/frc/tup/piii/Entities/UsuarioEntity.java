package ar.edu.utn.frc.tup.piii.Entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
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
    @NotBlank(message="Debes rellenar el campo nombre")
    @Pattern(regexp = "^[a-zA-ZáéíóúÁÉÍÓÚñÑ ]+$", message = "El nombre solo puede contener letras y espacios")
    private String nombre;

    @NotBlank(message="Debes rellenar el campo apellido")
    @Pattern(regexp = "^[a-zA-ZáéíóúÁÉÍÓÚñÑ ]+$", message = "El nombre solo puede contener letras y espacios")
    private String apellido;

    @Email
    private String correo;

    @NotBlank(message="Debes rellenar el campo contraseña")
    @Pattern(
            regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@$!%?&])[A-Za-z\\d@$!%?&]{8,}$",
            message = "La contraseña debe tener al menos una mayúscula, una minúscula, un número, un carácter especial y mínimo 8 caracteres")

    private String contrasenia;

    @NotBlank(message = "Debe seleccionar una imagen")
    private String imagen;

}
