package ar.edu.utn.frc.tup.piii.Services.ServicesImpl;

import ar.edu.utn.frc.tup.piii.Entities.*;
import ar.edu.utn.frc.tup.piii.Repositories.*;
import ar.edu.utn.frc.tup.piii.models.EstadoPartida;
import ar.edu.utn.frc.tup.piii.models.FaseJuego;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JuegoServiceImplTest {
    @Mock
    private PartidaRepository partidaRepository;
    @Mock
    private JugadorRepository jugadorRepository;
    @Mock
    private ObjetivoRepository objetivoRepository;
    @Mock
    private PaisRepository paisRepository;
    @Mock
    private EstadoPaisRepository estadoPaisRepository;
    @InjectMocks
    private JuegoServiceImpl juegoService;

    @Test
    void testRepartirPaisesYObjetivos() {
        Long idPartida = 1L;
        PartidaEntity partida = new PartidaEntity();
        SalaEntity sala = new SalaEntity();

        List<JugadorEntity> jugadores = Arrays.asList(new JugadorEntity(), new JugadorEntity());
        sala.setJugadores(jugadores);
        partida.setConfiguracion(sala);

        List<PaisEntity> paises = Arrays.asList(new PaisEntity(), new PaisEntity(), new PaisEntity());
        List<ObjetivoEntity> objetivos = Arrays.asList(new ObjetivoEntity(), new ObjetivoEntity());

        when(partidaRepository.findById(idPartida)).thenReturn(Optional.of(partida));
        when(paisRepository.findAll()).thenReturn(paises);
        when(objetivoRepository.findAll()).thenReturn(objetivos);

        juegoService.repartirPaisesYObjetivos(idPartida);

        verify(estadoPaisRepository, times(paises.size())).save(any(EstadoPaisEntity.class));
        verify(jugadorRepository, times(jugadores.size())).save(any(JugadorEntity.class));
    }

    @Test
    void testIniciarColocacionEjercitos() {
        Long idPartida = 1L;
        PartidaEntity partida = new PartidaEntity();

        when(partidaRepository.findById(idPartida)).thenReturn(Optional.of(partida));

        juegoService.iniciarColocacionEjercitos(idPartida);

        assertEquals(FaseJuego.COLOCACION, partida.getFaseActual());
        assertEquals(0, partida.getTurnoActual());
        verify(partidaRepository).save(partida);
    }

    @Test
    void testAvanzarFase() {
        Long idPartida = 1L;
        PartidaEntity partida = new PartidaEntity();
        SalaEntity sala = new SalaEntity();
        List<JugadorEntity> jugadores = Arrays.asList(new JugadorEntity(), new JugadorEntity());
        sala.setJugadores(jugadores);
        partida.setConfiguracion(sala);

        partida.setFaseActual(FaseJuego.COLOCACION);
        partida.setTurnoActual(0);

        when(partidaRepository.findById(idPartida)).thenReturn(Optional.of(partida));

        juegoService.avanzarFase(idPartida);
        assertEquals(FaseJuego.HOSTILIDADES, partida.getFaseActual());

        partida.setFaseActual(FaseJuego.HOSTILIDADES);
        juegoService.avanzarFase(idPartida);
        assertEquals(FaseJuego.FIN_TURNO, partida.getFaseActual());

        partida.setFaseActual(FaseJuego.FIN_TURNO);
        partida.setTurnoActual(0);
        juegoService.avanzarFase(idPartida);
        assertEquals(FaseJuego.COLOCACION, partida.getFaseActual());
        assertEquals(1, partida.getTurnoActual());
    }

    @Test
    void testComprobarObjetivoCumplido_Cumple() {
        Long idPartida = 1L;
        PartidaEntity partida = new PartidaEntity();
        SalaEntity sala = new SalaEntity();

        JugadorEntity jugador = new JugadorEntity();
        ObjetivoEntity objetivo = new ObjetivoEntity();
        objetivo.setCantidadPaisesObjetivo(3);
        jugador.setObjetivo(objetivo);

        sala.setJugadores(List.of(jugador));
        partida.setConfiguracion(sala);

        when(partidaRepository.findById(idPartida)).thenReturn(Optional.of(partida));
        when(estadoPaisRepository.countByJugador(jugador)).thenReturn(3L);

        boolean cumplido = juegoService.comprobarObjetivoCumplido(idPartida);

        assertTrue(cumplido);
        assertEquals(EstadoPartida.TERMINADA, partida.getEstadoPartida());
        assertEquals(jugador, partida.getGanador());
        verify(partidaRepository).save(partida);
    }

    @Test
    void testComprobarObjetivoCumplido_NoCumple() {
        Long idPartida = 1L;
        PartidaEntity partida = new PartidaEntity();
        SalaEntity sala = new SalaEntity();

        JugadorEntity jugador = new JugadorEntity();
        ObjetivoEntity objetivo = new ObjetivoEntity();
        objetivo.setCantidadPaisesObjetivo(5);
        jugador.setObjetivo(objetivo);

        sala.setJugadores(List.of(jugador));
        partida.setConfiguracion(sala);

        when(partidaRepository.findById(idPartida)).thenReturn(Optional.of(partida));
        when(estadoPaisRepository.countByJugador(jugador)).thenReturn(2L);

        boolean cumplido = juegoService.comprobarObjetivoCumplido(idPartida);

        assertFalse(cumplido);
        verify(partidaRepository, never()).save(partida);
    }

    @Test
    void testExcepcionesPartidaNoEncontrada() {
        Long id = 1L;
        when(partidaRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> juegoService.repartirPaisesYObjetivos(id));
        assertThrows(RuntimeException.class, () -> juegoService.iniciarColocacionEjercitos(id));
        assertThrows(RuntimeException.class, () -> juegoService.avanzarFase(id));
        assertThrows(RuntimeException.class, () -> juegoService.comprobarObjetivoCumplido(id));
    }
}