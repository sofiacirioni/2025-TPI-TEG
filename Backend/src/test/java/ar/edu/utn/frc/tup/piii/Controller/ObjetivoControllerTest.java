package ar.edu.utn.frc.tup.piii.Controller;

import ar.edu.utn.frc.tup.piii.Dtos.ObjetivoDto;
import ar.edu.utn.frc.tup.piii.Services.ObjetivoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@ExtendWith(MockitoExtension.class)
class ObjetivoControllerTest {

    @InjectMocks
    private ObjetivoController objetivoController;

    @Mock
    private ObjetivoService objetivoService;

    private MockMvc mockMvc;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(objetivoController).build();
    }

    @Test
    void testGetObjetivoGeneral() throws Exception {
        ObjetivoDto dto = ObjetivoDto.builder()
                .id(1L)
                .descripcion("Objetivo general")
                .build();

        when(objetivoService.obtenerObjetivoGral()).thenReturn(dto);

        mockMvc.perform(get("/api/v1/objetivos/general"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.descripcion").value("Objetivo general"));
    }

    @Test
    void testGetObjetivosSecretos() throws Exception {
        List<ObjetivoDto> lista = List.of(
                ObjetivoDto.builder().id(1L).descripcion("Secreto 1").build(),
                ObjetivoDto.builder().id(2L).descripcion("Secreto 2").build()
        );

        when(objetivoService.obtenerObjetivosSecretos()).thenReturn(lista);

        mockMvc.perform(get("/api/v1/objetivos/secretos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].descripcion").value("Secreto 1"))
                .andExpect(jsonPath("$[1].id").value(2L))
                .andExpect(jsonPath("$[1].descripcion").value("Secreto 2"));
    }
    @Test
    void testGetObjetivoById() throws Exception {
        Long objetivoId = 10L;
        ObjetivoDto dto = ObjetivoDto.builder()
                .id(objetivoId)
                .descripcion("Objetivo por ID")
                .build();

        when(objetivoService.obtenerObjetivoById(objetivoId)).thenReturn(dto);

        mockMvc.perform(get("/api/v1/objetivos/{id}", objetivoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(objetivoId))
                .andExpect(jsonPath("$.descripcion").value("Objetivo por ID"));
    }
}