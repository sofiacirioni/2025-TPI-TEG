package ar.edu.utn.frc.tup.piii.Services;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Store in-memory de ataques pendientes de resolución. El flujo de ataque dual
 * requiere dos pasos: el atacante inicia (registra pendiente) y el defensor
 * confirma (dispara resolución). El scheduler de TurnoService resuelve con
 * valores por defecto los ataques cuyo defensor no responde dentro del timeout
 * (5s frontend + 2s de margen = 7s backend).
 *
 * Indexado por idPartida — solo puede haber un ataque pendiente por partida
 * en un momento dado, porque el atacante bloquea la UI mientras aguarda.
 */
@Component
public class AtaquePendienteStore {

    public static final int TIMEOUT_SEGUNDOS = 7;

    private final ConcurrentHashMap<Long, AtaquePendiente> pendientes = new ConcurrentHashMap<>();

    public void registrar(AtaquePendiente ap) {
        pendientes.put(ap.getIdPartida(), ap);
    }

    public AtaquePendiente obtener(Long idPartida) {
        return pendientes.get(idPartida);
    }

    public AtaquePendiente remover(Long idPartida) {
        return pendientes.remove(idPartida);
    }

    public Collection<AtaquePendiente> todos() {
        return pendientes.values();
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AtaquePendiente {
        private Long idPartida;
        private Long idAtacante;
        private Long idDefensor;
        private Long idPaisOrigen;
        private Long idPaisDestino;
        private Integer cantDadosAtacante;
        private Integer maxDadosDefensor;
        /** Momento en que el atacante disparó iniciar. Usado por el scheduler. */
        private Instant iniciadoEn;
    }
}
