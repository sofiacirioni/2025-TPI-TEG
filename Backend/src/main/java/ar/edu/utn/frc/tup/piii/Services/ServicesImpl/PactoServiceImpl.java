package ar.edu.utn.frc.tup.piii.Services.ServicesImpl;

import ar.edu.utn.frc.tup.piii.Dtos.PactoDto;
import ar.edu.utn.frc.tup.piii.Dtos.PartidaEventDto;
import ar.edu.utn.frc.tup.piii.Dtos.ProponerPactoDto;
import ar.edu.utn.frc.tup.piii.Entities.*;
import ar.edu.utn.frc.tup.piii.Repositories.*;
import ar.edu.utn.frc.tup.piii.Services.PactoService;
import ar.edu.utn.frc.tup.piii.models.EstadoPacto;
import ar.edu.utn.frc.tup.piii.models.EstadoPartida;
import ar.edu.utn.frc.tup.piii.models.FaseTurno;
import ar.edu.utn.frc.tup.piii.models.TipoPacto;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PactoServiceImpl implements PactoService {

    private final PactoRepository pactoRepository;
    private final JugadorRepository jugadorRepository;
    private final PaisRepository paisRepository;
    private final ContinenteRepository continenteRepository;
    private final PartidaRepository partidaRepository;
    private final TurnoRepository turnoRepository;
    private final EstadoPaisRepository estadoPaisRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Override
    @Transactional
    public PactoDto proponer(ProponerPactoDto dto) {
        if (dto.getIdJugadorProponente().equals(dto.getIdJugadorReceptor())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "No se puede proponer un pacto con uno mismo.");
        }

        JugadorEntity proponente = jugadorRepository.findById(dto.getIdJugadorProponente())
                .orElseThrow(() -> new EntityNotFoundException("Jugador proponente no encontrado."));
        JugadorEntity receptor = jugadorRepository.findById(dto.getIdJugadorReceptor())
                .orElseThrow(() -> new EntityNotFoundException("Jugador receptor no encontrado."));
        PartidaEntity partida = partidaRepository.findById(dto.getIdPartida())
                .orElseThrow(() -> new EntityNotFoundException("Partida no encontrada."));

        if (partida.getEstadoPartida() != EstadoPartida.EN_JUEGO) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La partida no está en juego.");
        }

        // Es turno del proponente.
        TurnoEntity turnoActual = turnoRepository
                .findByNroTurnoAndPartida_IdPartida(partida.getTurnoActual(), partida.getIdPartida())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "No hay turno actual."));
        if (!turnoActual.getJugador().getIdJugador().equals(proponente.getIdJugador())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Solo se pueden proponer pactos durante tu turno.");
        }

        // Receptor sigue vivo en la partida.
        if (receptor.isPerdio()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "No se puede proponer un pacto a un jugador eliminado.");
        }

        // Sin pacto activo del mismo tipo entre estos dos.
        List<PactoEntity> existentes = pactoRepository.findActivosEntreJugadores(
                proponente.getIdJugador(), receptor.getIdJugador(), partida.getIdPartida());
        for (PactoEntity p : existentes) {
            if (p.getTipo() == dto.getTipo()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Ya existe un pacto del mismo tipo entre ambos jugadores.");
            }
        }
        // También bloqueamos PROPUESTOS pendientes para evitar dobles propuestas.
        List<PactoEntity> todos = pactoRepository.findByPartida_IdPartida(partida.getIdPartida());
        for (PactoEntity p : todos) {
            if (p.getEstado() == EstadoPacto.PROPUESTO
                    && p.getTipo() == dto.getTipo()
                    && involucraA(p, proponente.getIdJugador(), receptor.getIdJugador())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Ya hay una propuesta pendiente de este tipo entre ambos jugadores.");
            }
        }

        PactoEntity pacto = new PactoEntity();
        pacto.setTipo(dto.getTipo());
        pacto.setJugadorA(proponente);
        pacto.setJugadorB(receptor);
        pacto.setEstado(EstadoPacto.PROPUESTO);
        pacto.setFechaPropuesta(LocalDateTime.now());
        pacto.setPartida(partida);

        switch (dto.getTipo()) {
            case PACTO_PAISES -> validarYAsignarPaisesProtegidos(pacto, dto, proponente, receptor, partida);
            case PACTO_MUNDIAL -> { /* sin campos extra */ }
            case PACTO_ZONA_INTERNACIONAL -> validarYAsignarZonaInternacional(pacto, dto, proponente, receptor, partida);
        }

        PactoEntity saved = pactoRepository.save(pacto);
        PactoDto out = mapToDto(saved);

        emitirEventoPacto("PACTO_PROPUESTO", saved, out);

        return out;
    }

    private void validarYAsignarPaisesProtegidos(PactoEntity pacto, ProponerPactoDto dto,
                                                  JugadorEntity proponente, JugadorEntity receptor,
                                                  PartidaEntity partida) {
        if (dto.getIdPaisProtegidoA() == null || dto.getIdPaisProtegidoB() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Para PACTO_PAISES se requieren los dos países protegidos.");
        }

        if (!perteneceAlJugador(dto.getIdPaisProtegidoA(), proponente.getIdJugador(), partida.getIdPartida())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "El país protegido por el proponente no le pertenece.");
        }
        if (!perteneceAlJugador(dto.getIdPaisProtegidoB(), receptor.getIdJugador(), partida.getIdPartida())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "El país protegido por el receptor no le pertenece.");
        }

        PaisEntity paisA = paisRepository.findById(dto.getIdPaisProtegidoA())
                .orElseThrow(() -> new EntityNotFoundException("País A no encontrado."));
        PaisEntity paisB = paisRepository.findById(dto.getIdPaisProtegidoB())
                .orElseThrow(() -> new EntityNotFoundException("País B no encontrado."));
        pacto.setPaisProtegidoA(paisA);
        pacto.setPaisProtegidoB(paisB);
    }

    private void validarYAsignarZonaInternacional(PactoEntity pacto, ProponerPactoDto dto,
                                                   JugadorEntity proponente, JugadorEntity receptor,
                                                   PartidaEntity partida) {
        if (dto.getIdContinenteZona() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Para PACTO_ZONA_INTERNACIONAL se requiere el continente.");
        }

        ContinenteEntity continente = continenteRepository.findById(dto.getIdContinenteZona())
                .orElseThrow(() -> new EntityNotFoundException("Continente no encontrado."));

        List<PaisEntity> paisesContinente = paisRepository.findByContinente_IdContinente(continente.getIdContinente());
        if (paisesContinente.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "El continente no tiene países registrados.");
        }

        // El proponente debe tener TODOS los países menos uno; el restante debe pertenecer al receptor.
        PaisEntity paisAislado = null;
        int delProponente = 0;
        for (PaisEntity pais : paisesContinente) {
            EstadoPaisEntity estado = estadoPaisRepository
                    .findByPais_IdPaisAndPartida_IdPartida(pais.getIdPais(), partida.getIdPartida())
                    .orElse(null);
            if (estado == null || estado.getJugador() == null) continue;
            Long dueno = estado.getJugador().getIdJugador();
            if (dueno.equals(proponente.getIdJugador())) {
                delProponente++;
            } else if (dueno.equals(receptor.getIdJugador())) {
                paisAislado = pais;
            } else {
                // Hay un tercer dueño en el continente: no califica.
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "El continente tiene países de un tercer jugador; no califica para zona internacional.");
            }
        }

        if (paisAislado == null || delProponente != paisesContinente.size() - 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "No se cumple la condición de zona internacional para este continente.");
        }

        pacto.setPaisZona(paisAislado);
        pacto.setContinenteZona(continente.getNombre());
    }

    @Override
    @Transactional
    public PactoDto aceptar(Long pactoId, Long jugadorReceptorId) {
        PactoEntity pacto = pactoRepository.findById(pactoId)
                .orElseThrow(() -> new EntityNotFoundException("Pacto no encontrado."));
        if (pacto.getEstado() != EstadoPacto.PROPUESTO) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "El pacto no está en estado de propuesta.");
        }
        if (!pacto.getJugadorB().getIdJugador().equals(jugadorReceptorId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Solo el receptor de la propuesta puede aceptarla.");
        }

        pacto.setEstado(EstadoPacto.ACTIVO);
        pacto.setFechaAceptacion(LocalDateTime.now());
        PactoEntity saved = pactoRepository.save(pacto);
        PactoDto out = mapToDto(saved);

        emitirEventoPacto("PACTO_ACEPTADO", saved, out);
        return out;
    }

    @Override
    @Transactional
    public PactoDto rechazar(Long pactoId, Long jugadorReceptorId) {
        PactoEntity pacto = pactoRepository.findById(pactoId)
                .orElseThrow(() -> new EntityNotFoundException("Pacto no encontrado."));
        if (pacto.getEstado() != EstadoPacto.PROPUESTO) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "El pacto no está en estado de propuesta.");
        }
        if (!pacto.getJugadorB().getIdJugador().equals(jugadorReceptorId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Solo el receptor de la propuesta puede rechazarla.");
        }

        pacto.setEstado(EstadoPacto.RECHAZADO);
        PactoEntity saved = pactoRepository.save(pacto);
        PactoDto out = mapToDto(saved);

        emitirEventoPacto("PACTO_RECHAZADO", saved, out);
        return out;
    }

    @Override
    @Transactional
    public PactoDto romperVoluntariamente(Long pactoId, Long jugadorId) {
        PactoEntity pacto = pactoRepository.findById(pactoId)
                .orElseThrow(() -> new EntityNotFoundException("Pacto no encontrado."));
        if (pacto.getEstado() != EstadoPacto.ACTIVO) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Solo se pueden romper pactos activos.");
        }
        boolean esA = pacto.getJugadorA().getIdJugador().equals(jugadorId);
        boolean esB = pacto.getJugadorB().getIdJugador().equals(jugadorId);
        if (!esA && !esB) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Solo las partes del pacto pueden romperlo.");
        }

        pacto.setEstado(EstadoPacto.ROTO_VOLUNTARIO);
        pacto.setFechaRuptura(LocalDateTime.now());
        pacto.setTurnoRupturaJugador(jugadorId);
        // Guardamos el nroTurno actual para el cálculo del período de gracia.
        PartidaEntity partida = pacto.getPartida();
        pacto.setTurnoNumeroAlRomper(partida.getTurnoActual());

        PactoEntity saved = pactoRepository.save(pacto);
        PactoDto out = mapToDto(saved);

        emitirEventoPacto("PACTO_ROTO", saved, out);
        return out;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PactoDto> listarActivos(Long partidaId) {
        List<PactoEntity> activos = new ArrayList<>();
        // Las propuestas todavía sin respuesta también viajan: un pacto ofrecido a
        // un bot se resuelve recién cuando le toca el turno, y sin mostrarlo el
        // proponente no tenía ninguna señal de que su oferta existía.
        activos.addAll(pactoRepository.findByPartida_IdPartidaAndEstado(partidaId, EstadoPacto.PROPUESTO));
        activos.addAll(pactoRepository.findByPartida_IdPartidaAndEstado(partidaId, EstadoPacto.ACTIVO));
        activos.addAll(pactoRepository.findByPartida_IdPartidaAndEstado(partidaId, EstadoPacto.ROTO_VOLUNTARIO));
        return activos.stream().map(this::mapToDto).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean ataqueViolaPactoActivo(Long jugadorAtacanteId, Long paisAtacadoId, Long partidaId) {
        EstadoPaisEntity estadoDestino = estadoPaisRepository
                .findByPais_IdPaisAndPartida_IdPartida(paisAtacadoId, partidaId)
                .orElse(null);
        if (estadoDestino == null || estadoDestino.getJugador() == null) return false;
        Long dueno = estadoDestino.getJugador().getIdJugador();

        List<PactoEntity> entreAmbos = pactoRepository.findActivosEntreJugadores(jugadorAtacanteId, dueno, partidaId);
        for (PactoEntity p : entreAmbos) {
            if (!esVigenteParaValidacion(p)) continue;

            switch (p.getTipo()) {
                case PACTO_MUNDIAL -> { return true; }
                case PACTO_PAISES -> {
                    Long protegido = (p.getJugadorA().getIdJugador().equals(dueno))
                            ? (p.getPaisProtegidoA() != null ? p.getPaisProtegidoA().getIdPais() : null)
                            : (p.getPaisProtegidoB() != null ? p.getPaisProtegidoB().getIdPais() : null);
                    if (protegido != null && protegido.equals(paisAtacadoId)) return true;
                }
                case PACTO_ZONA_INTERNACIONAL -> {
                    if (p.getPaisZona() != null && paisAtacadoId.equals(p.getPaisZona().getIdPais())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    @Transactional
    public void verificarRupturaAutomaticaPorConquista(Long paisConquistadoId, Long nuevoDueñoId, Long partidaId) {
        List<PactoEntity> todos = pactoRepository.findByPartida_IdPartida(partidaId);
        for (PactoEntity p : todos) {
            if (p.getEstado() != EstadoPacto.ACTIVO && p.getEstado() != EstadoPacto.ROTO_VOLUNTARIO) continue;

            boolean afectado = false;
            if (p.getTipo() == TipoPacto.PACTO_PAISES) {
                Long pa = p.getPaisProtegidoA() != null ? p.getPaisProtegidoA().getIdPais() : null;
                Long pb = p.getPaisProtegidoB() != null ? p.getPaisProtegidoB().getIdPais() : null;
                if (paisConquistadoId.equals(pa) || paisConquistadoId.equals(pb)) {
                    afectado = true;
                }
            } else if (p.getTipo() == TipoPacto.PACTO_ZONA_INTERNACIONAL) {
                Long pz = p.getPaisZona() != null ? p.getPaisZona().getIdPais() : null;
                if (paisConquistadoId.equals(pz)) {
                    afectado = true;
                }
            }
            // Conquista por una de las dos partes del pacto NO rompe automáticamente — solo si fue tercer jugador.
            // Pero si es uno de las dos partes, ese jugador estaría violando el pacto, lo cual ya fue bloqueado por la validación previa.
            // Sin embargo, si por alguna razón el jugador atacante ES uno de los del pacto y el país atacado figura en él,
            // el chequeo previo lo hubiera bloqueado. Mantenemos el chequeo de tercero por seguridad.
            if (afectado) {
                boolean tercero = !p.getJugadorA().getIdJugador().equals(nuevoDueñoId)
                        && !p.getJugadorB().getIdJugador().equals(nuevoDueñoId);
                if (tercero) {
                    p.setEstado(EstadoPacto.ROTO_AUTOMATICO);
                    p.setFechaRuptura(LocalDateTime.now());
                    pactoRepository.save(p);
                    emitirEventoPacto("PACTO_ROTO", p, mapToDto(p));
                }
            }
        }
    }

    @Override
    @Transactional
    public void expirarPactosDeJugadorEliminado(Long jugadorId, Long partidaId) {
        List<PactoEntity> activos = pactoRepository.findActivosDelJugador(jugadorId, partidaId);
        for (PactoEntity p : activos) {
            p.setEstado(EstadoPacto.EXPIRADO);
            p.setFechaRuptura(LocalDateTime.now());
            pactoRepository.save(p);
            emitirEventoPacto("PACTO_ROTO", p, mapToDto(p));
        }
    }

    @Override
    @Transactional
    public void procesarPactosEnGracia(Long partidaId, Integer turnoNumeroQueInicia) {
        List<PactoEntity> rotos = pactoRepository.findByPartida_IdPartidaAndEstado(partidaId, EstadoPacto.ROTO_VOLUNTARIO);
        if (rotos.isEmpty()) return;

        PartidaEntity partida = partidaRepository.findById(partidaId)
                .orElseThrow(() -> new EntityNotFoundException("Partida no encontrada."));
        int cantJugadores = (int) partida.getJugadores().stream()
                .filter(j -> !j.isPerdio())
                .count();
        if (cantJugadores < 2) cantJugadores = 2;

        for (PactoEntity p : rotos) {
            if (p.getTurnoNumeroAlRomper() == null || p.getTurnoRupturaJugador() == null) continue;

            // Período de gracia: el jugador que rompe debe respetar el pacto durante su próximo turno
            // y el siguiente turno completo del otro jugador. En términos de nroTurno absoluto eso
            // equivale a esperar cantJugadores * 2 turnos antes de poder atacar libremente. Cuando
            // el nroTurno actual ya supera el de ruptura por más de 2 vueltas, pasa a EXPIRADO.
            int diferencia = turnoNumeroQueInicia - p.getTurnoNumeroAlRomper();
            if (diferencia >= cantJugadores * 2) {
                p.setEstado(EstadoPacto.EXPIRADO);
                pactoRepository.save(p);
                emitirEventoPacto("PACTO_ROTO", p, mapToDto(p));
            }
        }
    }

    /** Vigente para validación de ataques: ACTIVO siempre; ROTO_VOLUNTARIO durante el período de gracia. */
    private boolean esVigenteParaValidacion(PactoEntity p) {
        return p.getEstado() == EstadoPacto.ACTIVO || p.getEstado() == EstadoPacto.ROTO_VOLUNTARIO;
    }

    private boolean involucraA(PactoEntity p, Long idA, Long idB) {
        Long a = p.getJugadorA().getIdJugador();
        Long b = p.getJugadorB().getIdJugador();
        return (a.equals(idA) && b.equals(idB)) || (a.equals(idB) && b.equals(idA));
    }

    private boolean perteneceAlJugador(Long idPais, Long idJugador, Long idPartida) {
        return estadoPaisRepository
                .findByPais_IdPaisAndPartida_IdPartida(idPais, idPartida)
                .map(EstadoPaisEntity::getJugador)
                .map(JugadorEntity::getIdJugador)
                .map(id -> id.equals(idJugador))
                .orElse(false);
    }

    private void emitirEventoPacto(String tipo, PactoEntity pacto, PactoDto pactoDto) {
        try {
            PartidaEventDto evt = new PartidaEventDto();
            evt.setTipo(tipo);
            evt.setIdPartida(pacto.getPartida().getIdPartida());
            evt.setJugadorNombre(pacto.getJugadorA().getNombre());
            evt.setJugadorColor(pacto.getJugadorA().getColor() != null
                    ? pacto.getJugadorA().getColor().name() : "");
            evt.setJugadorDefensor(pacto.getJugadorB().getNombre());
            evt.setJugadorColorDefensor(pacto.getJugadorB().getColor() != null
                    ? pacto.getJugadorB().getColor().name() : "");
            evt.setDescripcion(buildDescripcion(tipo, pacto));
            evt.setPacto(pactoDto);
            evt.setIdAtacante(pacto.getJugadorA().getIdJugador());
            evt.setIdDefensor(pacto.getJugadorB().getIdJugador());
            messagingTemplate.convertAndSend(
                    "/topic/partida." + pacto.getPartida().getIdPartida() + ".evento", evt);
        } catch (Exception ignored) {
            // No bloquear el flujo principal si el WS falla.
        }
    }

    private String buildDescripcion(String tipo, PactoEntity p) {
        String nA = p.getJugadorA().getNombre();
        String nB = p.getJugadorB().getNombre();
        return switch (tipo) {
            case "PACTO_PROPUESTO" -> nA + " propuso un pacto a " + nB;
            case "PACTO_ACEPTADO" -> nA + " y " + nB + " firmaron un pacto";
            case "PACTO_RECHAZADO" -> nB + " rechazó la propuesta de " + nA;
            case "PACTO_ROTO" -> "Pacto entre " + nA + " y " + nB + " quedó sin efecto";
            default -> "";
        };
    }

    private PactoDto mapToDto(PactoEntity p) {
        return PactoDto.builder()
                .id(p.getId())
                .tipo(p.getTipo())
                .estado(p.getEstado())
                .idJugadorA(p.getJugadorA().getIdJugador())
                .nombreJugadorA(p.getJugadorA().getNombre())
                .colorJugadorA(p.getJugadorA().getColor() != null ? p.getJugadorA().getColor().name() : "")
                .idJugadorB(p.getJugadorB().getIdJugador())
                .nombreJugadorB(p.getJugadorB().getNombre())
                .colorJugadorB(p.getJugadorB().getColor() != null ? p.getJugadorB().getColor().name() : "")
                .idPaisProtegidoA(p.getPaisProtegidoA() != null ? p.getPaisProtegidoA().getIdPais() : null)
                .nombrePaisProtegidoA(p.getPaisProtegidoA() != null ? p.getPaisProtegidoA().getNombre() : null)
                .idPaisProtegidoB(p.getPaisProtegidoB() != null ? p.getPaisProtegidoB().getIdPais() : null)
                .nombrePaisProtegidoB(p.getPaisProtegidoB() != null ? p.getPaisProtegidoB().getNombre() : null)
                .idPaisZona(p.getPaisZona() != null ? p.getPaisZona().getIdPais() : null)
                .nombrePaisZona(p.getPaisZona() != null ? p.getPaisZona().getNombre() : null)
                .continenteZona(p.getContinenteZona())
                .idPartida(p.getPartida().getIdPartida())
                .fechaPropuesta(p.getFechaPropuesta())
                .fechaAceptacion(p.getFechaAceptacion())
                .fechaRuptura(p.getFechaRuptura())
                .turnoRupturaJugador(p.getTurnoRupturaJugador())
                .turnoNumeroAlRomper(p.getTurnoNumeroAlRomper())
                .build();
    }
}
