package ar.edu.utn.frc.tup.piii.Services.ServicesImpl;

import ar.edu.utn.frc.tup.piii.Entities.*;
import ar.edu.utn.frc.tup.piii.Repositories.*;
import ar.edu.utn.frc.tup.piii.Services.JuegoService;
import ar.edu.utn.frc.tup.piii.models.EstadoPartida;
import ar.edu.utn.frc.tup.piii.models.FaseJuego;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class JuegoServiceImpl implements JuegoService {

    @Autowired
    private PartidaRepository partidaRepository;
    @Autowired
    private JugadorRepository jugadorRepository;
    @Autowired
    private ObjetivoRepository objetivoRepository;
    @Autowired
    private PaisRepository paisRepository;
    @Autowired
    private EstadoPaisRepository estadoPaisRepository;


    @Override
    public void repartirPaisesYObjetivos(Long idPartida) {
        PartidaEntity partida = partidaRepository.findById(idPartida)
                .orElseThrow(() -> new RuntimeException("Partida no encontrada"));

        // todo, no usar pais, solo usar estadopaisEntity
        List<PaisEntity> paises = paisRepository.findAll();
        List<JugadorEntity> jugadores = partida.getConfiguracion().getJugadores();

        Collections.shuffle(paises);
        Collections.shuffle(jugadores);

        int index = 0;
        for (PaisEntity pais : paises) {
            JugadorEntity jugador = jugadores.get(index % jugadores.size());
            EstadoPaisEntity estado = new EstadoPaisEntity();
            estado.setJugador(jugador);
            estado.setPais(pais);
            estado.setCantidadTropas(1); // asignamos 1 tropa al inicio
            estado.setPartida(partida);
            estadoPaisRepository.save(estado);
            index++;
        }

        // todo debe filtrar por secretos
        List<ObjetivoEntity> objetivos = objetivoRepository.findAll();
        Collections.shuffle(objetivos);
        for (int i = 0; i < jugadores.size(); i++) {
            jugadores.get(i).setObjetivo(objetivos.get(i));
            jugadorRepository.save(jugadores.get(i));
        }
    }

    // todo, lo hace estado pais service
    @Override
    public void iniciarColocacionEjercitos(Long idPartida) {
        PartidaEntity partida = partidaRepository.findById(idPartida)
                .orElseThrow(() -> new RuntimeException("Partida no encontrada"));

        partida.setFaseActual(FaseJuego.COLOCACION);
        partida.setTurnoActual(0);
        partidaRepository.save(partida);
    }

    @Override
    public void avanzarFase(Long idPartida) {
        PartidaEntity partida = partidaRepository.findById(idPartida)
                .orElseThrow(() -> new RuntimeException("Partida no encontrada"));

        switch (partida.getFaseActual()) {
            case COLOCACION -> partida.setFaseActual(FaseJuego.HOSTILIDADES);
            case HOSTILIDADES -> partida.setFaseActual(FaseJuego.FIN_TURNO);
            case FIN_TURNO -> {
                partida.setFaseActual(FaseJuego.COLOCACION);
                int totalJugadores = partida.getConfiguracion().getJugadores().size();
                partida.setTurnoActual((partida.getTurnoActual() + 1) % totalJugadores);
            }
        }
        partidaRepository.save(partida);
    }

    @Override
    public boolean comprobarObjetivoCumplido(Long idPartida) {
        PartidaEntity partida = partidaRepository.findById(idPartida)
                .orElseThrow(() -> new RuntimeException("Partida no encontrada"));

        for (JugadorEntity jugador : partida.getConfiguracion().getJugadores()) {
            ObjetivoEntity objetivo = jugador.getObjetivo();
            long cantidad = estadoPaisRepository.countByJugador(jugador);
            if (objetivo.getCantidadPaisesObjetivo() != null && cantidad >= objetivo.getCantidadPaisesObjetivo()) {
                partida.setEstadoPartida(EstadoPartida.TERMINADA);
                partida.setGanador(jugador);
                partidaRepository.save(partida);
                return true;
            }
        }
        return false;
    }
}

