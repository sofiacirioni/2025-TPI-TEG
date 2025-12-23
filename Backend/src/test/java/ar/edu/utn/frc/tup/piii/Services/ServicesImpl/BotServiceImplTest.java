package ar.edu.utn.frc.tup.piii.Services.ServicesImpl;

import ar.edu.utn.frc.tup.piii.Dtos.EstadoPaises.AgregarFichas;
import ar.edu.utn.frc.tup.piii.Dtos.EstadoPaises.Ataque;
import ar.edu.utn.frc.tup.piii.Dtos.EstadoPaises.AtaqueResponseDto;
import ar.edu.utn.frc.tup.piii.Dtos.UsarTarjetaEnPaisDto;
import ar.edu.utn.frc.tup.piii.Entities.*;
import ar.edu.utn.frc.tup.piii.Repositories.EstadoPaisRepository;
import ar.edu.utn.frc.tup.piii.Repositories.EstadoTarjetaRepository;
import ar.edu.utn.frc.tup.piii.Repositories.JugadorRepository;
import ar.edu.utn.frc.tup.piii.Services.EstadoPaisService;
import ar.edu.utn.frc.tup.piii.Services.TurnoService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
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
    private EstadoTarjetaRepository estadoTarjetaRepository;

    @Mock
    private JugadorRepository jugadorRepository;

    @Test
    void faseReagrupar_deberiaCambiarFase() {
        JugadorEntity bot = new JugadorEntity();
        bot.setPartida(new PartidaEntity());

        boolean resultado = botServiceImpl.faseReagrupar(bot);

        assertFalse(resultado);
        verify(turnoService).cambiarFaseTurno(bot.getPartida().getIdPartida());
    }

    @Test
    void pedirCarta_deberiaEntregarCartaYUsarlaSiCorresponde() {
        JugadorEntity bot = new JugadorEntity();
        bot.setIdJugador(1L);
        PartidaEntity partida = new PartidaEntity();
        bot.setPartida(partida);

        PaisEntity pais = new PaisEntity(); pais.setIdPais(10L);
        EstadoPaisEntity estadoPais = new EstadoPaisEntity(); estadoPais.setPais(pais);
        estadoPais.setJugador(bot);

        TarjetaEntity tarjeta = new TarjetaEntity(); tarjeta.setPais(pais);
        EstadoTarjetaEntity tarjetaEstado = new EstadoTarjetaEntity();
        tarjetaEstado.setTarjeta(tarjeta);
        tarjetaEstado.setUsada(false);

        when(estadoPaisRepository.findByJugador_IdJugador(1L)).thenReturn(List.of(estadoPais));
        when(estadoTarjetaRepository.findByJugador_IdJugador(1L)).thenReturn(List.of(tarjetaEstado));

        boolean resultado = botServiceImpl.pedirCarta(bot);

        assertTrue(resultado);
        verify(turnoService).entregarTarjetaSiCorresponde(1L, partida.getIdPartida());
        verify(turnoService).validarUsarTarjetaEnPais(any());
    }
    @Test
    void canjearCarta_deberiaRetornarFalseSiempre() {
        JugadorEntity bot = new JugadorEntity();

        boolean resultado = botServiceImpl.canjearCarta(bot);

        assertFalse(resultado);
    }

    @Test
    void usarCarta_deberiaLlamarValidacionDeTarjeta() {
        JugadorEntity bot = new JugadorEntity();
        bot.setIdJugador(1L);

        EstadoTarjetaEntity tarjeta = new EstadoTarjetaEntity();
        tarjeta.setIdEstadoTarjeta(99L);

        boolean resultado = botServiceImpl.usarCarta(bot, tarjeta);

        assertFalse(resultado);
        verify(turnoService).validarUsarTarjetaEnPais(any(UsarTarjetaEnPaisDto.class));
    }

    @Test
    void turnoBot_deberiaEjecutarFlujoCompleto() {
        Long idJugador = 1L;

        JugadorEntity bot = new JugadorEntity();
        bot.setIdJugador(idJugador);
        bot.setEjercito(3);

        PartidaEntity partida = new PartidaEntity();
        partida.setHostilidad(true);
        partida.setIdPartida(10L);
        bot.setPartida(partida);

        List<EstadoTarjetaEntity> tarjetasEstado = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            EstadoTarjetaEntity estadoTarjeta = new EstadoTarjetaEntity();
            tarjetasEstado.add(estadoTarjeta);
        }
        bot.setTarjetas(tarjetasEstado);

        when(jugadorRepository.findByIdJugador(idJugador)).thenReturn(Optional.of(bot));

        BotServiceImpl spyService = Mockito.spy(botServiceImpl);
        doReturn(true).when(spyService).faseDefensa(bot);
        doReturn(true).when(spyService).faseAtaque(bot);
        doReturn(true).when(spyService).pedirCarta(bot);
        doReturn(true).when(spyService).canjearCarta(bot);
        doReturn(true).when(spyService).faseReagrupar(bot);

        boolean resultado = spyService.turnoBot(idJugador);

        assertTrue(resultado);
        verify(jugadorRepository).findByIdJugador(idJugador);
        verify(spyService).faseDefensa(bot);
        verify(spyService).faseAtaque(bot);
        verify(spyService).pedirCarta(bot);
        verify(spyService).canjearCarta(bot);
        verify(spyService).faseReagrupar(bot);
    }

    @Test

    void faseAtaque_deberiaIntentarAtaquesYCambiarFase() {
        JugadorEntity bot = new JugadorEntity();
        bot.setIdJugador(1L);

        PartidaEntity partida = new PartidaEntity();
        partida.setIdPartida(99L);
        bot.setPartida(partida);

        EstadoPaisEntity paisAtacante = new EstadoPaisEntity();
        paisAtacante.setIdEstadoPais(10L);
        paisAtacante.setCantidadTropas(3);
        PaisEntity paisAtacantePais = new PaisEntity();
        paisAtacantePais.setIdPais(100L);
        paisAtacante.setPais(paisAtacantePais);

        EstadoPaisEntity paisDefensor1 = new EstadoPaisEntity();
        paisDefensor1.setIdEstadoPais(11L);
        paisDefensor1.setCantidadTropas(1);
        paisDefensor1.setJugador(bot);
        PaisEntity paisDefensor1Pais = new PaisEntity();
        paisDefensor1Pais.setIdPais(101L);
        paisDefensor1.setPais(paisDefensor1Pais);

        EstadoPaisEntity paisDefensor2 = new EstadoPaisEntity();
        paisDefensor2.setIdEstadoPais(12L);
        paisDefensor2.setCantidadTropas(1);
        JugadorEntity otroJugador = new JugadorEntity();
        otroJugador.setIdJugador(2L);
        paisDefensor2.setJugador(otroJugador);
        PaisEntity paisDefensor2Pais = new PaisEntity();
        paisDefensor2Pais.setIdPais(102L);
        paisDefensor2.setPais(paisDefensor2Pais);

        when(estadoPaisRepository.findByJugador_IdJugador(1L)).thenReturn(List.of(paisAtacante));
        when(estadoPaisService.getLimitesEstadoPaisEntity(10L)).thenReturn(List.of(paisDefensor1, paisDefensor2));
        when(turnoService.ataque(any(Ataque.class))).thenReturn(new AtaqueResponseDto(true, List.of(), List.of()));

        boolean resultado = botServiceImpl.faseAtaque(bot);

        assertTrue(resultado);
        verify(turnoService, atLeastOnce()).ataque(any(Ataque.class));
        verify(turnoService).cambiarFaseTurno(99L);
    }
    @Test
    void faseDefensa_conEjercitoVacio_deberiaTerminarInmediatamente() {
        JugadorEntity bot = new JugadorEntity();
        bot.setIdJugador(1L);
        bot.setEjercito(0);

        PartidaEntity partida = new PartidaEntity();
        partida.setIdPartida(99L);
        bot.setPartida(partida);

        EstadoPaisEntity pais1 = new EstadoPaisEntity();
        pais1.setIdEstadoPais(10L);
        List<EstadoPaisEntity> paises = List.of(pais1);

        when(estadoPaisRepository.findByJugador_IdJugador(1L)).thenReturn(paises);

        boolean resultado = botServiceImpl.faseDefensa(bot);

        assertTrue(resultado);
        verify(turnoService, never()).agregarFichas(any(AgregarFichas.class));
        verify(estadoPaisRepository).saveAll(paises);
        verify(jugadorRepository).save(bot);
        verify(turnoService).cambiarFaseTurno(99L);
    }
    @Test
    void faseDefensa_deberiaEjecutarseCorrectamente() {
        JugadorEntity bot = new JugadorEntity();
        bot.setIdJugador(1L);
        bot.setEjercito(5);

        PartidaEntity partida = new PartidaEntity();
        partida.setIdPartida(99L);
        bot.setPartida(partida);

        EstadoPaisEntity pais = new EstadoPaisEntity();
        pais.setIdEstadoPais(10L);

        List<EstadoPaisEntity> paises = List.of(pais);

        when(estadoPaisRepository.findByJugador_IdJugador(1L)).thenReturn(paises);
        when(turnoService.agregarFichas(any())).thenAnswer(invocation -> {
            bot.setEjercito(bot.getEjercito() - 1);
            return true;
        });
        when(estadoPaisRepository.saveAll(anyList())).thenReturn(paises);
        when(jugadorRepository.save(any())).thenReturn(bot);
        when(turnoService.cambiarFaseTurno(99L)).thenReturn(true);

        boolean resultado = botServiceImpl.faseDefensa(bot);

        assertTrue(resultado);
        assertEquals(0, bot.getEjercito());
        verify(turnoService, atLeast(1)).agregarFichas(any());
        verify(estadoPaisRepository).saveAll(paises);
        verify(jugadorRepository).save(bot);
        verify(turnoService).cambiarFaseTurno(99L);
    }
    @Test
    void faseDefensa_conEjercitoCero_noHaceNada() {
        JugadorEntity bot = new JugadorEntity();
        bot.setIdJugador(1L);
        bot.setEjercito(0);

        PartidaEntity partida = new PartidaEntity();
        partida.setIdPartida(99L);
        bot.setPartida(partida);

        EstadoPaisEntity pais = new EstadoPaisEntity();
        pais.setIdEstadoPais(10L);
        List<EstadoPaisEntity> paises = List.of(pais);

        when(estadoPaisRepository.findByJugador_IdJugador(1L)).thenReturn(paises);
        when(turnoService.cambiarFaseTurno(99L)).thenReturn(true);
        when(jugadorRepository.save(any())).thenReturn(bot);
        when(estadoPaisRepository.saveAll(anyList())).thenReturn(paises);

        boolean resultado = botServiceImpl.faseDefensa(bot);

        assertTrue(resultado);
        verify(turnoService, never()).agregarFichas(any());
        verify(turnoService).cambiarFaseTurno(99L);
        verify(jugadorRepository).save(bot);
        verify(estadoPaisRepository).saveAll(paises);
    }

}
