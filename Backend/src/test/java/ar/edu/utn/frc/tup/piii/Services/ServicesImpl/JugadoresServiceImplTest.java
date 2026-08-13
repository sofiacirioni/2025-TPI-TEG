package ar.edu.utn.frc.tup.piii.Services.ServicesImpl;

import ar.edu.utn.frc.tup.piii.Dtos.JugadorDto;
import ar.edu.utn.frc.tup.piii.Entities.JugadorEntity;
import ar.edu.utn.frc.tup.piii.Entities.PartidaEntity;
import ar.edu.utn.frc.tup.piii.Entities.SalaEntity;
import ar.edu.utn.frc.tup.piii.Repositories.JugadorRepository;
import ar.edu.utn.frc.tup.piii.Repositories.PartidaRepository;
import ar.edu.utn.frc.tup.piii.models.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.modelmapper.ModelMapper;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class JugadoresServiceImplTest {

    @InjectMocks
    private JugadorServiceImpl jugadorService;

    @Mock
    private JugadorRepository jugadorRepository;

    @Mock
    private PartidaRepository partidaRepository;

    private ModelMapper modelMapper;

    @BeforeEach
    void setUp() {
        modelMapper = new ModelMapper();
        jugadorService.modelMapper=modelMapper;
    }

    @Test
    void testCrearJugador_CuandoNombreYaExiste_DevuelveNull() {
        Jugador jugador = new Jugador();
        jugador.setNombre("Juan");

        Sala sala = new Sala();
        sala.setIdSala(1L);

        Usuario usuario = new Usuario();
        usuario.setIdUsuario(1L);

        when(jugadorRepository.findByNombreAndSala_IdSala("Juan", 1L))
                .thenReturn(Optional.of(new JugadorEntity()));

        assertThrows(IllegalArgumentException.class, () -> jugadorService.crearJugador(jugador, usuario, sala));
    }

    @Test
    void testCrearJugador_SinColoresDisponibles_DevuelveNull() {
        Jugador jugador = new Jugador();
        jugador.setNombre("Pedro");

        Sala sala = new Sala();
        sala.setIdSala(2L);


        List<JugadorEntity> jugadores = new ArrayList<>();
        for (Color color : Color.values()) {
            JugadorEntity j = new JugadorEntity();
            j.setColor(color);
            jugadores.add(j);
        }

        when(jugadorRepository.findBySala_IdSala(2L)).thenReturn(jugadores);

        Color color = jugadorService.obtenerColorDisponible(sala, 2L);
        assertNull(color);
    }

    @Test
    void testCrearJugador_Exitosamente() {
        Jugador jugador = new Jugador();
        jugador.setNombre("Lucia");

        Sala sala = new Sala();
        sala.setIdSala(3L);

        Usuario usuario = new Usuario();
        usuario.setIdUsuario(10L);

        when(jugadorRepository.findByNombreAndSala_IdSala("Lucia", 3L))
                .thenReturn(Optional.empty());

        JugadorEntity jugadorExistente = new JugadorEntity();
        jugadorExistente.setColor(Color.ROJO);

        when(jugadorRepository.findBySala_IdSala(3L)).thenReturn(List.of(jugadorExistente));
        when(jugadorRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);

        Jugador resultado = jugadorService.crearJugador(jugador, usuario, sala);

        assertNotNull(resultado);
        assertEquals(usuario.getIdUsuario(), resultado.getUsuario().getIdUsuario());
        assertEquals(sala.getIdSala(), resultado.getSala().getIdSala());
        assertNotNull(resultado.getColor());
        assertEquals(TipoJugador.HUMANO, resultado.getTipoJugador());
    }

    @Test
    void testCrearBot_CorrectamenteDevuelveBot() {
        Usuario usuario = new Usuario();
        usuario.setIdUsuario(1L);

        Sala sala = new Sala();
        sala.setIdSala(1L);
        sala.setCreador(usuario);

        when(jugadorRepository.findBySala_IdSala(1L)).thenReturn(new ArrayList<>());
        when(jugadorRepository.save(any(JugadorEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Jugador result = jugadorService.crearBot(sala, usuario);

        assertNotNull(result);
        assertEquals(TipoJugador.BOT, result.getTipoJugador());
        assertEquals(usuario, result.getUsuario());
        assertEquals(sala.getIdSala(), result.getSala().getIdSala());
    }

    @Test
    void testVotarPausa_TodosAceptanPausa() {
        Long jugadorId = 1L;
        Long partidaId = 100L;

        PartidaEntity partida = new PartidaEntity();
        partida.setIdPartida(partidaId);
        partida.setEstadoPartida(EstadoPartida.EN_JUEGO);

        JugadorEntity jugador1 = new JugadorEntity();
        jugador1.setIdJugador(jugadorId);
        jugador1.setAceptoPausa(false);
        jugador1.setPartida(partida);

        JugadorEntity jugador2 = new JugadorEntity();
        jugador2.setIdJugador(2L);
        jugador2.setAceptoPausa(true);
        jugador2.setPartida(partida);

        when(jugadorRepository.findById(jugadorId)).thenReturn(Optional.of(jugador1));
        when(jugadorRepository.findHumanosByPartidaId(partidaId)).thenReturn(List.of(jugador1, jugador2));

        jugadorService.votarPausa(jugadorId);

        assertTrue(jugador1.isAceptoPausa());
        verify(jugadorRepository).save(jugador1);
        verify(partidaRepository).save(partida);
        assertEquals(EstadoPartida.PAUSADA, partida.getEstadoPartida());
    }
    @Test
    void testVotarReanudar_TodosAceptan() {
        Long jugadorId = 1L;
        Long partidaId = 10L;

        PartidaEntity partida = new PartidaEntity();
        partida.setIdPartida(partidaId);
        partida.setEstadoPartida(EstadoPartida.PAUSADA);

        JugadorEntity jugador1 = new JugadorEntity();
        jugador1.setIdJugador(jugadorId);
        jugador1.setAceptoRenudar(false);
        jugador1.setAceptoPausa(true);
        jugador1.setPartida(partida);

        JugadorEntity jugador2 = new JugadorEntity();
        jugador2.setIdJugador(2L);
        jugador2.setAceptoRenudar(true);
        jugador2.setPartida(partida);

        when(jugadorRepository.findById(jugadorId)).thenReturn(Optional.of(jugador1));
        when(jugadorRepository.findHumanosByPartidaId(partidaId)).thenReturn(List.of(jugador1, jugador2));

        jugadorService.votarReanudar(jugadorId);

        assertTrue(jugador1.isAceptoRenudar());
        assertFalse(jugador1.isAceptoPausa());
        verify(jugadorRepository).save(jugador1);
        verify(partidaRepository).save(partida);
        assertEquals(EstadoPartida.EN_JUEGO, partida.getEstadoPartida());
    }
    @Test
    void testFinalizarPartida_TodosAceptan_HumanosYBotsEliminados() {
        Long jugadorId = 1L;
        Long partidaId = 20L;

        PartidaEntity partida = new PartidaEntity();
        partida.setIdPartida(partidaId);
        partida.setEstadoPartida(EstadoPartida.EN_JUEGO);

        JugadorEntity jugador1 = new JugadorEntity();
        jugador1.setIdJugador(jugadorId);
        jugador1.setFinalizarPartida(false);
        jugador1.setPartida(partida);

        JugadorEntity jugador2 = new JugadorEntity();
        jugador2.setIdJugador(2L);
        jugador2.setFinalizarPartida(true);
        jugador2.setPartida(partida);

        JugadorEntity bot1 = new JugadorEntity();
        bot1.setIdJugador(3L);
        bot1.setPartida(partida);

        when(jugadorRepository.findById(jugadorId)).thenReturn(Optional.of(jugador1));
        when(jugadorRepository.findHumanosByPartidaId(partidaId)).thenReturn(List.of(jugador1, jugador2));
        when(jugadorRepository.findBotByPartidaId(partidaId)).thenReturn(List.of(bot1));

        jugadorService.finalizarPartida(jugadorId);

        assertTrue(jugador1.isFinalizarPartida());
        assertTrue(jugador1.isPerdio());
        assertEquals(EstadoJugador.ELIMINADO, jugador1.getEstadoJugador());
        verify(jugadorRepository).save(jugador1);

        assertTrue(bot1.isFinalizarPartida());
        assertTrue(bot1.isPerdio());
        assertEquals(EstadoJugador.ELIMINADO, bot1.getEstadoJugador());
        verify(jugadorRepository).save(bot1);

        // Nadie ganó: los comandantes se retiraron. La campaña queda abandonada
        // y no suma al historial de ninguno.
        assertEquals(EstadoPartida.ABANDONADA, partida.getEstadoPartida());
        verify(partidaRepository).save(partida);
    }
    @Test
    @DisplayName("obtenerJugadoresPorSala → devuelve lista de JugadorDto cuando hay entidades")
    void testObtenerJugadoresPorSala_conJugadores_retornaListaDto() {

        Long idSala = 42L;
        SalaEntity sala = new SalaEntity();
        sala.setIdSala(idSala);

        JugadorEntity e1 = new JugadorEntity();
        e1.setIdJugador(1L);
        e1.setNombre("Alice");
        e1.setSala(sala);

        JugadorEntity e2 = new JugadorEntity();
        e2.setIdJugador(2L);
        e2.setNombre("Bob");
        e2.setSala(sala);

        when(jugadorRepository.findBySala_IdSala(idSala))
                .thenReturn(Arrays.asList(e1, e2));

        List<JugadorDto> resultado = jugadorService.obtenerJugadoresPorSala(idSala);

        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        assertEquals(1L, resultado.get(0).getIdJugador());
        assertEquals("Alice", resultado.get(0).getNombre());
        assertEquals(2L, resultado.get(1).getIdJugador());
        assertEquals("Bob", resultado.get(1).getNombre());

        verify(jugadorRepository).findBySala_IdSala(idSala);
    }

    @Test
    @DisplayName("obtenerJugadoresPorSala → devuelve lista vacía cuando no hay entidades")
    void testObtenerJugadoresPorSala_sinJugadores_retornaListaVacia() {

        Long idSala = 99L;
        when(jugadorRepository.findBySala_IdSala(idSala))
                .thenReturn(Collections.emptyList());

        List<JugadorDto> resultado = jugadorService.obtenerJugadoresPorSala(idSala);

        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());

        verify(jugadorRepository).findBySala_IdSala(idSala);
    }

}
