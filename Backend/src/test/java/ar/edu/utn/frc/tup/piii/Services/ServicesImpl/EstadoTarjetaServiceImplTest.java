package ar.edu.utn.frc.tup.piii.Services.ServicesImpl;

import ar.edu.utn.frc.tup.piii.Dtos.EstadoTarjetaDto;
import ar.edu.utn.frc.tup.piii.Dtos.TarjetaDto;
import ar.edu.utn.frc.tup.piii.Entities.*;
import ar.edu.utn.frc.tup.piii.Repositories.*;
import ar.edu.utn.frc.tup.piii.Services.TarjetaService;
import ar.edu.utn.frc.tup.piii.models.Pais;
import ar.edu.utn.frc.tup.piii.models.Simbolo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.util.*;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class EstadoTarjetaServiceImplTest {
    @Mock
    private TarjetaRepository tarjetaRepository;

    @Mock
    private EstadoTarjetaRepository estadoTarjetaRepository;

    @Mock
    private PartidaRepository partidaRepository;
    @Mock
    private EstadoPaisRepository estadoPaisRepository;
    @Mock
    private JugadorRepository jugadorRepository;

    @Mock
    private ModelMapper modelMapper;
    @InjectMocks
    EstadoTarjetaServiceImpl sut;
    @InjectMocks
    private EstadoTarjetaServiceImpl tarjetaService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testInicializarTarjetas_Success() {
        Long idPartida = 1L;
        PartidaEntity partida = new PartidaEntity();
        partida.setIdPartida(idPartida);

        TarjetaEntity tarjeta1 = new TarjetaEntity();
        tarjeta1.setIdTarjeta(1L);
        TarjetaEntity tarjeta2 = new TarjetaEntity();
        tarjeta2.setIdTarjeta(2L);

        List<TarjetaEntity> tarjetas = Arrays.asList(tarjeta1, tarjeta2);

        when(partidaRepository.findById(idPartida)).thenReturn(Optional.of(partida));
        when(tarjetaRepository.findAll()).thenReturn(tarjetas);

        tarjetaService.inicializarTarjetas(idPartida);

        verify(estadoTarjetaRepository, times(2)).save(any(EstadoTarjetaEntity.class));
        verify(partidaRepository).save(partida);
    }

    @Test
    void testInicializarTarjetas_ListaVacia() {
        Long idPartida = 1L;
        PartidaEntity partida = new PartidaEntity();
        partida.setIdPartida(idPartida);

        when(partidaRepository.findById(idPartida)).thenReturn(Optional.of(partida));
        when(tarjetaRepository.findAll()).thenReturn(Collections.emptyList());

        tarjetaService.inicializarTarjetas(idPartida);

        verify(estadoTarjetaRepository, never()).save(any());
        verify(partidaRepository).save(partida);
    }

    @Test
    void testAsignarTarjeta_Success() {
        EstadoTarjetaDto dto = new EstadoTarjetaDto();
        dto.setIdJugador(1l);
        dto.setIdEstadoTarjeta(1L);

        JugadorEntity jugador = new JugadorEntity();
        jugador.setIdJugador(1L);

        TarjetaEntity tarjeta = new TarjetaEntity();
        tarjeta.setIdTarjeta(1L);
        tarjeta.setSimbolo(Simbolo.GALEON);

        PaisEntity pais = new PaisEntity();
        pais.setIdPais(1L);
        tarjeta.setPais(pais);

        PartidaEntity partida = new PartidaEntity();
        partida.setIdPartida(1L);

        EstadoTarjetaEntity estado = new EstadoTarjetaEntity();
        estado.setIdEstadoTarjeta(1L);
        estado.setTarjeta(tarjeta);
        estado.setPartida(partida);
        estado.setJugador(null);

        EstadoTarjetaEntity estadoDisponible = new EstadoTarjetaEntity();
        estadoDisponible.setJugador(null);
        estadoDisponible.setCanjeada(false);
        partida.setMazo(Arrays.asList(estado, estadoDisponible));

        Pais paisDto = new Pais();
        paisDto.setIdPais(1L);

        when(jugadorRepository.findById(1L)).thenReturn(Optional.of(jugador));
        when(estadoTarjetaRepository.findById(1L)).thenReturn(Optional.of(estado));
        when(partidaRepository.findById(1L)).thenReturn(Optional.of(partida));
        when(modelMapper.map(pais, Pais.class)).thenReturn(paisDto);

        TarjetaDto resultado = tarjetaService.asignarTarjeta(dto);

        assertNotNull(resultado);
        verify(estadoTarjetaRepository).save(estado);
        verify(jugadorRepository).save(jugador);
        assertEquals(jugador, estado.getJugador());
    }

    @Test
    void testAsignarTarjeta_JugadorNoEncontrado() {
        EstadoTarjetaDto dto = new EstadoTarjetaDto();
        dto.setIdJugador(999l);
        dto.setIdEstadoTarjeta(1L);

        when(jugadorRepository.findById(999L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> tarjetaService.asignarTarjeta(dto));

        assertTrue(exception.getMessage().contains("Una o más entidades no fueron encontradas"));
    }

    @Test
    void testAsignarTarjeta_EstadoTarjetaNoEncontrado() {
        EstadoTarjetaDto dto = new EstadoTarjetaDto();
        dto.setIdJugador(1l);
        dto.setIdEstadoTarjeta(999L);

        JugadorEntity jugador = new JugadorEntity();
        when(jugadorRepository.findById(1L)).thenReturn(Optional.of(jugador));
        when(estadoTarjetaRepository.findById(999L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> tarjetaService.asignarTarjeta(dto));

        assertTrue(exception.getMessage().contains("Una o más entidades no fueron encontradas"));
    }

    @Test
    void testAsignarTarjeta_TarjetaYaAsignada() {
        EstadoTarjetaDto dto = new EstadoTarjetaDto();
        dto.setIdJugador(1l);
        dto.setIdEstadoTarjeta(1L);

        JugadorEntity jugador = new JugadorEntity();
        JugadorEntity otroJugador = new JugadorEntity();
        otroJugador.setIdJugador(2L);

        EstadoTarjetaEntity estado = new EstadoTarjetaEntity();
        estado.setJugador(otroJugador);

        when(jugadorRepository.findById(1L)).thenReturn(Optional.of(jugador));
        when(estadoTarjetaRepository.findById(1L)).thenReturn(Optional.of(estado));

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> tarjetaService.asignarTarjeta(dto));

        assertTrue(exception.getMessage().contains("No se puede asignar la tarjeta"));
    }

    @Test
    void testValidarMazo_DebeRenovarMazo() {
        EstadoTarjetaDto dto = new EstadoTarjetaDto();
        dto.setIdJugador(1l);
        dto.setIdEstadoTarjeta(1L);

        JugadorEntity jugador = new JugadorEntity();
        TarjetaEntity tarjeta = new TarjetaEntity();
        tarjeta.setIdTarjeta(1L);
        tarjeta.setSimbolo(Simbolo.GLOBO);
        tarjeta.setPais(new PaisEntity());

        PartidaEntity partida = new PartidaEntity();
        partida.setIdPartida(1L);

        EstadoTarjetaEntity estado = new EstadoTarjetaEntity();
        estado.setTarjeta(tarjeta);
        estado.setPartida(partida);
        estado.setJugador(null);

        EstadoTarjetaEntity estadoAsignado = new EstadoTarjetaEntity();
        estadoAsignado.setJugador(jugador);
        estadoAsignado.setCanjeada(false);

        EstadoTarjetaEntity estadoCanjeado = new EstadoTarjetaEntity();
        estadoCanjeado.setJugador(null);
        estadoCanjeado.setCanjeada(true);

        partida.setMazo(Arrays.asList(estadoAsignado, estadoCanjeado));

        EstadoTarjetaEntity estadoRenovable1 = new EstadoTarjetaEntity();
        estadoRenovable1.setCanjeada(true);
        estadoRenovable1.setUsada(true);

        EstadoTarjetaEntity estadoRenovable2 = new EstadoTarjetaEntity();
        estadoRenovable2.setCanjeada(true);
        estadoRenovable2.setUsada(true);

        when(jugadorRepository.findById(1L)).thenReturn(Optional.of(jugador));
        when(estadoTarjetaRepository.findById(1L)).thenReturn(Optional.of(estado));
        when(partidaRepository.findById(1L)).thenReturn(Optional.of(partida));
        when(estadoTarjetaRepository.findAllByPartida_IdPartida(1L))
                .thenReturn(Arrays.asList(estadoRenovable1, estadoRenovable2));
        when(modelMapper.map(any(), eq(Pais.class))).thenReturn(new Pais());

        tarjetaService.asignarTarjeta(dto);

        verify(estadoTarjetaRepository).saveAll(anyList());
    }

    @Test
    void testValidarMazo_NoDebeRenovarMazo() {
        EstadoTarjetaDto dto = new EstadoTarjetaDto();
        dto.setIdJugador(1L);
        dto.setIdEstadoTarjeta(1L);

        JugadorEntity jugador = new JugadorEntity();
        TarjetaEntity tarjeta = new TarjetaEntity();
        tarjeta.setIdTarjeta(1L);
        tarjeta.setSimbolo(Simbolo.GALEON);
        tarjeta.setPais(new PaisEntity());

        PartidaEntity partida = new PartidaEntity();
        partida.setIdPartida(1L);

        EstadoTarjetaEntity estado = new EstadoTarjetaEntity();
        estado.setTarjeta(tarjeta);
        estado.setPartida(partida);
        estado.setJugador(null);

        EstadoTarjetaEntity estadoDisponible = new EstadoTarjetaEntity();
        estadoDisponible.setJugador(null);
        estadoDisponible.setCanjeada(false);

        partida.setMazo(Arrays.asList(estado, estadoDisponible));

        when(jugadorRepository.findById(1L)).thenReturn(Optional.of(jugador));
        when(estadoTarjetaRepository.findById(1L)).thenReturn(Optional.of(estado));
        when(partidaRepository.findById(1L)).thenReturn(Optional.of(partida));
        when(modelMapper.map(any(), eq(Pais.class))).thenReturn(new Pais());

        tarjetaService.asignarTarjeta(dto);

        verify(estadoTarjetaRepository, never()).findAllByPartida_IdPartida(anyLong());
        verify(estadoTarjetaRepository, never()).saveAll(anyList());
    }

    @Test
    void usarTarjetaEnPais_deberiaAsignarTropasSiTodoOK() {
        TarjetaEntity tarjeta = new TarjetaEntity();
        PaisEntity pais = new PaisEntity();
        pais.setIdPais(10L);
        tarjeta.setPais(pais);

        EstadoTarjetaEntity estadoTarjeta = new EstadoTarjetaEntity();
        estadoTarjeta.setTarjeta(tarjeta);
        estadoTarjeta.setUsada(false);

        EstadoPaisEntity estadoPais = new EstadoPaisEntity();
        estadoPais.setCantidadTropas(3);

        when(estadoTarjetaRepository.findById(1L)).thenReturn(Optional.of(estadoTarjeta));
        when(estadoPaisRepository.findByPaisIdPaisAndJugadorIdJugador(10L, 1L))
                .thenReturn(Optional.of(estadoPais));

        sut.usarTarjetaEnPais(1L, 1L);

        assertTrue(estadoTarjeta.isUsada());
        assertEquals(5, estadoPais.getCantidadTropas());
        verify(estadoTarjetaRepository).save(estadoTarjeta);
        verify(estadoPaisRepository).save(estadoPais);
    }

    @Test
    void canjearTarjetas_conTresDistintasYValidas_devuelveEjercitos() {
        JugadorEntity jugador = new JugadorEntity();
        jugador.setIdJugador(1L);
        jugador.setEjercito(0);

        EstadoTarjetaEntity t1 = crearTarjetaConSimbolo(Simbolo.CANION, jugador);
        EstadoTarjetaEntity t2 = crearTarjetaConSimbolo(Simbolo.GLOBO, jugador);
        EstadoTarjetaEntity t3 = crearTarjetaConSimbolo(Simbolo.GALEON, jugador);

        List<Long> ids = List.of(1L, 2L, 3L);
        List<EstadoTarjetaEntity> tarjetas = List.of(t1, t2, t3);

        when(estadoTarjetaRepository.findAllById(ids)).thenReturn(tarjetas);
        when(estadoTarjetaRepository.findByJugadorId(1L)).thenReturn(tarjetas);
        when(jugadorRepository.findById(1L)).thenReturn(Optional.of(jugador));

        Integer ejercitos = sut.canjearTarjetas(ids, 1L);

        assertEquals(4, ejercitos);
        assertTrue(t1.isCanjeada());
        assertTrue(t2.isCanjeada());
        assertTrue(t3.isCanjeada());
        assertEquals(4, jugador.getEjercito());
        verify(estadoTarjetaRepository).saveAll(tarjetas);
        verify(jugadorRepository).save(jugador);
    }

    private EstadoTarjetaEntity crearTarjetaConSimbolo(Simbolo simbolo, JugadorEntity jugador) {
        SimboloEntity simboloEntity = new SimboloEntity();
        simboloEntity.setTipo(simbolo.name());

        TarjetaEntity tarjeta = new TarjetaEntity();
        tarjeta.setIdTarjeta(1L);
        tarjeta.setSimbolo(simbolo);
        PaisEntity pais = new PaisEntity();
        pais.setIdPais(100L);
        tarjeta.setPais(pais);

        EstadoTarjetaEntity estado = new EstadoTarjetaEntity();
        estado.setTarjeta(tarjeta);
        estado.setJugador(jugador);
        estado.setCanjeada(false);
        return estado;
    }

    @Test
    void obtenerEstadoTarjeta_devuelveLista() {
        List<EstadoTarjetaEntity> lista = List.of(new EstadoTarjetaEntity());
        when(estadoTarjetaRepository.findAll()).thenReturn(lista);

        List<EstadoTarjetaEntity> resultado = sut.obtenerEstadoTarjeta();

        assertEquals(1, resultado.size());
    }

    @Test
    void obtenerPorId_existe() {
        TarjetaEntity tarjeta = new TarjetaEntity();
        tarjeta.setIdTarjeta(1L);

        EstadoTarjetaEntity estadoTarjeta = new EstadoTarjetaEntity();
        estadoTarjeta.setTarjeta(tarjeta);
        estadoTarjeta.setUsada(false);

        when(estadoTarjetaRepository.findById(1L)).thenReturn(Optional.of(estadoTarjeta));

        EstadoTarjetaEntity resultado = sut.obtenerPorId(1L);

        assertEquals(1L, resultado.getTarjeta().getIdTarjeta());
    }

    @Test
    void eliminarEstadoTarjeta_exito() {
        when(estadoTarjetaRepository.existsById(1L)).thenReturn(true);

        sut.eliminarEstadoTarjeta(1L);

        verify(estadoTarjetaRepository).deleteById(1L);
    }

    @Test
    void obtenerTarjeta_existente() {
        EstadoTarjetaEntity estado = new EstadoTarjetaEntity();
        TarjetaDto dto = new TarjetaDto();

        when(estadoTarjetaRepository.findById(1L)).thenReturn(Optional.of(estado));
        when(modelMapper.map(estado, TarjetaDto.class)).thenReturn(dto);

        TarjetaDto resultado = sut.obtenerTarjeta(1L);

        assertEquals(dto, resultado);
    }

    @Test
    void usarTarjetaEnPais_exito() {
        EstadoTarjetaEntity estadoTarjeta = new EstadoTarjetaEntity();
        estadoTarjeta.setUsada(false);

        TarjetaEntity tarjeta = new TarjetaEntity();
        PaisEntity pais = new PaisEntity();
        pais.setIdPais(1L);
        tarjeta.setPais(pais);
        estadoTarjeta.setTarjeta(tarjeta);

        EstadoPaisEntity estadoPais = new EstadoPaisEntity();
        estadoPais.setCantidadTropas(3);

        when(estadoTarjetaRepository.findById(1L)).thenReturn(Optional.of(estadoTarjeta));
        when(estadoPaisRepository.findByPaisIdPaisAndJugadorIdJugador(1L, 1L))
                .thenReturn(Optional.of(estadoPais));

        sut.usarTarjetaEnPais(1L, 1L);

        assertTrue(estadoTarjeta.isUsada());
        assertEquals(5, estadoPais.getCantidadTropas());
        verify(estadoTarjetaRepository).save(estadoTarjeta);
        verify(estadoPaisRepository).save(estadoPais);
    }

    @Test
    void canjearTarjetas_3ValidasDistintas_exito() {
        Long idJugador = 1L;
        JugadorEntity jugador = new JugadorEntity();
        jugador.setIdJugador(idJugador);
        jugador.setEjercito(0);

        TarjetaEntity t1 = new TarjetaEntity();
        t1.setIdTarjeta(1L);
        t1.setSimbolo(Simbolo.GLOBO);

        TarjetaEntity t2 = new TarjetaEntity();
        t2.setIdTarjeta(2L);
        t2.setSimbolo(Simbolo.CANION);

        TarjetaEntity t3 = new TarjetaEntity();
        t3.setIdTarjeta(3L);
        t3.setSimbolo(Simbolo.GALEON);

        EstadoTarjetaEntity et1 = new EstadoTarjetaEntity();
        et1.setTarjeta(t1);
        et1.setJugador(jugador);
        et1.setCanjeada(false);

        EstadoTarjetaEntity et2 = new EstadoTarjetaEntity();
        et2.setTarjeta(t2);
        et2.setJugador(jugador);
        et2.setCanjeada(false);

        EstadoTarjetaEntity et3 = new EstadoTarjetaEntity();
        et3.setTarjeta(t3);
        et3.setJugador(jugador);
        et3.setCanjeada(false);

        List<Long> ids = List.of(1L, 2L, 3L);
        List<EstadoTarjetaEntity> tarjetas = List.of(et1, et2, et3);

        when(estadoTarjetaRepository.findAllById(ids)).thenReturn(tarjetas);
        when(estadoTarjetaRepository.findByJugadorId(idJugador)).thenReturn(tarjetas);
        when(jugadorRepository.findById(idJugador)).thenReturn(Optional.of(jugador));

        Integer ejercitos = sut.canjearTarjetas(ids, idJugador);

        assertEquals(4, ejercitos);
        assertTrue(et1.isCanjeada());
        assertTrue(et2.isCanjeada());
        assertTrue(et3.isCanjeada());
        assertEquals(4, jugador.getEjercito());
        verify(estadoTarjetaRepository).saveAll(tarjetas);
        verify(jugadorRepository).save(jugador);
    }

    @Test
    void obtenerEstadoTarjeta_deberiaRetornarLista() {
        EstadoTarjetaEntity e1 = new EstadoTarjetaEntity();
        EstadoTarjetaEntity e2 = new EstadoTarjetaEntity();
        List<EstadoTarjetaEntity> listaMock = new ArrayList<>();
        listaMock.add(e1);
        listaMock.add(e2);

        when(estadoTarjetaRepository.findAll()).thenReturn(listaMock);

        List<EstadoTarjetaEntity> resultado = sut.obtenerEstadoTarjeta();

        assertEquals(2, resultado.size());
        assertEquals(listaMock, resultado);
    }

    @Test
    void obtenerPorId_deberiaRetornarEntidadCuandoExiste() {
        EstadoTarjetaEntity estado = new EstadoTarjetaEntity();

        when(estadoTarjetaRepository.findById(1L)).thenReturn(Optional.of(estado));

        EstadoTarjetaEntity resultado = sut.obtenerPorId(1L);

        assertNotNull(resultado);
        assertEquals(estado, resultado);
    }

    @Test
    void eliminarEstadoTarjeta_deberiaEliminarCuandoExiste() {
        when(estadoTarjetaRepository.existsById(1L)).thenReturn(true);

        sut.eliminarEstadoTarjeta(1L);

        verify(estadoTarjetaRepository).deleteById(1L);
    }

    @Test
    void obtenerTarjeta_deberiaRetornarDto() {
        EstadoTarjetaEntity estado = new EstadoTarjetaEntity();
        TarjetaDto dto = new TarjetaDto();

        when(estadoTarjetaRepository.findById(1L)).thenReturn(Optional.of(estado));
        when(modelMapper.map(estado, TarjetaDto.class)).thenReturn(dto);

        TarjetaDto resultado = sut.obtenerTarjeta(1L);

        assertEquals(dto, resultado);
    }

    @Test
    void canjearTarjetas_deberiaLanzarExcepcionSiTarjetaNoExiste() {
        Long idJugador = 1L;

        List<Long> idTarjetas = List.of(1L, 2L, 3L);


        EstadoTarjetaEntity tarjeta1 = new EstadoTarjetaEntity();
        EstadoTarjetaEntity tarjeta2 = new EstadoTarjetaEntity();

        when(estadoTarjetaRepository.findAllById(idTarjetas))
                .thenReturn(List.of(tarjeta1, tarjeta2));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            sut.canjearTarjetas(idTarjetas, idJugador);
        });

        System.out.println("Mensaje excepción: " + exception.getMessage());

        assertTrue(exception.getMessage().contains("Una o más tarjetas no existen."));
    }

    @Test
    void canjearTarjetas_deberiaLanzarExcepcionSiTarjetaNoPerteneceAlJugador() {
        Long idJugador = 1L;
        List<Long> idTarjetas = List.of(10L, 11L, 12L);

        JugadorEntity jugadorDiferente = new JugadorEntity();
        jugadorDiferente.setIdJugador(2L);

        JugadorEntity jugadorCorrecto = new JugadorEntity();
        jugadorCorrecto.setIdJugador(idJugador);

        EstadoTarjetaEntity tarjeta1 = new EstadoTarjetaEntity();
        tarjeta1.setTarjeta(new TarjetaEntity());
        tarjeta1.getTarjeta().setIdTarjeta(10L);
        tarjeta1.setJugador(jugadorCorrecto);
        tarjeta1.setCanjeada(false);

        EstadoTarjetaEntity tarjeta2 = new EstadoTarjetaEntity();
        tarjeta2.setTarjeta(new TarjetaEntity());
        tarjeta2.getTarjeta().setIdTarjeta(11L);
        tarjeta2.setJugador(jugadorDiferente);
        tarjeta2.setCanjeada(false);

        EstadoTarjetaEntity tarjeta3 = new EstadoTarjetaEntity();
        tarjeta3.setTarjeta(new TarjetaEntity());
        tarjeta3.getTarjeta().setIdTarjeta(12L);
        tarjeta3.setJugador(jugadorCorrecto);
        tarjeta3.setCanjeada(false);

        when(estadoTarjetaRepository.findAllById(idTarjetas))
                .thenReturn(List.of(tarjeta1, tarjeta2, tarjeta3));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            sut.canjearTarjetas(idTarjetas, idJugador);
        });

        System.out.println("Mensaje excepción: " + exception.getMessage());

        assertTrue(exception.getMessage().contains("no pertenece al jugador"));
    }

    @Test
    void asignarTarjeta_tarjetaYaAsignada_lanzaExcepcion() {
        EstadoTarjetaDto dto = new EstadoTarjetaDto();
        dto.setIdJugador(1L);
        dto.setIdEstadoTarjeta(10L);

        JugadorEntity jugador = new JugadorEntity();
        jugador.setIdJugador(1L);

        EstadoTarjetaEntity estado = new EstadoTarjetaEntity();
        estado.setIdEstadoTarjeta(10L);
        estado.setJugador(new JugadorEntity());

        when(jugadorRepository.findById(1L)).thenReturn(Optional.of(jugador));
        when(estadoTarjetaRepository.findById(10L)).thenReturn(Optional.of(estado));

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                sut.asignarTarjeta(dto));

        assertTrue(ex.getMessage().contains("La tarjeta ya fue asignada"));
    }

    @Test
    void asignarTarjeta_jugadorNoExiste_lanzaExcepcion() {
        EstadoTarjetaDto dto = new EstadoTarjetaDto();
        dto.setIdJugador(1L);
        dto.setIdEstadoTarjeta(10L);

        when(jugadorRepository.findById(1L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                sut.asignarTarjeta(dto));

        assertTrue(ex.getMessage().contains("Jugador no encontrado"));
    }

}

