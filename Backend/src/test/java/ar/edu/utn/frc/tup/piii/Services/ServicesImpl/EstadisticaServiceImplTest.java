package ar.edu.utn.frc.tup.piii.Services.ServicesImpl;

import ar.edu.utn.frc.tup.piii.Dtos.EstadisticaDto;
import ar.edu.utn.frc.tup.piii.Dtos.EstadoTarjetaDto;
import ar.edu.utn.frc.tup.piii.Dtos.TarjetaDto;
import ar.edu.utn.frc.tup.piii.Entities.*;
import ar.edu.utn.frc.tup.piii.Repositories.*;
import ar.edu.utn.frc.tup.piii.Services.EstadoTarjetaService;
import ar.edu.utn.frc.tup.piii.Services.TarjetaService;
import ar.edu.utn.frc.tup.piii.models.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.modelmapper.ModelMapper;

import java.time.LocalDate;
import java.util.*;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;


class EstadisticaServiceImplTest {
    @Mock
    private EstadisticaRepository estadisticaRepository;

    @Mock
    private EstadoPaisRepository estadoPaisRepository;

    @Mock
    private JugadorRepository jugadorRepository;

    @Mock
    private TarjetaRepository tarjetaRepository;

    @Mock
    private EstadoTarjetaRepository estadoTarjetaRepository;

    @Mock
    private EstadoTarjetaService tarjetaService;

    @Mock
    private PartidaRepository partidaRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private EstadisticaServiceImpl estadisticaService;

    @BeforeEach
    void setUp(){
        MockitoAnnotations.openMocks(this);
    }


    @Test
    void saveEstadisticas_deberiaGuardarEstadisticaCorrectamente() {
        Jugador jugador = new Jugador();
        jugador.setPerdio(false);

        Partida partida = new Partida();
        partida.setFechaInicio(LocalDate.of(2023, 5, 1));
        jugador.setPartida(partida);

        List<EstadoPaisEntity> conquistados = List.of(new EstadoPaisEntity(), new EstadoPaisEntity());
        List<EstadoPaisEntity> perdidos = List.of(new EstadoPaisEntity());

        when(estadoPaisRepository.findPaisesConquistados(jugador)).thenReturn(conquistados);
        when(estadoPaisRepository.findPaisesPerdidos(jugador)).thenReturn(perdidos);

        ArgumentCaptor<EstadisticaEntity> captor = ArgumentCaptor.forClass(EstadisticaEntity.class);

        Estadistica estadisticaMapeada = new Estadistica();
        estadisticaMapeada.setGanador(true);
        estadisticaMapeada.setPaisesConquistados(2);
        estadisticaMapeada.setPaisesPerdidos(1);

        EstadisticaEntity estadisticaEntity = new EstadisticaEntity();

        when(modelMapper.map(any(Estadistica.class), eq(EstadisticaEntity.class)))
                .thenReturn(estadisticaEntity);

        estadisticaService.saveEstadisticas(jugador);

        verify(estadoPaisRepository).findPaisesConquistados(jugador);
        verify(estadoPaisRepository).findPaisesPerdidos(jugador);
        verify(modelMapper).map(any(Estadistica.class), eq(EstadisticaEntity.class));
        verify(estadisticaRepository).save(estadisticaEntity);
    }
    @Test
    void testRegistrarEvento() {
        String evento = "Conquistó un país";
        Jugador jugador = new Jugador();
        Partida partida = new Partida();

        JugadorEntity jugadorEntity = new JugadorEntity();
        PartidaEntity partidaEntity = new PartidaEntity();

        when(modelMapper.map(jugador, JugadorEntity.class)).thenReturn(jugadorEntity);
        when(modelMapper.map(partida, PartidaEntity.class)).thenReturn(partidaEntity);

        ArgumentCaptor<EstadisticaEntity> captor = ArgumentCaptor.forClass(EstadisticaEntity.class);
        estadisticaService.registrarEvento(evento, jugador, partida);

        verify(estadisticaRepository).save(captor.capture());
        EstadisticaEntity saved = captor.getValue();

        assertEquals(jugadorEntity, saved.getJugador());
        assertEquals(partidaEntity, saved.getPartida());
        assertEquals(evento, saved.getEvento());
        assertEquals(LocalDate.now().toString(), saved.getFechaPartida());
    }

    @Test
    void allSaveEstadisticas() {
        EstadisticaServiceImpl spyService = spy(estadisticaService);

        Jugador jugador1 = new Jugador();
        Jugador jugador2 = new Jugador();
        Jugador jugador3 = new Jugador();

        List<Jugador> jugadores = Arrays.asList(jugador1, jugador2, jugador3);

        doNothing().when(spyService).saveEstadisticas(any(Jugador.class));
        spyService.allSaveEstadisticas(jugadores);

        verify(spyService, times(3)).saveEstadisticas(any(Jugador.class));
    }

    @Test
    void getEstadisticas() {
        Long idUsuario = 1L;

        JugadorEntity jugadorEntity = new JugadorEntity();
        when(jugadorRepository.findByUsuario_IdUsuario(idUsuario)).thenReturn(jugadorEntity);

        EstadisticaEntity estadistica1 = new EstadisticaEntity();
        EstadisticaEntity estadistica2 = new EstadisticaEntity();
        List<EstadisticaEntity> lista = Arrays.asList(estadistica1, estadistica2);
        when(estadisticaRepository.findByJugador(jugadorEntity)).thenReturn(lista);

        EstadisticaDto dto1 = EstadisticaDto.builder().build();
        EstadisticaDto dto2 = EstadisticaDto.builder().build();

        EstadisticaServiceImpl spyService = spy(estadisticaService);
        doReturn(dto1).when(spyService).convertirEstadistica(estadistica1);
        doReturn(dto2).when(spyService).convertirEstadistica(estadistica2);

        List<EstadisticaDto> resultado = spyService.getEstadisticas(idUsuario);

        assertEquals(2, resultado.size());
        assertTrue(resultado.contains(dto1));
        assertTrue(resultado.contains(dto2));

        verify(jugadorRepository).findByUsuario_IdUsuario(idUsuario);
        verify(estadisticaRepository).findByJugador(jugadorEntity);
        verify(spyService, times(2)).convertirEstadistica(any());

    }

    @Test
    void calcularPorcentajeMundo() {
        int paisesConquistados = 25;

        double resultado = estadisticaService.calcularPorcentajeMundo(paisesConquistados);

        assertEquals(50.0, resultado);
    }

    @Test
    void convertirEstadistica_deberiaRetornarDtoCorrectamente() {
        JugadorEntity jugador = new JugadorEntity();
        jugador.setColor(Color.ROJO);

        PartidaEntity partida = new PartidaEntity();
        partida.setIdPartida(10L);

        EstadisticaEntity estadistica = new EstadisticaEntity();
        estadistica.setIdEstadistica(1L);
        estadistica.setJugador(jugador);
        estadistica.setPartida(partida);
        estadistica.setFechaPartida("2025-07-05");
        estadistica.setPaisesConquistados(12);
        estadistica.setPaisesPerdidos(3);
        estadistica.setGanador(true);

        EstadisticaDto dto = estadisticaService.convertirEstadistica(estadistica);

        assertEquals(1L, dto.getId());
        assertEquals("ROJO", dto.getColorJugador());
        assertEquals(10L, dto.getIdPartida());
        assertEquals(LocalDate.of(2025, 7, 5), dto.getFecha());
        assertEquals(12, dto.getPaisesConquistados());
        assertEquals(3, dto.getPaisesPerdidos());
        assertTrue(dto.isGanador());
        assertEquals(estadisticaService.calcularPorcentajeMundo(12), dto.getPorcentajeMundo());
    }

    }






