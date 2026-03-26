package ar.edu.utn.frc.tup.piii.Services.ServicesImpl;

import ar.edu.utn.frc.tup.piii.Entities.UsuarioEntity;
import ar.edu.utn.frc.tup.piii.Repositories.UsuarioRepository;
import ar.edu.utn.frc.tup.piii.models.Usuario;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class UsuariosServiceImplTest {
    private UsuarioRepository usuarioRepository;
    private ModelMapper modelMapper;
    private UsuarioServiceImpl usuarioService;

    @BeforeEach
    void setUp() {
        usuarioRepository = mock(UsuarioRepository.class);
        modelMapper = new ModelMapper();
        usuarioService = new UsuarioServiceImpl();
        usuarioService.usuarioRepository = usuarioRepository;
        usuarioService.modelMapper = modelMapper;
    }

    @Test
    void testActualizarUsuario_correcto() {
        UsuarioEntity entity = new UsuarioEntity();
        entity.setCorreo("user@domain.com");
        entity.setContrasenia("oldpass");

        when(usuarioRepository.findByCorreo("user@domain.com")).thenReturn(Optional.of(entity));
        when(usuarioRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Usuario actualizado = usuarioService.actualizarUsuario("user@domain.com", "oldpass", "newpass", "imagen.png");

        assertNotNull(actualizado);
        assertEquals("newpass", actualizado.getContrasenia());
        assertEquals("imagen.png", actualizado.getImagen());
    }

    @Test
    void testActualizarUsuario_contraseniaIncorrecta() {
        UsuarioEntity entity = new UsuarioEntity();
        entity.setCorreo("user@domain.com");
        entity.setContrasenia("Clave321!");

        when(usuarioRepository.findByCorreo("user@domain.com")).thenReturn(Optional.of(entity));


        assertThrows(IllegalArgumentException.class, () -> usuarioService.actualizarUsuario("user@domain.com", "Clave321!", "Clave123!", "img"));
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

