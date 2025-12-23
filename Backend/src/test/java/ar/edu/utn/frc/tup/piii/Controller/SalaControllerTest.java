package ar.edu.utn.frc.tup.piii.Controller;

import ar.edu.utn.frc.tup.piii.Dtos.SalaDto;
import ar.edu.utn.frc.tup.piii.models.Sala;
import ar.edu.utn.frc.tup.piii.models.Usuario;
import ar.edu.utn.frc.tup.piii.Services.SalaService;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class SalaControllerTest {
    private SalaService salaService;
    private ModelMapper modelMapper;
    private SalaController salaController;
    private HttpSession session;

    @BeforeEach
    void setUp() {
        salaService = mock(SalaService.class);
        modelMapper = mock(ModelMapper.class);
        session = mock(HttpSession.class);

        salaController = new SalaController();
        salaController.salaService = salaService;
        salaController.modelMapper = modelMapper;
    }

    @Test
    void crearSala_usuarioNoLogueado_Unauthorized() {
        when(session.getAttribute("usuarioActual")).thenReturn(null);

        Sala sala = new Sala();

        ResponseEntity<?> response = salaController.crearSala(sala, session);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void crearSala_salaNoCreada_BadRequest() {
        Usuario usuario = new Usuario();
        usuario.setIdUsuario(1L);

        Sala sala = new Sala();
        sala.setNombreSala("SalaTest");

        when(session.getAttribute("usuarioActual")).thenReturn(usuario);
        when(salaService.crearSala(sala, usuario, "SalaTest")).thenReturn(null);

        ResponseEntity<?> response = salaController.crearSala(sala, session);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void crearSala_salaCreada_Ok() {
        Usuario usuario = new Usuario();
        usuario.setIdUsuario(1L);

        Sala sala = new Sala();
        sala.setNombreSala("SalaTest");

        Sala salaGuardada = new Sala();
        salaGuardada.setNombreSala("SalaTest");

        SalaDto salaDto = new SalaDto();

        when(session.getAttribute("usuarioActual")).thenReturn(usuario);
        when(salaService.crearSala(sala, usuario, "SalaTest")).thenReturn(salaGuardada);
        when(modelMapper.map(salaGuardada, SalaDto.class)).thenReturn(salaDto);

        ResponseEntity<SalaDto> response = salaController.crearSala(sala, session);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(salaDto, response.getBody());
    }

    @Test
    void obtenerSala_salaNoEncontrada_NotFound() {
        when(salaService.obtenerSala(1L)).thenReturn(null);

        ResponseEntity<?> response = salaController.obtenerSala(1L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void obtenerSala_salaEncontrada_Ok() {
        Sala sala = new Sala();
        SalaDto salaDto = new SalaDto();

        when(salaService.obtenerSala(1L)).thenReturn(sala);
        when(modelMapper.map(sala, SalaDto.class)).thenReturn(salaDto);

        ResponseEntity<SalaDto> response = salaController.obtenerSala(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(salaDto, response.getBody());
    }
    @Test
    void obtenerSalaPorUrl_salaEncontrada_Ok() {
        String url = "url-existente";

        Sala sala = new Sala();
        SalaDto salaDto = new SalaDto();

        when(salaService.obtenerSala(url)).thenReturn(sala);
        when(modelMapper.map(sala, SalaDto.class)).thenReturn(salaDto);

        ResponseEntity<SalaDto> response = salaController.obtenerSalaPorUrl(url);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(salaDto, response.getBody());
    }
    @Test
    void obtenerSalaPorUrl_salaNoEncontrada_NotFound() {
        String urlInexistente = "url-no-existe";

        when(salaService.obtenerSala(urlInexistente)).thenReturn(null);

        ResponseEntity<?> response = salaController.obtenerSalaPorUrl(urlInexistente);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }
    @Test
    void crearSala_nombreSalaVacio_BadRequest() {
        Usuario usuario = new Usuario();
        usuario.setIdUsuario(1L);

        Sala sala = new Sala();

        when(session.getAttribute("usuarioActual")).thenReturn(usuario);
        when(salaService.crearSala(eq(sala), eq(usuario), anyString())).thenReturn(null);

        ResponseEntity<?> response = salaController.crearSala(sala, session);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNull(response.getBody());
    }
    @Test
    void enviarInicioPartida_devuelveDatosCorrectamente() {
        Map<String, String> inputData = new HashMap<>();
        inputData.put("mensaje", "¡Que comience!");

        Map<String, String> resultado = salaController.enviarInicioPartida(42L, inputData);

        assertEquals(inputData, resultado);
        assertEquals("¡Que comience!", resultado.get("mensaje"));
    }
}
