package ar.edu.utn.frc.tup.piii.Controller;

import ar.edu.utn.frc.tup.piii.Dtos.JugadorDto;
import ar.edu.utn.frc.tup.piii.Services.JugadorService;
import ar.edu.utn.frc.tup.piii.Services.PartidaService;
import ar.edu.utn.frc.tup.piii.Services.SalaService;
import ar.edu.utn.frc.tup.piii.Services.UsuarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

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
        assertEquals("Retiro registrado", response.getBody());
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
