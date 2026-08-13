package ar.edu.utn.frc.tup.piii.Services.ServicesImpl;

import ar.edu.utn.frc.tup.piii.Dtos.ObjetivoDto;
import ar.edu.utn.frc.tup.piii.Entities.*;
import ar.edu.utn.frc.tup.piii.Repositories.EstadoPaisRepository;
import ar.edu.utn.frc.tup.piii.Repositories.JugadorRepository;
import ar.edu.utn.frc.tup.piii.Repositories.ObjetivoRepository;
import ar.edu.utn.frc.tup.piii.Repositories.PartidaRepository;
import ar.edu.utn.frc.tup.piii.Services.LimiteService;
import ar.edu.utn.frc.tup.piii.models.Color;
import ar.edu.utn.frc.tup.piii.models.TipoObjetivo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

@ExtendWith(MockitoExtension.class)
class ObjetivoServiceImplTest {
    @InjectMocks
    private ObjetivoServiceImpl objetivoService;

    @Mock
    private ObjetivoRepository objetivoRepository;

    @Mock
    private EstadoPaisRepository estadoPaisRepository;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private JugadorRepository jugadorRepository;

    @Mock
    private PartidaRepository partidaRepository;

    @Test
    void asignarObjetivosSecretos_DeberiaAsignarCorrectamente() {
        ObjetivoEntity obj1 = new ObjetivoEntity(1L, "Objetivo 1", TipoObjetivo.SECRETO, 0, 0, 0, 0, 0, 0, 0, 0, null);
        ObjetivoEntity obj2 = new ObjetivoEntity(2L, "Objetivo 2", TipoObjetivo.SECRETO, 0, 0, 0, 0, 0, 0, 0, 0, null);

        List<ObjetivoEntity> objetivos = new ArrayList<>(List.of(obj1, obj2));

        JugadorEntity jugador1 = new JugadorEntity();
        JugadorEntity jugador2 = new JugadorEntity();

        List<JugadorEntity> jugadores = List.of(jugador1, jugador2);

        when(objetivoRepository.findByTipoObjetivo(TipoObjetivo.SECRETO)).thenReturn(objetivos);

        objetivoService.asignarObjetivosSecretos(jugadores);

        assertNotNull(jugador1.getObjetivo(), "El jugador 1 debería tener un objetivo asignado.");
        assertNotNull(jugador2.getObjetivo(), "El jugador 2 debería tener un objetivo asignado.");
        verify(jugadorRepository, times(2)).save(any(JugadorEntity.class));
    }

    @Test
    void asignarObjetivosSecretos_DeberiaFallarPorFaltaDeObjetivos() {
        List<ObjetivoEntity> pocosObjetivos = List.of(
                new ObjetivoEntity(1L, "Objetivo 1", TipoObjetivo.SECRETO, 0, 0, 0, 0, 0, 0, 0, 0, null)
        );

        JugadorEntity jugador1 = new JugadorEntity();
        JugadorEntity jugador2 = new JugadorEntity();

        List<JugadorEntity> jugadores = List.of(jugador1, jugador2);

        when(objetivoRepository.findByTipoObjetivo(TipoObjetivo.SECRETO)).thenReturn(pocosObjetivos);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                objetivoService.asignarObjetivosSecretos(jugadores));

        assertEquals("500 INTERNAL_SERVER_ERROR \"No hay suficientes objetivos para asignar a jugadores.\"", ex.getMessage());
        verify(jugadorRepository, never()).save(any());
    }

    @Test
    void verificarObjetivos_jugadorNoExiste_lanzaExcepcion() {
        Long idJugador = 1L;
        Mockito.when(jugadorRepository.findById(idJugador)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> objetivoService.verificarObjetivos(idJugador));
    }

    @Test
    void verificarObjetivos_destruccionJugadorEliminado_devuelveVictoria() {
        Long idJugador = 1L;
        JugadorEntity jugador = new JugadorEntity();
        jugador.setIdJugador(idJugador);
        jugador.setObjetivo(new ObjetivoEntity());
        jugador.getObjetivo().setColorEnemigo(Color.ROJO);

        PartidaEntity partida = new PartidaEntity();
        JugadorEntity enemigo = new JugadorEntity();
        enemigo.setColor(Color.ROJO);
        enemigo.setPerdio(true);
        partida.setJugadores(List.of(enemigo));
        jugador.setPartida(partida);

        Mockito.when(jugadorRepository.findById(idJugador)).thenReturn(Optional.of(jugador));
        Mockito.when(estadoPaisRepository.findEstadoPaisEntitiesByJugador_IdJugador(idJugador)).thenReturn(List.of());
        Mockito.when(objetivoRepository.findById(Mockito.any())).thenReturn(Optional.of(jugador.getObjetivo()));
        Mockito.when(partidaRepository.findById(Mockito.any())).thenReturn(Optional.of(partida));

        var resultado = objetivoService.verificarObjetivos(idJugador);
        assertTrue(resultado.isGano());
    }

    @Test
    void verificarObjetivos_alGanar_noMarcaAlVencedorComoDerrotado() {
        // El cierre de partida marcaba `perdio` para todos, vencedor incluido, y
        // la pizarra de estadísticas lo mostraba tachado junto a los eliminados.
        Long idGanador = 1L;
        JugadorEntity ganador = new JugadorEntity();
        ganador.setIdJugador(idGanador);
        ganador.setColor(Color.AZUL);
        ganador.setObjetivo(new ObjetivoEntity());
        ganador.getObjetivo().setColorEnemigo(Color.ROJO);

        JugadorEntity enemigo = new JugadorEntity();
        enemigo.setIdJugador(2L);
        enemigo.setColor(Color.ROJO);
        enemigo.setPerdio(true);

        JugadorEntity tercero = new JugadorEntity();
        tercero.setIdJugador(3L);
        tercero.setColor(Color.VERDE);

        PartidaEntity partida = new PartidaEntity();
        partida.setJugadores(List.of(ganador, enemigo, tercero));
        ganador.setPartida(partida);

        Mockito.when(jugadorRepository.findById(idGanador)).thenReturn(Optional.of(ganador));
        Mockito.when(estadoPaisRepository.findEstadoPaisEntitiesByJugador_IdJugador(idGanador))
                .thenReturn(List.of());
        Mockito.when(objetivoRepository.findById(Mockito.any())).thenReturn(Optional.of(ganador.getObjetivo()));
        Mockito.when(partidaRepository.findById(Mockito.any())).thenReturn(Optional.of(partida));

        assertTrue(objetivoService.verificarObjetivos(idGanador).isGano());

        assertFalse(ganador.isPerdio(), "el vencedor no puede figurar como derrotado");
        assertTrue(tercero.isPerdio(), "los demás comandantes sí quedan derrotados");
    }


    @Test
    void obtenerObjetivoGral_noEncontrado_lanzaException() {
        when(objetivoRepository.findFirstByTipoObjetivo(TipoObjetivo.GENERAL)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> {
            objetivoService.obtenerObjetivoGral();
        });

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, ex.getStatusCode());
    }

    @Test
    void testObtenerObjetivoGral() {
        ObjetivoEntity entity = new ObjetivoEntity();
        entity.setId(1L);
        entity.setDescripcion("General");

        ObjetivoDto dto = ObjetivoDto.builder()
                .id(1L)
                .descripcion("General")
                .build();

        when(objetivoRepository.findFirstByTipoObjetivo(TipoObjetivo.GENERAL))
                .thenReturn(Optional.of(entity));

        when(modelMapper.map(entity, ObjetivoDto.class)).thenReturn(dto);

        ObjetivoDto resultado = objetivoService.obtenerObjetivoGral();

        assertEquals("General", resultado.getDescripcion());
        assertEquals(1L, resultado.getId());
    }

    @Test
    void testObtenerObjetivoById() {
        ObjetivoEntity entity = new ObjetivoEntity();
        entity.setId(99L);
        entity.setDescripcion("Objetivo 99");

        ObjetivoDto dto = ObjetivoDto.builder()
                .id(99L)
                .descripcion("Objetivo 99")
                .build();

        when(objetivoRepository.findById(99L)).thenReturn(Optional.of(entity));
        when(modelMapper.map(entity, ObjetivoDto.class)).thenReturn(dto);

        ObjetivoDto resultado = objetivoService.obtenerObjetivoById(99L);

        assertEquals(99L, resultado.getId());
        assertEquals("Objetivo 99", resultado.getDescripcion());
    }

    @Test
    void testObtenerObjetivosSecretos() {
        ObjetivoEntity e1 = new ObjetivoEntity();
        e1.setId(1L);
        e1.setDescripcion("Secreto 1");

        ObjetivoEntity e2 = new ObjetivoEntity();
        e2.setId(2L);
        e2.setDescripcion("Secreto 2");

        ObjetivoDto dto1 = ObjetivoDto.builder().id(1L).descripcion("Secreto 1").build();
        ObjetivoDto dto2 = ObjetivoDto.builder().id(2L).descripcion("Secreto 2").build();

        when(objetivoRepository.findByTipoObjetivo(TipoObjetivo.SECRETO))
                .thenReturn(List.of(e1, e2));

        when(modelMapper.map(e1, ObjetivoDto.class)).thenReturn(dto1);
        when(modelMapper.map(e2, ObjetivoDto.class)).thenReturn(dto2);

        List<ObjetivoDto> resultado = objetivoService.obtenerObjetivosSecretos();

        assertEquals(2, resultado.size());
        assertEquals("Secreto 1", resultado.get(0).getDescripcion());
        assertEquals("Secreto 2", resultado.get(1).getDescripcion());
    }

    @Test
    void obtenerObjetivoById_noEncontrado_lanzaException() {
        when(objetivoRepository.findById(99L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> {
            objetivoService.obtenerObjetivoById(99L);
        });

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, ex.getStatusCode());
    }
}




