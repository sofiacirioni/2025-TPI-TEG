package ar.edu.utn.frc.tup.piii.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Usuario {
    private Long idUsuario;
    private String nombre;
    private String apellido;
    private String correo;
    private String contrasenia;
    private String imagen;


}