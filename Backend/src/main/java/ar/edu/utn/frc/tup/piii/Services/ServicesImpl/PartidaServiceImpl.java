package ar.edu.utn.frc.tup.piii.Services.ServicesImpl;

import ar.edu.utn.frc.tup.piii.Dtos.*;
import ar.edu.utn.frc.tup.piii.Entities.*;
import ar.edu.utn.frc.tup.piii.Repositories.*;
import ar.edu.utn.frc.tup.piii.Services.*;
import ar.edu.utn.frc.tup.piii.models.*;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import ar.edu.utn.frc.tup.piii.Entities.JugadorEntity;
import ar.edu.utn.frc.tup.piii.Repositories.JugadorRepository;
import ar.edu.utn.frc.tup.piii.Repositories.PartidaRepository;
import ar.edu.utn.frc.tup.piii.Services.PartidaService;
import ar.edu.utn.frc.tup.piii.models.Partida;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

import java.util.stream.Collectors;

@Service
public class PartidaServiceImpl implements PartidaService {

        @Autowired
        private PartidaRepository partidaRepository;
        @Autowired
        private SalaRepository salaRepository;
        @Autowired
        private JugadorRepository jugadorRepository;
        @Autowired
        private EstadoTarjetaService estadoTarjetaService;
        @Autowired
        private ModelMapper modelMapper;
        @Autowired
        private UsuarioRepository usuarioRepository;
        @Autowired
        private EstadisticaService estadisticaService;
        @Autowired
        private EstadoPaisService estadoPaisService;
        @Autowired
        private ObjetivoService objetivoService;
        @Autowired
        private TurnoService turnoService;

        @Transactional
        public Partida crearPartidaYAsignarJugadores(Long idSala, Usuario usuarioActual) {
                SalaEntity sala = salaRepository.findById(idSala)
                                .orElseThrow(() -> new RuntimeException("Sala no encontrada"));

                if (!sala.getCreador().getIdUsuario().equals(usuarioActual.getIdUsuario())) {
                        throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                                        "Solo el creador de la sala puede iniciar la partida.");
                }

                sala.setEstado(EstadoSala.INICIADA);
                salaRepository.save(sala);

                List<JugadorEntity> jugadores = jugadorRepository.findBySala_IdSala(idSala);
                if (jugadores.size() < 2) {
                        throw new IllegalArgumentException("Se necesitan mínimo 2 jugadores para iniciar la partida.");
                }
                PartidaEntity partidaEntity = new PartidaEntity();
                partidaEntity.setFechaInicio(LocalDate.now());
                partidaEntity.setEstadoPartida(EstadoPartida.EN_JUEGO);
                partidaEntity.setConfiguracion(sala);
                partidaEntity.setJugadores(jugadores);
                partidaEntity.setTurnoActual(1);

                objetivoService.asignarObjetivosSecretos(jugadores);
                partidaRepository.save(partidaEntity);
                estadoPaisService.inicializarEstadosPaises(partidaEntity.getIdPartida());
                estadoPaisService.repartirPaises(partidaEntity.getIdPartida());
                estadoTarjetaService.inicializarTarjetas(partidaEntity.getIdPartida());

                for (JugadorEntity jugador : jugadores) {
                        jugador.setPartida(partidaEntity);
                        jugadorRepository.save(jugador);
                }

                turnoService.establecerOrdenJugadores(partidaEntity.getIdPartida());

                Partida partida = new Partida();
                partida.setIdPartida(partidaEntity.getIdPartida());
                partida.setFechaInicio(partidaEntity.getFechaInicio());
                partida.setEstadoPartida(partidaEntity.getEstadoPartida());
                partida.setTurnoActual(partidaEntity.getTurnoActual());

                if (partidaEntity.getConfiguracion() != null) {
                        partida.setIdSala(partidaEntity.getConfiguracion().getIdSala());
                }

                List<JugadorDto> jugadoresDto = jugadores.stream().map(j -> {
                        JugadorDto dto = new JugadorDto();
                        dto.setIdJugador(j.getIdJugador());
                        dto.setNombre(j.getNombre());
                        return dto;
                }).toList();

                partida.setJugadores(jugadoresDto);

                return partida;
        }

        @Override
        public List<Partida> listarPartidasDisponibles() {
                List<PartidaEntity> entidades = partidaRepository.findByEstadoPartida(EstadoPartida.EN_JUEGO);
                return entidades.stream()
                                .map(ent -> modelMapper.map(ent, Partida.class))
                                .toList();
        }

        @Override
        public Partida unirseAPartida(Long idUsuario, Long idPartida) {
                PartidaEntity partida = partidaRepository.findById(idPartida)
                                .orElseThrow(() -> new RuntimeException("Partida no encontrada"));

                UsuarioEntity usuario = usuarioRepository.findById(idUsuario)
                                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

                boolean yaEsJugador = partida.getConfiguracion().getJugadores().stream()
                                .anyMatch(j -> j.getUsuario().getIdUsuario().equals(idUsuario));

                if (partida.getEstadoPartida() == EstadoPartida.EN_JUEGO) {
                        throw new RuntimeException("No se puede unir a una partida en curso.");
                }

                if (yaEsJugador) {
                        throw new RuntimeException("El usuario ya está en la partida");
                }

                JugadorEntity nuevoJugador = new JugadorEntity();
                nuevoJugador.setUsuario(usuario);
                nuevoJugador.setSala(partida.getConfiguracion());
                nuevoJugador.setNombre(usuario.getUsuario());
                nuevoJugador.setPerdio(false);
                jugadorRepository.save(nuevoJugador);

                partida.getConfiguracion().getJugadores().add(nuevoJugador);
                partidaRepository.save(partida);

                return modelMapper.map(partida, Partida.class);
        }

        @Transactional
        public PartidaDto cargarPartidaBySala(String url, Long idUsuario) {
                SalaEntity sala = salaRepository.findByUrl(url)
                                .orElseThrow(() -> new IllegalArgumentException(
                                                "No se encontró la sala con URL: " + url));
                boolean esJugador = sala.getJugadores().stream()
                                .anyMatch(jugador -> jugador.getUsuario().getIdUsuario().equals(idUsuario));

                if (!esJugador) {
                        throw new IllegalArgumentException("El usuario no pertenece a esta partida.");
                }
                if (sala.getEstado() == EstadoSala.ESPERANDO) {
                        throw new IllegalArgumentException("La partida no está iniciada");
                }

                PartidaEntity partida = partidaRepository.findByConfiguracion_IdSala(sala.getIdSala())
                                .orElseThrow(() -> new IllegalArgumentException(
                                                "No se encontró la partida activa para esta sala."));

                return cargarPartida(partida.getIdPartida());
        }

        @Override
        @Transactional
        public PartidaDto cargarPartida(Long idPartida) {
                PartidaEntity partidaEntity = partidaRepository.findById(idPartida)
                                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                                                "Partida con ID " + idPartida + " no encontrada"));

                PartidaDto partida = new PartidaDto();
                partida.setIdPartida(partidaEntity.getIdPartida());
                partida.setFechaInicio(partidaEntity.getFechaInicio());
                partida.setEstado(partidaEntity.getEstadoPartida());
                partida.setTurnoActual(partidaEntity.getTurnoActual());
                partida.setGanador(
                                partidaEntity.getGanador() != null
                                                ? modelMapper.map(partidaEntity.getGanador(), JugadorDto.class)
                                                : null);

                // partida.setConfiguracion(
                // new SalaDto().builder()
                // .idSala(partida.getConfiguracion().getIdSala())
                // .nombreSala(partida.getConfiguracion().getNombreSala())
                // .url(partida.getConfiguracion().getUrl())
                // .build()
                // );

                partida
                                .setEstadoPaises(
                                                partidaEntity.getEstadoPaises().stream()
                                                                .map(e -> new EstadoPaisDto().builder()
                                                                                .id(e.getIdEstadoPais())
                                                                                .idJugador(e.getJugador() != null ? e
                                                                                                .getJugador()
                                                                                                .getIdJugador() : null)
                                                                                .pais(
                                                                                                new Pais().builder()
                                                                                                                .idPais(e.getPais()
                                                                                                                                .getIdPais())
                                                                                                                .nombre(e.getPais()
                                                                                                                                .getNombre())
                                                                                                                .continente(
                                                                                                                                new Continente().builder()
                                                                                                                                                .idContinente(e.getPais()
                                                                                                                                                                .getContinente()
                                                                                                                                                                .getIdContinente())
                                                                                                                                                .nombre(e.getPais()
                                                                                                                                                                .getContinente()
                                                                                                                                                                .getNombre())
                                                                                                                                                .build())
                                                                                                                .build())
                                                                                .cantidadTropas(e.getCantidadTropas())
                                                                                .build())
                                                                .collect(Collectors.toList()));

                partida.setTurnos(
                                partidaEntity.getTurnos().stream()
                                                .map(t -> new TurnoDto().builder()
                                                                .idTurno(t.getIdTurno())
                                                                .nroTurno(t.getNroTurno())
                                                                .fase(t.getFase())
                                                                .idJugador(t.getJugador().getIdJugador())
                                                                .build())
                                                .collect(Collectors.toList()));

                partida.setEstadoTarjetas(
                                partidaEntity.getMazo().stream()
                                                .map(et -> new EstadoTarjetaDto().builder()
                                                                .idEstadoTarjeta(et.getIdEstadoTarjeta())
                                                                .idJugador(et.getJugador() != null
                                                                                ? et.getJugador().getIdJugador()
                                                                                : null)
                                                                .tarjeta(new TarjetaDto().builder()
                                                                                .idTarjeta(et.getTarjeta()
                                                                                                .getIdTarjeta())
                                                                                .simbolo(String.valueOf(et.getTarjeta()
                                                                                                .getSimbolo()))
                                                                                .pais(modelMapper.map(et.getTarjeta()
                                                                                                .getPais(), Pais.class))
                                                                                .build())
                                                                .usada(et.isUsada())
                                                                .canjeada(et.isCanjeada())
                                                                .build())
                                                .collect(Collectors.toList()));

                java.util.Map<Long, Long> tarjetasPorJugador = partidaEntity.getMazo().stream()
                                .filter(et -> et.getJugador() != null && !et.isCanjeada())
                                .collect(Collectors.groupingBy(
                                                et -> et.getJugador().getIdJugador(),
                                                Collectors.counting()));

                partida.setJugadores(
                                partidaEntity.getJugadores().stream()
                                                .map(j -> {
                                                        JugadorDto dto = modelMapper.map(j, JugadorDto.class);
                                                        dto.setUrl(j.avatarUrl());
                                                        dto.setCantidadTarjetas(
                                                                        tarjetasPorJugador.getOrDefault(j.getIdJugador(), 0L).intValue());
                                                        return dto;
                                                })
                                                .collect(Collectors.toList()));

                return partida;
        }

        @Override
        public Partida guardarPartida(Partida partida) {
                PartidaEntity entidad = modelMapper.map(partida, PartidaEntity.class);
                entidad = partidaRepository.save(entidad);
                return modelMapper.map(entidad, Partida.class);
        }

        @Override
        public Partida obtenerEstadoPartida(Long idPartida) {
                PartidaEntity partida = partidaRepository.findById(idPartida)
                                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                                                "Estado de partida con ID " + idPartida + " no encontrada"));
                return modelMapper.map(partida, Partida.class);
        }

        @Override
        public boolean finalizarPartida(Long idPartida) {
                PartidaEntity partida = partidaRepository.findById(idPartida)
                                .orElseThrow(() -> new RuntimeException("Partida no encontrada"));
                partida.setEstadoPartida(EstadoPartida.TERMINADA);
                partidaRepository.save(partida);
                return true;
        }

        public void verificarCondicionVictoria(Partida partida) {
                PartidaEntity partidaEntity = modelMapper.map(partida, PartidaEntity.class);

                List<JugadorEntity> jugadoresActivos = jugadorRepository.findByPartidaAndEstadoJugador(partidaEntity,
                                EstadoJugador.ACTIVO);

                if (jugadoresActivos.size() == 1) {
                        JugadorEntity ganador = jugadoresActivos.get(0);
                        ganador.setEstadoJugador(EstadoJugador.GANADOR);
                        jugadorRepository.save(ganador);

                        partidaEntity.setEstadoPartida(EstadoPartida.TERMINADA);
                        partidaRepository.save(partidaEntity);

                        estadisticaService.registrarEvento(
                                        "Victoria automática por eliminación de todos los oponentes",
                                        modelMapper.map(ganador, Jugador.class),
                                        modelMapper.map(partidaEntity, Partida.class));
                }

        }

        @Transactional
        public PartidaDto obtenerPartidaActivaPorUrlYUsuario(String url, Long idUsuario) {
                SalaEntity sala = salaRepository.findByUrl(url)
                                .orElseThrow(() -> new IllegalArgumentException(
                                                "No se encontró la sala con URL: " + url));

                if (sala.getEstado() == EstadoSala.ESPERANDO) {
                        throw new IllegalArgumentException("La partida no está iniciada");
                }

                PartidaEntity partida = partidaRepository.findByConfiguracion_IdSala(sala.getIdSala())
                                .orElseThrow(() -> new IllegalArgumentException(
                                                "No se encontró la partida activa para esta sala."));

                boolean esJugador = sala.getJugadores().stream()
                                .anyMatch(jugador -> jugador.getUsuario().getIdUsuario().equals(idUsuario));

                if (!esJugador) {
                        throw new IllegalArgumentException("El usuario no pertenece a esta partida.");
                }

                PartidaDto dto = new PartidaDto();
                dto.setIdPartida(partida.getIdPartida());
                dto.setEstado(partida.getEstadoPartida());
                dto.setFechaInicio(partida.getFechaInicio());
                dto.setTurnoActual(partida.getTurnoActual() != null ? partida.getTurnoActual() : 0);

                SalaDto salaDto = new SalaDto();
                salaDto.setIdSala(sala.getIdSala());
                salaDto.setNombreSala(sala.getNombreSala());
                salaDto.setUrl(sala.getUrl());
                dto.setConfiguracion(salaDto);
                return dto;
        }

}
