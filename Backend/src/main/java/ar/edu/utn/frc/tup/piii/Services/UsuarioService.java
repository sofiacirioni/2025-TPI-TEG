package ar.edu.utn.frc.tup.piii.Services;

import ar.edu.utn.frc.tup.piii.models.Usuario;


public interface UsuarioService {
    Usuario actualizarUsuario(String correo, String contraseniaActual, String nuevaContrasenia, String imagen);
    Usuario eliminarUsuario(Long id);
    Usuario obtenerByIdUsuario(Long id);
    Usuario actualizarImagen(String correo, String imagen);
}
