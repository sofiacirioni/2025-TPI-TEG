package ar.edu.utn.frc.tup.piii.Controller;

import ar.edu.utn.frc.tup.piii.Dtos.Login.UsuarioDto;
import ar.edu.utn.frc.tup.piii.Dtos.PartidaDto;
import ar.edu.utn.frc.tup.piii.Dtos.SalaDto;
import ar.edu.utn.frc.tup.piii.Entities.UsuarioEntity;
import ar.edu.utn.frc.tup.piii.Services.PartidaService;
import ar.edu.utn.frc.tup.piii.Services.UsuarioService;
import ar.edu.utn.frc.tup.piii.models.EstadoPartida;
import ar.edu.utn.frc.tup.piii.models.Partida;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
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
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class PartidaControllerTest {
    private MockMvc mockMvc;
    @Mock
    private PartidaService partidaService;
    @Mock
    private UsuarioService usuarioService;
    @InjectMocks
    private PartidaController partidaController;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private Partida partida;
    private UsuarioEntity usuario;
    private PartidaDto partidaDto;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(partidaController).build();
        objectMapper.findAndRegisterModules();

        partida = new Partida();
        partida.setIdPartida(1L);
        partida.setEstadoPartida(EstadoPartida.EN_JUEGO);
        partida.setFechaInicio(LocalDate.now());

        usuario = new UsuarioEntity();
        usuario.setIdUsuario(1L);
        usuario.setUsuario("Usuario Test");
        usuario.setCorreo("test@example.com");
        usuario.setContrasenia("Password1@");
        usuario.setImagen("avatar.png");

        partidaDto = new PartidaDto();
        partidaDto.setIdPartida(1L);
        partidaDto.setEstado(EstadoPartida.EN_JUEGO);
        partidaDto.setFechaInicio(LocalDate.now());


    }

    @Test
    void listarPartidasDisponibles_deberiaRetornarListaDePartidas() throws Exception {
        List<Partida> partidas = Arrays.asList(partida);
        when(partidaService.listarPartidasDisponibles()).thenReturn(partidas);

        mockMvc.perform(get("/api/v1/partida/disponibles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].idPartida").value(1L));

        verify(partidaService).listarPartidasDisponibles();
    }

    @Test
    void unirseAPartida_deberiaRetornarPartida() throws Exception {

        Long idPartida = 1L;
        Long idUsuario = 1L;

        when(partidaService.unirseAPartida(idUsuario, idPartida)).thenReturn(partida);

        mockMvc.perform(post("/api/v1/partida/{idPartida}/unirse/{idUsuario}", idPartida, idUsuario))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idPartida").value(1L));

        verify(partidaService).unirseAPartida(idUsuario, idPartida);
    }

    @Test
    void cargarPartida_deberiaRetornarPartidaDto() throws Exception {
        Long idPartida = 1L;
        when(partidaService.cargarPartida(idPartida)).thenReturn(partidaDto);

        mockMvc.perform(get("/api/v1/partida/{id}", idPartida))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idPartida").value(1L))
                .andExpect(jsonPath("$.estado").value("EN_JUEGO"));

        verify(partidaService).cargarPartida(idPartida);
    }

    @Test
    void cargarPartidaBySala_deberiaRetornarPartidaDto() throws Exception {
        String url = "sala-test";
        Long idUsuario = 1L;

        when(partidaService.cargarPartidaBySala(url, idUsuario)).thenReturn(partidaDto);

        mockMvc.perform(get("/api/v1/partida/url/{url}", url)
                        .param("idUsuario", idUsuario.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idPartida").value(1L));

        verify(partidaService).cargarPartidaBySala(url, idUsuario);
    }

    @Test
    void obtenerEstadoPartida_deberiaRetornarPartida() throws Exception {
        Long idPartida = 1L;
        when(partidaService.obtenerEstadoPartida(idPartida)).thenReturn(partida);

        mockMvc.perform(get("/api/v1/partida/{idPartida}/estado", idPartida))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idPartida").value(1L));

        verify(partidaService).obtenerEstadoPartida(idPartida);
    }

    @Test
    void finalizarPartida_deberiaRetornarOk() throws Exception {
        when(partidaService.finalizarPartida(anyLong())).thenReturn(true);

        mockMvc.perform(post("/api/v1/partida/1/finalizar"))
                .andExpect(status().isOk());

        verify(partidaService).finalizarPartida(1L);
    }

    @Test
    void finalizarPartida_conError_deberiaRetornarInternalServerError() throws Exception {
        when(partidaService.finalizarPartida(anyLong())).thenReturn(false);

        mockMvc.perform(post("/api/v1/partida/1/finalizar"))
                .andExpect(status().isInternalServerError());

        verify(partidaService).finalizarPartida(1L);
    }

    @Test
    void accederAPartidaPorUrl_deberiaRetornarPartidaDto() throws Exception {
        String url = "sala-test";
        Long idUsuario = 1L;

        when(partidaService.obtenerPartidaActivaPorUrlYUsuario(url, idUsuario)).thenReturn(partidaDto);

        mockMvc.perform(get("/api/v1/partida/sala")
                        .param("url", url)
                        .param("idUsuario", idUsuario.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idPartida").value(1L))
                .andExpect(jsonPath("$.estado").value("EN_JUEGO"));

        verify(partidaService).obtenerPartidaActivaPorUrlYUsuario(url, idUsuario);
    }

    @Test
    void guardarPartida_datosValidos_deberiaRetornarPartidaGuardada() throws Exception {

        UsuarioDto usuarioDto = new UsuarioDto();
        usuarioDto.setIdUsuario(1L);

        SalaDto salaDto = new SalaDto();
        salaDto.setIdSala(1L);
        salaDto.setNombreSala("Hola");
        salaDto.setUrl("hola");
        salaDto.setCreador(usuarioDto);

        PartidaDto partidaDto = new PartidaDto();
        partidaDto.setIdPartida(1L);
        partidaDto.setEstado(EstadoPartida.PAUSADA);
        partidaDto.setFechaInicio(LocalDate.now());
        partidaDto.setEstadoPaises(List.of());
        partidaDto.setJugadores(List.of());
        partidaDto.setTurnos(List.of());
        partidaDto.setEstadoTarjetas(List.of());
        partidaDto.setTurnoActual(0);
        partidaDto.setConfiguracion(salaDto);

        when(partidaService.guardarPartida(any(Partida.class))).thenReturn(partida);

        mockMvc.perform(put("/api/v1/partida/{idPartida}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(partidaDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idPartida").value(1L));

        verify(partidaService).guardarPartida(any(Partida.class));
    }

    @Test
    void guardarPartida_conErroresDeValidacion_deberiaRetornarBadRequest() throws Exception {
        PartidaDto dtoInvalido = new PartidaDto();

        mockMvc.perform(put("/api/v1/partida/{idPartida}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dtoInvalido)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void guardarPartida_idNoCoincide_deberiaRetornarBadRequest() throws Exception {
        partidaDto.setIdPartida(99L);

        mockMvc.perform(put("/api/v1/partida/{idPartida}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(partidaDto)))
                .andExpect(status().isBadRequest());
    }
}