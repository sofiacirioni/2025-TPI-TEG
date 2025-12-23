package ar.edu.utn.frc.tup.piii.Controller;

import ar.edu.utn.frc.tup.piii.Services.JuegoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JuegoControllerTest {
    @Mock
    private JuegoService juegoService;
    @InjectMocks
    private JuegoController juegoController;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testRepartirPaisesYObjetivos() {
        Long idPartida = 1L;

        doNothing().when(juegoService).repartirPaisesYObjetivos(idPartida);

        ResponseEntity<String> response = juegoController.repartirPaisesYObjetivos(idPartida);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Países y objetivos repartidos", response.getBody());

        verify(juegoService, times(1)).repartirPaisesYObjetivos(idPartida);
    }

    @Test
    void testIniciarColocacionEjercitos() {
        Long idPartida = 2L;

        doNothing().when(juegoService).iniciarColocacionEjercitos(idPartida);

        ResponseEntity<String> response = juegoController.iniciarColocacionEjercitos(idPartida);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Colocación de ejércitos iniciada", response.getBody());

        verify(juegoService, times(1)).iniciarColocacionEjercitos(idPartida);
    }

    @Test
    void testAvanzarFase() {
        Long idPartida = 3L;

        doNothing().when(juegoService).avanzarFase(idPartida);

        ResponseEntity<String> response = juegoController.avanzarFase(idPartida);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Fase avanzada", response.getBody());

        verify(juegoService, times(1)).avanzarFase(idPartida);
    }

    @Test
    void testComprobarObjetivoCumplido() {
        Long idPartida = 4L;
        boolean resultado = true;

        when(juegoService.comprobarObjetivoCumplido(idPartida)).thenReturn(resultado);

        ResponseEntity<Boolean> response = juegoController.comprobarObjetivoCumplido(idPartida);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(Boolean.TRUE, response.getBody());

        verify(juegoService, times(1)).comprobarObjetivoCumplido(idPartida);
    }
}