package ar.edu.utn.frc.tup.piii.Services.ServicesImpl;

import ar.edu.utn.frc.tup.piii.Dtos.JugadorDto;
import ar.edu.utn.frc.tup.piii.Entities.JugadorEntity;
import ar.edu.utn.frc.tup.piii.Entities.PartidaEntity;
import ar.edu.utn.frc.tup.piii.Repositories.JugadorRepository;
import ar.edu.utn.frc.tup.piii.Repositories.PartidaRepository;
import ar.edu.utn.frc.tup.piii.Repositories.SalaRepository;
import ar.edu.utn.frc.tup.piii.Repositories.UsuarioRepository;
import ar.edu.utn.frc.tup.piii.Services.*;
import ar.edu.utn.frc.tup.piii.models.*;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class JugadorServiceImpl implements JugadorService {
    @Autowired
    public JugadorRepository jugadorRepository;
    @Autowired
    public UsuarioService usuarioService;
    @Autowired
    public ModelMapper modelMapper;
    @Autowired
    public PartidaRepository partidaRepository;
    @Autowired
    public ObjetivoService objetivoService;

    public Jugador crearJugador(Jugador jugador, Usuario usuarioActual, Sala sala) {
        if (jugadorRepository.findByNombreAndSala_IdSala(jugador.getNombre(), sala.getIdSala()).isPresent()) {
            throw new IllegalArgumentException("Ya existe un jugador con ese nombre en esta sala.");

        }
        int maxJugadores = 6;
        List<JugadorEntity> jugadoresEnSala = jugadorRepository.findBySala_IdSala(sala.getIdSala());
        if (jugadoresEnSala.size() >= maxJugadores) {
            throw new IllegalStateException("La sala ya tiene el número máximo de jugadores permitido.");
        }
        Color colorLibre = obtenerColorDisponible(sala, null);
        if (colorLibre == null) {
            throw new IllegalStateException("No hay más colores disponibles. La sala está llena.");
        }

        jugador.setColor(colorLibre);
        jugador.setUsuario(usuarioActual);
        jugador.setTipoJugador(TipoJugador.HUMANO);
        jugador.setSala(sala);
        jugador.setEstadoJugador(EstadoJugador.ACTIVO);

        JugadorEntity jugadorEntity = modelMapper.map(jugador, JugadorEntity.class);
        JugadorEntity jugadorGuardado = jugadorRepository.save(jugadorEntity);

        return modelMapper.map(jugadorGuardado, Jugador.class);
    }

    public Jugador crearBot(Sala sala, Usuario usuarioActual) {
        if (!sala.getCreador().getIdUsuario().equals(usuarioActual.getIdUsuario())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo el creador de la sala puede agregar bots.");
        }

        int maxJugadores = 6;
        List<JugadorEntity> jugadoresEnSala = jugadorRepository.findBySala_IdSala(sala.getIdSala());
        if (jugadoresEnSala.size() >= maxJugadores) {
            throw new IllegalStateException("La sala ya tiene el número máximo de jugadores permitido.");
        }

        Color colorLibre = obtenerColorDisponible(sala, null);
        if (colorLibre == null) {
            return null;
        }

        Jugador bot = new Jugador();
        bot.setIdJugador(null);
        bot.setNombre("Bot_" + UUID.randomUUID().toString().substring(0, 8));
        bot.setTipoJugador(TipoJugador.BOT);
        bot.setColor(colorLibre);
        bot.setPartida(null);
        bot.setPerdio(false);
        bot.setUsuario(usuarioActual);

        Sala salaStub = new Sala();
        salaStub.setIdSala(sala.getIdSala());
        bot.setSala(salaStub);

        JugadorEntity jugadorEntity = modelMapper.map(bot, JugadorEntity.class);
        jugadorEntity = jugadorRepository.save(jugadorEntity);

        bot.setSala(sala);

        return modelMapper.map(jugadorEntity, Jugador.class);
    }

    public Color obtenerColorDisponible(Sala sala, Long excluirIdJugador) {
        List<Color> todosLosColores = Arrays.asList(Color.values());

        Collections.shuffle(todosLosColores);

        List<JugadorEntity> jugadores = jugadorRepository.findBySala_IdSala(sala.getIdSala());

        Set<Color> coloresUsados = jugadores.stream()
                .filter(j -> excluirIdJugador == null || j.getIdJugador() != excluirIdJugador)
                .map(JugadorEntity::getColor)
                .collect(Collectors.toSet());

        return todosLosColores.stream()
                .filter(c -> !coloresUsados.contains(c))
                .findFirst()
                .orElse(null);
    }

    // public Jugador eliminarJugador(Long idJugador, Long idUsuarioCreador) {
    // JugadorEntity jugador = jugadorRepository.findByIdJugador(idJugador)
    // .orElseThrow(() -> new IllegalArgumentException("Jugador no encontrado"));
    //
    // SalaEntity sala = jugador.getSala();
    // if (!sala.getCreador().getIdUsuario().equals(idUsuarioCreador)) {
    // throw new IllegalArgumentException("Solo el creador puede eliminar
    // jugadores");
    // }
    //
    // jugador.setEstadoJugador(EstadoJugador.ELIMINADO);
    // jugadorRepository.save(jugador);
    //
    // PartidaEntity partidaEntity = jugador.getPartida();
    //
    // if (partidaEntity != null) {
    // Partida partida = modelMapper.map(partidaEntity, Partida.class);
    // partidaService.verificarCondicionVictoria(partida);
    // estadisticaService.registrarEvento(
    // "Jugador eliminado por el creador",
    // modelMapper.map(jugador, Jugador.class),
    // partida
    // );
    // }
    //
    // return modelMapper.map(jugador, Jugador.class);
    // }

    @Override
    @Transactional
    public void votarPausa(Long idJugador) {
        JugadorEntity jugador = jugadorRepository.findById(idJugador)
                .orElseThrow(() -> new RuntimeException("Jugador no encontrado"));

        jugador.setAceptoPausa(true);
        jugadorRepository.save(jugador);

        Long partidaId = jugador.getPartida().getIdPartida();

        List<JugadorEntity> jugadores = jugadorRepository.findHumanosByPartidaId(partidaId);

        boolean todosAceptaron = jugadores.stream().allMatch(JugadorEntity::isAceptoPausa);

        if (todosAceptaron) {
            PartidaEntity partida = jugador.getPartida();
            partida.setEstadoPartida(EstadoPartida.PAUSADA);
            partidaRepository.save(partida);
        }
    }

    @Override
    @Transactional
    public void votarReanudar(Long idJugador) {
        JugadorEntity jugador = jugadorRepository.findById(idJugador)
                .orElseThrow(() -> new RuntimeException("Jugador no encontrado"));

        jugador.setAceptoRenudar(true);
        jugador.setAceptoPausa(false);

        jugadorRepository.save(jugador);

        Long partidaId = jugador.getPartida().getIdPartida();

        List<JugadorEntity> jugadores = jugadorRepository.findHumanosByPartidaId(partidaId);

        boolean todosAceptaron = jugadores.stream().allMatch(JugadorEntity::isAceptoRenudar);

        if (todosAceptaron) {
            PartidaEntity partida = jugador.getPartida();
            partida.setEstadoPartida(EstadoPartida.EN_JUEGO);
            partidaRepository.save(partida);
        }
    }

    @Override
    @Transactional
    public void finalizarPartida(Long idJugador) {
        JugadorEntity jugador = jugadorRepository.findById(idJugador)
                .orElseThrow(() -> new RuntimeException("Jugador no encontrado"));

        jugador.setFinalizarPartida(true);
        jugador.setPerdio(true);
        jugador.setEstadoJugador(EstadoJugador.ELIMINADO);
        jugadorRepository.save(jugador);

        Long partidaId = jugador.getPartida().getIdPartida();

        List<JugadorEntity> humanos = jugadorRepository.findHumanosByPartidaId(partidaId);
        boolean todosAceptaron = humanos.stream().allMatch(JugadorEntity::isFinalizarPartida);

        if (todosAceptaron) {
            List<JugadorEntity> bots = jugadorRepository.findBotByPartidaId(partidaId);

            for (JugadorEntity bot : bots) {
                bot.setEstadoJugador(EstadoJugador.ELIMINADO);
                bot.setPerdio(true);
                bot.setFinalizarPartida(true);
                jugadorRepository.save(bot);
            }

            PartidaEntity partida = jugador.getPartida();
            partida.setEstadoPartida(EstadoPartida.TERMINADA);
            partidaRepository.save(partida);
        }
    }

    public List<JugadorDto> obtenerJugadoresPorSala(Long idSala) {
        List<JugadorEntity> jugadores = jugadorRepository.findBySala_IdSala(idSala);

        return jugadores.stream()
                .map(jugador -> modelMapper.map(jugador, JugadorDto.class))
                .collect(Collectors.toList());

    }

}
