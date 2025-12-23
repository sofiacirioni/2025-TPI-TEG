package ar.edu.utn.frc.tup.piii.Controller;

import ar.edu.utn.frc.tup.piii.Dtos.JugadorDto;
import ar.edu.utn.frc.tup.piii.Services.JugadorService;
import ar.edu.utn.frc.tup.piii.Services.PartidaService;
import ar.edu.utn.frc.tup.piii.Services.SalaService;
import ar.edu.utn.frc.tup.piii.Services.UsuarioService;
import ar.edu.utn.frc.tup.piii.models.*;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JugadorControllerTest {

    private JugadorController controller;
    private JugadorService jugadorService;
    private SalaService salaService;
    private UsuarioService usuarioService;
    private PartidaService partidaService;
    private ModelMapper modelMapper;

    @BeforeEach
    void setUp() {
        jugadorService = mock(JugadorService.class);
        salaService = mock(SalaService.class);
        usuarioService = mock(UsuarioService.class);
        partidaService = mock(PartidaService.class);
        modelMapper = mock(ModelMapper.class);

        controller = new JugadorController();
        controller.jugadorService = jugadorService;
        controller.salaService = salaService;
        controller.usuarioService = usuarioService;
        controller.partidaService = partidaService;
        controller.modelMapper = modelMapper;
    }

    @Test
    void testNotificarNuevoJugador() {
        JugadorDto dto = new JugadorDto();
        dto.setNombre("Test");
        JugadorDto result = controller.notificarNuevoJugador("1", dto);
        assertEquals("Test", result.getNombre());
    }

    @Test
    void testGuardarJugador_CuandoUsuarioNoAutenticado() {
        HttpSession session = mock(HttpSession.class);
        when(session.getAttribute("usuarioActual")).thenReturn(null);

        ResponseEntity<JugadorDto> response = controller.guardarJugador(1L, new Jugador(), session);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void testGuardarJugador_CuandoSalaNoExiste() {
        HttpSession session = mock(HttpSession.class);
        Usuario usuario = new Usuario();
        when(session.getAttribute("usuarioActual")).thenReturn(usuario);
        when(salaService.obtenerSala(1L)).thenReturn(null);

        ResponseEntity<JugadorDto> response = controller.guardarJugador(1L, new Jugador(), session);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void testGuardarJugador_CuandoJugadorServiceDevuelveNull() {
        HttpSession session = mock(HttpSession.class);
        Usuario usuario = new Usuario();
        Sala sala = new Sala();

        when(session.getAttribute("usuarioActual")).thenReturn(usuario);
        when(salaService.obtenerSala(1L)).thenReturn(sala);
        when(jugadorService.crearJugador(any(), eq(usuario), eq(sala))).thenReturn(null);

        ResponseEntity<JugadorDto> response = controller.guardarJugador(1L, new Jugador(), session);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void testGuardarJugador_Correcto() {
        HttpSession session = mock(HttpSession.class);
        Usuario usuario = new Usuario();
        Sala sala = new Sala();
        Jugador jugador = new Jugador();
        JugadorDto dto = new JugadorDto();

        when(session.getAttribute("usuarioActual")).thenReturn(usuario);
        when(salaService.obtenerSala(1L)).thenReturn(sala);
        when(jugadorService.crearJugador(any(), eq(usuario), eq(sala))).thenReturn(jugador);
        when(modelMapper.map(jugador, JugadorDto.class)).thenReturn(dto);

        ResponseEntity<JugadorDto> response = controller.guardarJugador(1L, new Jugador(), session);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(dto, response.getBody());
    }

    @Test
    void testCrearBot_CuandoSalaNoExiste() {
        when(salaService.obtenerSala(1L)).thenReturn(null);
        ResponseEntity<JugadorDto> response = controller.crearBot(1L, 2L);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void testCrearBot_CuandoUsuarioNoExiste() {
        Sala sala = new Sala();
        when(salaService.obtenerSala(1L)).thenReturn(sala);
        when(usuarioService.obtenerByIdUsuario(2L)).thenReturn(null);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                controller.crearBot(1L, 2L));
        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
    }

    @Test
    void testCrearBot_Correcto() {
        Sala sala = new Sala();
        Usuario usuario = new Usuario();
        Jugador bot = new Jugador();
        bot.setIdJugador(99L);
        bot.setNombre("Bot_1");
        bot.setColor(Color.ROJO);
        bot.setTipoJugador(TipoJugador.BOT);
        bot.setUsuario(usuario);
        bot.setSala(sala);

        when(salaService.obtenerSala(1L)).thenReturn(sala);
        when(usuarioService.obtenerByIdUsuario(2L)).thenReturn(usuario);
        when(jugadorService.crearBot(sala, usuario)).thenReturn(bot);

        ResponseEntity<JugadorDto> response = controller.crearBot(1L, 2L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Bot_1", response.getBody().getNombre());
    }

    @Test
    void testVotarPausa() {
        ResponseEntity<String> response = controller.votarPausa(1L);
        verify(jugadorService).votarPausa(1L);
        assertEquals("Voto de pausa registrado", response.getBody());
    }

    @Test
    void testVotarReanudar() {
        ResponseEntity<String> response = controller.votarReanudar(2L);
        verify(jugadorService).votarReanudar(2L);
        assertEquals("Voto de reanudación registrado", response.getBody());
    }

    @Test
    void testFinalizarPartida() {
        ResponseEntity<String> response = controller.finalizarPartida(3L);
        verify(jugadorService).finalizarPartida(3L);
        assertEquals("Voto de reanudación registrado", response.getBody());
    }

    @Test
    void testGetJugadoresPorSala() {
        JugadorDto dto = new JugadorDto();
        dto.setNombre("Jugador 1");

        when(jugadorService.obtenerJugadoresPorSala(1L)).thenReturn(Collections.singletonList(dto));

        ResponseEntity<List<JugadorDto>> response = controller.getJugadoresPorSala(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        assertEquals("Jugador 1", response.getBody().get(0).getNombre());
    }
}
