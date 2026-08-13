package ar.edu.utn.frc.tup.piii.Services.ServicesImpl;

import ar.edu.utn.frc.tup.piii.Dtos.EstadoPaises.Ataque;
import ar.edu.utn.frc.tup.piii.Dtos.EstadoPaises.AtaqueResponseDto;
import ar.edu.utn.frc.tup.piii.Entities.*;
import ar.edu.utn.frc.tup.piii.Repositories.EstadoPaisRepository;
import ar.edu.utn.frc.tup.piii.Repositories.EstadoTarjetaRepository;
import ar.edu.utn.frc.tup.piii.Repositories.JugadorRepository;
import ar.edu.utn.frc.tup.piii.Repositories.PactoRepository;
import ar.edu.utn.frc.tup.piii.Services.EstadoPaisService;
import ar.edu.utn.frc.tup.piii.Services.PactoService;
import ar.edu.utn.frc.tup.piii.Services.TurnoService;
import ar.edu.utn.frc.tup.piii.models.EstadoPacto;
import ar.edu.utn.frc.tup.piii.models.Simbolo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BotServiceImplTest {

    @InjectMocks
    private BotServiceImpl botServiceImpl;

    @Mock
    private TurnoService turnoService;

    @Mock
    private EstadoPaisService estadoPaisService;

    @Mock
    private EstadoPaisRepository estadoPaisRepository;

    @Mock
    private JugadorRepository jugadorRepository;

    /**
     * Necesario desde que realizarIncorporacion() arranca con el canje obligatorio de
     * tarjetas. Sin este mock el campo queda null, la incorporación lanza NPE y el bot
     * cae en la rama de recuperación del catch — con lo cual nunca distribuye ni ataca.
     * Sin stubear: Mockito devuelve lista vacía y el bot sigue de largo (no hay canje).
     */
    @Mock
    private EstadoTarjetaRepository estadoTarjetaRepository;

    @Mock
    private PactoRepository pactoRepository;

    @Mock
    private PactoService pactoService;

    @BeforeEach
    void setUp() {
        // @Lazy @Autowired no lo inyecta @InjectMocks — lo seteamos manualmente
        ReflectionTestUtils.setField(botServiceImpl, "turnoService", turnoService);
        // Eliminar las pausas para que los tests corran instantáneamente
        botServiceImpl.cooldownMs = 0;
        botServiceImpl.pausaEntreAccionesMs = 0;
    }

    // ── Helpers ────────────────────────────────────────────────────────────────

    private JugadorEntity crearBot(Long id) {
        JugadorEntity bot = new JugadorEntity();
        bot.setIdJugador(id);
        PartidaEntity partida = new PartidaEntity();
        partida.setIdPartida(99L);
        bot.setPartida(partida);
        bot.setEjercito(0);
        return bot;
    }

    private EstadoTarjetaEntity crearTarjeta(Simbolo simbolo) {
        TarjetaEntity tarjeta = new TarjetaEntity();
        tarjeta.setSimbolo(simbolo);

        EstadoTarjetaEntity estado = new EstadoTarjetaEntity();
        estado.setTarjeta(tarjeta);
        estado.setCanjeada(false);
        return estado;
    }

    private EstadoPaisEntity crearEstadoPais(Long idEstado, Long idPais, JugadorEntity jugador, int tropas) {
        PaisEntity pais = new PaisEntity();
        pais.setIdPais(idPais);

        EstadoPaisEntity ep = new EstadoPaisEntity();
        ep.setIdEstadoPais(idEstado);
        ep.setPais(pais);
        ep.setJugador(jugador);
        ep.setCantidadTropas(tropas);
        return ep;
    }

    // ── Tests ──────────────────────────────────────────────────────────────────

    /**
     * Si el bot no tiene ejércitos, executeTurnAsync avanza las 3 fases de todas formas
     * (incorporación sin acción, ataque vacío, reagrupación).
     * Como @Async se ignora en tests sin contexto Spring, el método corre de forma síncrona.
     */
    @Test
    void executeTurnAsync_sinEjercitos_avanzaTresFases() throws Exception {
        JugadorEntity bot = crearBot(1L);
        when(jugadorRepository.findByIdJugador(1L)).thenReturn(Optional.of(bot));
        when(estadoPaisRepository.findByJugador_IdJugador(1L)).thenReturn(List.of());

        botServiceImpl.executeTurnAsync(1L);

        // INC→ATK, ATK→REAG, REAG→INC (siguiente jugador)
        verify(turnoService, times(3)).cambiarFaseTurno(99L);
    }

    /**
     * El bot con ejércitos distribuye todos sus ejércitos (llama agregarFichas al menos una vez)
     * y luego avanza las 3 fases.
     */
    @Test
    void executeTurnAsync_conEjercitos_distribuyeYAvanzaFases() throws Exception {
        JugadorEntity bot = crearBot(2L);
        bot.setEjercito(3);

        // 3 tropas es el mínimo con el que el bot se anima a atacar; con menos
        // ni siquiera mira a sus vecinos.
        EstadoPaisEntity ep = crearEstadoPais(10L, 1L, bot, 3);

        when(jugadorRepository.findByIdJugador(2L)).thenReturn(Optional.of(bot));
        when(estadoPaisRepository.findByJugador_IdJugador(2L)).thenReturn(List.of(ep));
        when(estadoPaisRepository.findById(10L)).thenReturn(Optional.of(ep));
        when(estadoPaisService.getLimitesEstadoPaisEntity(10L)).thenReturn(List.of());

        botServiceImpl.executeTurnAsync(2L);

        verify(turnoService, atLeastOnce()).agregarFichas(any());
        verify(turnoService, times(3)).cambiarFaseTurno(99L);
    }

    /**
     * El bot conserva una guarnición mínima: con menos de 3 tropas en el país
     * de origen no ataca. Antes atacaba hasta quedarse con una sola tropa en
     * todos sus países, lo que lo dejaba servido para el turno siguiente.
     */
    @Test
    void realizarAtaque_conGuarnicionMinima_noAtaca() throws Exception {
        JugadorEntity bot = crearBot(3L);
        bot.setEjercito(0);

        EstadoPaisEntity debil = crearEstadoPais(20L, 2L, bot, 2);

        when(jugadorRepository.findByIdJugador(3L)).thenReturn(Optional.of(bot));
        when(estadoPaisRepository.findByJugador_IdJugador(3L)).thenReturn(List.of(debil));

        botServiceImpl.executeTurnAsync(3L);

        // Ni siquiera pregunta por los vecinos: descarta el país antes.
        verify(estadoPaisService, never()).getLimitesEstadoPaisEntity(anyLong());
        verify(turnoService, never()).ataque(any());
    }

    /**
     * El bot no ataca países que le pertenecen: el vecino que es del mismo bot
     * es filtrado y turnoService.ataque() nunca se llama.
     */
    @Test
    void realizarAtaque_noAtacaPaisesPropioss() throws Exception {
        JugadorEntity bot = crearBot(3L);
        bot.setEjercito(0);

        EstadoPaisEntity origen = crearEstadoPais(20L, 1L, bot, 3);
        EstadoPaisEntity vecinoPropio = crearEstadoPais(21L, 2L, bot, 1); // mismo dueño

        when(jugadorRepository.findByIdJugador(3L)).thenReturn(Optional.of(bot));
        when(estadoPaisRepository.findByJugador_IdJugador(3L)).thenReturn(List.of(origen));
        when(estadoPaisRepository.findById(20L)).thenReturn(Optional.of(origen));
        when(estadoPaisService.getLimitesEstadoPaisEntity(20L)).thenReturn(List.of(vecinoPropio));

        botServiceImpl.executeTurnAsync(3L);

        verify(turnoService, never()).ataque(any(Ataque.class));
        verify(turnoService, times(3)).cambiarFaseTurno(99L);
    }

    /**
     * El bot ataca al vecino enemigo con menos tropas cuando tiene más de 1 tropa en origen.
     */
    @Test
    void realizarAtaque_atacaVecinoEnemigo() throws Exception {
        JugadorEntity bot = crearBot(4L);
        bot.setEjercito(0);

        JugadorEntity enemigo = new JugadorEntity();
        enemigo.setIdJugador(99L);

        EstadoPaisEntity origen = crearEstadoPais(30L, 1L, bot, 3);
        EstadoPaisEntity vecino = crearEstadoPais(31L, 2L, enemigo, 1);

        when(jugadorRepository.findByIdJugador(4L)).thenReturn(Optional.of(bot));
        when(estadoPaisRepository.findByJugador_IdJugador(4L)).thenReturn(List.of(origen));
        when(estadoPaisRepository.findById(30L)).thenReturn(Optional.of(origen));
        when(estadoPaisRepository.findById(31L)).thenReturn(Optional.of(vecino));
        when(estadoPaisService.getLimitesEstadoPaisEntity(30L)).thenReturn(List.of(vecino));
        when(turnoService.ataque(any())).thenReturn(new AtaqueResponseDto(true, false, List.of(), List.of(), 0, 0));

        botServiceImpl.executeTurnAsync(4L);

        verify(turnoService, atLeastOnce()).ataque(any(Ataque.class));
    }

    /**
     * Con 5 tarjetas donde dos comparten símbolo, el bot tiene que seguir jugando.
     *
     * <p>Era el bug que dejaba a los bots pasando el turno sin hacer nada a partir
     * de cierta altura de la partida: la combinación se evaluaba con {@code Set.of},
     * que lanza IllegalArgumentException ante elementos repetidos. La excepción
     * escapaba de la incorporación y el turno entero caía en la rama de
     * recuperación. Como hace falta juntar 5 tarjetas, aparecía recién promediando
     * la partida y ya no se iba más.
     */
    @Test
    void executeTurnAsync_conTarjetasDelMismoSimbolo_igualJuegaElTurno() throws Exception {
        JugadorEntity bot = crearBot(6L);
        bot.setEjercito(2);

        EstadoPaisEntity ep = crearEstadoPais(50L, 1L, bot, 2);

        // GALEON repetido: el trío (0,1,2) tiene dos símbolos, que no es canje válido,
        // pero tampoco puede hacer explotar el turno.
        when(estadoTarjetaRepository.findByJugadorId(6L)).thenReturn(List.of(
                crearTarjeta(Simbolo.GALEON), crearTarjeta(Simbolo.GALEON),
                crearTarjeta(Simbolo.CANION), crearTarjeta(Simbolo.GALEON),
                crearTarjeta(Simbolo.CANION)));

        when(jugadorRepository.findByIdJugador(6L)).thenReturn(Optional.of(bot));
        when(estadoPaisRepository.findByJugador_IdJugador(6L)).thenReturn(List.of(ep));

        botServiceImpl.executeTurnAsync(6L);

        // Reparte sus ejércitos y avanza las fases con normalidad.
        verify(turnoService, atLeastOnce()).agregarFichas(any());
        verify(turnoService, times(3)).cambiarFaseTurno(99L);
    }

    /**
     * El bot contesta las propuestas de pacto que le hayan quedado pendientes.
     * Antes las ignoraba: quedaban en PROPUESTO para siempre y el humano nunca
     * recibía respuesta.
     */
    @Test
    void executeTurnAsync_conPactoPendiente_loAcepta() throws Exception {
        JugadorEntity bot = crearBot(7L);
        JugadorEntity proponente = crearBot(70L);

        PactoEntity propuesta = new PactoEntity();
        propuesta.setId(500L);
        propuesta.setEstado(EstadoPacto.PROPUESTO);
        propuesta.setJugadorA(proponente);
        propuesta.setJugadorB(bot);

        when(jugadorRepository.findByIdJugador(7L)).thenReturn(Optional.of(bot));
        when(pactoRepository.findByPartida_IdPartidaAndEstado(99L, EstadoPacto.PROPUESTO))
                .thenReturn(List.of(propuesta));
        // Mismos países: nadie saca ventaja, así que firma.
        when(estadoPaisRepository.findByJugador_IdJugador(anyLong())).thenReturn(List.of());

        botServiceImpl.executeTurnAsync(7L);

        verify(pactoService).aceptar(500L, 7L);
        verify(pactoService, never()).rechazar(anyLong(), anyLong());
    }

    /**
     * No le firma inmunidad a quien ya le saca varios países de ventaja.
     */
    @Test
    void executeTurnAsync_pactoDelQueVaGanando_loRechaza() throws Exception {
        JugadorEntity bot = crearBot(8L);
        JugadorEntity lider = crearBot(80L);

        PactoEntity propuesta = new PactoEntity();
        propuesta.setId(501L);
        propuesta.setEstado(EstadoPacto.PROPUESTO);
        propuesta.setJugadorA(lider);
        propuesta.setJugadorB(bot);

        when(jugadorRepository.findByIdJugador(8L)).thenReturn(Optional.of(bot));
        when(pactoRepository.findByPartida_IdPartidaAndEstado(99L, EstadoPacto.PROPUESTO))
                .thenReturn(List.of(propuesta));
        when(estadoPaisRepository.findByJugador_IdJugador(8L)).thenReturn(List.of());
        // El proponente domina 6 países contra 0 del bot: demasiada ventaja.
        when(estadoPaisRepository.findByJugador_IdJugador(80L)).thenReturn(List.of(
                crearEstadoPais(1L, 1L, lider, 1), crearEstadoPais(2L, 2L, lider, 1),
                crearEstadoPais(3L, 3L, lider, 1), crearEstadoPais(4L, 4L, lider, 1),
                crearEstadoPais(5L, 5L, lider, 1), crearEstadoPais(6L, 6L, lider, 1)));

        botServiceImpl.executeTurnAsync(8L);

        verify(pactoService).rechazar(501L, 8L);
        verify(pactoService, never()).aceptar(anyLong(), anyLong());
    }

    /**
     * Si turnoService.ataque() lanza excepción (p.ej. territorio ya conquistado),
     * el bot continúa sin detenerse y sigue avanzando fases.
     */
    @Test
    void realizarAtaque_excepccionEnAtaque_continuaSinDetener() throws Exception {
        JugadorEntity bot = crearBot(5L);
        bot.setEjercito(0);

        JugadorEntity enemigo = new JugadorEntity();
        enemigo.setIdJugador(88L);

        EstadoPaisEntity origen = crearEstadoPais(40L, 1L, bot, 4);
        EstadoPaisEntity vecino = crearEstadoPais(41L, 2L, enemigo, 1);

        when(jugadorRepository.findByIdJugador(5L)).thenReturn(Optional.of(bot));
        when(estadoPaisRepository.findByJugador_IdJugador(5L)).thenReturn(List.of(origen));
        when(estadoPaisRepository.findById(40L)).thenReturn(Optional.of(origen));
        when(estadoPaisRepository.findById(41L)).thenReturn(Optional.of(vecino));
        when(estadoPaisService.getLimitesEstadoPaisEntity(40L)).thenReturn(List.of(vecino));
        when(turnoService.ataque(any())).thenThrow(new IllegalArgumentException("No se puede atacar su propio país"));

        botServiceImpl.executeTurnAsync(5L);

        // A pesar de la excepción, las 3 fases deben avanzar
        verify(turnoService, times(3)).cambiarFaseTurno(99L);
    }
}
