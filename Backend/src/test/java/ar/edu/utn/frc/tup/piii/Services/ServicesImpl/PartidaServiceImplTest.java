package ar.edu.utn.frc.tup.piii.Services.ServicesImpl;

import ar.edu.utn.frc.tup.piii.Dtos.JugadorDto;
import ar.edu.utn.frc.tup.piii.Dtos.PartidaDto;
import ar.edu.utn.frc.tup.piii.Entities.*;
import ar.edu.utn.frc.tup.piii.Repositories.*;
import ar.edu.utn.frc.tup.piii.Services.*;
import ar.edu.utn.frc.tup.piii.models.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PartidaServiceImplTest {
    @Mock
    private PartidaRepository partidaRepository;
    @Mock
    private SalaRepository salaRepository;
    @Mock
    private EstadoPaisService estadoPaisService;
    @Mock
    private ObjetivoService objetivoService;
    @Mock
    private JugadorRepository jugadorRepository;
    @Mock
    private EstadisticaService estadisticaService;
    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private ModelMapper modelMapper;
    @Mock
    private EstadoTarjetaService estadoTarjetaService;
    @Mock
    private TurnoService turnoService;
    @InjectMocks
    private PartidaServiceImpl partidaService;

    @Captor
    private ArgumentCaptor<PartidaEntity> partidaEntityCaptor;
    @Captor
    private ArgumentCaptor<JugadorEntity> jugadorEntityCaptor;

    private PartidaEntity partidaEntity;
    private Partida partida;
    private SalaEntity salaEntity;
    private UsuarioEntity usuarioEntity;
    private UsuarioEntity creadorEntity;
    private JugadorEntity jugadorEntity;
    private JugadorEntity jugador2Entity;
    private List<JugadorEntity> jugadores;

    @BeforeEach
    void setUp() {
        creadorEntity = new UsuarioEntity();
        creadorEntity.setIdUsuario(1L);
        creadorEntity.setUsuario("Creador");

        usuarioEntity = new UsuarioEntity();
        usuarioEntity.setIdUsuario(2L);
        usuarioEntity.setUsuario("Usuario Test");
        usuarioEntity.setCorreo("test@example.com");
        usuarioEntity.setContrasenia("Password1@");
        usuarioEntity.setImagen("avatar.png");

        salaEntity = new SalaEntity();
        salaEntity.setIdSala(1L);
        salaEntity.setNombreSala("Sala Test");
        salaEntity.setUrl("abc123");
        salaEntity.setEstado(EstadoSala.ESPERANDO);
        salaEntity.setCreador(creadorEntity);
        salaEntity.setJugadores(new ArrayList<>());

        jugadorEntity = new JugadorEntity();
        jugadorEntity.setIdJugador(1L);
        jugadorEntity.setNombre("Jugador 1");
        jugadorEntity.setUsuario(creadorEntity);
        jugadorEntity.setSala(salaEntity);
        jugadorEntity.setPerdio(false);
        jugadorEntity.setEstadoJugador(EstadoJugador.ACTIVO);

        jugador2Entity = new JugadorEntity();
        jugador2Entity.setIdJugador(2L);
        jugador2Entity.setNombre("Jugador 2");
        jugador2Entity.setUsuario(usuarioEntity);
        jugador2Entity.setSala(salaEntity);
        jugador2Entity.setPerdio(false);
        jugador2Entity.setEstadoJugador(EstadoJugador.ACTIVO);

        jugadores = List.of(jugadorEntity, jugador2Entity);
        salaEntity.setJugadores(new ArrayList<>(jugadores));

        partidaEntity = new PartidaEntity();
        partidaEntity.setIdPartida(1L);
        partidaEntity.setEstadoPartida(EstadoPartida.EN_JUEGO);
        partidaEntity.setFechaInicio(LocalDate.now());
        partidaEntity.setTurnoActual(1);
        partidaEntity.setConfiguracion(salaEntity);
        partidaEntity.setJugadores(jugadores);
        partidaEntity.setTurnos(new ArrayList<>());
        partidaEntity.setMazo(new ArrayList<>());
        partidaEntity.setEstadoPaises(new ArrayList<>());

        partida = new Partida();
        partida.setIdPartida(1L);
        partida.setEstadoPartida(EstadoPartida.EN_JUEGO);
        partida.setFechaInicio(LocalDate.now());
    }

    @Test
    void crearPartidaYAsignarJugadores_deberiaCrearPartidaCuandoTodoEsValido() {
        Long idSala = 1L;
        Usuario usuario = new Usuario();
        usuario.setIdUsuario(10L);

        UsuarioEntity creador = new UsuarioEntity();
        creador.setIdUsuario(10L);

        SalaEntity sala = new SalaEntity();
        sala.setIdSala(idSala);
        sala.setCreador(creador);
        sala.setEstado(EstadoSala.ESPERANDO);

        JugadorEntity jugador1 = new JugadorEntity();
        jugador1.setIdJugador(1L);
        jugador1.setNombre("Jugador 1");

        JugadorEntity jugador2 = new JugadorEntity();
        jugador2.setIdJugador(2L);
        jugador2.setNombre("Jugador 2");

        List<JugadorEntity> jugadores = List.of(jugador1, jugador2);

        PartidaEntity partidaGuardada = new PartidaEntity();
        partidaGuardada.setIdPartida(99L);
        partidaGuardada.setFechaInicio(LocalDate.now());
        partidaGuardada.setEstadoPartida(EstadoPartida.EN_JUEGO);
        partidaGuardada.setConfiguracion(sala);
        partidaGuardada.setTurnoActual(1);

        when(salaRepository.findById(idSala)).thenReturn(Optional.of(sala));
        when(jugadorRepository.findBySala_IdSala(idSala)).thenReturn(jugadores);
        when(partidaRepository.save(any(PartidaEntity.class))).thenAnswer(invocation -> {
            PartidaEntity saved = invocation.getArgument(0);
            saved.setIdPartida(99L);
            return saved;
        });

        Partida partida = partidaService.crearPartidaYAsignarJugadores(idSala, usuario);

        assertNotNull(partida);
        assertEquals(99L, partida.getIdPartida());
        assertEquals(2, partida.getJugadores().size());
        assertEquals(EstadoPartida.EN_JUEGO, partida.getEstadoPartida());
        assertEquals(idSala, partida.getIdSala());

        verify(objetivoService).asignarObjetivosSecretos(jugadores);
        verify(estadoPaisService).inicializarEstadosPaises(99L);
        verify(estadoPaisService).repartirPaises(99L);
        verify(estadoTarjetaService).inicializarTarjetas(99L);
        verify(turnoService).establecerOrdenJugadores(99L);
    }

    @Test
    void listarPartidasDisponibles_deberiaFiltrarPorEstadoEnCurso() {
        List<PartidaEntity> partidasEntities = Collections.singletonList(partidaEntity);
        when(partidaRepository.findByEstadoPartida(EstadoPartida.EN_JUEGO)).thenReturn(partidasEntities);
        when(modelMapper.map(any(PartidaEntity.class), eq(Partida.class))).thenReturn(partida);

        List<Partida> resultado = partidaService.listarPartidasDisponibles();

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        verify(partidaRepository).findByEstadoPartida(EstadoPartida.EN_JUEGO);
    }

    @Test
    void unirseAPartida_deberiaCrearNuevoJugadorYActualizarPartida() {
        UsuarioEntity nuevoUsuario = new UsuarioEntity();
        nuevoUsuario.setIdUsuario(999L);
        nuevoUsuario.setUsuario("Nuevo Usuario");

        // El fixture compartido queda EN_JUEGO, estado en el que unirseAPartida
        // rechaza por diseño. Para ejercitar el alta hay que partir de otro estado.
        partidaEntity.setEstadoPartida(EstadoPartida.PAUSADA);
        salaEntity.setJugadores(new ArrayList<>());

        when(partidaRepository.findById(anyLong())).thenReturn(Optional.of(partidaEntity));
        when(usuarioRepository.findById(999L)).thenReturn(Optional.of(nuevoUsuario));
        when(jugadorRepository.save(any(JugadorEntity.class))).thenReturn(jugadorEntity);
        when(partidaRepository.save(any(PartidaEntity.class))).thenReturn(partidaEntity);
        when(modelMapper.map(any(PartidaEntity.class), eq(Partida.class))).thenReturn(partida);

        Partida resultado = partidaService.unirseAPartida(999L, 1L);

        assertNotNull(resultado);
        verify(jugadorRepository).save(jugadorEntityCaptor.capture());
        JugadorEntity jugadorCreado = jugadorEntityCaptor.getValue();
        assertEquals(nuevoUsuario, jugadorCreado.getUsuario());
        assertEquals(salaEntity, jugadorCreado.getSala());
        assertEquals("Nuevo Usuario", jugadorCreado.getNombre());
        assertFalse(jugadorCreado.isPerdio());
        verify(partidaRepository).save(partidaEntity);
    }

    @Test
    void unirseAPartida_usuarioYaEnPartida_deberiaLanzarExcepcion() {
        JugadorEntity jugadorExistente = new JugadorEntity();
        jugadorExistente.setUsuario(usuarioEntity);
        salaEntity.getJugadores().add(jugadorExistente);

        // Sin esto la partida sigue EN_JUEGO y se corta antes, con otro mensaje.
        partidaEntity.setEstadoPartida(EstadoPartida.PAUSADA);

        when(partidaRepository.findById(anyLong())).thenReturn(Optional.of(partidaEntity));
        when(usuarioRepository.findById(anyLong())).thenReturn(Optional.of(usuarioEntity));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            partidaService.unirseAPartida(1L, 1L);
        });

        assertEquals("El usuario ya está en la partida", exception.getMessage());
    }

    @Test
    void unirseAPartida_partidaEnCurso_deberiaLanzarExcepcion() {
        partidaEntity.setEstadoPartida(EstadoPartida.EN_JUEGO);

        when(partidaRepository.findById(anyLong())).thenReturn(Optional.of(partidaEntity));
        when(usuarioRepository.findById(anyLong())).thenReturn(Optional.of(usuarioEntity));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            partidaService.unirseAPartida(999L, 1L);
        });

        assertEquals("No se puede unir a una partida en curso.", exception.getMessage());
    }

    @Test
    void crearPartidaYAsignarJugadores_usuarioNoEsCreador_deberiaLanzarExcepcion() {
        when(salaRepository.findById(1L)).thenReturn(Optional.of(salaEntity));

        Usuario usuarioActual = new Usuario();
        usuarioActual.setIdUsuario(999L);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            partidaService.crearPartidaYAsignarJugadores(1L, usuarioActual);
        });

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
        assertTrue(exception.getReason().contains("Solo el creador de la sala puede iniciar la partida"));
    }

    @Test
    void crearPartidaYAsignarJugadores_salaNoEncontrada_deberiaLanzarExcepcion() {
        when(salaRepository.findById(1L)).thenReturn(Optional.empty());

        Usuario usuarioActual = new Usuario();
        usuarioActual.setIdUsuario(1L);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            partidaService.crearPartidaYAsignarJugadores(1L, usuarioActual);
        });

        assertEquals("Sala no encontrada", exception.getMessage());
    }

    @Test
    void crearPartidaYAsignarJugadores_menosDeDosJugadores_deberiaLanzarExcepcion() {
        when(salaRepository.findById(1L)).thenReturn(Optional.of(salaEntity));
        when(jugadorRepository.findBySala_IdSala(1L)).thenReturn(List.of(jugadorEntity));

        Usuario usuarioActual = new Usuario();
        usuarioActual.setIdUsuario(1L);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            partidaService.crearPartidaYAsignarJugadores(1L, usuarioActual);
        });

        assertEquals("Se necesitan mínimo 2 jugadores para iniciar la partida.", exception.getMessage());
    }

    @Test
    void cargarPartidaBySala_exitoso() {
        salaEntity.setEstado(EstadoSala.INICIADA);
        when(salaRepository.findByUrl("abc123")).thenReturn(Optional.of(salaEntity));
        when(partidaRepository.findByConfiguracion_IdSala(1L)).thenReturn(Optional.of(partidaEntity));

        PartidaDto mockDto = new PartidaDto();
        mockDto.setIdPartida(1L);

        PartidaServiceImpl spyService = Mockito.spy(partidaService);
        doReturn(mockDto).when(spyService).cargarPartida(1L);

        PartidaDto resultado = spyService.cargarPartidaBySala("abc123", 1L);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getIdPartida());
    }

    @Test
    void cargarPartidaBySala_salaNoEncontrada_deberiaLanzarExcepcion() {
        when(salaRepository.findByUrl("abc123")).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            partidaService.cargarPartidaBySala("abc123", 1L);
        });

        assertEquals("No se encontró la sala con URL: abc123", exception.getMessage());
    }

    @Test
    void cargarPartidaBySala_usuarioNoEsJugador_deberiaLanzarExcepcion() {
        when(salaRepository.findByUrl("abc123")).thenReturn(Optional.of(salaEntity));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            partidaService.cargarPartidaBySala("abc123", 999L);
        });

        assertEquals("El usuario no pertenece a esta partida.", exception.getMessage());
    }

    @Test
    void cargarPartidaBySala_partidaNoIniciada_deberiaLanzarExcepcion() {
        salaEntity.setEstado(EstadoSala.ESPERANDO);
        when(salaRepository.findByUrl("abc123")).thenReturn(Optional.of(salaEntity));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            partidaService.cargarPartidaBySala("abc123", 1L);
        });

        assertEquals("La partida no está iniciada", exception.getMessage());
    }

    @Test
    void cargarPartida_exitoso() {
        PaisEntity paisEntity = new PaisEntity();
        paisEntity.setIdPais(1L);
        paisEntity.setNombre("Argentina");

        ContinenteEntity continenteEntity = new ContinenteEntity();
        continenteEntity.setIdContinente(1L);
        continenteEntity.setNombre("América del Sur");
        paisEntity.setContinente(continenteEntity);

        EstadoPaisEntity estadoPaisEntity = new EstadoPaisEntity();
        estadoPaisEntity.setIdEstadoPais(1L);
        estadoPaisEntity.setPais(paisEntity);
        estadoPaisEntity.setJugador(jugadorEntity);
        estadoPaisEntity.setCantidadTropas(5);

        TurnoEntity turnoEntity = new TurnoEntity();
        turnoEntity.setIdTurno(1L);
        turnoEntity.setNroTurno(1);
        turnoEntity.setFase(FaseTurno.ATAQUE);
        turnoEntity.setJugador(jugadorEntity);

        TarjetaEntity tarjetaEntity = new TarjetaEntity();
        tarjetaEntity.setIdTarjeta(1L);
        tarjetaEntity.setSimbolo(Simbolo.GLOBO);
        tarjetaEntity.setPais(paisEntity);

        EstadoTarjetaEntity estadoTarjetaEntity = new EstadoTarjetaEntity();
        estadoTarjetaEntity.setIdEstadoTarjeta(1L);
        estadoTarjetaEntity.setTarjeta(tarjetaEntity);
        estadoTarjetaEntity.setJugador(jugadorEntity);

        partidaEntity.setEstadoPaises(List.of(estadoPaisEntity));
        partidaEntity.setTurnos(List.of(turnoEntity));
        partidaEntity.setMazo(List.of(estadoTarjetaEntity));

        when(partidaRepository.findById(1L)).thenReturn(Optional.of(partidaEntity));

        when(modelMapper.map(any(PaisEntity.class), eq(Pais.class))).thenAnswer(invocation -> {
            PaisEntity entity = invocation.getArgument(0);
            Pais pais = new Pais();
            pais.setIdPais(entity.getIdPais());
            pais.setNombre(entity.getNombre());
            return pais;
        });

        when(modelMapper.map(any(JugadorEntity.class), eq(JugadorDto.class))).thenAnswer(invocation -> {
            JugadorEntity entity = invocation.getArgument(0);
            JugadorDto dto = new JugadorDto();
            dto.setIdJugador(entity.getIdJugador());
            dto.setNombre(entity.getNombre());
            return dto;
        });

        PartidaDto resultado = partidaService.cargarPartida(1L);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getIdPartida());
        assertEquals(EstadoPartida.EN_JUEGO, resultado.getEstado());
        assertEquals(1, resultado.getTurnoActual());
        assertNotNull(resultado.getEstadoPaises());
        assertNotNull(resultado.getTurnos());
        assertNotNull(resultado.getEstadoTarjetas());
        assertNotNull(resultado.getJugadores());
    }

    @Test
    void cargarPartida_partidaNoEncontrada_deberiaLanzarExcepcionConStatusNotFound() {
        when(partidaRepository.findById(anyLong())).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            partidaService.cargarPartida(1L);
        });

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertNotNull(exception.getReason());
        assertTrue(exception.getReason().contains("Partida con ID 1 no encontrada"));
    }

    @Test
    void guardarPartida_deberiaMapearYGuardarPartida() {
        when(modelMapper.map(any(Partida.class), eq(PartidaEntity.class))).thenReturn(partidaEntity);
        when(partidaRepository.save(any(PartidaEntity.class))).thenReturn(partidaEntity);
        when(modelMapper.map(any(PartidaEntity.class), eq(Partida.class))).thenReturn(partida);

        Partida resultado = partidaService.guardarPartida(partida);

        assertNotNull(resultado);
        verify(modelMapper).map(partida, PartidaEntity.class);
        verify(partidaRepository).save(partidaEntity);
        verify(modelMapper).map(partidaEntity, Partida.class);
    }

    @Test
    void obtenerEstadoPartida_deberiaRetornarPartidaMapeada() {
        when(partidaRepository.findById(anyLong())).thenReturn(Optional.of(partidaEntity));
        when(modelMapper.map(any(PartidaEntity.class), eq(Partida.class))).thenReturn(partida);

        Partida resultado = partidaService.obtenerEstadoPartida(1L);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getIdPartida());
        verify(modelMapper).map(partidaEntity, Partida.class);
    }

    @Test
    void obtenerEstadoPartida_partidaNoEncontrada_deberiaLanzarExcepcionConStatusNotFound() {
        when(partidaRepository.findById(anyLong())).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            partidaService.obtenerEstadoPartida(1L);
        });

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertNotNull(exception.getReason());
        assertTrue(exception.getReason().contains("Estado de partida con ID 1 no encontrada"));
    }

    @Test
    void finalizarPartida_deberiaActualizarEstadoYGuardar() {
        when(partidaRepository.findById(anyLong())).thenReturn(Optional.of(partidaEntity));
        when(partidaRepository.save(any(PartidaEntity.class))).thenReturn(partidaEntity);

        boolean resultado = partidaService.finalizarPartida(1L);

        assertTrue(resultado);
        verify(partidaRepository).save(partidaEntityCaptor.capture());
        PartidaEntity partidaActualizada = partidaEntityCaptor.getValue();
        assertEquals(EstadoPartida.TERMINADA, partidaActualizada.getEstadoPartida());
    }

    @Test
    void finalizarPartida_partidaNoEncontrada_deberiaLanzarExcepcion() {
        when(partidaRepository.findById(1L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            partidaService.finalizarPartida(1L);
        });

        assertEquals("Partida no encontrada", exception.getMessage());
    }

    @Test
    void verificarCondicionVictoria_unSoloJugadorActivo_deberiaFinalizarPartida() {
        List<JugadorEntity> jugadoresActivos = List.of(jugadorEntity);

        when(modelMapper.map(any(Partida.class), eq(PartidaEntity.class))).thenReturn(partidaEntity);
        when(jugadorRepository.findByPartidaAndEstadoJugador(partidaEntity, EstadoJugador.ACTIVO))
                .thenReturn(jugadoresActivos);
        when(jugadorRepository.save(any(JugadorEntity.class))).thenReturn(jugadorEntity);
        when(partidaRepository.save(any(PartidaEntity.class))).thenReturn(partidaEntity);
        when(modelMapper.map(any(JugadorEntity.class), eq(Jugador.class))).thenReturn(new Jugador());
        when(modelMapper.map(any(PartidaEntity.class), eq(Partida.class))).thenReturn(partida);

        partidaService.verificarCondicionVictoria(partida);

        verify(jugadorRepository).save(jugadorEntityCaptor.capture());
        JugadorEntity ganador = jugadorEntityCaptor.getValue();
        assertEquals(EstadoJugador.GANADOR, ganador.getEstadoJugador());

        verify(partidaRepository).save(partidaEntityCaptor.capture());
        PartidaEntity partidaFinalizada = partidaEntityCaptor.getValue();
        assertEquals(EstadoPartida.TERMINADA, partidaFinalizada.getEstadoPartida());

        verify(estadisticaService).registrarEvento(
                eq("Victoria automática por eliminación de todos los oponentes"),
                any(Jugador.class),
                any(Partida.class)
        );
    }

    @Test
    void verificarCondicionVictoria_variosJugadoresActivos_noDeberiaFinalizarPartida() {
        List<JugadorEntity> jugadoresActivos = List.of(jugadorEntity, jugador2Entity);

        when(modelMapper.map(any(Partida.class), eq(PartidaEntity.class))).thenReturn(partidaEntity);
        when(jugadorRepository.findByPartidaAndEstadoJugador(partidaEntity, EstadoJugador.ACTIVO))
                .thenReturn(jugadoresActivos);

        partidaService.verificarCondicionVictoria(partida);

        verify(jugadorRepository, never()).save(any(JugadorEntity.class));
        verify(partidaRepository, never()).save(any(PartidaEntity.class));
        verify(estadisticaService, never()).registrarEvento(anyString(), any(), any());
    }

    @Test
    void obtenerPartidaActivaPorUrlYUsuario_exitoso() {
        salaEntity.setEstado(EstadoSala.INICIADA);
        when(salaRepository.findByUrl("abc123")).thenReturn(Optional.of(salaEntity));
        when(partidaRepository.findByConfiguracion_IdSala(1L)).thenReturn(Optional.of(partidaEntity));

        PartidaDto resultado = partidaService.obtenerPartidaActivaPorUrlYUsuario("abc123", 1L);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getIdPartida());
        assertEquals(EstadoPartida.EN_JUEGO, resultado.getEstado());
        assertEquals(1, resultado.getTurnoActual());
        assertNotNull(resultado.getConfiguracion());
        assertEquals("Sala Test", resultado.getConfiguracion().getNombreSala());
    }

    @Test
    void obtenerPartidaActivaPorUrlYUsuario_salaNoEncontrada_deberiaLanzarExcepcion() {
        when(salaRepository.findByUrl("abc123")).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            partidaService.obtenerPartidaActivaPorUrlYUsuario("abc123", 1L);
        });

        assertEquals("No se encontró la sala con URL: abc123", exception.getMessage());
    }

    @Test
    void obtenerPartidaActivaPorUrlYUsuario_partidaNoIniciada_deberiaLanzarExcepcion() {
        salaEntity.setEstado(EstadoSala.ESPERANDO);
        when(salaRepository.findByUrl("abc123")).thenReturn(Optional.of(salaEntity));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            partidaService.obtenerPartidaActivaPorUrlYUsuario("abc123", 1L);
        });

        assertEquals("La partida no está iniciada", exception.getMessage());
    }

    @Test
    void obtenerPartidaActivaPorUrlYUsuario_partidaNoEncontrada_deberiaLanzarExcepcion() {
        salaEntity.setEstado(EstadoSala.INICIADA);
        when(salaRepository.findByUrl("abc123")).thenReturn(Optional.of(salaEntity));
        when(partidaRepository.findByConfiguracion_IdSala(1L)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            partidaService.obtenerPartidaActivaPorUrlYUsuario("abc123", 1L);
        });

        assertEquals("No se encontró la partida activa para esta sala.", exception.getMessage());
    }

    @Test
    void obtenerPartidaActivaPorUrlYUsuario_usuarioNoEsJugador_deberiaLanzarExcepcion() {
        salaEntity.setEstado(EstadoSala.INICIADA);
        when(salaRepository.findByUrl("abc123")).thenReturn(Optional.of(salaEntity));
        when(partidaRepository.findByConfiguracion_IdSala(1L)).thenReturn(Optional.of(partidaEntity));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            partidaService.obtenerPartidaActivaPorUrlYUsuario("abc123", 999L);
        });

        assertEquals("El usuario no pertenece a esta partida.", exception.getMessage());
    }
}