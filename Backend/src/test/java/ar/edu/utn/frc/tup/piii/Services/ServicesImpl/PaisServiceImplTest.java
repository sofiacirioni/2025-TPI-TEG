package ar.edu.utn.frc.tup.piii.Services.ServicesImpl;

import ar.edu.utn.frc.tup.piii.Entities.PaisEntity;
import ar.edu.utn.frc.tup.piii.Repositories.PaisRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaisServiceImplTest {
    @Mock
    private PaisRepository paisRepository;

    @InjectMocks
    private PaisServiceImpl paisService;

    @Test
    void obtenerTodos_deberiaDevolverListaDePaises() {
        PaisEntity p1 = new PaisEntity();
        p1.setIdPais(1L);
        p1.setNombre("Argentina");

        PaisEntity p2 = new PaisEntity();
        p2.setIdPais(2L);
        p2.setNombre("Brasil");

        when(paisRepository.findAll()).thenReturn(List.of(p1, p2));

        List<PaisEntity> resultado = paisService.obtenerTodos();

        assertEquals(2, resultado.size());
        assertEquals("Argentina", resultado.get(0).getNombre());
        verify(paisRepository, times(1)).findAll();
    }

    @Test
    void buscarPorId_deberiaDevolverPaisCuandoExiste() {
        PaisEntity pais = new PaisEntity();
        pais.setIdPais(10L);
        pais.setNombre("Chile");

        when(paisRepository.findById(10L)).thenReturn(Optional.of(pais));

        Optional<PaisEntity> resultado = paisService.buscarPorId(10L);

        assertTrue(resultado.isPresent());
        assertEquals("Chile", resultado.get().getNombre());
        verify(paisRepository, times(1)).findById(10L);
    }

    @Test
    void buscarPorId_deberiaDevolverOptionalVacioCuandoNoExiste() {
        when(paisRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<PaisEntity> resultado = paisService.buscarPorId(99L);

        assertTrue(resultado.isEmpty());
        verify(paisRepository, times(1)).findById(99L);
    }

}