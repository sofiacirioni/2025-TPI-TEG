package ar.edu.utn.frc.tup.piii.Controller;

import ar.edu.utn.frc.tup.piii.Dtos.Login.UsuarioDto;
import ar.edu.utn.frc.tup.piii.Dtos.Login.UsuarioPutDto;
import ar.edu.utn.frc.tup.piii.Services.UsuarioService;
import ar.edu.utn.frc.tup.piii.models.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class UsuariosControllerTest {

    private UsuarioService usuarioService;
    private ModelMapper modelMapper;
    private UsuarioController usuarioController;

    @BeforeEach
    void setUp() {
        usuarioService = mock(UsuarioService.class);
        modelMapper = mock(ModelMapper.class);

        usuarioController = new UsuarioController();
        usuarioController.usuarioService = usuarioService;
        usuarioController.modelMapper = modelMapper;
    }

    @Test
    void testActualizarUsuario_Exitoso() {
        UsuarioPutDto putDto = new UsuarioPutDto();
        putDto.setCorreo("user@mail.com");
        putDto.setContraseniaActual("oldpass");
        putDto.setNuevaContrasenia("newpass");
        putDto.setImagen("img.png");

        Usuario usuarioActualizado = new Usuario();
        usuarioActualizado.setCorreo("user@mail.com");

        UsuarioDto usuarioDto = new UsuarioDto();
        usuarioDto.setCorreo("user@mail.com");

        when(usuarioService.actualizarUsuario("user@mail.com", "oldpass", "newpass", "img.png"))
                .thenReturn(usuarioActualizado);
        when(modelMapper.map(usuarioActualizado, UsuarioDto.class)).thenReturn(usuarioDto);

        ResponseEntity<UsuarioDto> response = usuarioController.actualizarUsuario(putDto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("user@mail.com", response.getBody().getCorreo());
    }

    @Test
    void testActualizarUsuario_Fallido_Unauthorized() {
        UsuarioPutDto putDto = new UsuarioPutDto();
        putDto.setCorreo("user@mail.com");
        putDto.setContraseniaActual("wrong");
        putDto.setNuevaContrasenia("newpass");
        putDto.setImagen(null);

        when(usuarioService.actualizarUsuario("user@mail.com", "wrong", "newpass", null))
                .thenReturn(null);

        ResponseEntity<UsuarioDto> response = usuarioController.actualizarUsuario(putDto);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void testEliminarUsuario_Exitoso() {
        Long id = 1L;

        Usuario usuarioEliminado = new Usuario();
        usuarioEliminado.setCorreo("del@mail.com");

        UsuarioDto usuarioDto = new UsuarioDto();
        usuarioDto.setCorreo("del@mail.com");

        when(usuarioService.eliminarUsuario(id)).thenReturn(usuarioEliminado);
        when(modelMapper.map(usuarioEliminado, UsuarioDto.class)).thenReturn(usuarioDto);

        ResponseEntity<UsuarioDto> response = usuarioController.eliminarUsuario(id);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("del@mail.com", response.getBody().getCorreo());
    }

    @Test
    void testEliminarUsuario_Fallido() {
        Long id = 1L;

        when(usuarioService.eliminarUsuario(id)).thenReturn(null);

        ResponseEntity<UsuarioDto> response = usuarioController.eliminarUsuario(id);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void testActualizarImagen_Exitoso() {
        String correo = "user@mail.com";
        String imagen = "imagen-nueva.png";

        Usuario usuarioActualizado = new Usuario();
        usuarioActualizado.setCorreo(correo);

        UsuarioDto usuarioDto = new UsuarioDto();
        usuarioDto.setCorreo(correo);

        Map<String, String> body = Map.of("correo", correo, "imagen", imagen);

        when(usuarioService.actualizarImagen(correo, imagen)).thenReturn(usuarioActualizado);
        when(modelMapper.map(usuarioActualizado, UsuarioDto.class)).thenReturn(usuarioDto);

        ResponseEntity<UsuarioDto> response = usuarioController.actualizarImagen(body);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(correo, response.getBody().getCorreo());
    }
}
