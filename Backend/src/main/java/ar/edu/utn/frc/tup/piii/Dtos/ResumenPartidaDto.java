package ar.edu.utn.frc.tup.piii.Dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

/**
 * Parte de campaña de una partida ya jugada: lo que muestra la pizarra de
 * estadísticas. Es el resumen de UNA partida, no el histórico del usuario.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResumenPartidaDto {

    private Long idPartida;
    private LocalDate fecha;
    private int turnosJugados;
    private boolean terminada;

    /** Nombre y color del ganador; null si la partida no terminó. */
    private String ganadorNombre;
    private String ganadorColor;
    /** Objetivo que cumplió el ganador. */
    private String objetivoCumplido;

    // ── Diplomacia ────────────────────────────────────────────────────────
    private int tratadosFirmados;
    private int tratadosRotos;

    private List<ComandanteResumenDto> comandantes;

    /** Fila por jugador: estado final + su parte de combate. */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ComandanteResumenDto {
        private Long idJugador;
        private String nombre;
        private String color;
        private String avatarUrl;
        private boolean esBot;

        // Estado final
        private int paises;
        private int ejercitos;
        private double porcentajeMapa;
        private boolean eliminado;
        /** Color de quien lo eliminó; null si sobrevivió. */
        private String eliminadoPor;
        private boolean ganador;

        // Combate (contadores de la migración V4)
        private int ataquesLanzados;
        private int conquistas;
        private int defensasResistidas;
        private int tropasAbatidas;
        private int tropasPerdidas;
        private int canjesRealizados;

        /** conquistas / ataquesLanzados, en porcentaje. 0 si no atacó nunca. */
        private double efectividad;
        /** tropasAbatidas / tropasPerdidas. 0 si no perdió ninguna. */
        private double ratioBajas;
    }
}
