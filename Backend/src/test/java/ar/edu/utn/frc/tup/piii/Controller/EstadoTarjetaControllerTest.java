package ar.edu.utn.frc.tup.piii.Controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import ar.edu.utn.frc.tup.piii.Dtos.CanjeTarjetasDto;
import ar.edu.utn.frc.tup.piii.Dtos.EstadoTarjetaDto;
import ar.edu.utn.frc.tup.piii.Entities.EstadoTarjetaEntity;
import ar.edu.utn.frc.tup.piii.Services.EstadoTarjetaService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import jakarta.persistence.EntityNotFoundException;

import java.util.List;

public class EstadoTarjetaControllerTest {

    private MockMvc mockMvc;
    @Mock
    private EstadoTarjetaService estadoTarjetaService;
    @InjectMocks
    private EstadoTarjetaController controller;
    private ObjectMapper objectMapper;
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void asignarTarjeta_exito() throws Exception {
        EstadoTarjetaDto dto = new EstadoTarjetaDto();
        dto.setIdEstadoTarjeta(1L);
        dto.setIdJugador(1L);
        dto.setIdTurno(1L);

        mockMvc.perform(post("/api/v1/estado/tarjeta/asignar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(content().string("Tarjeta asignada exitosamente"));

        verify(estadoTarjetaService).asignarTarjeta(any(EstadoTarjetaDto.class));
    }

    @Test
    void asignarTarjeta_error() throws Exception {
        EstadoTarjetaDto dto = new EstadoTarjetaDto();
        dto.setIdEstadoTarjeta(1L);
        dto.setIdJugador(1L);
        dto.setIdTurno(1L);

        doThrow(new RuntimeException("Error de prueba")).when(estadoTarjetaService).asignarTarjeta(any());

        mockMvc.perform(post("/api/v1/estado/tarjeta/asignar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Error al asignar tarjeta: Error de prueba"));
    }

    @Test
    void obtenerEstados_exito() throws Exception {
        EstadoTarjetaEntity estado1 = new EstadoTarjetaEntity();
        EstadoTarjetaEntity estado2 = new EstadoTarjetaEntity();

        when(estadoTarjetaService.obtenerEstadoTarjeta()).thenReturn(List.of(estado1, estado2));

        mockMvc.perform(get("/api/v1/estado/tarjeta"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));

        verify(estadoTarjetaService).obtenerEstadoTarjeta();
    }

    @Test
    void obtenerEstados_error() throws Exception {
        when(estadoTarjetaService.obtenerEstadoTarjeta()).thenThrow(new RuntimeException("Error inesperado"));

        mockMvc.perform(get("/api/v1/estado/tarjeta"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void obtenerEstadoPorId_exito() throws Exception {
        EstadoTarjetaEntity estado = new EstadoTarjetaEntity();
        estado.setIdEstadoTarjeta(1L);

        when(estadoTarjetaService.obtenerPorId(1L)).thenReturn(estado);

        mockMvc.perform(get("/api/v1/estado/tarjeta/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idEstadoTarjeta").value(1));
    }

    @Test
    void obtenerEstadoPorId_noEncontrado() throws Exception {
        when(estadoTarjetaService.obtenerPorId(1L)).thenThrow(EntityNotFoundException.class);

        mockMvc.perform(get("/api/v1/estado/tarjeta/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    void eliminarEstado_exito() throws Exception {
        doNothing().when(estadoTarjetaService).eliminarEstadoTarjeta(1L);

        mockMvc.perform(delete("/api/v1/estado/tarjeta/1"))
                .andExpect(status().isOk())
                .andExpect(content().string("Estado eliminado exitosamente"));
    }

    @Test
    void eliminarEstado_noEncontrado() throws Exception {
        doThrow(EntityNotFoundException.class).when(estadoTarjetaService).eliminarEstadoTarjeta(1L);

        mockMvc.perform(delete("/api/v1/estado/tarjeta/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    void obtenerTarjeta_noEncontrada() throws Exception {
        when(estadoTarjetaService.obtenerTarjeta(1L)).thenReturn(null);

        mockMvc.perform(get("/api/v1/estado/tarjeta/tarjeta/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    void usarTarjeta_exito() throws Exception {
        doNothing().when(estadoTarjetaService).usarTarjetaEnPais(1L, 1L);

        mockMvc.perform(put("/api/v1/estado/tarjeta/1/usar/1"))
                .andExpect(status().isOk())
                .andExpect(content().string("Tarjeta usada exitosamente en el país."));
    }

    @Test
    void usarTarjeta_error() throws Exception {
        doThrow(new RuntimeException("Error al usar tarjeta")).when(estadoTarjetaService).usarTarjetaEnPais(1L, 1L);

        mockMvc.perform(put("/api/v1/estado/tarjeta/1/usar/1"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Error al usar la tarjeta: Error al usar tarjeta"));
    }

    @Test
    void canjearTarjetas_exito() throws Exception {
        CanjeTarjetasDto dto = new CanjeTarjetasDto();
        dto.setIdJugador(1L);
        dto.setIdTarjetas(List.of(10L, 11L, 12L));

        when(estadoTarjetaService.canjearTarjetas(dto.getIdTarjetas(), dto.getIdJugador())).thenReturn(7);

        mockMvc.perform(post("/api/v1/estado/tarjeta/canjear")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(content().string("Tarjetas canjeadas exitosamente. Ejércitos otorgados: 7"));
    }

    @Test
    void canjearTarjetas_error() throws Exception {
        CanjeTarjetasDto dto = new CanjeTarjetasDto();
        dto.setIdJugador(1L);
        dto.setIdTarjetas(List.of(10L, 11L, 12L));

        doThrow(new RuntimeException("Error en canje")).when(estadoTarjetaService).canjearTarjetas(dto.getIdTarjetas(), dto.getIdJugador());

        mockMvc.perform(post("/api/v1/estado/tarjeta/canjear")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Error al canjear tarjetas: Error en canje"));
    }
}
