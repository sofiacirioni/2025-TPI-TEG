package ar.edu.utn.frc.tup.piii.Controller;

import ar.edu.utn.frc.tup.piii.Dtos.Login.Credencial;
import ar.edu.utn.frc.tup.piii.Dtos.Login.UsuarioDto;
import ar.edu.utn.frc.tup.piii.Dtos.Login.UsuarioPutDto;
import ar.edu.utn.frc.tup.piii.Services.UsuarioService;
import ar.edu.utn.frc.tup.piii.models.Usuario;
import jakarta.servlet.http.HttpSession;
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
    private HttpSession httpSession;

    @BeforeEach
    void setUp() {
        usuarioService = mock(UsuarioService.class);
        modelMapper = mock(ModelMapper.class);
        httpSession = mock(HttpSession.class);

        usuarioController = new UsuarioController();
        usuarioController.usuarioService = usuarioService;
        usuarioController.modelMapper = modelMapper;
    }

    @Test
    void testGuardarUsuario_Correcto() {
        Usuario usuario = new Usuario();
        usuario.setCorreo("test@mail.com");

        Usuario usuarioGuardado = new Usuario();
        usuarioGuardado.setCorreo("test@mail.com");

        UsuarioDto usuarioDto = new UsuarioDto();
        usuarioDto.setCorreo("test@mail.com");

        when(usuarioService.guardarUsuario(usuario)).thenReturn(usuarioGuardado);
        when(modelMapper.map(usuarioGuardado, UsuarioDto.class)).thenReturn(usuarioDto);

        ResponseEntity<UsuarioDto> response = usuarioController.GuardarUsuario(usuario);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("test@mail.com", response.getBody().getCorreo());
    }

    @Test
    void testGuardarUsuario_Falla_BadRequest() {
        Usuario usuario = new Usuario();

        when(usuarioService.guardarUsuario(usuario)).thenReturn(null);

        ResponseEntity<UsuarioDto> response = usuarioController.GuardarUsuario(usuario);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void testBuscarUsuario_LoginExitoso() {
        Credencial credencial = new Credencial();
        credencial.setCorreo("user@mail.com");
        credencial.setContrasenia("1234");

        Usuario usuario = new Usuario();
        usuario.setCorreo("user@mail.com");

        UsuarioDto usuarioDto = new UsuarioDto();
        usuarioDto.setCorreo("user@mail.com");

        when(usuarioService.obtenerUsuario("user@mail.com", "1234")).thenReturn(usuario);
        when(modelMapper.map(usuario, UsuarioDto.class)).thenReturn(usuarioDto);

        ResponseEntity<UsuarioDto> response = usuarioController.buscarUsuario(credencial, httpSession);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(httpSession).setAttribute("usuarioActual", usuario);
        assertEquals("user@mail.com", response.getBody().getCorreo());
    }

    @Test
    void testBuscarUsuario_LoginFallido() {
        Credencial credencial = new Credencial();
        credencial.setCorreo("user@mail.com");
        credencial.setContrasenia("wrong");

        when(usuarioService.obtenerUsuario("user@mail.com", "wrong")).thenReturn(null);

        ResponseEntity<UsuarioDto> response = usuarioController.buscarUsuario(credencial, httpSession);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNull(response.getBody());
        verify(httpSession, never()).setAttribute(anyString(), any());
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
    @Test
    void testActualizarImagen_Fallido_BodyIncompleto() {
        String correo = "user@mail.com";
        String imagen = null;

        Map<String, String> body = Map.of("correo", correo);

        when(usuarioService.actualizarImagen(correo, imagen)).thenReturn(null);

        ResponseEntity<UsuarioDto> response = usuarioController.actualizarImagen(body);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNull(response.getBody());
    }


}

