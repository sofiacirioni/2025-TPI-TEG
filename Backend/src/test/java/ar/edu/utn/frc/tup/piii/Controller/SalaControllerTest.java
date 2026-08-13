package ar.edu.utn.frc.tup.piii.Controller;

import ar.edu.utn.frc.tup.piii.Dtos.SalaDto;
import ar.edu.utn.frc.tup.piii.Services.SalaService;
import ar.edu.utn.frc.tup.piii.Services.UsuarioService;
import ar.edu.utn.frc.tup.piii.models.Sala;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class SalaControllerTest {
    private SalaService salaService;
    private UsuarioService usuarioService;
    private ModelMapper modelMapper;
    private SalaController salaController;

    @BeforeEach
    void setUp() {
        salaService = mock(SalaService.class);
        usuarioService = mock(UsuarioService.class);
        modelMapper = mock(ModelMapper.class);

        salaController = new SalaController();
        salaController.salaService = salaService;
        salaController.usuarioService = usuarioService;
        salaController.modelMapper = modelMapper;
    }

    @Test
    void obtenerSala_salaNoEncontrada_NotFound() {
        when(salaService.obtenerSala(1L)).thenReturn(null);

        ResponseEntity<?> response = salaController.obtenerSala(1L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void obtenerSala_salaEncontrada_Ok() {
        Sala sala = new Sala();
        SalaDto salaDto = new SalaDto();

        when(salaService.obtenerSala(1L)).thenReturn(sala);
        when(modelMapper.map(sala, SalaDto.class)).thenReturn(salaDto);

        ResponseEntity<SalaDto> response = salaController.obtenerSala(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(salaDto, response.getBody());
    }

    @Test
    void obtenerSalaPorUrl_salaEncontrada_Ok() {
        String url = "url-existente";

        Sala sala = new Sala();
        SalaDto salaDto = new SalaDto();

        when(salaService.obtenerSala(url)).thenReturn(sala);
        when(modelMapper.map(sala, SalaDto.class)).thenReturn(salaDto);

        ResponseEntity<SalaDto> response = salaController.obtenerSalaPorUrl(url);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(salaDto, response.getBody());
    }

    @Test
    void obtenerSalaPorUrl_salaNoEncontrada_NotFound() {
        String urlInexistente = "url-no-existe";

        when(salaService.obtenerSala(urlInexistente)).thenReturn(null);

        ResponseEntity<?> response = salaController.obtenerSalaPorUrl(urlInexistente);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void enviarInicioPartida_devuelveDatosCorrectamente() {
        Map<String, String> inputData = new HashMap<>();
        inputData.put("mensaje", "¡Que comience!");

        Map<String, String> resultado = salaController.enviarInicioPartida(42L, inputData);

        assertEquals(inputData, resultado);
        assertEquals("¡Que comience!", resultado.get("mensaje"));
    }
}
