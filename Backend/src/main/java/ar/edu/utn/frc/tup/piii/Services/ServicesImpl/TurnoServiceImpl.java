package ar.edu.utn.frc.tup.piii.Services.ServicesImpl;

import ar.edu.utn.frc.tup.piii.Dtos.*;
import ar.edu.utn.frc.tup.piii.Dtos.EstadoPaises.*;
import ar.edu.utn.frc.tup.piii.Dtos.PartidaEventDto;
import ar.edu.utn.frc.tup.piii.Entities.*;
import ar.edu.utn.frc.tup.piii.Repositories.*;
import ar.edu.utn.frc.tup.piii.Services.*;
import ar.edu.utn.frc.tup.piii.models.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.Comparator;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TurnoServiceImpl implements TurnoService {
    private final TurnoRepository turnoRepository;
    private final EstadoTarjetaService estadoTarjetaService;
    private final EstadoTarjetaRepository estadoTarjetaRepository;
    private final ContinenteRepository continenteRepository;
    private final PaisRepository paisRepository;
    private final PartidaRepository partidaRepository;
    private final EstadoPaisRepository estadoPaisRepository;
    private final EstadoPaisService estadoPaisService;
    private final ModelMapper modelMapper;
    private final JugadorRepository jugadorRepository;
    private final ObjetivoService objetivoService;
    private final LimiteRepository limiteRepository;
    private final SimpMessagingTemplate messagingTemplate;

    /** @Lazy rompe la dependencia circular: TurnoService ↔ BotService */
    @Lazy
    @Autowired
    private BotService botService;

    @Override
    @Transactional
    public Turno obtenerTurno(Long idTurno) {
        Optional<TurnoEntity> turnoOpt = turnoRepository.findById(idTurno);
        if (turnoOpt.isEmpty()) {
            throw new RuntimeException("El turno con ID: " + idTurno + " no existe.");
        }
        return mapToTurno(turnoOpt.get());
    }

    @Override
    @Transactional
    public boolean cambiarFaseTurno(Long idPartida) {
        PartidaEntity partidaEntity = partidaRepository.findById(idPartida)
                .orElseThrow(() -> new RuntimeException("Partida no encontrada"));

        int turnoActualNro = partidaEntity.getTurnoActual();

        TurnoEntity turnoE = partidaEntity.getTurnos().stream()
                .filter(t -> t.getNroTurno() == turnoActualNro)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No se encontró el turno actual"));

        FaseTurno faseActual = turnoE.getFase();
        boolean cambioTurno = false;

        JugadorEntity jugadorActual = jugadorRepository.findById(turnoE.getJugador().getIdJugador())
                .orElseThrow(() -> new RuntimeException("Jugador no encontrado"));

        Long siguienteBot = 0L;
        switch (faseActual) {
            case INCORPORACION -> turnoE.setFase(FaseTurno.ATAQUE);
            case ATAQUE -> turnoE.setFase(FaseTurno.REAGRUPACION);
            case REAGRUPACION -> {
                verificarGanador(turnoE.getJugador().getIdJugador());
                turnoE.setFase(FaseTurno.INCORPORACION);
                turnoE.getJugador().setConsquisto(false);
                cambioTurno = true;

                if (turnoE.getJugador() == null) {
                    throw new RuntimeException("El turno actual no tiene jugador asignado.");
                }

                siguienteBot = pasarTurno(idPartida);

            }

            default -> throw new RuntimeException("Fase desconocida: " + faseActual);
        }

        turnoRepository.save(turnoE);
        partidaRepository.save(partidaEntity);

        // Broadcast FIN_TURNO cuando se termina la fase REAGRUPAR
        if (cambioTurno) {
            PartidaEventDto finTurno = new PartidaEventDto();
            finTurno.setTipo("FIN_TURNO");
            finTurno.setJugadorNombre(jugadorActual.getNombre());
            finTurno.setJugadorColor(jugadorActual.getColor() != null ? jugadorActual.getColor().name() : "");
            finTurno.setDescripcion("Fin del turno de " + jugadorActual.getNombre());
            finTurno.setIdPartida(idPartida);
            messagingTemplate.convertAndSend("/topic/partida." + idPartida + ".evento", finTurno);
        }

        if (siguienteBot != 0L) {
            botService.executeTurnAsync(siguienteBot);
        }
        return cambioTurno;
    }

    public Boolean SiguienteNumeroTurno(int turnoActual, Long idPartida) {
        return turnoRepository.findByNroTurnoAndPartida_IdPartida(turnoActual, idPartida).isPresent();
    }

    @Transactional
    public TurnoEntity crearTurno(JugadorEntity jugador, int nroTurno, Long idPartida) {
        PartidaEntity partida = partidaRepository.findById(idPartida)
                .orElseThrow(() -> new RuntimeException("Partida no encontrada"));

        TurnoEntity turnoE = new TurnoEntity();
        turnoE.setJugador(jugador);
        turnoE.setFase(FaseTurno.INCORPORACION);
        turnoE.setNroTurno(nroTurno);
        turnoE.setInicio(LocalDateTime.now());
        turnoE.setPartida(partida);
        turnoE.setReagrupado(false);

        // Calcular fichas al inicio del turno
        if (partida.getHostilidad()) {
            calcularCantidadFichasPorOcupacion(jugador.getIdJugador());
        }

        return turnoRepository.save(turnoE);
    }

    @Transactional
    @Override
    public void establecerOrdenJugadores(Long idPartida) {

        PartidaEntity partida = partidaRepository.findById(idPartida)
                .orElseThrow(() -> new RuntimeException("Partida no encontrada"));

        List<TurnoEntity> turnosEntity = new ArrayList<>();
        int nroTurno = 1;

        for (JugadorEntity jugador : partida.getJugadores()) {
            TurnoEntity turno = crearTurno(jugador, nroTurno, idPartida);
            turnosEntity.add(turno);
            jugador.setTurno(turno);
            nroTurno++;
        }
        for (JugadorEntity jugador : partida.getJugadores()) {
            System.out.println(
                    "Jugador en partida: id=" + jugador.getIdJugador() + ", nombre='" + jugador.getNombre() + "'");
        }
        turnoRepository.saveAll(turnosEntity);
        partida.setTurnoActual(1);
        partida.setTurnos(turnosEntity);
        partidaRepository.save(partida);

        List<Turno> turnos = turnosEntity.stream()
                .map(this::mapToTurno)
                .collect(Collectors.toList());
        for (Turno turno : turnos) {
            System.out.println("Turno: jugador id=" + turno.getJugador().getIdJugador() + ", nombre='"
                    + turno.getJugador().getNombre() + "'");
        }
        for (JugadorEntity jugador : partida.getJugadores()) {
            jugador.setEjercito(5);
            jugadorRepository.save(jugador);
        }
    }

    @Transactional
    public Long pasarTurno(Long idPartida) {
        PartidaEntity partidaEntity = partidaRepository.findById(idPartida)
                .orElseThrow(() -> new RuntimeException("Partida no encontrada"));

        if (partidaEntity.getEstadoPartida() != EstadoPartida.EN_JUEGO) {
            return 0L;
        }

        List<TurnoEntity> turnosEntity = partidaEntity.getTurnos();
        if (turnosEntity.isEmpty()) {
            throw new RuntimeException("No hay turnos en la partida");
        }

        TurnoEntity turnoActual = turnosEntity.stream()
                .filter(t -> t.getNroTurno() == partidaEntity.getTurnoActual())
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Turno actual no encontrado"));

        List<JugadorEntity> jugadores = turnosEntity.stream()
                .sorted(Comparator.comparingInt(TurnoEntity::getNroTurno))
                .map(TurnoEntity::getJugador)
                .distinct()
                .collect(Collectors.toList());

        JugadorEntity jugadorActual = turnoActual.getJugador();
        int indexActual = jugadores.indexOf(jugadorActual);
        if (indexActual == -1) {
            throw new RuntimeException("Jugador actual no encontrado en la lista");
        }

        int indexSiguiente = (indexActual + 1) % jugadores.size();
        JugadorEntity jugadorSiguiente = jugadores.get(indexSiguiente);

        int nroTurnoSiguiente = turnoActual.getNroTurno() + 1;

        if (!SiguienteNumeroTurno(nroTurnoSiguiente, idPartida)) {
            TurnoEntity turnoNuevo = crearTurno(jugadorSiguiente, nroTurnoSiguiente, idPartida);
            partidaEntity.getTurnos().add(turnoNuevo);
            turnoRepository.save(turnoNuevo);
        }

        partidaEntity.setTurnoActual(nroTurnoSiguiente);
        partidaRepository.save(partidaEntity);

        int cantidadJugadores = jugadores.size();
        if (nroTurnoSiguiente == cantidadJugadores + 1) {
            List<Turno> turnosPrimeraVuelta = partidaEntity.getTurnos().stream()
                    .filter(t -> t.getNroTurno() <= cantidadJugadores)
                    .map(this::mapToTurno)
                    .collect(Collectors.toList());

            ejecutarSegundaVuelta(turnosPrimeraVuelta);
        }

        if (jugadorSiguiente.getTipoJugador().equals(TipoJugador.BOT)) {
            return jugadorSiguiente.getIdJugador();
        }

        return 0L;
    }

    @Transactional
    @Override
    public VerificacionObjetivoDto verificarGanador(Long idJugador) {
        JugadorEntity jugador = jugadorRepository.findById(idJugador).orElseThrow(
                () -> new EntityNotFoundException("Jugador no encontrado."));
        PartidaEntity partida = partidaRepository.findById(jugador.getPartida().getIdPartida()).orElseThrow(
                () -> new EntityNotFoundException("Partida no encontrado."));

        VerificacionObjetivoDto response = objetivoService.verificarObjetivos(idJugador);

        if (response.isGano()) {
            partida.setGanador(jugador);
            partida.setEstadoPartida(EstadoPartida.TERMINADA);
            partidaRepository.save(partida);
        }

        return response;
    }

    @Transactional
    @Override
    public boolean validarAtaque(Pais origen, Pais destino, Jugador jugador) {
        JugadorEntity jugadorEntity = jugadorRepository.findById(jugador.getIdJugador())
                .orElseThrow(() -> new IllegalArgumentException("Jugador no encontrado"));
        Long idPartida = jugadorEntity.getPartida().getIdPartida();

        Optional<EstadoPaisEntity> estadoOrigen = estadoPaisRepository
                .findByPaisIdPaisAndJugadorIdJugador(origen.getIdPais(), jugador.getIdJugador());
        Optional<EstadoPaisEntity> estadoDestino = estadoPaisRepository
                .findByPais_IdPaisAndPartida_IdPartida(destino.getIdPais(), idPartida);

        // Ambos países deben existir
        if (estadoOrigen.isEmpty() || estadoDestino.isEmpty()) {
            return false;
        }

        EstadoPaisEntity eOrigen = estadoOrigen.get();
        EstadoPaisEntity eDestino = estadoDestino.get();

        // No puede atacar su propio país
        if (eDestino.getJugador() != null &&
                eDestino.getJugador().getIdJugador().equals(jugador.getIdJugador())) {
            return false;
        }

        // Los países deben ser limítrofes
        if (!estadoPaisService.sonLimitrofes(eOrigen.getPais().getIdPais(), eDestino.getPais().getIdPais())) {
            return false;
        }

        // Necesita al menos 2 tropas para atacar
        if (eOrigen.getCantidadTropas() < 2) {
            return false;
        }

        return true;
    }

    @Transactional
    @Override
    public boolean validarMovimiento(Pais origen, Pais destino, Jugador jugador) {
        Optional<EstadoPaisEntity> estadoOrigen = estadoPaisRepository
                .findByPaisIdPaisAndJugadorIdJugador(origen.getIdPais(), jugador.getIdJugador());
        Optional<EstadoPaisEntity> estadosDestino = estadoPaisRepository
                .findByPaisIdPaisAndJugadorIdJugador(destino.getIdPais(), jugador.getIdJugador());

        // Ambos países deben pertenecer al jugador
        if (estadoOrigen.isEmpty() || estadosDestino.isEmpty()) {
            return false;
        }

        EstadoPaisEntity eOrigen = estadoOrigen.get();
        EstadoPaisEntity eDestino = estadosDestino.get();

        // Los países deben ser limítrofes
        if (!estadoPaisService.sonLimitrofes(eOrigen.getPais().getIdPais(), eDestino.getPais().getIdPais())) {
            return false;
        }

        // El origen debe tener al menos 2 tropas (siempre deja 1 fija)
        if (eOrigen.getCantidadTropas() < 2) {
            return false;
        }

        return true;
    }

    @Transactional
    @Override
    public boolean verificarOrdenAcciones(Jugador jugador, String accion) {
        JugadorEntity jugadorE = modelMapper.map(jugador, JugadorEntity.class);
        TurnoEntity turno = turnoRepository.findByJugador(jugadorE);
        FaseTurno faseActual = turno.getFase();

        switch (faseActual) {
            case INCORPORACION:
                return accion.equals("Incorporacion");
            case ATAQUE:
                return accion.equals("Ataque");
            case REAGRUPACION:
                return accion.equals("Reagrupacion");
            default:
                return false;
        }
    }

    @Transactional
    @Override
    public void gestionarTimeoutTurno(Long idTurno) {
        TurnoEntity turno = turnoRepository.findById(idTurno)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Turno no encontrado."));

        PartidaEntity partidaEntity = partidaRepository.findById(turno.getPartida().getIdPartida())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Partida no encontrado."));

        if (Duration.between(turno.getInicio(), LocalDateTime.now()).toMinutes() >= 5) {
            // jugador.setEstadoJugador(EstadoJugador.INACTIVO);

            if (partidaEntity.getFaseActual() == FaseJuego.COLOCACION) {

            } else if (partidaEntity.getFaseActual() == FaseJuego.HOSTILIDADES) {
                // pasarTurno(partidaEntity.getIdPartida());
            }
        }
    }

    @Transactional
    @Override
    public Boolean moverFichas(MoverFichas moverFichas) {
        JugadorEntity jugador = jugadorRepository.findById(moverFichas.getIdJugador())
                .orElseThrow(() -> new EntityNotFoundException("Jugador no encontrado."));

        PartidaEntity partida = partidaRepository.findById(jugador.getPartida().getIdPartida())
                .orElseThrow(() -> new EntityNotFoundException("Partida no encontrada."));

        TurnoEntity turnoActual = turnoRepository
                .findByNroTurnoAndPartida_IdPartida(partida.getTurnoActual(), partida.getIdPartida())
                .orElseThrow(() -> new IllegalStateException("No hay turno actual para la partida."));

        if (!jugador.getIdJugador().equals(turnoActual.getJugador().getIdJugador())) {
            throw new IllegalArgumentException("Turno no correspondiente.");
        }

        if (!turnoActual.getFase().equals(FaseTurno.REAGRUPACION)) {
            throw new IllegalArgumentException("Fase no correspondiente.");
        }

        if (turnoActual.isReagrupado()) {
            throw new IllegalArgumentException("Ya reagrupaste tropas en este turno. Solo se permite reagrupar una vez por turno.");
        }

        // Validar conectividad: origen y destino deben estar conectados a través de territorio propio
        if (!esConectadoPorTerritorioPropio(moverFichas.getIdPaisOrigen(), moverFichas.getIdPaisDestino(),
                moverFichas.getIdJugador(), partida.getIdPartida())) {
            throw new IllegalArgumentException("Los países no están conectados a través de tu territorio.");
        }

        var rta = estadoPaisService.agrupacionFichas(moverFichas);

        if (rta) {
            turnoActual.setReagrupado(true);
            turnoRepository.save(turnoActual);

            PartidaEventDto evento = new PartidaEventDto();
            evento.setTipo("REAGRUPAMIENTO");
            evento.setJugadorNombre(jugador.getNombre());
            evento.setJugadorColor(jugador.getColor() != null ? jugador.getColor().name() : "");
            evento.setDescripcion(jugador.getNombre() + " reagrupó tropas");
            evento.setIdPartida(partida.getIdPartida());
            messagingTemplate.convertAndSend("/topic/partida." + partida.getIdPartida() + ".evento", evento);
        }

        return rta;
    }

    /**
     * BFS: verifica si existe un camino entre origen y destino
     * pasando únicamente por países del mismo jugador.
     */
    private boolean esConectadoPorTerritorioPropio(Long idOrigen, Long idDestino,
            Long idJugador, Long idPartida) {
        if (idOrigen.equals(idDestino)) return false;

        Set<Long> visited = new HashSet<>();
        Queue<Long> queue = new LinkedList<>();
        queue.add(idOrigen);
        visited.add(idOrigen);

        while (!queue.isEmpty()) {
            Long current = queue.poll();

            // Obtener todos los países limítrofes del actual
            List<LimiteEntity> limites = limiteRepository
                    .findByPais1_IdPaisOrPais2_IdPais(current, current);
            Set<Long> adjacentIds = limites.stream()
                    .map(l -> l.getPais1().getIdPais().equals(current)
                            ? l.getPais2().getIdPais()
                            : l.getPais1().getIdPais())
                    .collect(Collectors.toSet());

            // Filtrar solo los que son del jugador en esta partida
            List<EstadoPaisEntity> ownedAdjacent = estadoPaisRepository
                    .findAllByPais_IdPaisInAndPartida_IdPartida(adjacentIds, idPartida)
                    .stream()
                    .filter(ep -> ep.getJugador() != null
                            && ep.getJugador().getIdJugador().equals(idJugador))
                    .collect(Collectors.toList());

            for (EstadoPaisEntity adj : ownedAdjacent) {
                Long adjId = adj.getPais().getIdPais();
                if (adjId.equals(idDestino)) return true;
                if (!visited.contains(adjId)) {
                    visited.add(adjId);
                    queue.add(adjId);
                }
            }
        }
        return false;
    }

    @Override
    public List<EstadoPaisDto> getDestinosReagrupamiento(Long idPaisOrigen, Long idJugador, Long idPartida) {
        List<EstadoPaisEntity> todosPropios = estadoPaisRepository
                .findAllByJugador_IdJugadorAndPartida_IdPartida(idJugador, idPartida);
        return todosPropios.stream()
                .filter(ep -> !ep.getPais().getIdPais().equals(idPaisOrigen))
                .filter(ep -> esConectadoPorTerritorioPropio(idPaisOrigen, ep.getPais().getIdPais(), idJugador, idPartida))
                .map(ep -> modelMapper.map(ep, EstadoPaisDto.class))
                .collect(Collectors.toList());
    }

    @Transactional
    @Override
    public TarjetaDto entregarTarjetaSiCorresponde(Long idJugador, Long idPartida) {
        JugadorEntity jugador = jugadorRepository.findById(idJugador)
                .orElseThrow(() -> new IllegalArgumentException("Jugador no encontrado"));
        PartidaEntity partida = partidaRepository.findById(idPartida)
                .orElseThrow(() -> new IllegalArgumentException("Partida no encontrado"));

        // Integer turnoActual = partida.getTurnoActual();
        // if(jugador.getTurno().getNroTurno() != turnoActual){
        // throw new IllegalStateException("El turno ya cambió. Perdiste el derecho a
        // recibir tarjeta.");
        // }

        if (jugador.isConsquisto()) {
            List<EstadoTarjetaEntity> estadoTarjetaEntities = estadoTarjetaRepository
                    .findByPartida_IdPartidaAndJugadorIsNull(partida.getIdPartida());

            List<EstadoTarjetaEntity> estadoTarjetaEntitiesCopy = new ArrayList<>(estadoTarjetaEntities);

            estadoTarjetaEntitiesCopy.removeIf(estadoTarjetaEntity -> estadoTarjetaEntity.isCanjeada());

            Collections.shuffle(estadoTarjetaEntitiesCopy);

            TarjetaDto tarjeta = estadoTarjetaService.asignarTarjeta(
                    new EstadoTarjetaDto().builder()
                            .idEstadoTarjeta(estadoTarjetaEntitiesCopy.get(0).getIdEstadoTarjeta())
                            .idJugador(jugador.getIdJugador())
                            .build());

            PartidaEventDto evento = new PartidaEventDto();
            evento.setTipo("TARJETA_OBTENIDA");
            evento.setJugadorNombre(jugador.getNombre());
            evento.setJugadorColor(jugador.getColor() != null ? jugador.getColor().name() : "");
            evento.setDescripcion(jugador.getNombre() + " obtuvo una tarjeta");
            evento.setIdPartida(partida.getIdPartida());
            messagingTemplate.convertAndSend("/topic/partida." + partida.getIdPartida() + ".evento", evento);

            return tarjeta;
        } else {
            throw new IllegalArgumentException("El jugador no conquisto en esta ronda.");
        }
    }

    @Transactional
    public int calcularCantidadFichasPorOcupacion(Long idJugador) {
        JugadorEntity jugadorEntity = jugadorRepository.findById(idJugador)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Jugador no encontrado."));

        List<EstadoPaisEntity> estadoPaises = estadoPaisRepository.findByJugador_IdJugador(idJugador);
        int cantidadFichas = 0;

        cantidadFichas = Math.floorDiv(estadoPaises.size(), 2);

        for (int i = 1; i < 6; i++) {
            Long idContinente = (long) i;

            ContinenteEntity continente = continenteRepository.findById(idContinente)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Continente no encontrado."));

            if (conquistoContinente(idContinente, estadoPaises)) {

                switch (continente.getNombre()) {
                    case "Asia" -> cantidadFichas += 7;
                    case "Europa", "America del Norte" -> cantidadFichas += 5;
                    case "America del Sur", "Africa" -> cantidadFichas += 3;
                    default -> cantidadFichas += 2;
                }
            }
        }

        /*
         * List<Long> idTarjetas =
         * estadoTarjetaRepository.findByJugadorId(idJugador).stream().map(
         * EstadoTarjetaEntity::getIdEstadoTarjeta).toList();
         * Integer fichasCanje = estadoTarjetaService.canjearTarjetas(idTarjetas,
         * idJugador);
         * if (fichasCanje > 0) {
         * cantidadFichas += fichasCanje;
         * }
         */

        jugadorEntity.setEjercito(jugadorEntity.getEjercito() + cantidadFichas);
        jugadorRepository.save(jugadorEntity);
        return jugadorEntity.getEjercito();
    }

    public boolean conquistoContinente(Long idContinente, List<EstadoPaisEntity> estadoPaises) {
        List<PaisEntity> paisesPorContinente = paisRepository.findByContinente_IdContinente(idContinente);
        List<PaisEntity> paisesConquistados = estadoPaises.stream()
                .map(EstadoPaisEntity::getPais)
                .toList();
        if (new HashSet<>(paisesConquistados).containsAll(paisesPorContinente)) {
            return true;
        }
        return false;
    }

    public void ejecutarSegundaVuelta(List<Turno> turnos) {
        for (Turno turno : turnos) {
            Jugador jugador = turno.getJugador();

            JugadorEntity jugadorEntity = jugadorRepository.findById(jugador.getIdJugador())
                    .orElseThrow(() -> new RuntimeException("Jugador no encontrado con ID: " + jugador.getIdJugador()));

            int ejercitoActual = jugadorEntity.getEjercito() != null ? jugadorEntity.getEjercito() : 0;
            jugadorEntity.setEjercito(ejercitoActual + 3);

            jugadorRepository.save(jugadorEntity);
        }

        PartidaEntity partidaEntity = partidaRepository.findById(turnos.get(0).getIdPartida())
                .orElseThrow(() -> new RuntimeException("Partida no encontrada"));

        partidaEntity.setHostilidad(true);

        partidaRepository.save(partidaEntity);
    }

    @Transactional
    @Override
    public void validarUsarTarjetaEnPais(UsarTarjetaEnPaisDto usarTarjetaEnPaisDto) {
        JugadorEntity jugadorEntity = jugadorRepository.findById(usarTarjetaEnPaisDto.getIdJugador())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Jugador no encontrado."));
        Optional<EstadoTarjetaEntity> EstadoTarjetaEntity = Optional.ofNullable(estadoTarjetaRepository
                .findById(usarTarjetaEnPaisDto.getIdTarjeta())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Estado Tarjeta no encontrado.")));

        estadoTarjetaService.usarTarjetaEnPais(usarTarjetaEnPaisDto.getIdTarjeta(),
                usarTarjetaEnPaisDto.getIdJugador());
    }

    @Transactional
    @Override
    public Integer validarCanjeTarjetas(CanjeTarjetasDto canjeTarjetasDto) {
        JugadorEntity jugadorEntity = jugadorRepository.findById(canjeTarjetasDto.getIdJugador())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Jugador no encontrado."));

        // Validar que el canje solo se pueda realizar en la fase INCORPORAR
        PartidaEntity partidaParaCanje = partidaRepository.findById(jugadorEntity.getPartida().getIdPartida())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Partida no encontrada."));
        TurnoEntity turnoParaCanje = turnoRepository
                .findByNroTurnoAndPartida_IdPartida(partidaParaCanje.getTurnoActual(), partidaParaCanje.getIdPartida())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Turno no encontrado."));
        if (!turnoParaCanje.getJugador().getIdJugador().equals(canjeTarjetasDto.getIdJugador())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No es el turno del jugador.");
        }
        if (!turnoParaCanje.getFase().equals(FaseTurno.INCORPORACION)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El canje de tarjetas solo está permitido en la fase de INCORPORACION.");
        }

        List<EstadoTarjetaEntity> listEstadoTarjetaEntity = estadoTarjetaRepository
                .findAllById(canjeTarjetasDto.getIdTarjetas());
        for (EstadoTarjetaEntity estadoTarjetaEntity : listEstadoTarjetaEntity) {
            if (estadoTarjetaEntity == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Estado Tarjeta no encontrado.");
            }
        }
        Integer ejercito = estadoTarjetaService.canjearTarjetas(canjeTarjetasDto.getIdTarjetas(),
                canjeTarjetasDto.getIdJugador());
        if (ejercito < 4) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No se pudieron canjear los ejercitos.");
        }

        PartidaEventDto evento = new PartidaEventDto();
        evento.setTipo("TARJETA_CANJEADA");
        evento.setJugadorNombre(jugadorEntity.getNombre());
        evento.setJugadorColor(jugadorEntity.getColor() != null ? jugadorEntity.getColor().name() : "");
        evento.setDescripcion(jugadorEntity.getNombre() + " canjeó tarjetas y obtuvo " + ejercito + " ejércitos");
        evento.setIdPartida(jugadorEntity.getPartida().getIdPartida());
        messagingTemplate.convertAndSend("/topic/partida." + jugadorEntity.getPartida().getIdPartida() + ".evento", evento);

        return ejercito;
    }

    // private boolean desempatar(List<TurnoEntity> turnosE){
    // boolean hayEmpate = false;
    // List<Turno> turnos = turnosE.stream().map(turno -> modelMapper.map(turno,
    // Turno.class)).toList();
    // for (int i = 0; i < turnos.size(); i++) {
    // for (int j = i + 1; j < turnos.size(); j++) {
    // Turno t1 = turnos.get(i);
    // Turno t2 = turnos.get(j);
    // if (t1.getNroTurno() == t2.getNroTurno()) {
    // t1.setNroTurno((int) (Math.random() * 6) + 1);
    // t2.setNroTurno((int) (Math.random() * 6) + 1);
    // hayEmpate = true;
    // }
    // }
    // }
    // return true;
    // }

    private Turno mapToTurno(TurnoEntity turnoEntity) {
        Turno turno = new Turno();
        turno.setIdTurno(turnoEntity.getIdTurno());
        turno.setNroTurno(turnoEntity.getNroTurno());
        turno.setFase(turnoEntity.getFase());

        // Mapeo manual de Partida
        if (turnoEntity.getPartida() != null) {
            Partida partida = new Partida();
            PartidaEntity partidaEntity = turnoEntity.getPartida();

            partida.setIdPartida(partidaEntity.getIdPartida());
            partida.setFechaInicio(partidaEntity.getFechaInicio());
            partida.setTurnoActual(partidaEntity.getTurnoActual());
            partida.setEstadoPartida(partidaEntity.getEstadoPartida());

            turno.setIdPartida(partida.getIdPartida());
        }

        JugadorEntity jugadorEntity = turnoEntity.getJugador();
        if (jugadorEntity != null) {
            Jugador jugador = new Jugador();

            jugador.setIdJugador(jugadorEntity.getIdJugador());
            jugador.setNombre(jugadorEntity.getNombre());
            jugador.setColor(jugadorEntity.getColor());
            jugador.setTipoJugador(jugadorEntity.getTipoJugador());
            jugador.setEjercito(jugadorEntity.getEjercito());
            jugador.setEstadoJugador(jugadorEntity.getEstadoJugador());
            jugador.setPerdio(jugadorEntity.isPerdio());
            jugador.setAceptoPausa(jugadorEntity.isAceptoPausa());
            jugador.setAceptoRenudar(jugadorEntity.isAceptoRenudar());
            jugador.setFinalizarPartida(jugadorEntity.isFinalizarPartida());

            if (jugadorEntity.getSala() != null) {
                SalaEntity salaEntity = jugadorEntity.getSala();
                Sala sala = new Sala();
                sala.setIdSala(salaEntity.getIdSala());
                sala.setNombreSala(salaEntity.getNombreSala());
                sala.setEstado(salaEntity.getEstado());
                sala.setUrl(salaEntity.getUrl());
                jugador.setSala(sala);
            }

            turno.setJugador(jugador);
        }

        return turno;
    }

    // private TurnoEntity mapToTurno(Turno turno) {
    //
    // TurnoEntity turnoE = new TurnoEntity();
    // turnoE.setIdTurno(turno.getIdTurno());
    // turnoE.setNroTurno(turno.getNroTurno());
    // turnoE.setFase(turno.getFase());
    //
    // JugadorEntity jugador = new JugadorEntity();
    // jugador.setIdJugador(turno.getJugador().getIdJugador());
    // turnoE.setJugador(jugador);
    // turnoE.getPartida();
    //
    // return turnoE;
    // }

    @Transactional
    @Override
    public AtaqueResponseDto ataque(Ataque ataque) {
        JugadorEntity jugador = jugadorRepository.findById(ataque.getIdJugador()).orElseThrow(
                () -> new EntityNotFoundException("Jugador no encontrado"));

        PartidaEntity partida = partidaRepository.findById(jugador.getPartida().getIdPartida()).orElseThrow(
                () -> new EntityNotFoundException("Partida no encontrada"));

        TurnoEntity turnoActual = turnoRepository
                .findByNroTurnoAndPartida_IdPartida(partida.getTurnoActual(), partida.getIdPartida())
                .orElseThrow(() -> new IllegalStateException("No hay turno actual para la partida"));

        if (!jugador.getIdJugador().equals(turnoActual.getJugador().getIdJugador())) {
            throw new IllegalArgumentException("Turno no correspondiente.");
        }

        if (!turnoActual.getFase().equals(FaseTurno.ATAQUE)) {
            throw new IllegalArgumentException("No se puede atacar en la fase de " + turnoActual.getFase());
        }

        EstadoPaisEntity estadoPaisAtacante = estadoPaisRepository.findByPaisIdPaisAndJugadorIdJugador(
                ataque.getIdPaisOrigen(), ataque.getIdJugador())
                .orElseThrow(() -> new RuntimeException("El país no existe o no pertenece al atacante"));

        if (estadoPaisAtacante.getCantidadTropas() < 2) {
            throw new IllegalArgumentException("No tiene las suficientes tropas para atacar");
        }

        EstadoPaisEntity estadoPaisDefensor = estadoPaisRepository.findByPais_IdPaisAndPartida_IdPartida(
                ataque.getIdPaisDestino(), jugador.getPartida().getIdPartida())
                .orElseThrow(() -> new RuntimeException("El país atacado no existe"));

        if (estadoPaisDefensor.getJugador().getIdJugador().equals(ataque.getIdJugador())) {
            throw new IllegalArgumentException("No se puede atacar su propio país");
        }

        // Capturar info del defensor ANTES de que cambiarPropietario modifique la entidad
        // en la primera-caché de JPA (misma sesión @Transactional)
        String defensorNombre = estadoPaisDefensor.getJugador().getNombre();
        String defensorColor = estadoPaisDefensor.getJugador().getColor() != null
                ? estadoPaisDefensor.getJugador().getColor().name() : "";

        int cantidadDefensor = calcularCantidadDados(estadoPaisDefensor.getCantidadTropas(), false);
        int maxAtacante = calcularCantidadDados(estadoPaisAtacante.getCantidadTropas(), true);
        int cantidadAtacante = (ataque.getCantDadosAtacante() != null && ataque.getCantDadosAtacante() > 0)
                ? Math.min(ataque.getCantDadosAtacante(), maxAtacante)
                : maxAtacante;

        Random random = new Random();
        AtaqueResponseDto response = new AtaqueResponseDto();
        response.setDadosDefensor(new ArrayList<>());
        response.setDadosAtaque(new ArrayList<>());

        for (int i = 0; i < cantidadDefensor; i++) {
            response.getDadosDefensor().add(random.nextInt(1, 7));
        }
        for (int i = 0; i < cantidadAtacante; i++) {
            response.getDadosAtaque().add(random.nextInt(1, 7));
        }

        response.getDadosAtaque().sort(Collections.reverseOrder());
        response.getDadosDefensor().sort(Collections.reverseOrder());

        int comparaciones = Math.min(cantidadAtacante, cantidadDefensor);
        int perdidasAtacante = 0;
        int perdidasDefensor = 0;

        for (int i = 0; i < comparaciones; i++) {
            int a = response.getDadosAtaque().get(i);
            int d = response.getDadosDefensor().get(i);

            if (a > d) {
                perdidasDefensor++;
            } else {
                // Empate o gana defensor -> pierde atacante
                perdidasAtacante++;
            }
        }

        // Aplicar perdidas
        estadoPaisAtacante.setCantidadTropas(estadoPaisAtacante.getCantidadTropas() - perdidasAtacante);
        estadoPaisDefensor.setCantidadTropas(estadoPaisDefensor.getCantidadTropas() - perdidasDefensor);

        boolean conquista = false;
        if (estadoPaisDefensor.getCantidadTropas() <= 0) {
            // Capturar el defensor antes de cambiar propietario
            JugadorEntity defensorEntity = estadoPaisDefensor.getJugador();

            // Conquista
            estadoPaisService.cambiarPropietario(
                    estadoPaisDefensor.getIdEstadoPais(),
                    estadoPaisAtacante.getIdEstadoPais(),
                    ataque.getIdJugador());

            jugador.setConsquisto(true);
            jugadorRepository.save(jugador);
            conquista = true;

            // Verificar si el defensor fue eliminado (0 países restantes)
            List<EstadoPaisEntity> paisesDefensor = estadoPaisRepository
                    .findEstadoPaisEntitiesByJugador_IdJugador(defensorEntity.getIdJugador());
            if (paisesDefensor.isEmpty()) {
                defensorEntity.setPerdio(true);
                defensorEntity.setEliminadoPorColor(jugador.getColor());
                jugadorRepository.save(defensorEntity);
            }

            // Emitir progreso de objetivo al jugador atacante (topic personal)
            try {
                ObjetivoProgresoDto progreso = objetivoService.calcularProgreso(jugador.getIdJugador());
                messagingTemplate.convertAndSend(
                        "/topic/partida." + partida.getIdPartida() + ".objetivo." + jugador.getIdJugador(),
                        progreso);
            } catch (Exception ignored) {
                // No interrumpir el flujo de ataque si el progreso falla
            }

            // Asignar tarjeta automáticamente al conquistar (solo una por turno)
            boolean yaObtuvoCarta = estadoTarjetaRepository.findByTurnoId(turnoActual.getIdTurno())
                    .stream().anyMatch(et -> et.getJugador() != null
                            && et.getJugador().getIdJugador().equals(jugador.getIdJugador()));
            if (!yaObtuvoCarta) {
                List<EstadoTarjetaEntity> disponibles = estadoTarjetaRepository
                        .findByPartida_IdPartidaAndJugadorIsNull(partida.getIdPartida()).stream()
                        .filter(et -> !et.isCanjeada())
                        .collect(Collectors.toList());
                if (!disponibles.isEmpty()) {
                    Collections.shuffle(disponibles);
                    EstadoTarjetaEntity cartaAsignada = disponibles.get(0);
                    cartaAsignada.setJugador(jugador);
                    cartaAsignada.setTurno(turnoActual);
                    estadoTarjetaRepository.save(cartaAsignada);

                    PartidaEventDto evtTarjeta = new PartidaEventDto();
                    evtTarjeta.setTipo("TARJETA_OBTENIDA");
                    evtTarjeta.setJugadorNombre(jugador.getNombre());
                    evtTarjeta.setJugadorColor(jugador.getColor() != null ? jugador.getColor().name() : "");
                    evtTarjeta.setDescripcion(jugador.getNombre() + " obtuvo una tarjeta");
                    evtTarjeta.setIdPartida(partida.getIdPartida());
                    messagingTemplate.convertAndSend("/topic/partida." + partida.getIdPartida() + ".evento", evtTarjeta);
                }
            }
        } else {
            estadoPaisRepository.save(estadoPaisDefensor);
            estadoPaisRepository.save(estadoPaisAtacante);
        }

        response.setConquista(conquista);
        response.setAtaqueExitoso(conquista || perdidasDefensor > 0);
        response.setPerdidasAtacante(perdidasAtacante);
        response.setPerdidasDefensor(perdidasDefensor);

        // Broadcast evento a todos los jugadores de la partida
        PartidaEventDto evento = new PartidaEventDto();
        evento.setTipo(conquista ? "CONQUISTA" : "ATAQUE");
        evento.setJugadorNombre(jugador.getNombre());
        evento.setJugadorColor(jugador.getColor() != null ? jugador.getColor().name() : "");
        evento.setPaisOrigen(estadoPaisAtacante.getPais().getNombre());
        evento.setPaisDestino(estadoPaisDefensor.getPais().getNombre());
        evento.setDadosAtaque(response.getDadosAtaque());
        evento.setDadosDefensor(response.getDadosDefensor());
        evento.setConquista(conquista);
        evento.setPerdidasAtacante(perdidasAtacante);
        evento.setPerdidasDefensor(perdidasDefensor);
        evento.setIdPartida(partida.getIdPartida());
        evento.setJugadorDefensor(defensorNombre);
        evento.setJugadorColorDefensor(defensorColor);
        messagingTemplate.convertAndSend("/topic/partida." + partida.getIdPartida() + ".evento", evento);

        return response;
    }

    private Integer calcularCantidadDados(Integer cantidadTropas, boolean esAtacante) {
        if (esAtacante) {
            return Math.min(3, cantidadTropas - 1);
        } else {
            return Math.min(3, cantidadTropas);
        }
    }

    @Transactional
    @Override
    public boolean agregarFichas(AgregarFichas agregarFichas) {
        JugadorEntity jugador = jugadorRepository.findById(agregarFichas.getIdJugador()).orElseThrow(
                () -> new EntityNotFoundException("Jugador no encontrado."));

        PartidaEntity partida = partidaRepository.findById(jugador.getPartida().getIdPartida()).orElseThrow(
                () -> new EntityNotFoundException("Partida no encontrada."));

        TurnoEntity turnoActual = turnoRepository
                .findByNroTurnoAndPartida_IdPartida(partida.getTurnoActual(), partida.getIdPartida())
                .orElseThrow(() -> new IllegalStateException("No hay turno actual para la partida"));

        System.out.println("Turno jugador: " + jugador.getTurno().getNroTurno());
        System.out.println("Turno actual partida: " + partida.getTurnoActual());

        if (!jugador.getIdJugador().equals(turnoActual.getJugador().getIdJugador())) {
            throw new IllegalArgumentException("Turno no correspondiente.");
        }

        if (!turnoActual.getFase().equals(FaseTurno.INCORPORACION)) {
            throw new IllegalArgumentException("Fase no correspondiente.");
        }

        boolean resultado = estadoPaisService.agregarFichasEstadosPaises(agregarFichas);

        if (resultado) {
            PartidaEventDto evento = new PartidaEventDto();
            evento.setTipo("INCORPORACION");
            evento.setJugadorNombre(jugador.getNombre());
            evento.setJugadorColor(jugador.getColor() != null ? jugador.getColor().name() : "");
            int totalFichas = agregarFichas.getPaisesFichas().stream()
                    .mapToInt(e -> e.getCantidadFichas() != null ? e.getCantidadFichas().intValue() : 0)
                    .sum();
            String paisesStr = agregarFichas.getPaisesFichas().stream()
                    .map(ef -> estadoPaisRepository
                            .findByPaisIdPaisAndJugadorIdJugador(ef.getIdPais(), jugador.getIdJugador())
                            .map(ep -> ep.getPais().getNombre()).orElse(""))
                    .filter(s -> !s.isEmpty())
                    .distinct()
                    .collect(Collectors.joining(", "));
            evento.setDescripcion(jugador.getNombre() + " incorporó " + totalFichas + " ejércitos"
                    + (paisesStr.isEmpty() ? "" : " en " + paisesStr));
            evento.setIdPartida(partida.getIdPartida());
            messagingTemplate.convertAndSend("/topic/partida." + partida.getIdPartida() + ".evento", evento);
        }

        return resultado;
    }

}