package ar.edu.utn.frc.tup.piii.Services.ServicesImpl;
import ar.edu.utn.frc.tup.piii.Entities.SalaEntity;
import ar.edu.utn.frc.tup.piii.Repositories.SalaRepository;
import ar.edu.utn.frc.tup.piii.models.EstadoSala;
import ar.edu.utn.frc.tup.piii.models.Sala;
import ar.edu.utn.frc.tup.piii.models.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;

import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
public class SalaServiceImplTest {
    private SalaRepository salaRepository;
    private ModelMapper modelMapper;
    private SalaServiceImpl salaService;

    @BeforeEach
    void setUp() {
        salaRepository = mock(SalaRepository.class);
        modelMapper = mock(ModelMapper.class);
        salaService = new SalaServiceImpl();
        salaService.salaRepository = salaRepository;
        salaService.modelMapper = modelMapper;
    }

    @Test
    void crearSala_SalaExistente_Null() {
        Usuario creador = new Usuario();
        creador.setIdUsuario(1L);

        Sala sala = new Sala();

        when(salaRepository.findByCreadorIdUsuario(creador.getIdUsuario()))
                .thenReturn(Optional.of(new SalaEntity()));

        Sala result = salaService.crearSala(sala, creador, "NombreSala");

        assertNull(result);
    }

    @Test
    void crearSala_SalaNueva_Correcta() {
        Usuario creador = new Usuario();
        creador.setIdUsuario(1L);

        Sala sala = new Sala();
        sala.setJugadores(new ArrayList<>());

        when(salaRepository.findByCreadorIdUsuario(creador.getIdUsuario()))
                .thenReturn(Optional.empty());

        SalaEntity salaEntity = new SalaEntity();
        when(modelMapper.map(any(Sala.class), eq(SalaEntity.class)))
                .thenReturn(salaEntity);

        SalaEntity salaEntityGuardada = new SalaEntity();
        when(salaRepository.save(salaEntity))
                .thenReturn(salaEntityGuardada);

        Sala salaMapeada = new Sala();
        salaMapeada.setNombreSala("NombreSala");
        salaMapeada.setEstado(EstadoSala.ESPERANDO);
        salaMapeada.setUrl("https://miapp.com/sala/uuid-ejemplo");

        when(modelMapper.map(salaEntityGuardada, Sala.class))
                .thenReturn(salaMapeada);

        Sala result = salaService.crearSala(sala, creador, "NombreSala");

        assertNotNull(result);
        assertEquals(salaMapeada, result);
        assertEquals("NombreSala", result.getNombreSala());
        assertEquals(EstadoSala.ESPERANDO, result.getEstado());
        assertTrue(result.getUrl().startsWith("https://miapp.com/sala/"));
    }
    @Test
    void obtenerSala_SalaNoEncontrada_LanzaExcepcion() {
        when(salaRepository.findByIdSalaWithJugadores(1L))
                .thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> salaService.obtenerSala(1L));
    }

    @Test
    void obtenerSala_SalaExistente_Correcta() {
        SalaEntity salaEntity = new SalaEntity();
        Sala sala = new Sala();

        when(salaRepository.findByIdSalaWithJugadores(1L))
                .thenReturn(Optional.of(salaEntity));

        when(modelMapper.map(salaEntity, Sala.class))
                .thenReturn(sala);

        Sala result = salaService.obtenerSala(1L);

        assertNotNull(result);
        assertEquals(sala, result);
    }
    @Test
    void obtenerSala_PorUrl_SalaExistente_Correcta() {
        String url = "url-existente";

        SalaEntity salaEntity = new SalaEntity();
        Sala sala = new Sala();

        when(salaRepository.findByUrl(url)).thenReturn(Optional.of(salaEntity));
        when(modelMapper.map(salaEntity, Sala.class)).thenReturn(sala);

        Sala result = salaService.obtenerSala(url);

        assertNotNull(result);
        assertEquals(sala, result);
    }
}
