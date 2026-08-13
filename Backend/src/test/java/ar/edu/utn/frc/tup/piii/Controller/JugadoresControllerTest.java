package ar.edu.utn.frc.tup.piii.Controller;


import ar.edu.utn.frc.tup.piii.Entities.JugadorEntity;

import ar.edu.utn.frc.tup.piii.Repositories.JugadorRepository;
import ar.edu.utn.frc.tup.piii.Services.ObjetivoService;
import ar.edu.utn.frc.tup.piii.Services.ServicesImpl.JugadorServiceImpl;
import ar.edu.utn.frc.tup.piii.Services.UsuarioService;
import ar.edu.utn.frc.tup.piii.models.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
public class JugadoresControllerTest {
    private JugadorServiceImpl jugadorService;
    private JugadorRepository jugadorRepository;
    private UsuarioService usuarioService;
    private ObjetivoService objetivoService;
    private ModelMapper modelMapper;

    @BeforeEach
    void setUp() {
        jugadorRepository = mock(JugadorRepository.class);
        usuarioService = mock(UsuarioService.class);
        objetivoService = mock(ObjetivoService.class);
        modelMapper = new ModelMapper();

        jugadorService = new JugadorServiceImpl();
        jugadorService.jugadorRepository = jugadorRepository;
        jugadorService.usuarioService = usuarioService;
        jugadorService.objetivoService = objetivoService;
        jugadorService.modelMapper = modelMapper;
    }

    @Test
    void testCrearBot_UsuarioEsCreador_DeberiaCrearBotCorrectamente() {
        Usuario usuarioCreador = new Usuario();
        usuarioCreador.setIdUsuario(1L);

        Sala sala = new Sala();
        sala.setIdSala(10L);
        sala.setCreador(usuarioCreador);

        Usuario usuarioModel = modelMapper.map(usuarioCreador, Usuario.class);

        when(jugadorRepository.findBySala_IdSala(sala.getIdSala())).thenReturn(Collections.emptyList());
        when(jugadorRepository.save(any(JugadorEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0)); // Simula guardado

        Jugador botCreado = jugadorService.crearBot(sala, usuarioModel);

        assertNotNull(botCreado, "El bot no debe ser null");
        assertNotNull(botCreado.getNombre(), "El nombre del bot no debe ser null");
        // Los bots llevan nombre de tropa: "Sgto. BOTACCIO", "Sgto. BOTANA"…
        assertTrue(botCreado.getNombre().startsWith("Sgto. BOT"),
                "El nombre del bot debe tener el formato 'Sgto. BOT...', y fue: " + botCreado.getNombre());
        assertEquals(TipoJugador.BOT, botCreado.getTipoJugador(), "El tipo de jugador debe ser BOT");
        assertNotNull(botCreado.getColor(), "El color del bot no debe ser null");
        assertEquals(sala.getIdSala(), botCreado.getSala().getIdSala(), "El bot debe pertenecer a la sala correcta");
    }

    @Test
    void testCrearBot_UsuarioNoEsCreador_DeberiaLanzarExcepcion() {
        Usuario usuarioCreador = new Usuario();
        usuarioCreador.setIdUsuario(1L);

        Usuario otroUsuario = new Usuario();
        otroUsuario.setIdUsuario(2L);

        Sala sala = new Sala();
        sala.setIdSala(20L);
        sala.setCreador(usuarioCreador);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            jugadorService.crearBot(sala, otroUsuario);
        });

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
        assertEquals("Solo el creador de la sala puede agregar bots.", exception.getReason());
    }

}

