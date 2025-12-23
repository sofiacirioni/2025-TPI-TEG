package ar.edu.utn.frc.tup.piii.Controller;

import ar.edu.utn.frc.tup.piii.Dtos.EstadisticaDto;
import ar.edu.utn.frc.tup.piii.Services.EstadisticaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class EstadisticaControllerTest {

    private MockMvc mockMvc;

    @Mock
    private EstadisticaService estadisticaService;

    @InjectMocks
    private EstadisticaController estadisticaController;

    @BeforeEach
    void setUp() {

        mockMvc = MockMvcBuilders
                .standaloneSetup(estadisticaController)
                .build();
    }

    @Test
    @DisplayName("200 OK + JSON cuando hay estadísticas")
    void cuandoHayEstadisticas_entonces200() throws Exception {
        Long userId = 5L;
        EstadisticaDto dto1 = EstadisticaDto.builder()
                .id(1L)
                .colorJugador("Rojo")
                .idPartida(10L)
                .fecha(LocalDate.of(2025,6,30))
                .paisesConquistados(8)
                .paisesPerdidos(2)
                .porcentajeMundo(20.5)
                .ganador(true)
                .build();
        EstadisticaDto dto2 = EstadisticaDto.builder()
                .id(2L)
                .colorJugador("Azul")
                .idPartida(11L)
                .fecha(LocalDate.of(2025,7,1))
                .paisesConquistados(5)
                .paisesPerdidos(5)
                .porcentajeMundo(12.0)
                .ganador(false)
                .build();

        when(estadisticaService.getEstadisticas(userId))
                .thenReturn(Arrays.asList(dto1, dto2));

        mockMvc.perform(get("/api/v1/estadisticas/usuario/{id}", userId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].colorJugador", is("Rojo")))
                .andExpect(jsonPath("$[1].idPartida", is(11)));
    }

    @Test
    @DisplayName("404 Not Found cuando la lista está vacía")
    void cuandoListaVacia_entonces404() throws Exception {
        Long userId = 7L;
        when(estadisticaService.getEstadisticas(userId))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/v1/estadisticas/usuario/{id}", userId))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("500 Internal Server Error si el servicio lanza excepción")
    void cuandoServicioError_entonces500() throws Exception {
        Long userId = 9L;
        when(estadisticaService.getEstadisticas(userId))
                .thenThrow(new RuntimeException("falla inesperada"));

        mockMvc.perform(get("/api/v1/estadisticas/usuario/{id}", userId))
                .andExpect(status().isInternalServerError());
    }
}