package ar.edu.utn.frc.tup.piii.Controller;

import ar.edu.utn.frc.tup.piii.Dtos.EstadoPaisDto;
import ar.edu.utn.frc.tup.piii.Services.EstadoPaisService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class EstadoPaisControllerTest {

    private MockMvc mockMvc;
    private EstadoPaisService estadoPaisService;
    private EstadoPaisController estadoPaisController;

    @BeforeEach
    void setUp() {
        estadoPaisService = mock(EstadoPaisService.class);
        estadoPaisController = new EstadoPaisController(estadoPaisService);
        mockMvc = MockMvcBuilders.standaloneSetup(estadoPaisController).build();
    }

    @Test
    void testGetEstadoPais() throws Exception {
        EstadoPaisDto estadoDto = new EstadoPaisDto();
        estadoDto.setId(1L);
        estadoDto.setCantidadTropas(5);

        when(estadoPaisService.getEstadoPais(1L)).thenReturn(estadoDto);

        mockMvc.perform(get("/api/v1/estado/pais/1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.cantidadTropas").value(5));
    }

    @Test
    void testCreateEstadoPais_Success() throws Exception {
        EstadoPaisDto inputDto = new EstadoPaisDto();
        inputDto.setId(1L);
        inputDto.setCantidadTropas(5);

        EstadoPaisDto resultDto = new EstadoPaisDto();
        resultDto.setId(1L);
        resultDto.setCantidadTropas(5);

        when(estadoPaisService.createEstadoPais(any(EstadoPaisDto.class))).thenReturn(resultDto);

        mockMvc.perform(post("/api/v1/estado/pais")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(inputDto)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.cantidadTropas").value(5));
    }

    @Test
    void testUpdateEstadoPais_Success() throws Exception {
        EstadoPaisDto inputDto = new EstadoPaisDto();
        inputDto.setId(1L);
        inputDto.setCantidadTropas(10);

        EstadoPaisDto updatedDto = new EstadoPaisDto();
        updatedDto.setId(1L);
        updatedDto.setCantidadTropas(10);

        when(estadoPaisService.updateEstadoPais(any(EstadoPaisDto.class))).thenReturn(updatedDto);

        mockMvc.perform(put("/api/v1/estado/pais")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(inputDto)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.cantidadTropas").value(10));
    }

    @Test
    void testGetLimitesEstadoPais_Success() throws Exception {
        EstadoPaisDto limite1 = new EstadoPaisDto();
        limite1.setId(2L);
        limite1.setCantidadTropas(3);

        EstadoPaisDto limite2 = new EstadoPaisDto();
        limite2.setId(3L);
        limite2.setCantidadTropas(4);

        List<EstadoPaisDto> limites = Arrays.asList(limite1, limite2);
        when(estadoPaisService.getLimitesEstadoPais(1L)).thenReturn(limites);

        mockMvc.perform(get("/api/v1/estado/pais/limites/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(2))
                .andExpect(jsonPath("$[0].cantidadTropas").value(3))
                .andExpect(jsonPath("$[1].id").value(3))
                .andExpect(jsonPath("$[1].cantidadTropas").value(4));
    }

}
