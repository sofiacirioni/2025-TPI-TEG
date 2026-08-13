package ar.edu.utn.frc.tup.piii.Services.ServicesImpl;

import ar.edu.utn.frc.tup.piii.Entities.UsuarioEntity;
import ar.edu.utn.frc.tup.piii.Repositories.UsuarioRepository;
import ar.edu.utn.frc.tup.piii.Services.UsuarioService;
import ar.edu.utn.frc.tup.piii.models.Usuario;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@Service
public class UsuarioServiceImpl implements UsuarioService {
    @Autowired
    public UsuarioRepository usuarioRepository;
    @Autowired
    public ModelMapper modelMapper;
    @Autowired
    public PasswordEncoder passwordEncoder;

    @Override
    public Usuario actualizarUsuario(String correo, String contraseniaActual, String nuevaContrasenia, String imagen) {

        Optional<UsuarioEntity> usuarioEntityOpcional = usuarioRepository.findByCorreo(correo);

        if (usuarioEntityOpcional.isEmpty()) {
            throw new IllegalArgumentException("El usuario con ese correo no existe.");
        }

        UsuarioEntity usuarioEntity = usuarioEntityOpcional.get();

        if (!passwordEncoder.matches(contraseniaActual, usuarioEntity.getContrasenia())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La contraseña actual es incorrecta");
        }
        if (contraseniaActual.equals(nuevaContrasenia)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La nueva contraseña no puede ser igual a la actual");
        }

        usuarioEntity.setContrasenia(passwordEncoder.encode(nuevaContrasenia));
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
    public Usuario obtenerByIdUsuario(Long id) {

        return usuarioRepository.findByIdUsuario(id)
                .map(usuario -> modelMapper.map(usuario, Usuario.class))
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado con ID: " + id));
    }

    @Override
    public Usuario obtenerByCorreo(String correo) {
        return usuarioRepository.findByCorreo(correo)
                .map(u -> modelMapper.map(u, Usuario.class))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no encontrado"));
    }

    @Override
    public Usuario actualizarImagen(String correo, String imagen) {
        UsuarioEntity u = usuarioRepository
                .findByCorreo(correo)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado con correo: " + correo));

        u.setImagen(imagen);

        UsuarioEntity saved = usuarioRepository.save(u);
        return modelMapper.map(saved, Usuario.class);
    }

}
