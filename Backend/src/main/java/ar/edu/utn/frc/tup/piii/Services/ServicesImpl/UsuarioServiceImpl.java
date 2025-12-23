package ar.edu.utn.frc.tup.piii.Services.ServicesImpl;

import ar.edu.utn.frc.tup.piii.Dtos.Login.UsuarioDto;
import ar.edu.utn.frc.tup.piii.Entities.UsuarioEntity;
import ar.edu.utn.frc.tup.piii.Repositories.UsuarioRepository;
import ar.edu.utn.frc.tup.piii.Services.UsuarioService;
import ar.edu.utn.frc.tup.piii.models.Usuario;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@Service
public class UsuarioServiceImpl implements UsuarioService {
    @Autowired
    public UsuarioRepository usuarioRepository;
    @Autowired
    public ModelMapper modelMapper;

    @Override
    public Usuario obtenerUsuario(String correo, String contrasenia) {
        Optional<UsuarioEntity> usuarioEntity = usuarioRepository.findByCorreoAndContrasenia(correo, contrasenia);

        if (usuarioEntity.isEmpty()) {
                throw new IllegalArgumentException("Correo o contraseña incorrectos.");
            }
        return modelMapper.map(usuarioEntity.get(), Usuario.class);
    }

    @Override
    public Usuario guardarUsuario(Usuario usuario) {
        Optional<UsuarioEntity> existente = usuarioRepository.findByCorreo(usuario.getCorreo());

        if (existente.isPresent()) {
            throw new IllegalArgumentException("Ya existe un usuario con ese correo.");
        }

        UsuarioEntity usuarioEntity = modelMapper.map(usuario, UsuarioEntity.class);
        UsuarioEntity usuarioGuardado = usuarioRepository.save(usuarioEntity);

        return modelMapper.map(usuarioGuardado, Usuario.class);
    }

    @Override
    public Usuario actualizarUsuario(String correo, String contraseniaActual, String nuevaContrasenia, String imagen) {

        Optional<UsuarioEntity> usuarioEntityOpcional = usuarioRepository.findByCorreo(correo);

        if (usuarioEntityOpcional.isEmpty()) {
            throw new IllegalArgumentException("El usuario con ese correo no existe.");
        }

        UsuarioEntity usuarioEntity = usuarioEntityOpcional.get();

        if (!usuarioEntity.getContrasenia().equals(contraseniaActual)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La contraseña actual es incorrecta");
        }
        if (contraseniaActual.equals(nuevaContrasenia)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La nueva contraseña no puede ser igual a la actual");
        }

        usuarioEntity.setContrasenia(nuevaContrasenia);
        usuarioEntity.setImagen(imagen);

        UsuarioEntity usuarioActualizado = usuarioRepository.save(usuarioEntity);
        return modelMapper.map(usuarioActualizado, Usuario.class);
    }

    @Override
    public Usuario eliminarUsuario(Long id) {
        Optional<UsuarioEntity> usuarioEntityOpcional = usuarioRepository.findByIdUsuario(id);

        if (usuarioEntityOpcional.isEmpty()) {
            throw new IllegalArgumentException("No se encontró el usuario a eliminar.");
        }

        UsuarioEntity usuarioEntity = usuarioEntityOpcional.get();
        usuarioRepository.delete(usuarioEntity);

        return modelMapper.map(usuarioEntity, Usuario.class);
    }

    @Override
    public Usuario obtenerByIdUsuario(Long id)
    {

        return usuarioRepository.findByIdUsuario(id)
                .map(usuario -> modelMapper.map(usuario, Usuario.class))
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado con ID: " + id));
    }

    @Override
    public Usuario actualizarImagen(String correo, String imagen) {
        UsuarioEntity u = usuarioRepository
                .findByCorreo(correo)
                .orElseThrow(() ->
                        new IllegalArgumentException("Usuario no encontrado con correo: " + correo));

        u.setImagen(imagen);

        UsuarioEntity saved = usuarioRepository.save(u);
        return modelMapper.map(saved, Usuario.class);
    }

}



