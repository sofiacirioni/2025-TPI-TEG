package ar.edu.utn.frc.tup.piii.Services.ServicesImpl;

import ar.edu.utn.frc.tup.piii.Dtos.HistorialComandanteDto;
import ar.edu.utn.frc.tup.piii.Dtos.ResumenPartidaDto;
import ar.edu.utn.frc.tup.piii.Entities.JugadorEntity;
import ar.edu.utn.frc.tup.piii.Entities.PactoEntity;
import ar.edu.utn.frc.tup.piii.Entities.PartidaEntity;
import ar.edu.utn.frc.tup.piii.Entities.UsuarioEntity;
import ar.edu.utn.frc.tup.piii.Repositories.JugadorRepository;
import ar.edu.utn.frc.tup.piii.Repositories.PactoRepository;
import ar.edu.utn.frc.tup.piii.Repositories.UsuarioRepository;
import ar.edu.utn.frc.tup.piii.Services.ResumenPartidaService;
import ar.edu.utn.frc.tup.piii.models.EstadoPacto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HistorialComandanteServiceImplTest {

    @Mock private UsuarioRepository usuarioRepository;
    @Mock private JugadorRepository jugadorRepository;
    @Mock private PactoRepository pactoRepository;
    @Mock private ResumenPartidaService resumenPartidaService;

    @InjectMocks private HistorialComandanteServiceImpl service;

    private UsuarioEntity usuario;

    @BeforeEach
    void setUp() {
        usuario = new UsuarioEntity();
        usuario.setIdUsuario(1L);
        usuario.setUsuario("comandante");
        usuario.setCorreo("comandante@test.com");
        usuario.setImagen("assets/images/avatars/Maxi.png");
        usuario.setFechaAlta(LocalDateTime.of(2026, 3, 4, 10, 0));
    }

    @Test
    @DisplayName("Suma los contadores de las campañas concluidas y reparte las medallas por puesto")
    void acumulaCampaniasTerminadas() {
        // Ganó la primera y salió tercero en la segunda.
        prepararParticipaciones(
                campania(10L, LocalDate.of(2026, 5, 1), true, 1, 12, 6, 30, 10),
                campania(11L, LocalDate.of(2026, 5, 2), true, 3, 8, 2, 14, 20));
        when(pactoRepository.findByJugadorIdIn(anyList())).thenReturn(List.of());

        HistorialComandanteDto historial = service.obtenerHistorial(1L);

        assertEquals(2, historial.getCampaniasLibradas());
        assertEquals(1, historial.getVictorias());
        assertEquals(0, historial.getSegundosPuestos());
        assertEquals(1, historial.getTerceroPuestos());
        assertEquals(50.0, historial.getTasaVictoria());

        assertEquals(20, historial.getAtaquesLanzados());
        assertEquals(8, historial.getConquistas());
        // 8 conquistas sobre 20 ataques.
        assertEquals(40.0, historial.getEfectividad());
        assertEquals(44, historial.getTropasAbatidas());
        assertEquals(30, historial.getTropasPerdidas());
        assertEquals(1.5, historial.getRatioBajas());
    }

    @Test
    @DisplayName("La campaña en curso figura en el registro pero no suma al historial")
    void ignoraCampaniasSinTerminar() {
        prepararParticipaciones(
                campania(10L, LocalDate.of(2026, 5, 1), true, 1, 12, 6, 30, 10),
                campania(11L, LocalDate.of(2026, 5, 3), false, 2, 5, 1, 4, 4));
        when(pactoRepository.findByJugadorIdIn(anyList())).thenReturn(List.of());

        HistorialComandanteDto historial = service.obtenerHistorial(1L);

        assertEquals(1, historial.getCampaniasLibradas());
        assertEquals(12, historial.getAtaquesLanzados());
        assertEquals(2, historial.getCampanias().size());

        HistorialComandanteDto.CampaniaDto enCurso = historial.getCampanias().stream()
                .filter(c -> !c.isTerminada())
                .findFirst()
                .orElseThrow();
        // Sin desenlace no hay puesto que mostrar.
        assertEquals(0, enCurso.getPuesto());
    }

    @Test
    @DisplayName("Cuenta los tratados firmados y sólo los que rompió este usuario")
    void cuentaTratadosPropios() {
        prepararParticipaciones(campania(10L, LocalDate.of(2026, 5, 1), true, 1, 4, 2, 6, 3));

        PactoEntity cumplido = pacto(EstadoPacto.ACTIVO, null);
        PactoEntity rotoPorMi = pacto(EstadoPacto.ROTO_VOLUNTARIO, 100L);
        PactoEntity rotoPorOtro = pacto(EstadoPacto.ROTO_VOLUNTARIO, 999L);
        PactoEntity soloPropuesto = pacto(EstadoPacto.PROPUESTO, null);
        when(pactoRepository.findByJugadorIdIn(anyList()))
                .thenReturn(List.of(cumplido, rotoPorMi, rotoPorOtro, soloPropuesto));

        HistorialComandanteDto historial = service.obtenerHistorial(1L);

        assertEquals(3, historial.getTratadosFirmados());
        assertEquals(1, historial.getTratadosRotos());
    }

    @Test
    @DisplayName("Un usuario inexistente no tiene hoja de servicios")
    void usuarioInexistente() {
        when(usuarioRepository.findById(7L)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> service.obtenerHistorial(7L));
    }

    // ── Armado del escenario ──────────────────────────────────────────────

    /** Participación del usuario en una partida, con el resumen que devolverá el servicio. */
    private record Campania(JugadorEntity participacion, ResumenPartidaDto resumen) { }

    private void prepararParticipaciones(Campania... campanias) {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(jugadorRepository.findParticipacionesHumanas(1L))
                .thenReturn(java.util.Arrays.stream(campanias).map(Campania::participacion).toList());
        for (Campania c : campanias) {
            when(resumenPartidaService.obtenerResumen(c.resumen().getIdPartida()))
                    .thenReturn(c.resumen());
        }
    }

    /**
     * Una campaña donde el usuario terminó en {@code puesto}. La clasificación se
     * rellena con rivales hasta esa posición, porque el servicio deduce el puesto
     * del orden en que vienen los comandantes.
     */
    private Campania campania(Long idPartida, LocalDate fecha, boolean terminada, int puesto,
                              int ataques, int conquistas, int abatidas, int perdidas) {
        PartidaEntity partida = new PartidaEntity();
        partida.setIdPartida(idPartida);
        partida.setFechaInicio(fecha);

        JugadorEntity participacion = new JugadorEntity();
        // El id de la participación es siempre 100 para simplificar las aserciones
        // sobre pactos; cada partida tiene una sola fila de este usuario.
        participacion.setIdJugador(100L);
        participacion.setUsuario(usuario);
        participacion.setPartida(partida);

        List<ResumenPartidaDto.ComandanteResumenDto> clasificacion = new java.util.ArrayList<>();
        for (int i = 1; i < puesto; i++) {
            clasificacion.add(ResumenPartidaDto.ComandanteResumenDto.builder()
                    .idJugador(900L + i)
                    .nombre("rival " + i)
                    .color("AZUL")
                    .build());
        }
        clasificacion.add(ResumenPartidaDto.ComandanteResumenDto.builder()
                .idJugador(100L)
                .nombre("comandante")
                .color("ROJO")
                .ganador(puesto == 1)
                .ataquesLanzados(ataques)
                .conquistas(conquistas)
                .tropasAbatidas(abatidas)
                .tropasPerdidas(perdidas)
                .build());

        ResumenPartidaDto resumen = ResumenPartidaDto.builder()
                .idPartida(idPartida)
                .fecha(fecha)
                .terminada(terminada)
                .turnosJugados(9)
                .comandantes(clasificacion)
                .build();

        return new Campania(participacion, resumen);
    }

    private PactoEntity pacto(EstadoPacto estado, Long idJugadorQueRompio) {
        PartidaEntity partida = new PartidaEntity();
        partida.setIdPartida(10L);

        PactoEntity pacto = new PactoEntity();
        pacto.setPartida(partida);
        pacto.setEstado(estado);
        pacto.setTurnoRupturaJugador(idJugadorQueRompio);
        return pacto;
    }
}
