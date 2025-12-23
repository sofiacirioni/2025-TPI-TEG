package ar.edu.utn.frc.tup.piii.Services.ServicesImpl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.*;

import ar.edu.utn.frc.tup.piii.Dtos.ObjetivoDto;
import ar.edu.utn.frc.tup.piii.Dtos.VerificacionObjetivoDto;
import ar.edu.utn.frc.tup.piii.Entities.EstadoPaisEntity;
import ar.edu.utn.frc.tup.piii.Entities.JugadorEntity;
import ar.edu.utn.frc.tup.piii.Entities.ObjetivoEntity;
import ar.edu.utn.frc.tup.piii.Repositories.EstadoPaisRepository;
import ar.edu.utn.frc.tup.piii.Repositories.JugadorRepository;
import ar.edu.utn.frc.tup.piii.Repositories.ObjetivoRepository;
import ar.edu.utn.frc.tup.piii.Repositories.PartidaRepository;
import ar.edu.utn.frc.tup.piii.models.Color;
import ar.edu.utn.frc.tup.piii.models.TipoObjetivo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class ObjetivoServiceTest {

    @Mock
    private ObjetivoRepository objetivoRepository;

    @Mock
    private JugadorRepository jugadorRepository;

    @Mock
    private EstadoPaisRepository estadoPaisRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private ObjetivoServiceImpl objetivoService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testObtenerObjetivosSecretos_DevuelveLista() {
        ObjetivoEntity obj1 = new ObjetivoEntity();
        ObjetivoEntity obj2 = new ObjetivoEntity();
        List<ObjetivoEntity> secretos = Arrays.asList(obj1, obj2);

        when(objetivoRepository.findByTipoObjetivo(TipoObjetivo.SECRETO)).thenReturn(secretos);
        when(modelMapper.map(any(), eq(ObjetivoDto.class))).thenReturn(new ObjetivoDto());

        List<ObjetivoDto> resultado = objetivoService.obtenerObjetivosSecretos();

        assertEquals(2, resultado.size());
        verify(objetivoRepository).findByTipoObjetivo(TipoObjetivo.SECRETO);
    }

    @Test
    public void testObtenerObjetivoGral_Existente() {
        ObjetivoEntity objetivo = new ObjetivoEntity();
        ObjetivoDto dto = new ObjetivoDto();

        when(objetivoRepository.findFirstByTipoObjetivo(TipoObjetivo.GENERAL)).thenReturn(Optional.of(objetivo));
        when(modelMapper.map(objetivo, ObjetivoDto.class)).thenReturn(dto);

        ObjetivoDto resultado = objetivoService.obtenerObjetivoGral();

        assertNotNull(resultado);
        verify(objetivoRepository).findFirstByTipoObjetivo(TipoObjetivo.GENERAL);
    }

    @Test
    public void testObtenerObjetivoGral_NoEncontrado() {
        when(objetivoRepository.findFirstByTipoObjetivo(TipoObjetivo.GENERAL)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> {
            objetivoService.obtenerObjetivoGral();
        });

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, ex.getStatusCode());
        assertTrue(ex.getReason().contains("Objetivo general no encontrado"));
    }

    @Test
    public void testObtenerObjetivoById_Existente() {
        ObjetivoEntity objetivo = new ObjetivoEntity();
        ObjetivoDto dto = new ObjetivoDto();

        when(objetivoRepository.findById(1L)).thenReturn(Optional.of(objetivo));
        when(modelMapper.map(objetivo, ObjetivoDto.class)).thenReturn(dto);

        ObjetivoDto resultado = objetivoService.obtenerObjetivoById(1L);

        assertNotNull(resultado);
        verify(objetivoRepository).findById(1L);
    }

    @Test
    public void testObtenerObjetivoById_NoEncontrado() {
        when(objetivoRepository.findById(1L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> {
            objetivoService.obtenerObjetivoById(1L);
        });

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, ex.getStatusCode());
        assertTrue(ex.getReason().contains("Objetivo no encontrado"));
    }

    @Test
    public void testAsignarObjetivosSecretos_SuficientesObjetivos() {
        JugadorEntity jugador1 = new JugadorEntity();
        jugador1.setColor(Color.ROJO);

        JugadorEntity jugador2 = new JugadorEntity();
        jugador2.setColor(Color.AZUL);

        List<JugadorEntity> jugadores = Arrays.asList(jugador1, jugador2);

        ObjetivoEntity obj1 = new ObjetivoEntity();
        obj1.setColorEnemigo(Color.AZUL);

        ObjetivoEntity obj2 = new ObjetivoEntity();
        obj2.setColorEnemigo(null);

        List<ObjetivoEntity> objetivos = Arrays.asList(obj1, obj2);

        when(objetivoRepository.findByTipoObjetivo(TipoObjetivo.SECRETO)).thenReturn(objetivos);
        when(jugadorRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);

        objetivoService.asignarObjetivosSecretos(jugadores);

        verify(jugadorRepository, times(2)).save(any(JugadorEntity.class));
    }

    @Test
    public void testAsignarObjetivosSecretos_NoSuficientesObjetivos() {
        List<JugadorEntity> jugadores = Arrays.asList(new JugadorEntity(), new JugadorEntity());

        List<ObjetivoEntity> objetivos = Collections.singletonList(new ObjetivoEntity());

        when(objetivoRepository.findByTipoObjetivo(TipoObjetivo.SECRETO)).thenReturn(objetivos);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> {
            objetivoService.asignarObjetivosSecretos(jugadores);
        });

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, ex.getStatusCode());
        assertTrue(ex.getReason().contains("No hay suficientes objetivos"));
    }

}
