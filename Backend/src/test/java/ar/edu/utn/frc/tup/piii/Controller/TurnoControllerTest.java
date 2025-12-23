package ar.edu.utn.frc.tup.piii.Controller;

import ar.edu.utn.frc.tup.piii.Dtos.CanjeTarjetasDto;
import ar.edu.utn.frc.tup.piii.Dtos.EstadoPaises.AgregarFichas;
import ar.edu.utn.frc.tup.piii.Dtos.EstadoPaises.EstadoPaisFicha;
import ar.edu.utn.frc.tup.piii.Dtos.EstadoPaises.Ataque;
import ar.edu.utn.frc.tup.piii.Dtos.EstadoPaises.AtaqueResponseDto;
import ar.edu.utn.frc.tup.piii.Dtos.EstadoPaises.MoverFichas;
import ar.edu.utn.frc.tup.piii.Dtos.TarjetaDto;
import ar.edu.utn.frc.tup.piii.Dtos.UsarTarjetaEnPaisDto;
import ar.edu.utn.frc.tup.piii.Dtos.VerificacionObjetivoDto;
import ar.edu.utn.frc.tup.piii.models.Jugador;
import ar.edu.utn.frc.tup.piii.models.Pais;
import ar.edu.utn.frc.tup.piii.models.Turno;
import ar.edu.utn.frc.tup.piii.Services.TurnoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
@ExtendWith(MockitoExtension.class)
class TurnoControllerTest {

    private static final String BASE = "/api/v1/turno";

    @Mock
    private TurnoService turnoService;

    @InjectMocks
    private TurnoController controller;

    private MockMvc mockMvc;
    private ObjectMapper mapper;

    @BeforeEach
    void setup() {
        mapper = new ObjectMapper();
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .setValidator(validator)
                .build();
    }

    @Test
    @DisplayName("GET /turno/{id} → 200 OK with Turno")
    void getTurno_ok() throws Exception {
        Turno t = new Turno();
        t.setIdTurno(7L);
        when(turnoService.obtenerTurno(7L)).thenReturn(t);

        mockMvc.perform(get(BASE + "/{id}", 7L)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idTurno").value(7));

        verify(turnoService).obtenerTurno(7L);
    }

    @Test
    @DisplayName("GET /turno/{id} → 404 if not found")
    void getTurno_notFound() throws Exception {
        when(turnoService.obtenerTurno(1L)).thenReturn(null);

        mockMvc.perform(get(BASE + "/{id}", 1L))
                .andExpect(status().isNotFound());

        verify(turnoService).obtenerTurno(1L);
    }

    @Test
    @DisplayName("GET /turno/{id} → 500 on exception")
    void getTurno_error() throws Exception {
        when(turnoService.obtenerTurno(2L)).thenThrow(new RuntimeException());

        mockMvc.perform(get(BASE + "/{id}", 2L))
                .andExpect(status().isInternalServerError());

        verify(turnoService).obtenerTurno(2L);
    }

    @Test
    @DisplayName("PUT /tarjeta/obtener → 200 with TarjetaDto")
    void putObtenerTarjeta_ok() throws Exception {
        Pais pais = new Pais();
        pais.setNombre("Argentina");
        TarjetaDto dto = TarjetaDto.builder()
                .idTarjeta(99L)
                .pais(pais)
                .simbolo("estrella")
                .build();

        when(turnoService.entregarTarjetaSiCorresponde(3L, 5L)).thenReturn(dto);

        mockMvc.perform(put(BASE + "/tarjeta/obtener")
                        .param("idJugador", "3")
                        .param("idPartida", "5")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idtarjeta").value(99))
                .andExpect(jsonPath("$.pais.nombre").value("Argentina"))
                .andExpect(jsonPath("$.simbolo").value("estrella"));

        verify(turnoService).entregarTarjetaSiCorresponde(3L, 5L);
    }

    @Test
    @DisplayName("PUT /defender → 200 with Boolean")
    void putDefender_ok() throws Exception {
        EstadoPaisFicha ficha = new EstadoPaisFicha();
        ficha.setIdPais(10L);
        ficha.setCantidadFichas(4L);
        List<EstadoPaisFicha> lista = Collections.singletonList(ficha);
        AgregarFichas body = new AgregarFichas(1L, lista);

        when(turnoService.agregarFichas(any(AgregarFichas.class))).thenReturn(true);

        mockMvc.perform(put(BASE + "/defender")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        verify(turnoService).agregarFichas(any(AgregarFichas.class));
    }

    @Test
    @DisplayName("PUT /reagrupar → 200 with Boolean")
    void putReagrupar_ok() throws Exception {
        MoverFichas req = new MoverFichas(1L, 10L, 20L, 5L);
        when(turnoService.moverFichas(any(MoverFichas.class))).thenReturn(false);

        mockMvc.perform(put(BASE + "/reagrupar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));

        verify(turnoService).moverFichas(any(MoverFichas.class));
    }

    @Test
    @DisplayName("PUT /ataque → 200 with AtaqueResponseDto")
    void putAtaque_ok() throws Exception {
        Ataque req = new Ataque(1L, 10L, 20L);
        AtaqueResponseDto resp = new AtaqueResponseDto(
                true,
                Arrays.asList(6, 4, 2),
                Arrays.asList(5, 3)
        );
        when(turnoService.ataque(any(Ataque.class))).thenReturn(resp);

        mockMvc.perform(put(BASE + "/ataque")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ataqueExitoso").value(true))
                .andExpect(jsonPath("$.dadosAtaque", Matchers.contains(6, 4, 2)))
                .andExpect(jsonPath("$.dadosDefensor", Matchers.contains(5, 3)));

        verify(turnoService).ataque(any(Ataque.class));
    }

    @Test
    @DisplayName("POST /timeout → 200 no body")
    void postTimeout_ok() throws Exception {
        mockMvc.perform(post(BASE + "/timeout")
                        .param("idTurno", "33"))
                .andExpect(status().isOk());

        verify(turnoService).gestionarTimeoutTurno(33L);
    }

    @Test
    @DisplayName("PUT /verificarGanador → 200 with VerificacionObjetivoDto")
    void putVerificarGanador_ok() throws Exception {
        VerificacionObjetivoDto dto = new VerificacionObjetivoDto();
        dto.setGano(true);
        dto.setObjetivoCumplido("Cumplido");
        when(turnoService.verificarGanador(5L)).thenReturn(dto);

        mockMvc.perform(put(BASE + "/verificarGanador")
                        .param("idJugador", "5")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gano").value(true))
                .andExpect(jsonPath("$.objetivoCumplido").value("Cumplido"));

        verify(turnoService).verificarGanador(5L);
    }

    @Test
    @DisplayName("PUT /canje/realizar → 200 with int")
    void putCanjeRealizar_ok() throws Exception {
        CanjeTarjetasDto req = new CanjeTarjetasDto(
                Arrays.asList(1L, 2L, 3L),
                42L
        );
        when(turnoService.validarCanjeTarjetas(any(CanjeTarjetasDto.class))).thenReturn(7);

        mockMvc.perform(put(BASE + "/canje/realizar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(content().string("7"));

        verify(turnoService).validarCanjeTarjetas(any(CanjeTarjetasDto.class));
    }

    @Test
    @DisplayName("PUT /canje/realizar → 400 on invalid")
    void putCanjeRealizar_invalid() throws Exception {
        CanjeTarjetasDto bad1 = new CanjeTarjetasDto(Collections.singletonList(1L), 5L);
        mockMvc.perform(put(BASE + "/canje/realizar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(bad1)))
                .andExpect(status().isBadRequest());
        CanjeTarjetasDto bad2 = new CanjeTarjetasDto(Arrays.asList(1L, -2L, 3L), 5L);
        mockMvc.perform(put(BASE + "/canje/realizar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(bad2)))
                .andExpect(status().isBadRequest());
        String missing = "{\"idTarjetas\":[1,2,3]}";
        mockMvc.perform(put(BASE + "/canje/realizar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(missing))
                .andExpect(status().isBadRequest());
        verify(turnoService, never()).validarCanjeTarjetas(any());
    }

    @Test
    @DisplayName("PUT /usarTarjeta → 200 no body")
    void putUsarTarjeta_ok() throws Exception {
        UsarTarjetaEnPaisDto req = new UsarTarjetaEnPaisDto();
        req.setIdJugador(4L);

        mockMvc.perform(put(BASE + "/usarTarjeta")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isOk());

        verify(turnoService).validarUsarTarjetaEnPais(any(UsarTarjetaEnPaisDto.class));
    }

    @Test
    @DisplayName("GET /{id} → 200 OK cuando encuentra el turno")
    void obtenerTurno_ok() throws Exception {
        Turno turno = new Turno(); // Asumiendo que existe el constructor vacío
        when(turnoService.obtenerTurno(1L)).thenReturn(turno);

        mockMvc.perform(get(BASE + "/{id}", 1L))
                .andExpect(status().isOk());

        verify(turnoService).obtenerTurno(1L);
    }

    @Test
    @DisplayName("GET /{id} → 404 Not Found cuando no encuentra el turno")
    void obtenerTurno_notFound() throws Exception {
        when(turnoService.obtenerTurno(1L)).thenReturn(null);

        mockMvc.perform(get(BASE + "/{id}", 1L))
                .andExpect(status().isNotFound());

        verify(turnoService).obtenerTurno(1L);
    }

    @Test
    @DisplayName("GET /{id} → 500 Internal Server Error cuando falla el servicio")
    void obtenerTurno_error() throws Exception {
        when(turnoService.obtenerTurno(1L)).thenThrow(new RuntimeException("Error interno"));

        mockMvc.perform(get(BASE + "/{id}", 1L))
                .andExpect(status().isInternalServerError());

        verify(turnoService).obtenerTurno(1L);
    }

    @Test
    @DisplayName("POST /timeout → 200 OK cuando gestiona el timeout correctamente")
    void gestionarTimeout_ok() throws Exception {
        mockMvc.perform(post(BASE + "/timeout")
                        .param("idTurno", "1"))
                .andExpect(status().isOk());

        verify(turnoService).gestionarTimeoutTurno(1L);
    }

    @Test
    @DisplayName("POST /timeout → 500 Internal Server Error cuando falla el servicio")
    void gestionarTimeout_error() throws Exception {
        doThrow(new RuntimeException()).when(turnoService).gestionarTimeoutTurno(1L);

        mockMvc.perform(post(BASE + "/timeout")
                        .param("idTurno", "1"))
                .andExpect(status().isInternalServerError());

        verify(turnoService).gestionarTimeoutTurno(1L);
    }

    @Test
    void pasarTurno_deberiaDevolver200() throws Exception {
        mockMvc.perform(put("/api/v1/turno/pasarTurno")
                        .param("idPartida", "1"))
                .andExpect(status().isOk());

        verify(turnoService).pasarTurno(1L);
    }

    @Test
    void cambiarFase_deberiaRetornar200() throws Exception {
        when(turnoService.cambiarFaseTurno(123L)).thenReturn(true);

        mockMvc.perform(put(BASE + "/fase")
                        .param("idPartida", "123"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        verify(turnoService).cambiarFaseTurno(123L);
    }
    @Test
    void validarAtaque_multipleRequestBody_fails() throws Exception {

        String json1 = mapper.writeValueAsString(new Pais());
        String json2 = mapper.writeValueAsString(new Pais());
        String json3 = mapper.writeValueAsString(new Jugador());

        String invalidBody = json1 + json2 + json3;

        mockMvc.perform(post("/api/v1/turno/validar-ataque")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void validarMovimiento_multipleRequestBody_fails() throws Exception {
        String json1 = mapper.writeValueAsString(new Pais());
        String json2 = mapper.writeValueAsString(new Pais());
        String json3 = mapper.writeValueAsString(new Jugador());

        String invalidBody = json1 + json2 + json3;

        mockMvc.perform(post("/api/v1/turno/validar-movimiento")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void verificarOrdenAcciones_errorPorPathVariableFaltante() throws Exception {
        Jugador jugador = new Jugador();
        jugador.setIdJugador(1L);

        mockMvc.perform(post(BASE + "/verificar-accion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(jugador)))
                .andExpect(status().isInternalServerError()); // o .is4xxClientError() si preferís
    }
    @Test
    void validarAtaque_lanzaErrorPorMultiplesRequestBody() throws Exception {
        Pais pais = new Pais();
        pais.setNombre("Argentina");

        String json = mapper.writeValueAsString(pais);

        mockMvc.perform(post("/api/v1/turno/validar-ataque")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /validar-movimiento → 500 al usar múltiples @RequestBody")
    void validarMovimiento_lanzaExcepcionPorMultiplesRequestBody() throws Exception {
        Pais pais = new Pais();
        pais.setNombre("Brasil");

        String json = mapper.writeValueAsString(pais);

        mockMvc.perform(post("/api/v1/turno/validar-movimiento")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }
    @Test
    void validarAtaque_deberiaRetornarOk() {
        Pais pais1 = new Pais();
        Pais pais2 = new Pais();
        Jugador jugador = new Jugador();

        ResponseEntity<?> response = controller.ValidarAtaque(pais1, pais2, jugador);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(turnoService).validarAtaque(pais1, pais2, jugador);
    }

    @Test
    void validarAtaque_deberiaRetornarInternalServerErrorSiFalla() {
        Pais pais1 = new Pais();
        Pais pais2 = new Pais();
        Jugador jugador = new Jugador();

        doThrow(new RuntimeException("error")).when(turnoService).validarAtaque(any(), any(), any());

        ResponseEntity<?> response = controller.ValidarAtaque(pais1, pais2, jugador);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        verify(turnoService).validarAtaque(pais1, pais2, jugador);
    }
    @Test
    void validarMovimiento_deberiaRetornarOk() {
        Pais pais1 = new Pais();
        Pais pais2 = new Pais();
        Jugador jugador = new Jugador();

        ResponseEntity<?> response = controller.ValidarMovimiento(pais1, pais2, jugador);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(turnoService).validarMovimiento(pais1, pais2, jugador);
    }

    @Test
    void validarMovimiento_deberiaRetornarInternalServerErrorSiFalla() {
        Pais pais1 = new Pais();
        Pais pais2 = new Pais();
        Jugador jugador = new Jugador();

        doThrow(new RuntimeException()).when(turnoService).validarMovimiento(any(), any(), any());

        ResponseEntity<?> response = controller.ValidarMovimiento(pais1, pais2, jugador);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        verify(turnoService).validarMovimiento(pais1, pais2, jugador);
    }
    @Test
    void verificarOrdenAcciones_deberiaRetornarOk() {
        Jugador jugador = new Jugador();
        String accion = "algunaAccion";

        ResponseEntity<?> response = controller.VerificarOrdenAcciones(jugador, accion);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(turnoService).verificarOrdenAcciones(jugador, accion);
    }

    @Test
    void verificarOrdenAcciones_deberiaRetornarInternalServerErrorSiFalla() {
        Jugador jugador = new Jugador();
        String accion = "accionError";

        doThrow(new RuntimeException()).when(turnoService).verificarOrdenAcciones(any(), anyString());

        ResponseEntity<?> response = controller.VerificarOrdenAcciones(jugador, accion);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        verify(turnoService).verificarOrdenAcciones(jugador, accion);
    }

}

