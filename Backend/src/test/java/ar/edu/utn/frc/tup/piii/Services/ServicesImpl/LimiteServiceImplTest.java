package ar.edu.utn.frc.tup.piii.Services.ServicesImpl;

import ar.edu.utn.frc.tup.piii.Entities.LimiteEntity;
import ar.edu.utn.frc.tup.piii.Entities.PaisEntity;
import ar.edu.utn.frc.tup.piii.Repositories.LimiteRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LimiteServiceImplTest {

    @Mock
    private LimiteRepository limiteRepository;

    @InjectMocks
    private LimiteServiceImpl limiteService;

    @Test
    void obtenerTodos_debeDevolverListaDeLimites() {
        List<LimiteEntity> limites = List.of(new LimiteEntity(), new LimiteEntity());
        when(limiteRepository.findAll()).thenReturn(limites);

        List<LimiteEntity> resultado = limiteService.obtenerTodos();

        assertEquals(2, resultado.size());
        verify(limiteRepository, times(1)).findAll();
    }

    @Test
    void obtenerVecinos_debeDevolverVecinosCorrectos() {
        PaisEntity argentina = new PaisEntity();
        argentina.setIdPais(1L);
        PaisEntity chile = new PaisEntity();
        chile.setIdPais(2L);
        PaisEntity brasil = new PaisEntity();
        brasil.setIdPais(3L);

        LimiteEntity lim1 = new LimiteEntity(1L, argentina, chile);
        LimiteEntity lim2 = new LimiteEntity(2L, brasil, argentina);
        LimiteEntity lim3 = new LimiteEntity(3L, chile, brasil);

        when(limiteRepository.findAll()).thenReturn(List.of(lim1, lim2, lim3));

        List<PaisEntity> vecinos = limiteService.obtenerVecinos(argentina);

        assertEquals(2, vecinos.size());
        assertTrue(vecinos.contains(chile));
        assertTrue(vecinos.contains(brasil));
    }
}