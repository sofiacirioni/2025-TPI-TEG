package ar.edu.utn.frc.tup.piii.Services.ServicesImpl;

import ar.edu.utn.frc.tup.piii.Entities.UsuarioEntity;
import ar.edu.utn.frc.tup.piii.Repositories.UsuarioRepository;
import ar.edu.utn.frc.tup.piii.models.Usuario;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class UsuariosServiceImplTest {
    private UsuarioRepository usuarioRepository;
    private ModelMapper modelMapper;
    private PasswordEncoder passwordEncoder;
    private UsuarioServiceImpl usuarioService;

    @BeforeEach
    void setUp() {
        usuarioRepository = mock(UsuarioRepository.class);
        modelMapper = new ModelMapper();
        // Encoder real (no mock): así el test ejercita el hashing de verdad, que es
        // justamente lo que cambió cuando actualizarUsuario migró a BCrypt.
        passwordEncoder = new BCryptPasswordEncoder();
        usuarioService = new UsuarioServiceImpl();
        usuarioService.usuarioRepository = usuarioRepository;
        usuarioService.modelMapper = modelMapper;
        usuarioService.passwordEncoder = passwordEncoder;
    }

    @Test
    void testActualizarUsuario_correcto() {
        UsuarioEntity entity = new UsuarioEntity();
        entity.setCorreo("user@domain.com");
        entity.setContrasenia(passwordEncoder.encode("oldpass"));

        when(usuarioRepository.findByCorreo("user@domain.com")).thenReturn(Optional.of(entity));
        when(usuarioRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Usuario actualizado = usuarioService.actualizarUsuario("user@domain.com", "oldpass", "newpass", "imagen.png");

        assertNotNull(actualizado);
        // La contraseña se guarda hasheada, no en texto plano.
        assertNotEquals("newpass", actualizado.getContrasenia());
        assertTrue(passwordEncoder.matches("newpass", actualizado.getContrasenia()));
        assertEquals("imagen.png", actualizado.getImagen());
    }

    @Test
    void testActualizarUsuario_contraseniaIncorrecta() {
        UsuarioEntity entity = new UsuarioEntity();
        entity.setCorreo("user@domain.com");
        entity.setContrasenia(passwordEncoder.encode("Clave321!"));

        when(usuarioRepository.findByCorreo("user@domain.com")).thenReturn(Optional.of(entity));

        // La actual que se envía no coincide con la almacenada.
        assertThrows(ResponseStatusException.class,
                () -> usuarioService.actualizarUsuario("user@domain.com", "ClaveEquivocada!", "Clave123!", "img"));
    }

    @Test
    void testActualizarUsuario_nuevaIgualALaActual() {
        UsuarioEntity entity = new UsuarioEntity();
        entity.setCorreo("user@domain.com");
        entity.setContrasenia(passwordEncoder.encode("Clave321!"));

        when(usuarioRepository.findByCorreo("user@domain.com")).thenReturn(Optional.of(entity));

        assertThrows(ResponseStatusException.class,
                () -> usuarioService.actualizarUsuario("user@domain.com", "Clave321!", "Clave321!", "img"));
    }

    @Test
    void testEliminarUsuario_existente() {
        UsuarioEntity entity = new UsuarioEntity();
        entity.setIdUsuario(1L);
        entity.setCorreo("eliminar@correo.com");

        when(usuarioRepository.findByIdUsuario(1L)).thenReturn(Optional.of(entity));

        Usuario eliminado = usuarioService.eliminarUsuario(1L);

        assertNotNull(eliminado);
        verify(usuarioRepository).delete(entity);
    }

    @Test
    void testEliminarUsuario_inexistente() {
        when(usuarioRepository.findByIdUsuario(99L)).thenReturn(Optional.empty());


        assertThrows(IllegalArgumentException.class, () -> usuarioService.eliminarUsuario(99L));
    }
    @Test
    void obtenerByIdUsuario_deberiaLanzarExcepcionSiNoExiste() {
        Long id = 99L;

        when(usuarioRepository.findByIdUsuario(id)).thenReturn(Optional.empty());


        assertThrows(IllegalArgumentException.class, () -> usuarioService.obtenerByIdUsuario(99L));

    }


    @Test
    void actualizarImagen_deberiaLanzarExcepcionSiCorreoNoExiste() {
        String correo = "noexiste@example.com";
        String nuevaImagen = "imagen.png";

        when(usuarioRepository.findByCorreo(correo)).thenReturn(Optional.empty());


        assertThrows(IllegalArgumentException.class, () -> usuarioService.actualizarImagen("noexiste@example.com", nuevaImagen));
    }

    @Test
    void testActualizarImagen_CuandoUsuarioExiste_DevuelveUsuarioActualizado() {
        String correo = "maxi@example.com";
        String nuevaImagen = "nueva_imagen.png";

        UsuarioEntity usuarioExistente = new UsuarioEntity();
        usuarioExistente.setCorreo(correo);
        usuarioExistente.setImagen("vieja_imagen.png");

        UsuarioEntity usuarioActualizado = new UsuarioEntity();
        usuarioActualizado.setCorreo(correo);
        usuarioActualizado.setImagen(nuevaImagen);

        when(usuarioRepository.findByCorreo(correo)).thenReturn(Optional.of(usuarioExistente));
        when(usuarioRepository.save(usuarioExistente)).thenReturn(usuarioActualizado);

        Usuario resultado = usuarioService.actualizarImagen(correo, nuevaImagen);

        assertNotNull(resultado);
        assertEquals(correo, resultado.getCorreo());
        assertEquals(nuevaImagen, resultado.getImagen());

        verify(usuarioRepository).findByCorreo(correo);
        verify(usuarioRepository).save(usuarioExistente);
    }
}

