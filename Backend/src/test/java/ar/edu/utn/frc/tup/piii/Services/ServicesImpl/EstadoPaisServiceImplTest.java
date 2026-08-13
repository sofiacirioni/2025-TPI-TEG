package ar.edu.utn.frc.tup.piii.Services.ServicesImpl;

import ar.edu.utn.frc.tup.piii.Dtos.EstadoPaisDto;
import ar.edu.utn.frc.tup.piii.Dtos.EstadoPaises.AgregarFichas;
import ar.edu.utn.frc.tup.piii.Dtos.EstadoPaises.EstadoPaisFicha;
import ar.edu.utn.frc.tup.piii.Dtos.EstadoPaises.MoverFichas;
import ar.edu.utn.frc.tup.piii.Entities.*;
import ar.edu.utn.frc.tup.piii.Repositories.*;
import ar.edu.utn.frc.tup.piii.Services.PaisService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EstadoPaisServiceImplTest {
    @Mock
    private EstadoPaisRepository estadoPaisRepository;

    @Mock
    private LimiteRepository limiteRepository;

    @Mock
    private PaisService paisService;

    @Mock
    private JugadorRepository jugadorRepository;

    @Mock
    private PartidaRepository partidaRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private EstadoPaisServiceImpl estadoPaisService;

    @Test
    void testRepartirPaises_reparteEquitativamente() {
        Long partidaId = 1L;

        PartidaEntity partidaMock = new PartidaEntity();
        List<EstadoPaisEntity> estados = new ArrayList<>();
        List<JugadorEntity> jugadores = new ArrayList<>();

        for (int i = 0; i < 6; i++) {
            estados.add(Mockito.spy(new EstadoPaisEntity()));
        }

        for (int i = 0; i < 2; i++) {
            jugadores.add(new JugadorEntity());
        }

        partidaMock.setEstadoPaises(estados);
        partidaMock.setJugadores(jugadores);

        when(partidaRepository.findById(partidaId)).thenReturn(Optional.of(partidaMock));

        estadoPaisService.repartirPaises(partidaId);

        for (EstadoPaisEntity estado : estados) {
            verify(estado).setJugador(any(JugadorEntity.class));
            verify(estado).setCantidadTropas(1);
            verify(estadoPaisRepository).save(estado);
        }
    }
    @Test
    void testGetEstadoPaisEntity_devuelveEntitySiExiste() {
        Long id = 2L;
        EstadoPaisEntity entity = new EstadoPaisEntity();
        entity.setIdEstadoPais(id);
        entity.setCantidadTropas(5);

        when(estadoPaisRepository.findById(id)).thenReturn(Optional.of(entity));

        EstadoPaisEntity result = estadoPaisService.getEstadoPaisEntity(id);

        assertNotNull(result);
        assertEquals(id, result.getIdEstadoPais());
        assertEquals(5, result.getCantidadTropas());
        verify(estadoPaisRepository).findById(id);
    }
    @Test
    void testRepartirPaises_lanzaExcepcionSiPartidaNoExiste() {
        Long partidaId = 99L;
        when(partidaRepository.findById(partidaId)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                estadoPaisService.repartirPaises(partidaId)
        );

        assertEquals("Partida no encontrada", ex.getMessage());
    }
    @Test
    void testInicializarEstadosPaises_creaEstadosYGuardaPartida() {

        Long partidaId = 1L;

        List<PaisEntity> paisesMock = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            PaisEntity pais = new PaisEntity();
            pais.setIdPais((long) i);
            paisesMock.add(pais);
        }

        PartidaEntity partidaMock = new PartidaEntity();

        when(paisService.obtenerTodos()).thenReturn(paisesMock);
        when(partidaRepository.findById(partidaId)).thenReturn(Optional.of(partidaMock));

        estadoPaisService.inicializarEstadosPaises(partidaId);

        verify(estadoPaisRepository, times(3)).save(any(EstadoPaisEntity.class));

        verify(partidaRepository).save(partidaMock);
    }
    @Test
    void testInicializarEstadosPaises_partidaNoEncontrada_lanzaExcepcion() {
        Long partidaId = 99L;

        when(paisService.obtenerTodos()).thenReturn(new ArrayList<>());
        when(partidaRepository.findById(partidaId)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            estadoPaisService.inicializarEstadosPaises(partidaId);
        });

        assertEquals("Partida no encontrada", exception.getMessage());

        verify(estadoPaisRepository, never()).save(any());
        verify(partidaRepository, never()).save(any());
    }


    @Test
    void testAgregarFichasEstadosPaises_exitoso() {
        AgregarFichas agregarFichas = new AgregarFichas();
        agregarFichas.setIdJugador(1L);

        EstadoPaisFicha ficha1 = new EstadoPaisFicha();
        ficha1.setIdPais(10L);
        ficha1.setCantidadFichas(3L);

        EstadoPaisFicha ficha2 = new EstadoPaisFicha();
        ficha2.setIdPais(20L);
        ficha2.setCantidadFichas(2L);

        agregarFichas.setPaisesFichas(List.of(ficha1, ficha2));

        JugadorEntity jugador = new JugadorEntity();
        jugador.setIdJugador(1L);
        jugador.setEjercito(10);

        EstadoPaisEntity estado1 = new EstadoPaisEntity();
        estado1.setCantidadTropas(5);

        EstadoPaisEntity estado2 = new EstadoPaisEntity();
        estado2.setCantidadTropas(1);

        when(jugadorRepository.findById(1L)).thenReturn(Optional.of(jugador));
        when(estadoPaisRepository.findByPaisIdPaisAndJugadorIdJugador(10L, 1L))
                .thenReturn(Optional.of(estado1));
        when(estadoPaisRepository.findByPaisIdPaisAndJugadorIdJugador(20L, 1L))
                .thenReturn(Optional.of(estado2));

        boolean resultado = estadoPaisService.agregarFichasEstadosPaises(agregarFichas);

        assertTrue(resultado);

        assertEquals(8, estado1.getCantidadTropas());
        assertEquals(3, estado2.getCantidadTropas());

        assertEquals(5, jugador.getEjercito());

        verify(estadoPaisRepository).save(estado1);
        verify(estadoPaisRepository).save(estado2);
        verify(jugadorRepository).save(jugador);
    }

    @Test
    void testAgregarFichasEstadosPaises_lanzaExcepcionSiJugadorNoEncontrado() {
        AgregarFichas agregarFichas = new AgregarFichas();
        agregarFichas.setIdJugador(1L);

        EstadoPaisFicha ficha1 = new EstadoPaisFicha();
        ficha1.setIdPais(10L);
        ficha1.setCantidadFichas(3L);

        agregarFichas.setPaisesFichas(List.of(ficha1));

        when(jugadorRepository.findById(1L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            estadoPaisService.agregarFichasEstadosPaises(agregarFichas);
        });

        assertEquals("Jugador no encontrado", ex.getMessage());

        verify(estadoPaisRepository, never()).save(any());
        verify(jugadorRepository, never()).save(any());
    }

    @Test
    void testAgregarFichasEstadosPaises_lanzaExcepcionSiJugadorNoControlaPais() {
        AgregarFichas agregarFichas = new AgregarFichas();
        agregarFichas.setIdJugador(1L);

        EstadoPaisFicha ficha1 = new EstadoPaisFicha();
        ficha1.setIdPais(10L);
        ficha1.setCantidadFichas(3L);

        agregarFichas.setPaisesFichas(List.of(ficha1));

        JugadorEntity jugador = new JugadorEntity();
        jugador.setIdJugador(1L);
        jugador.setEjercito(10);

        when(jugadorRepository.findById(1L)).thenReturn(Optional.of(jugador));
        when(estadoPaisRepository.findByPaisIdPaisAndJugadorIdJugador(10L, 1L))
                .thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            estadoPaisService.agregarFichasEstadosPaises(agregarFichas);
        });

        assertEquals("El jugador no controla el país con ID: 10", ex.getMessage());

        verify(estadoPaisRepository, never()).save(any());
        verify(jugadorRepository, never()).save(any());
    }

    @Test
    void testAgregarFichasEstadosPaises_conUnSoloPais() {
        AgregarFichas agregarFichas = new AgregarFichas();
        agregarFichas.setIdJugador(1L);

        EstadoPaisFicha ficha1 = new EstadoPaisFicha();
        ficha1.setIdPais(10L);
        ficha1.setCantidadFichas(5L);

        agregarFichas.setPaisesFichas(List.of(ficha1));

        JugadorEntity jugador = new JugadorEntity();
        jugador.setIdJugador(1L);
        jugador.setEjercito(20);

        EstadoPaisEntity estado1 = new EstadoPaisEntity();
        estado1.setCantidadTropas(2);

        when(jugadorRepository.findById(1L)).thenReturn(Optional.of(jugador));
        when(estadoPaisRepository.findByPaisIdPaisAndJugadorIdJugador(10L, 1L))
                .thenReturn(Optional.of(estado1));

        boolean resultado = estadoPaisService.agregarFichasEstadosPaises(agregarFichas);

        assertTrue(resultado);
        assertEquals(7, estado1.getCantidadTropas());
        assertEquals(15, jugador.getEjercito());

        verify(estadoPaisRepository).save(estado1);
        verify(jugadorRepository).save(jugador);
    }

    @Test
    void testAgrupacionFichas_exitoso() {

        // agrupacionFichas ya no valida adyacencia: esa regla se movió al chequeo de
        // conectividad por BFS de TurnoServiceImpl.moverFichas. Acá solo se prueba el
        // traslado de tropas. sonLimitrofes tiene sus propios tests más abajo.
        MoverFichas moverFichas = new MoverFichas();
        moverFichas.setIdJugador(1L);
        moverFichas.setIdPaisOrigen(10L);
        moverFichas.setIdPaisDestino(20L);
        moverFichas.setCantidadFichas(3L);

        PaisEntity paisOrigen = new PaisEntity();
        paisOrigen.setIdPais(10L);

        PaisEntity paisDestino = new PaisEntity();
        paisDestino.setIdPais(20L);

        EstadoPaisEntity estadoOrigen = new EstadoPaisEntity();
        estadoOrigen.setCantidadTropas(5);
        estadoOrigen.setPais(paisOrigen);

        EstadoPaisEntity estadoDestino = new EstadoPaisEntity();
        estadoDestino.setCantidadTropas(2);
        estadoDestino.setPais(paisDestino);

        when(estadoPaisRepository.findByPais_IdPaisAndJugador_IdJugador(10L, 1L))
                .thenReturn(Optional.of(estadoOrigen));
        when(estadoPaisRepository.findByPais_IdPaisAndJugador_IdJugador(20L, 1L))
                .thenReturn(Optional.of(estadoDestino));

        when(estadoPaisRepository.save(estadoOrigen)).thenReturn(estadoOrigen);
        when(estadoPaisRepository.save(estadoDestino)).thenReturn(estadoDestino);

        boolean resultado = estadoPaisService.agrupacionFichas(moverFichas);

        assertTrue(resultado);
        assertEquals(2, estadoOrigen.getCantidadTropas());
        assertEquals(5, estadoDestino.getCantidadTropas());

        verify(estadoPaisRepository).save(estadoOrigen);
        verify(estadoPaisRepository).save(estadoDestino);
    }

    @Test
    void testAgrupacionFichas_fichasInsuficientes() {
        // El origen debe conservar al menos 1 ficha: mover las 5 que tiene es inválido.
        MoverFichas moverFichas = new MoverFichas();
        moverFichas.setIdJugador(1L);
        moverFichas.setIdPaisOrigen(10L);
        moverFichas.setIdPaisDestino(20L);
        moverFichas.setCantidadFichas(5L);

        EstadoPaisEntity estadoOrigen = new EstadoPaisEntity();
        estadoOrigen.setCantidadTropas(5);
        estadoOrigen.setPais(new PaisEntity());
        estadoOrigen.getPais().setIdPais(10L);

        EstadoPaisEntity estadoDestino = new EstadoPaisEntity();
        estadoDestino.setCantidadTropas(2);
        estadoDestino.setPais(new PaisEntity());
        estadoDestino.getPais().setIdPais(20L);

        when(estadoPaisRepository.findByPais_IdPaisAndJugador_IdJugador(10L, 1L))
                .thenReturn(Optional.of(estadoOrigen));
        when(estadoPaisRepository.findByPais_IdPaisAndJugador_IdJugador(20L, 1L))
                .thenReturn(Optional.of(estadoDestino));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            estadoPaisService.agrupacionFichas(moverFichas);
        });

        assertEquals("Las fichas no son suficientes", ex.getMessage());
        verify(estadoPaisRepository, never()).save(any(EstadoPaisEntity.class));
    }

    @Test
    void testCambiarPropietario_exitoso() {

        Long idEstadoPaisObtenido = 1L;
        Long idEstadoPaisAtacante = 2L;
        Long idNuevoPropietario = 3L;

        EstadoPaisEntity estadoObtenido = new EstadoPaisEntity();
        estadoObtenido.setCantidadTropas(5);
        estadoObtenido.setJugador(new JugadorEntity());

        EstadoPaisEntity estadoAtacante = new EstadoPaisEntity();
        estadoAtacante.setCantidadTropas(4);
        estadoAtacante.setJugador(new JugadorEntity());

        JugadorEntity nuevoJugador = new JugadorEntity();
        nuevoJugador.setIdJugador(idNuevoPropietario);

        EstadoPaisServiceImpl spyService = spy(estadoPaisService);
        doReturn(estadoObtenido).when(spyService).getEstadoPaisEntity(idEstadoPaisObtenido);
        doReturn(estadoAtacante).when(spyService).getEstadoPaisEntity(idEstadoPaisAtacante);

        when(jugadorRepository.findById(idNuevoPropietario)).thenReturn(Optional.of(nuevoJugador));
        when(estadoPaisRepository.save(any(EstadoPaisEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        spyService.cambiarPropietario(idEstadoPaisObtenido, idEstadoPaisAtacante, idNuevoPropietario);

        assertEquals(nuevoJugador, estadoObtenido.getJugador());
        assertEquals(1, estadoObtenido.getCantidadTropas());
        assertEquals(3, estadoAtacante.getCantidadTropas());

        verify(estadoPaisRepository).save(estadoObtenido);
        verify(estadoPaisRepository).save(estadoAtacante);
    }

    @Test
    void testCambiarPropietario_jugadorNoEncontrado() {
        Long idEstadoPaisObtenido = 1L;
        Long idEstadoPaisAtacante = 2L;
        Long idNuevoPropietario = 3L;

        EstadoPaisServiceImpl spyService = spy(estadoPaisService);

        EstadoPaisEntity estadoObtenido = new EstadoPaisEntity();
        EstadoPaisEntity estadoAtacante = new EstadoPaisEntity();

        doReturn(estadoObtenido).when(spyService).getEstadoPaisEntity(idEstadoPaisObtenido);
        doReturn(estadoAtacante).when(spyService).getEstadoPaisEntity(idEstadoPaisAtacante);

        when(jugadorRepository.findById(idNuevoPropietario)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            spyService.cambiarPropietario(idEstadoPaisObtenido, idEstadoPaisAtacante, idNuevoPropietario);
        });

        assertEquals("Jugador no encontrado", exception.getMessage());

        verify(estadoPaisRepository, never()).save(any());
    }

    @Test
    void testSonLimitrofes_true() {
        Long idPais1 = 1L;
        Long idPais2 = 2L;

        when(limiteRepository.existeLimiteEntre(idPais1, idPais2)).thenReturn(true);

        boolean resultado = estadoPaisService.sonLimitrofes(idPais1, idPais2);

        assertTrue(resultado);
        verify(limiteRepository).existeLimiteEntre(idPais1, idPais2);
    }

    @Test
    void testSonLimitrofes_false() {
        Long idPais1 = 1L;
        Long idPais2 = 2L;

        when(limiteRepository.existeLimiteEntre(idPais1, idPais2)).thenReturn(false);

        boolean resultado = estadoPaisService.sonLimitrofes(idPais1, idPais2);

        assertFalse(resultado);
        verify(limiteRepository).existeLimiteEntre(idPais1, idPais2);
    }

    @Test
    void testCreateEstadoPais_deberiaGuardarYRetornarDto() {

        EstadoPaisDto dtoEntrada = new EstadoPaisDto();
        dtoEntrada.setId(1L);
        dtoEntrada.setCantidadTropas(5);

        EstadoPaisEntity entidadMapeada = new EstadoPaisEntity();
        entidadMapeada.setIdEstadoPais(1L);
        entidadMapeada.setCantidadTropas(5);

        EstadoPaisEntity entidadGuardada = new EstadoPaisEntity();
        entidadGuardada.setIdEstadoPais(1L);
        entidadGuardada.setCantidadTropas(5);

        EstadoPaisDto dtoSalida = new EstadoPaisDto();
        dtoSalida.setId(1L);
        dtoSalida.setCantidadTropas(5);

        when(modelMapper.map(dtoEntrada, EstadoPaisEntity.class)).thenReturn(entidadMapeada);
        when(estadoPaisRepository.save(entidadMapeada)).thenReturn(entidadGuardada);
        when(modelMapper.map(entidadGuardada, EstadoPaisDto.class)).thenReturn(dtoSalida);

        EstadoPaisDto resultado = estadoPaisService.createEstadoPais(dtoEntrada);

        assertNotNull(resultado);
        assertEquals(dtoSalida.getId(), resultado.getId());
        assertEquals(dtoSalida.getCantidadTropas(), resultado.getCantidadTropas());

        verify(modelMapper).map(dtoEntrada, EstadoPaisEntity.class);
        verify(estadoPaisRepository).save(entidadMapeada);
        verify(modelMapper).map(entidadGuardada, EstadoPaisDto.class);
    }

    @Test
    void testGetEstadoPais_deberiaRetornarDto_siExiste() {
        Long id = 1L;

        EstadoPaisEntity entidad = new EstadoPaisEntity();
        entidad.setIdEstadoPais(id);
        entidad.setCantidadTropas(10);

        EstadoPaisDto dtoEsperado = new EstadoPaisDto();
        dtoEsperado.setId(id);
        dtoEsperado.setCantidadTropas(10);

        when(estadoPaisRepository.findById(id)).thenReturn(Optional.of(entidad));
        when(modelMapper.map(entidad, EstadoPaisDto.class)).thenReturn(dtoEsperado);

        EstadoPaisDto resultado = estadoPaisService.getEstadoPais(id);

        assertNotNull(resultado);
        assertEquals(id, resultado.getId());
        assertEquals(10, resultado.getCantidadTropas());

        verify(estadoPaisRepository).findById(id);
        verify(modelMapper).map(entidad, EstadoPaisDto.class);
    }

    @Test
    void testGetEstadoPais_deberiaLanzarExcepcion_siNoExiste() {
        Long id = 1L;

        when(estadoPaisRepository.findById(id)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            estadoPaisService.getEstadoPais(id);
        });

        assertEquals("EstadoPais no encontrado", exception.getMessage());

        verify(estadoPaisRepository).findById(id);
        verifyNoInteractions(modelMapper);
    }

    @Test
    void testUpdateEstadoPais_deberiaActualizarYRetornarDto() {
        EstadoPaisDto dtoEntrada = new EstadoPaisDto();
        dtoEntrada.setId(1L);
        dtoEntrada.setCantidadTropas(5);

        EstadoPaisEntity entidadMapeada = new EstadoPaisEntity();
        entidadMapeada.setIdEstadoPais(1L);
        entidadMapeada.setCantidadTropas(5);

        EstadoPaisEntity entidadGuardada = new EstadoPaisEntity();
        entidadGuardada.setIdEstadoPais(1L);
        entidadGuardada.setCantidadTropas(10);

        EstadoPaisDto dtoSalida = new EstadoPaisDto();
        dtoSalida.setId(1L);
        dtoSalida.setCantidadTropas(10);

        when(modelMapper.map(dtoEntrada, EstadoPaisEntity.class)).thenReturn(entidadMapeada);
        when(estadoPaisRepository.save(entidadMapeada)).thenReturn(entidadGuardada);
        when(modelMapper.map(entidadGuardada, EstadoPaisDto.class)).thenReturn(dtoSalida);

        EstadoPaisDto resultado = estadoPaisService.updateEstadoPais(dtoEntrada);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals(10, resultado.getCantidadTropas());

        verify(modelMapper).map(dtoEntrada, EstadoPaisEntity.class);
        verify(estadoPaisRepository).save(entidadMapeada);
        verify(modelMapper).map(entidadGuardada, EstadoPaisDto.class);
    }
    @Test
    void testGetLimitesEstadoPais_devuelveLimitesCorrectos() {
        Long estadoId = 1L;
        Long paisId = 100L;
        Long partidaId = 200L;

        EstadoPaisEntity estadoBase = new EstadoPaisEntity();
        PaisEntity paisBase = new PaisEntity();
        paisBase.setIdPais(paisId);
        estadoBase.setPais(paisBase);

        PartidaEntity partida = new PartidaEntity();
        partida.setIdPartida(partidaId);
        estadoBase.setPartida(partida);

        when(estadoPaisRepository.findById(estadoId)).thenReturn(Optional.of(estadoBase));

        LimiteEntity limite1 = new LimiteEntity();
        PaisEntity pais1 = new PaisEntity();
        pais1.setIdPais(101L);
        limite1.setPais1(paisBase);
        limite1.setPais2(pais1);

        LimiteEntity limite2 = new LimiteEntity();
        PaisEntity pais2 = new PaisEntity();
        pais2.setIdPais(102L);
        limite2.setPais1(pais2);
        limite2.setPais2(paisBase);

        when(limiteRepository.findByPais1_IdPaisOrPais2_IdPais(paisId, paisId))
                .thenReturn(List.of(limite1, limite2));

        EstadoPaisEntity estadoLimite1 = new EstadoPaisEntity();
        estadoLimite1.setIdEstadoPais(10L);
        estadoLimite1.setCantidadTropas(5);

        EstadoPaisEntity estadoLimite2 = new EstadoPaisEntity();
        estadoLimite2.setIdEstadoPais(11L);
        estadoLimite2.setCantidadTropas(3);

        when(estadoPaisRepository.findAllByPais_IdPaisInAndPartida_IdPartida(anySet(), eq(partidaId)))
                .thenReturn(List.of(estadoLimite1, estadoLimite2));

        EstadoPaisDto dto1 = new EstadoPaisDto();
        dto1.setId(10L);
        dto1.setCantidadTropas(5);

        EstadoPaisDto dto2 = new EstadoPaisDto();
        dto2.setId(11L);
        dto2.setCantidadTropas(3);

        when(modelMapper.map(estadoLimite1, EstadoPaisDto.class)).thenReturn(dto1);
        when(modelMapper.map(estadoLimite2, EstadoPaisDto.class)).thenReturn(dto2);

        List<EstadoPaisDto> result = estadoPaisService.getLimitesEstadoPais(estadoId);

        assertEquals(2, result.size());
        assertEquals(10L, result.get(0).getId());
        assertEquals(11L, result.get(1).getId());

        verify(estadoPaisRepository).findById(estadoId);
        verify(limiteRepository).findByPais1_IdPaisOrPais2_IdPais(paisId, paisId);
        verify(estadoPaisRepository).findAllByPais_IdPaisInAndPartida_IdPartida(anySet(), eq(partidaId));
    }

}
