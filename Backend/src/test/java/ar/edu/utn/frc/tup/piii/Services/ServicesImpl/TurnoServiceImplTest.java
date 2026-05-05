package ar.edu.utn.frc.tup.piii.Services.ServicesImpl;

import ar.edu.utn.frc.tup.piii.Dtos.*;
import ar.edu.utn.frc.tup.piii.Dtos.EstadoPaises.AgregarFichas;
import ar.edu.utn.frc.tup.piii.Dtos.EstadoPaises.Ataque;
import ar.edu.utn.frc.tup.piii.Dtos.EstadoPaises.AtaqueResponseDto;
import ar.edu.utn.frc.tup.piii.Dtos.EstadoPaises.MoverFichas;
import ar.edu.utn.frc.tup.piii.Entities.*;
import ar.edu.utn.frc.tup.piii.Repositories.*;
import ar.edu.utn.frc.tup.piii.Services.*;
import ar.edu.utn.frc.tup.piii.models.*;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TurnoServiceImplTest {

    @Spy
    @InjectMocks
    private TurnoServiceImpl turnosServiceImpl;

    @Mock
    private TurnoRepository turnoRepository;
    @Mock
    private TurnoService turnoService;
    @Mock
    private JugadorRepository jugadorRepository;
    @Mock
    private PartidaRepository partidaRepository;
    @Mock
    private EstadoTarjetaService estadoTarjetaService;
    @Mock
    private EstadoTarjetaRepository estadoTarjetaRepository;
    @Mock
    private ObjetivoService objetivoService;
    @Mock
    private EstadoPaisService estadoPaisService;
    @Mock
    private ModelMapper modelMapper;
    @Mock
    private EstadoPaisRepository estadoPaisRepository;
    @Mock
    private ContinenteRepository continenteRepository;
    @Mock
    private PaisRepository paisRepository;
    @Mock
    private JugadorService jugadorService;

    private TurnoEntity turnoEntity;
    private Turno turno;
    private JugadorDto jugadorDto;
    private JugadorEntity jugadorEntity;
    private PartidaEntity partidaEntity;
    private Jugador jugador;
    private List<TurnoEntity> turnos;
    private List<JugadorEntity> jugadores;

    @BeforeEach
    void setUp() {
        jugadorEntity = new JugadorEntity();
        jugadorEntity.setIdJugador(1L);
        jugadorEntity.setNombre("Jugador Test");

        turnoEntity = new TurnoEntity();
        turnoEntity.setIdTurno(1L);
        turnoEntity.setJugador(jugadorEntity);
        turnoEntity.setFase(FaseTurno.INCORPORACION);
        turnoEntity.setNroTurno(1);

        jugador = new Jugador();
        jugador.setIdJugador(1L);

        turno = new Turno();
        turno.setIdTurno(1L);
        turno.setFase(FaseTurno.INCORPORACION);
        turno.setNroTurno(1);
        turno.setJugador(jugador);

        jugadorDto = new JugadorDto();
        jugadorDto.setIdJugador(1L);
        jugadorDto.setNombre("Jugador Test");

        partidaEntity = new PartidaEntity();
        partidaEntity.setIdPartida(1L);
        partidaEntity.setTurnoActual(1);
        partidaEntity.setEstadoPartida(EstadoPartida.EN_JUEGO);
        partidaEntity.setHostilidad(false);

        turnos = new ArrayList<>();
        turnos.add(turnoEntity);
        partidaEntity.setTurnos(turnos);

        jugadores = new ArrayList<>();
        jugadores.add(jugadorEntity);
        partidaEntity.setJugadores(jugadores);

        turnoEntity.setPartida(partidaEntity);
    }

    @Test
    void ejecutarSegundaVuelta_deberiaLanzarExcepcion_siJugadorNoExiste() {
        Jugador jugador = new Jugador();
        jugador.setIdJugador(1L);

        Turno turno = new Turno();
        turno.setJugador(jugador);
        turno.setIdPartida(10L);

        when(jugadorRepository.findById(1L)).thenReturn(Optional.empty());

        List<Turno> turnos = List.of(turno);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            turnosServiceImpl.ejecutarSegundaVuelta(turnos);
        });

        assertTrue(ex.getMessage().contains("Jugador no encontrado"));
    }

    @Test
    void ejecutarSegundaVuelta_deberiaLanzarExcepcion_siPartidaNoExiste() {
        Jugador jugador = new Jugador();
        jugador.setIdJugador(1L);

        Turno turno = new Turno();
        turno.setJugador(jugador);
        turno.setIdPartida(10L);

        JugadorEntity jugadorEntity = new JugadorEntity();
        jugadorEntity.setIdJugador(1L);
        jugadorEntity.setEjercito(5);

        when(jugadorRepository.findById(1L)).thenReturn(Optional.of(jugadorEntity));
        when(partidaRepository.findById(10L)).thenReturn(Optional.empty());

        List<Turno> turnos = List.of(turno);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            turnosServiceImpl.ejecutarSegundaVuelta(turnos);
        });

        assertTrue(ex.getMessage().contains("Partida no encontrada"));
    }

    @Test
    void conquistoContinente_deberiaRetornarTrue_siJugadorConquistoTodoElContinente() {
        Long idContinente = 1L;

        PaisEntity pais1 = new PaisEntity();
        PaisEntity pais2 = new PaisEntity();

        List<PaisEntity> paisesPorContinente = List.of(pais1, pais2);
        when(paisRepository.findByContinente_IdContinente(idContinente)).thenReturn(paisesPorContinente);

        EstadoPaisEntity estado1 = new EstadoPaisEntity();
        estado1.setPais(pais1);

        EstadoPaisEntity estado2 = new EstadoPaisEntity();
        estado2.setPais(pais2);

        List<EstadoPaisEntity> estadosJugador = List.of(estado1, estado2);

        boolean resultado = turnosServiceImpl.conquistoContinente(idContinente, estadosJugador);

        assertTrue(resultado);
    }

    @Test
    void entregarTarjetaSiCorresponde_deberiaLanzarExcepcion_siJugadorNoConquisto() {
        Long idJugador = 1L;
        Long idPartida = 10L;

        JugadorEntity jugador = new JugadorEntity();
        jugador.setIdJugador(idJugador);
        jugador.setConsquisto(false);

        PartidaEntity partida = new PartidaEntity();
        partida.setIdPartida(idPartida);

        when(jugadorRepository.findById(idJugador)).thenReturn(Optional.of(jugador));
        when(partidaRepository.findById(idPartida)).thenReturn(Optional.of(partida));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> turnosServiceImpl.entregarTarjetaSiCorresponde(idJugador, idPartida));

        assertEquals("El jugador no conquisto en esta ronda.", ex.getMessage());
    }

    @Test
    void calcularCantidadFichasPorOcupacion_deberiaLanzarExcepcion_siContinenteNoExiste() {
        Long idJugador = 1L;

        JugadorEntity jugador = new JugadorEntity();
        jugador.setIdJugador(idJugador);
        jugador.setEjercito(0);

        when(jugadorRepository.findById(idJugador)).thenReturn(Optional.of(jugador));
        when(estadoPaisRepository.findByJugador_IdJugador(idJugador)).thenReturn(List.of());

        when(continenteRepository.findById(1L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> turnosServiceImpl.calcularCantidadFichasPorOcupacion(idJugador));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        assertEquals("Continente no encontrado.", ex.getReason());
    }

    @Test
    void calcularCantidadFichasPorOcupacion_deberiaSumarFichasCorrectamente() {
        Long idJugador = 1L;

        List<EstadoPaisEntity> estados = IntStream.range(0, 6)
                .mapToObj(i -> new EstadoPaisEntity())
                .toList();

        JugadorEntity jugador = new JugadorEntity();
        jugador.setIdJugador(idJugador);
        jugador.setEjercito(10);

        when(jugadorRepository.findById(idJugador)).thenReturn(Optional.of(jugador));
        when(estadoPaisRepository.findByJugador_IdJugador(idJugador)).thenReturn(estados);

        List<ContinenteEntity> continentes = List.of(
                new ContinenteEntity(1L, "Asia"),
                new ContinenteEntity(2L, "Europa"),
                new ContinenteEntity(3L, "America del Norte"),
                new ContinenteEntity(4L, "America del Sur"),
                new ContinenteEntity(5L, "Africa"));

        for (ContinenteEntity cont : continentes) {
            when(continenteRepository.findById(cont.getIdContinente()))
                    .thenReturn(Optional.of(cont));
            boolean conquistado = cont.getNombre().equals("Asia");
            doReturn(conquistado).when(turnosServiceImpl).conquistoContinente(cont.getIdContinente(), estados);
        }

        int totalEjercito = turnosServiceImpl.calcularCantidadFichasPorOcupacion(idJugador);

        assertEquals(20, totalEjercito);

        verify(jugadorRepository).save(jugador);
    }

    @Test
    void entregarTarjetaSiCorresponde_deberiaLanzarExcepcion_siPartidaNoExiste() {
        JugadorEntity jugador = new JugadorEntity();
        jugador.setIdJugador(1L);
        jugador.setConsquisto(true);

        when(jugadorRepository.findById(1L)).thenReturn(Optional.of(jugador));
        when(partidaRepository.findById(10L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> turnosServiceImpl.entregarTarjetaSiCorresponde(1L, 10L));
    }

    @Test
    void entregarTarjetaSiCorresponde_deberiaRetornarTarjeta_siJugadorConquisto() {
        Long idJugador = 1L;
        Long idPartida = 10L;

        JugadorEntity jugador = new JugadorEntity();
        jugador.setIdJugador(idJugador);
        jugador.setConsquisto(true);

        PartidaEntity partida = new PartidaEntity();
        partida.setIdPartida(idPartida);

        EstadoTarjetaEntity tarjeta1 = new EstadoTarjetaEntity();
        tarjeta1.setIdEstadoTarjeta(100L);
        tarjeta1.setCanjeada(false);

        List<EstadoTarjetaEntity> tarjetas = List.of(tarjeta1);

        TarjetaDto tarjetaDtoEsperada = new TarjetaDto();
        tarjetaDtoEsperada.setIdTarjeta(100L);

        when(jugadorRepository.findById(idJugador)).thenReturn(Optional.of(jugador));
        when(partidaRepository.findById(idPartida)).thenReturn(Optional.of(partida));
        when(estadoTarjetaRepository.findByPartida_IdPartidaAndJugadorIsNull(idPartida))
                .thenReturn(tarjetas);
        when(estadoTarjetaService.asignarTarjeta(any(EstadoTarjetaDto.class)))
                .thenReturn(tarjetaDtoEsperada);

        TarjetaDto resultado = turnosServiceImpl.entregarTarjetaSiCorresponde(idJugador, idPartida);

        assertNotNull(resultado);
        assertEquals(100L, resultado.getIdTarjeta());
    }

    @Test
    void moverFichas_deberiaMoverFichasCorrectamente_siTodoEsValido() {
        Long idJugador = 1L;
        Long idPartida = 10L;

        MoverFichas moverFichas = new MoverFichas();
        moverFichas.setIdJugador(idJugador);

        PartidaEntity partida = new PartidaEntity();
        partida.setIdPartida(idPartida);
        partida.setTurnoActual(3);

        JugadorEntity jugador = new JugadorEntity();
        jugador.setIdJugador(idJugador);
        jugador.setPartida(partida);

        TurnoEntity turno = new TurnoEntity();
        turno.setFase(FaseTurno.REAGRUPACION);
        turno.setJugador(jugador);
        turno.setNroTurno(3);

        when(jugadorRepository.findById(idJugador)).thenReturn(Optional.of(jugador));
        when(partidaRepository.findById(idPartida)).thenReturn(Optional.of(partida));
        when(turnoRepository.findByNroTurnoAndPartida_IdPartida(3, idPartida)).thenReturn(Optional.of(turno));
        when(estadoPaisService.agrupacionFichas(moverFichas)).thenReturn(true);

        Boolean resultado = turnosServiceImpl.moverFichas(moverFichas);

        assertTrue(resultado);
        verify(estadoPaisService).agrupacionFichas(moverFichas);
    }

    @Test
    void moverFichas_deberiaLanzarExcepcion_siJugadorNoExiste() {
        MoverFichas moverFichas = new MoverFichas();
        moverFichas.setIdJugador(99L);

        when(jugadorRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> turnosServiceImpl.moverFichas(moverFichas));
    }

    @Test
    void moverFichas_deberiaLanzarExcepcion_siTurnoNoCorrespondeAlJugador() {
        MoverFichas moverFichas = new MoverFichas();
        moverFichas.setIdJugador(1L);

        JugadorEntity jugador = new JugadorEntity();
        jugador.setIdJugador(1L);
        PartidaEntity partida = new PartidaEntity();
        partida.setIdPartida(10L);
        partida.setTurnoActual(3);
        jugador.setPartida(partida);

        JugadorEntity otroJugador = new JugadorEntity();
        otroJugador.setIdJugador(2L);

        TurnoEntity turno = new TurnoEntity();
        turno.setJugador(otroJugador);
        turno.setFase(FaseTurno.REAGRUPACION);

        when(jugadorRepository.findById(1L)).thenReturn(Optional.of(jugador));
        when(partidaRepository.findById(10L)).thenReturn(Optional.of(partida));
        when(turnoRepository.findByNroTurnoAndPartida_IdPartida(3, 10L)).thenReturn(Optional.of(turno));

        assertThrows(IllegalArgumentException.class, () -> turnosServiceImpl.moverFichas(moverFichas));
    }

    @Test
    void gestionarTimeoutTurno_deberiaPasarSiTurnoExpiradoYEnHostilidades() {
        Long idTurno = 1L;

        PartidaEntity partida = new PartidaEntity();
        partida.setIdPartida(10L);
        partida.setFaseActual(FaseJuego.HOSTILIDADES);

        TurnoEntity turno = new TurnoEntity();
        turno.setIdTurno(idTurno);
        turno.setInicio(LocalDateTime.now().minusMinutes(6));
        turno.setPartida(partida);

        when(turnoRepository.findById(idTurno)).thenReturn(Optional.of(turno));
        when(partidaRepository.findById(partida.getIdPartida())).thenReturn(Optional.of(partida));

        turnosServiceImpl.gestionarTimeoutTurno(idTurno);

        verify(turnoRepository).findById(idTurno);
        verify(partidaRepository).findById(partida.getIdPartida());
    }

    @Test
    void gestionarTimeoutTurno_deberiaLanzarExcepcion_siTurnoNoExiste() {
        Long idTurno = 99L;
        when(turnoRepository.findById(idTurno)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> {
            turnosServiceImpl.gestionarTimeoutTurno(idTurno);
        });

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        assertEquals("Turno no encontrado.", ex.getReason());
    }

    @Test
    void gestionarTimeoutTurno_deberiaLanzarExcepcion_siPartidaNoExiste() {
        Long idTurno = 1L;
        TurnoEntity turno = new TurnoEntity();
        turno.setIdTurno(idTurno);
        turno.setInicio(LocalDateTime.now().minusMinutes(10));
        turno.setPartida(new PartidaEntity());
        turno.getPartida().setIdPartida(123L);

        when(turnoRepository.findById(idTurno)).thenReturn(Optional.of(turno));
        when(partidaRepository.findById(123L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> {
            turnosServiceImpl.gestionarTimeoutTurno(idTurno);
        });

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        assertEquals("Partida no encontrado.", ex.getReason());
    }

    @Test
    void gestionarTimeoutTurno_noHaceNada_siNoPasaron5Minutos() {
        Long idTurno = 1L;

        PartidaEntity partida = new PartidaEntity();
        partida.setIdPartida(10L);
        partida.setFaseActual(FaseJuego.HOSTILIDADES);

        TurnoEntity turno = new TurnoEntity();
        turno.setIdTurno(idTurno);
        turno.setInicio(LocalDateTime.now().minusMinutes(2));
        turno.setPartida(partida);

        when(turnoRepository.findById(idTurno)).thenReturn(Optional.of(turno));
        when(partidaRepository.findById(partida.getIdPartida())).thenReturn(Optional.of(partida));

        turnosServiceImpl.gestionarTimeoutTurno(idTurno);

        verify(turnoRepository).findById(idTurno);
        verify(partidaRepository).findById(partida.getIdPartida());
    }

    @Test
    void verificarOrdenAcciones_deberiaRetornarTrue_siAccionCoincideConFase_DEFENDER() {
        Jugador jugador = new Jugador();
        JugadorEntity jugadorEntity = new JugadorEntity();
        TurnoEntity turno = new TurnoEntity();
        turno.setFase(FaseTurno.INCORPORACION);

        when(modelMapper.map(jugador, JugadorEntity.class)).thenReturn(jugadorEntity);
        when(turnoRepository.findByJugador(jugadorEntity)).thenReturn(turno);

        boolean resultado = turnosServiceImpl.verificarOrdenAcciones(jugador, "Defender");

        assertTrue(resultado);
    }

    @Test
    void verificarOrdenAcciones_deberiaRetornarTrue_siAccionCoincideConFase_ATACAR() {
        Jugador jugador = new Jugador();
        JugadorEntity jugadorEntity = new JugadorEntity();
        TurnoEntity turno = new TurnoEntity();
        turno.setFase(FaseTurno.ATAQUE);

        when(modelMapper.map(jugador, JugadorEntity.class)).thenReturn(jugadorEntity);
        when(turnoRepository.findByJugador(jugadorEntity)).thenReturn(turno);

        boolean resultado = turnosServiceImpl.verificarOrdenAcciones(jugador, "Atacar");

        assertTrue(resultado);
    }

    @Test
    void verificarOrdenAcciones_deberiaRetornarTrue_siAccionCoincideConFase_MOVER_TROPAS() {
        Jugador jugador = new Jugador();
        JugadorEntity jugadorEntity = new JugadorEntity();
        TurnoEntity turno = new TurnoEntity();
        turno.setFase(FaseTurno.REAGRUPACION);

        when(modelMapper.map(jugador, JugadorEntity.class)).thenReturn(jugadorEntity);
        when(turnoRepository.findByJugador(jugadorEntity)).thenReturn(turno);

        boolean resultado = turnosServiceImpl.verificarOrdenAcciones(jugador, "Mover Tropas");

        assertTrue(resultado);
    }

    @Test
    void verificarOrdenAcciones_deberiaRetornarFalse_siAccionNoCoincideConFase() {
        Jugador jugador = new Jugador();
        JugadorEntity jugadorEntity = new JugadorEntity();
        TurnoEntity turno = new TurnoEntity();
        turno.setFase(FaseTurno.ATAQUE);

        when(modelMapper.map(jugador, JugadorEntity.class)).thenReturn(jugadorEntity);
        when(turnoRepository.findByJugador(jugadorEntity)).thenReturn(turno);

        boolean resultado = turnosServiceImpl.verificarOrdenAcciones(jugador, "Mover Tropas");

        assertFalse(resultado);
    }

    @Test
    void validarMovimiento_deberiaRetornarTrue_siCondicionesSonValidas() {
        Pais origen = new Pais();
        origen.setIdPais(1L);

        Pais destino = new Pais();
        destino.setIdPais(2L);

        Jugador jugador = new Jugador();
        jugador.setIdJugador(100L);

        EstadoPaisEntity origenEntity = new EstadoPaisEntity();
        EstadoPaisEntity destinoEntity = new EstadoPaisEntity();

        EstadoPais estadoOrigen = new EstadoPais();
        estadoOrigen.setCantidadTropas(3);

        EstadoPais estadoDestino = new EstadoPais();
        estadoDestino.setCantidadTropas(1);

        PaisEntity pais1Entity = new PaisEntity();
        pais1Entity.setIdPais(1L);

        PaisEntity pais2Entity = new PaisEntity();
        pais2Entity.setIdPais(2L);

        when(estadoPaisRepository.findByPaisIdPaisAndJugadorIdJugador(1L, 100L))
                .thenReturn(Optional.of(origenEntity));

        when(estadoPaisRepository.findByPaisIdPaisAndJugadorIdJugador(2L, 100L))
                .thenReturn(Optional.of(destinoEntity));

        when(modelMapper.map(Optional.of(origenEntity), EstadoPais.class)).thenReturn(estadoOrigen);
        when(modelMapper.map(Optional.of(destinoEntity), EstadoPais.class)).thenReturn(estadoDestino);
        when(modelMapper.map(origen, PaisEntity.class)).thenReturn(pais1Entity);
        when(modelMapper.map(destino, PaisEntity.class)).thenReturn(pais2Entity);

        when(estadoPaisService.sonLimitrofes(1L, 2L)).thenReturn(false);

        boolean resultado = turnosServiceImpl.validarMovimiento(origen, destino, jugador);

        assertTrue(resultado);
    }

    @Test
    void validarAtaque_estadoOrigenYDestinoPresentes_deberiaRetornarFalse() {
        Pais origen = new Pais();
        origen.setIdPais(1L);

        Pais destino = new Pais();
        destino.setIdPais(2L);

        Jugador jugador = new Jugador();
        jugador.setIdJugador(1L);

        EstadoPaisEntity estadoOrigenEntity = new EstadoPaisEntity();
        estadoOrigenEntity.setIdEstadoPais(10L);
        JugadorEntity jugadorEntity = new JugadorEntity();
        jugadorEntity.setIdJugador(1L);
        estadoOrigenEntity.setJugador(jugadorEntity);

        EstadoPaisEntity estadoDestinoEntity = new EstadoPaisEntity();
        estadoDestinoEntity.setIdEstadoPais(20L);
        estadoDestinoEntity.setJugador(new JugadorEntity());
        estadoDestinoEntity.getJugador().setIdJugador(2L);

        when(estadoPaisRepository.findByPaisIdPaisAndJugadorIdJugador(1L, 1L))
                .thenReturn(Optional.of(estadoOrigenEntity));
        when(estadoPaisRepository.findById(2L))
                .thenReturn(Optional.of(estadoDestinoEntity));

        when(modelMapper.map(Optional.of(estadoOrigenEntity), EstadoPais.class))
                .thenReturn(mapToEstadoPais(estadoOrigenEntity));
        when(modelMapper.map(Optional.of(estadoDestinoEntity), EstadoPais.class))
                .thenReturn(mapToEstadoPais(estadoDestinoEntity));

        when(modelMapper.map(origen, PaisEntity.class)).thenReturn(mapToPaisEntity(origen));
        when(modelMapper.map(destino, PaisEntity.class)).thenReturn(mapToPaisEntity(destino));

        boolean resultado = turnosServiceImpl.validarAtaque(origen, destino, jugador);

        assertFalse(resultado);

        verify(estadoPaisRepository).findByPaisIdPaisAndJugadorIdJugador(1L, 1L);
        verify(estadoPaisRepository).findById(2L);
        verify(modelMapper).map(Optional.of(estadoOrigenEntity), EstadoPais.class);
        verify(modelMapper).map(Optional.of(estadoDestinoEntity), EstadoPais.class);
        verify(modelMapper).map(origen, PaisEntity.class);
        verify(modelMapper).map(destino, PaisEntity.class);
    }

    @Test
    void obtenerTurnoExistente() {
        Long idTurno = 1L;
        when(turnoRepository.findById(idTurno)).thenReturn(Optional.of(turnoEntity));

        Turno resultado = turnosServiceImpl.obtenerTurno(idTurno);

        assertNotNull(resultado);
        assertEquals(idTurno, resultado.getIdTurno());
        assertEquals(FaseTurno.INCORPORACION, resultado.getFase());
        assertEquals(1, resultado.getNroTurno());
        assertEquals(1L, resultado.getJugador().getIdJugador());
        verify(turnoRepository).findById(idTurno);
    }

    @Test
    void testVerificarGanador_CuandoNoCumpleObjetivo() {
        Long idJugador = 10L;
        JugadorEntity jugador = new JugadorEntity();
        jugador.setIdJugador(idJugador);
        jugador.setPartida(new PartidaEntity());
        jugador.getPartida().setIdPartida(20L);

        when(jugadorRepository.findById(idJugador)).thenReturn(Optional.of(jugador));
        when(partidaRepository.findById(20L)).thenReturn(Optional.of(jugador.getPartida()));

        VerificacionObjetivoDto dto = new VerificacionObjetivoDto();
        dto.setGano(false);
        when(objetivoService.verificarObjetivos(idJugador)).thenReturn(dto);

        VerificacionObjetivoDto result = turnosServiceImpl.verificarGanador(idJugador);

        assertFalse(result.isGano());
        verify(partidaRepository, never()).save(any());
    }

    @Test
    void testObtenerTurno_CuandoNoExiste() {
        when(turnoRepository.findById(99L)).thenReturn(Optional.empty());

        Exception ex = assertThrows(RuntimeException.class, () -> turnosServiceImpl.obtenerTurno(99L));
        assertEquals("El turno con ID: 99 no existe.", ex.getMessage());
    }

    @Test
    void testObtenerTurno_existente() {
        TurnoEntity e = new TurnoEntity();
        e.setIdTurno(1L);
        e.setFase(FaseTurno.INCORPORACION);
        JugadorEntity je = new JugadorEntity();
        je.setIdJugador(2L);
        e.setJugador(je);

        when(turnoRepository.findById(1L)).thenReturn(Optional.of(e));

        Turno r = turnosServiceImpl.obtenerTurno(1L);
        assertEquals(1L, r.getIdTurno());
        assertEquals(2L, r.getJugador().getIdJugador());
        verify(turnoRepository).findById(1L);
    }

    @Test
    void testObtenerTurno_noExiste() {
        when(turnoRepository.findById(99L)).thenReturn(Optional.empty());
        Exception ex = assertThrows(RuntimeException.class, () -> turnosServiceImpl.obtenerTurno(99L));
        assertTrue(ex.getMessage().contains("99"));
    }

    @Test
    void testVerificarGanador_ganoYnoGano() {
        Long jId = 7L;
        JugadorEntity j = new JugadorEntity();
        j.setIdJugador(jId);
        PartidaEntity p = new PartidaEntity();
        p.setIdPartida(14L);
        j.setPartida(p);

        when(jugadorRepository.findById(jId)).thenReturn(Optional.of(j));
        when(partidaRepository.findById(14L)).thenReturn(Optional.of(p));

        VerificacionObjetivoDto dto0 = new VerificacionObjetivoDto();
        dto0.setGano(false);
        when(objetivoService.verificarObjetivos(jId)).thenReturn(dto0);
        assertFalse(turnosServiceImpl.verificarGanador(jId).isGano());
        verify(partidaRepository, never()).save(any());

        VerificacionObjetivoDto dto1 = new VerificacionObjetivoDto();
        dto1.setGano(true);
        when(objetivoService.verificarObjetivos(jId)).thenReturn(dto1);
        assertTrue(turnosServiceImpl.verificarGanador(jId).isGano());
        verify(partidaRepository).save(p);
    }

    @Test
    void testAtaqueJugadorNoEncontrado() {
        Ataque ataque = new Ataque();
        ataque.setIdJugador(1L);
        when(jugadorRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> turnosServiceImpl.ataque(ataque));
    }

    @Test
    void testCalcularCantidadFichasPorOcupacion_JugadorNoEncontrado() {
        when(jugadorRepository.findById(anyLong())).thenReturn(Optional.empty());
        assertThrows(ResponseStatusException.class,
                () -> turnosServiceImpl.calcularCantidadFichasPorOcupacion(1L));
    }

    @Test
    void testValidarUsarTarjetaEnPais_Correcto() {
        UsarTarjetaEnPaisDto dto = new UsarTarjetaEnPaisDto();
        dto.setIdJugador(1L);
        dto.setIdTarjeta(100L);

        JugadorEntity jugador = new JugadorEntity();
        when(jugadorRepository.findById(1L)).thenReturn(Optional.of(jugador));

        EstadoTarjetaEntity estadoTarjeta = new EstadoTarjetaEntity();
        when(estadoTarjetaRepository.findById(100L)).thenReturn(Optional.of(estadoTarjeta));

        doNothing().when(estadoTarjetaService).usarTarjetaEnPais(100L, 1L);

        turnosServiceImpl.validarUsarTarjetaEnPais(dto);

        verify(estadoTarjetaService).usarTarjetaEnPais(100L, 1L);
    }

    @Test
    void testValidarUsarTarjetaEnPais_JugadorNoEncontrado() {
        when(jugadorRepository.findById(anyLong())).thenReturn(Optional.empty());

        UsarTarjetaEnPaisDto dto = new UsarTarjetaEnPaisDto();
        dto.setIdJugador(1L);
        dto.setIdTarjeta(100L);

        assertThrows(ResponseStatusException.class, () -> turnosServiceImpl.validarUsarTarjetaEnPais(dto));
    }

    @Test
    void testValidarUsarTarjetaEnPais_TarjetaNoEncontrada() {
        JugadorEntity jugador = new JugadorEntity();
        when(jugadorRepository.findById(1L)).thenReturn(Optional.of(jugador));
        when(estadoTarjetaRepository.findById(anyLong())).thenReturn(Optional.empty());

        UsarTarjetaEnPaisDto dto = new UsarTarjetaEnPaisDto();
        dto.setIdJugador(1L);
        dto.setIdTarjeta(100L);

        assertThrows(ResponseStatusException.class, () -> turnosServiceImpl.validarUsarTarjetaEnPais(dto));
    }

    @Test
    void testValidarCanjeTarjetas_CanjeExitoso() {
        CanjeTarjetasDto dto = new CanjeTarjetasDto();
        dto.setIdJugador(1L);
        dto.setIdTarjetas(List.of(10L, 20L));

        JugadorEntity jugador = new JugadorEntity();
        when(jugadorRepository.findById(1L)).thenReturn(Optional.of(jugador));

        EstadoTarjetaEntity et1 = new EstadoTarjetaEntity();
        EstadoTarjetaEntity et2 = new EstadoTarjetaEntity();
        when(estadoTarjetaRepository.findAllById(dto.getIdTarjetas())).thenReturn(List.of(et1, et2));

        when(estadoTarjetaService.canjearTarjetas(dto.getIdTarjetas(), 1L)).thenReturn(5);

        Integer result = turnosServiceImpl.validarCanjeTarjetas(dto);

        assertEquals(5, result);
    }

    @Test
    void testValidarCanjeTarjetas_EjercitoMenorQueCuatro() {
        CanjeTarjetasDto dto = new CanjeTarjetasDto();
        dto.setIdJugador(1L);
        dto.setIdTarjetas(List.of(10L, 20L));

        JugadorEntity jugador = new JugadorEntity();
        when(jugadorRepository.findById(1L)).thenReturn(Optional.of(jugador));
        when(estadoTarjetaRepository.findAllById(dto.getIdTarjetas()))
                .thenReturn(List.of(new EstadoTarjetaEntity(), new EstadoTarjetaEntity()));

        when(estadoTarjetaService.canjearTarjetas(dto.getIdTarjetas(), 1L)).thenReturn(3);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> turnosServiceImpl.validarCanjeTarjetas(dto));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    void testCambiarFaseTurno_DefenderAAtacar() {
        turnoEntity.setFase(FaseTurno.INCORPORACION);
        when(partidaRepository.findById(1L)).thenReturn(Optional.of(partidaEntity));
        when(jugadorRepository.findById(1L)).thenReturn(Optional.of(jugadorEntity));
        when(turnoRepository.save(any(TurnoEntity.class))).thenReturn(turnoEntity);
        when(partidaRepository.save(any(PartidaEntity.class))).thenReturn(partidaEntity);

        boolean resultado = turnosServiceImpl.cambiarFaseTurno(1L);

        assertFalse(resultado);
        assertEquals(FaseTurno.ATAQUE, turnoEntity.getFase());
        verify(turnoRepository).save(turnoEntity);
        verify(partidaRepository).save(partidaEntity);
    }

    @Test
    void testCambiarFaseTurno_AtacarAMoverTropas() {
        turnoEntity.setFase(FaseTurno.ATAQUE);
        when(partidaRepository.findById(1L)).thenReturn(Optional.of(partidaEntity));
        when(jugadorRepository.findById(1L)).thenReturn(Optional.of(jugadorEntity));
        when(turnoRepository.save(any(TurnoEntity.class))).thenReturn(turnoEntity);
        when(partidaRepository.save(any(PartidaEntity.class))).thenReturn(partidaEntity);

        boolean resultado = turnosServiceImpl.cambiarFaseTurno(1L);

        assertFalse(resultado);
        assertEquals(FaseTurno.REAGRUPACION, turnoEntity.getFase());
        verify(turnoRepository).save(turnoEntity);
        verify(partidaRepository).save(partidaEntity);
    }

    @Test
    void testCambiarFaseTurno_PartidaNoEncontrada() {
        when(partidaRepository.findById(1L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> turnosServiceImpl.cambiarFaseTurno(1L));
        assertEquals("Partida no encontrada", exception.getMessage());
    }

    @Test
    void testCambiarFaseTurno_TurnoNoEncontrado() {
        partidaEntity.setTurnoActual(999);
        when(partidaRepository.findById(1L)).thenReturn(Optional.of(partidaEntity));

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> turnosServiceImpl.cambiarFaseTurno(1L));
        assertEquals("No se encontró el turno actual", exception.getMessage());
    }

    @Test
    void testCambiarFaseTurno_JugadorNoEncontrado() {
        when(partidaRepository.findById(1L)).thenReturn(Optional.of(partidaEntity));
        when(jugadorRepository.findById(1L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> turnosServiceImpl.cambiarFaseTurno(1L));
        assertEquals("Jugador no encontrado", exception.getMessage());
    }

    @Test
    void testCrearTurno_PartidaNoEncontrada() {
        when(partidaRepository.findById(1L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> turnosServiceImpl.crearTurno(jugadorEntity, 1, 1L));
        assertEquals("Partida no encontrada", exception.getMessage());
    }

    @Test
    void testEstablecerOrdenJugadores_Exitoso() {
        JugadorEntity jugador2 = new JugadorEntity();
        jugador2.setIdJugador(2L);
        jugador2.setNombre("Jugador 2");
        jugador2.setTipoJugador(TipoJugador.HUMANO);

        List<JugadorEntity> jugadoresLista = Arrays.asList(jugadorEntity, jugador2);
        partidaEntity.setJugadores(jugadoresLista);
        partidaEntity.setTurnos(new ArrayList<>());

        when(partidaRepository.findById(1L)).thenReturn(Optional.of(partidaEntity));
        when(turnoRepository.save(any(TurnoEntity.class))).thenReturn(turnoEntity);
        when(turnoRepository.saveAll(anyList())).thenReturn(Arrays.asList(turnoEntity));
        when(partidaRepository.save(any(PartidaEntity.class))).thenReturn(partidaEntity);
        when(jugadorRepository.save(any(JugadorEntity.class))).thenReturn(jugadorEntity);

        turnosServiceImpl.establecerOrdenJugadores(1L);

        verify(turnoRepository).saveAll(anyList());
        verify(partidaRepository).save(partidaEntity);
        verify(jugadorRepository, times(2)).save(any(JugadorEntity.class));
        assertEquals(1, partidaEntity.getTurnoActual());
    }

    @Test
    void testEstablecerOrdenJugadores_PartidaNoEncontrada() {
        when(partidaRepository.findById(1L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> turnosServiceImpl.establecerOrdenJugadores(1L));
        assertEquals("Partida no encontrada", exception.getMessage());
    }

    @Test
    void testPasarTurno_Exitoso() {
        JugadorEntity jugador2 = new JugadorEntity();
        jugador2.setIdJugador(2L);
        jugador2.setTipoJugador(TipoJugador.HUMANO);

        TurnoEntity turno2 = new TurnoEntity();
        turno2.setJugador(jugador2);
        turno2.setNroTurno(2);

        List<TurnoEntity> turnosLista = Arrays.asList(turnoEntity, turno2);
        partidaEntity.setTurnos(turnosLista);

        when(partidaRepository.findById(1L)).thenReturn(Optional.of(partidaEntity));
        when(turnoRepository.findByNroTurnoAndPartida_IdPartida(2, 1L))
                .thenReturn(Optional.of(turno2));
        when(partidaRepository.save(any(PartidaEntity.class))).thenReturn(partidaEntity);

        Long resultado = turnosServiceImpl.pasarTurno(1L);

        assertEquals(0L, resultado);
        assertEquals(2, partidaEntity.getTurnoActual());
        verify(partidaRepository).save(partidaEntity);
    }

    @Test
    void testPasarTurno_ConBot() {
        JugadorEntity botJugador = new JugadorEntity();
        botJugador.setIdJugador(2L);
        botJugador.setTipoJugador(TipoJugador.BOT);

        TurnoEntity turno2 = new TurnoEntity();
        turno2.setJugador(botJugador);
        turno2.setNroTurno(2);

        List<TurnoEntity> turnosLista = Arrays.asList(turnoEntity, turno2);
        partidaEntity.setTurnos(turnosLista);

        when(partidaRepository.findById(1L)).thenReturn(Optional.of(partidaEntity));
        when(turnoRepository.findByNroTurnoAndPartida_IdPartida(2, 1L))
                .thenReturn(Optional.of(turno2));
        when(partidaRepository.save(any(PartidaEntity.class))).thenReturn(partidaEntity);

        Long resultado = turnosServiceImpl.pasarTurno(1L);

        assertEquals(2L, resultado);
        assertEquals(2, partidaEntity.getTurnoActual());
    }

    @Test
    void testPasarTurno_PartidaNoEnJuego() {
        partidaEntity.setEstadoPartida(EstadoPartida.TERMINADA);
        when(partidaRepository.findById(1L)).thenReturn(Optional.of(partidaEntity));

        Long resultado = turnosServiceImpl.pasarTurno(1L);

        assertEquals(0L, resultado);
    }

    @Test
    void testPasarTurno_PartidaNoEncontrada() {
        when(partidaRepository.findById(1L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> turnosServiceImpl.pasarTurno(1L));
        assertEquals("Partida no encontrada", exception.getMessage());
    }

    @Test
    void testPasarTurno_SinTurnos() {
        partidaEntity.setTurnos(new ArrayList<>());
        when(partidaRepository.findById(1L)).thenReturn(Optional.of(partidaEntity));

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> turnosServiceImpl.pasarTurno(1L));
        assertEquals("No hay turnos en la partida", exception.getMessage());
    }

    @Test
    void testPasarTurno_TurnoActualNoEncontrado() {
        partidaEntity.setTurnoActual(999);
        when(partidaRepository.findById(1L)).thenReturn(Optional.of(partidaEntity));

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> turnosServiceImpl.pasarTurno(1L));
        assertEquals("Turno actual no encontrado", exception.getMessage());
    }

    @Test
    void testValidarCanjeTarjetas_JugadorNoEncontrado() {
        CanjeTarjetasDto dto = new CanjeTarjetasDto();
        dto.setIdJugador(999L);
        dto.setIdTarjetas(Arrays.asList(1L, 2L, 3L));

        when(jugadorRepository.findById(999L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> turnosServiceImpl.validarCanjeTarjetas(dto));
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    @Test
    void testCrearTurno_ConDatosValidos() {
        JugadorEntity jugador = new JugadorEntity();
        jugador.setIdJugador(1L);

        when(partidaRepository.findById(1L)).thenReturn(Optional.of(partidaEntity));
        when(turnoRepository.save(any(TurnoEntity.class))).thenReturn(turnoEntity);

        TurnoEntity resultado = turnosServiceImpl.crearTurno(jugador, 1, 1L);

        assertNotNull(resultado);
        verify(partidaRepository).findById(1L);
        verify(turnoRepository).save(any(TurnoEntity.class));
    }

    @Test
    void testVerificarGanador_JugadorNoEncontrado() {
        when(jugadorRepository.findById(999L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> turnosServiceImpl.verificarGanador(999L));
        assertTrue(exception.getMessage().contains("Jugador no encontrado"));
    }

    @Test
    void cambiarFaseTurno_deDefenderAAtaque_deberiaActualizarFase() {
        partidaEntity.setHostilidad(false);
        turnoEntity.setFase(FaseTurno.INCORPORACION);

        when(partidaRepository.findById(1L)).thenReturn(Optional.of(partidaEntity));
        when(jugadorRepository.findById(1L)).thenReturn(Optional.of(jugadorEntity));

        boolean result = turnosServiceImpl.cambiarFaseTurno(1L);

        assertFalse(result);
        assertEquals(FaseTurno.ATAQUE, turnoEntity.getFase());
    }

    @Test
    void cambiarFaseTurno_deAtacarAMoverTropas_deberiaActualizarFase() {
        turnoEntity.setFase(FaseTurno.ATAQUE);

        when(partidaRepository.findById(1L)).thenReturn(Optional.of(partidaEntity));
        when(jugadorRepository.findById(1L)).thenReturn(Optional.of(jugadorEntity));

        boolean result = turnosServiceImpl.cambiarFaseTurno(1L);

        assertFalse(result);
        assertEquals(FaseTurno.REAGRUPACION, turnoEntity.getFase());
    }

    @Test
    void validarAtaque_condicionesCorrectas_deberiaRetornarTrue() {
        Pais origen = new Pais();
        origen.setIdPais(1L);

        Pais destino = new Pais();
        destino.setIdPais(2L);

        Jugador jugador = new Jugador();
        jugador.setIdJugador(99L);

        EstadoPaisEntity estadoOrigenEntity = new EstadoPaisEntity();
        EstadoPaisEntity estadoDestinoEntity = new EstadoPaisEntity();

        when(estadoPaisRepository.findByPaisIdPaisAndJugadorIdJugador(1L, 99L))
                .thenReturn(Optional.empty());
        when(estadoPaisRepository.findById(2L))
                .thenReturn(Optional.empty());

        EstadoPais eOrigen = new EstadoPais();
        EstadoPais eDestino = new EstadoPais();

        Jugador jugadorOrigen = new Jugador();
        jugadorOrigen.setIdJugador(1L);
        Jugador jugadorDestino = new Jugador();
        jugadorDestino.setIdJugador(2L);
        eOrigen.setJugador(jugadorOrigen);
        eOrigen.setCantidadTropas(5);
        eDestino.setJugador(jugadorDestino);
        eDestino.setCantidadTropas(3);

        when(modelMapper.map(Optional.empty(), EstadoPais.class)).thenReturn(eOrigen).thenReturn(eDestino);

        PaisEntity pais1 = new PaisEntity();
        pais1.setIdPais(1L);
        PaisEntity pais2 = new PaisEntity();
        pais2.setIdPais(2L);

        when(modelMapper.map(origen, PaisEntity.class)).thenReturn(pais1);
        when(modelMapper.map(destino, PaisEntity.class)).thenReturn(pais2);

        when(estadoPaisService.sonLimitrofes(1L, 2L)).thenReturn(true);

        boolean resultado = turnosServiceImpl.validarAtaque(origen, destino, jugador);

        assertTrue(resultado);
    }

    @Test
    void validarAtaque_jugadoresIguales_deberiaRetornarFalse() {
        Pais origen = new Pais();
        origen.setIdPais(1L);

        Pais destino = new Pais();
        destino.setIdPais(2L);

        Jugador jugador = new Jugador();
        jugador.setIdJugador(99L);

        when(estadoPaisRepository.findByPaisIdPaisAndJugadorIdJugador(1L, 99L))
                .thenReturn(Optional.empty());
        when(estadoPaisRepository.findById(2L))
                .thenReturn(Optional.empty());

        Jugador jugadorComun = new Jugador();
        jugadorComun.setIdJugador(1L);

        EstadoPais eOrigen = new EstadoPais();
        eOrigen.setJugador(jugadorComun);
        eOrigen.setCantidadTropas(5);

        EstadoPais eDestino = new EstadoPais();
        eDestino.setJugador(jugadorComun);
        eDestino.setCantidadTropas(3);

        when(modelMapper.map(Optional.empty(), EstadoPais.class)).thenReturn(eOrigen).thenReturn(eDestino);

        PaisEntity pais1 = new PaisEntity();
        pais1.setIdPais(1L);
        PaisEntity pais2 = new PaisEntity();
        pais2.setIdPais(2L);

        when(modelMapper.map(origen, PaisEntity.class)).thenReturn(pais1);
        when(modelMapper.map(destino, PaisEntity.class)).thenReturn(pais2);

        boolean resultado = turnosServiceImpl.validarAtaque(origen, destino, jugador);

        assertFalse(resultado);
    }

    @Test
    void validarAtaque_estadosPresentes_deberiaRetornarFalse() {
        Pais origen = new Pais();
        origen.setIdPais(1L);

        Pais destino = new Pais();
        destino.setIdPais(2L);

        Jugador jugador = new Jugador();
        jugador.setIdJugador(99L);

        EstadoPaisEntity estadoOrigenEntity = new EstadoPaisEntity();
        EstadoPaisEntity estadoDestinoEntity = new EstadoPaisEntity();

        when(estadoPaisRepository.findByPaisIdPaisAndJugadorIdJugador(1L, 99L))
                .thenReturn(Optional.of(estadoOrigenEntity));

        when(estadoPaisRepository.findById(2L))
                .thenReturn(Optional.of(estadoDestinoEntity));

        boolean resultado = turnosServiceImpl.validarAtaque(origen, destino, jugador);

        assertFalse(resultado);
    }

    @Test
    void validarAtaque_estadoOrigenInexistente_deberiaRetornarFalse() {
        Pais origen = new Pais();
        origen.setIdPais(1L);

        Pais destino = new Pais();
        destino.setIdPais(2L);

        Jugador jugador = new Jugador();
        jugador.setIdJugador(99L);

        boolean resultado = turnoService.validarAtaque(origen, destino, jugador);

        assertFalse(resultado);
    }

    private EstadoPais mapToEstadoPais(EstadoPaisEntity entity) {
        EstadoPais estado = new EstadoPais();
        estado.setIdEstadoPais(entity.getIdEstadoPais());
        Jugador jugador = new Jugador();
        jugador.setIdJugador(entity.getJugador().getIdJugador());
        estado.setJugador(jugador);
        estado.setCantidadTropas(entity.getCantidadTropas() == 0 ? (int) 0L : entity.getCantidadTropas());
        return estado;
    }

    private PaisEntity mapToPaisEntity(Pais pais) {
        PaisEntity pe = new PaisEntity();
        pe.setIdPais(pais.getIdPais());
        return pe;

    }

}
