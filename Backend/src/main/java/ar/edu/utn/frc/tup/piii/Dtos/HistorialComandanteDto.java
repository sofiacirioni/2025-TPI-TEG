package ar.edu.utn.frc.tup.piii.Dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Hoja de servicios de un usuario: el acumulado de todas sus campañas.
 *
 * <p>Es el complemento de {@link ResumenPartidaDto}, que cuenta UNA partida.
 * Todas las cifras se calculan sobre campañas concluidas: una partida a medio
 * jugar aparece en el registro, pero no suma al historial.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class HistorialComandanteDto {

    // ── Identidad (la página izquierda de la libreta) ──────────────────────
    private Long idUsuario;
    private String usuario;
    private String correo;
    private String imagen;
    /** Alta de la cuenta (migración V5). */
    private LocalDateTime fechaAlta;
    /** Color con el que más veces jugó; null si nunca entró a una partida. */
    private String divisaHabitual;

    // ── Condecoraciones ───────────────────────────────────────────────────
    private int campaniasLibradas;
    private int victorias;
    private int segundosPuestos;
    private int terceroPuestos;
    /** victorias / campañas libradas, en porcentaje. */
    private double tasaVictoria;

    // ── Combate acumulado ─────────────────────────────────────────────────
    private int ataquesLanzados;
    private int conquistas;
    /** conquistas / ataques lanzados, en porcentaje. */
    private double efectividad;
    private int defensasResistidas;
    private int tropasAbatidas;
    private int tropasPerdidas;
    /** tropas abatidas / tropas perdidas. */
    private double ratioBajas;
    private int canjesRealizados;
    /** Comandantes que quedaron eliminados por el color de este usuario. */
    private int comandantesAbatidos;

    // ── Diplomacia ────────────────────────────────────────────────────────
    private int tratadosFirmados;
    /** Tratados que rompió este usuario (no los que se rompieron a su alrededor). */
    private int tratadosRotos;

    /** Registro de campañas, de la más reciente a la más vieja. */
    private List<CampaniaDto> campanias;

    /** Una línea del registro; enlaza al parte de campaña de esa partida. */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CampaniaDto {
        private Long idPartida;
        private LocalDate fecha;
        private boolean terminada;
        /** Puesto en la clasificación final, 1 = vencedor. 0 si sigue en curso. */
        private int puesto;
        private int comandantes;
        private int turnosJugados;
        private String color;
        private boolean ganador;
        private boolean eliminado;
        /** Nombre del vencedor; null si la campaña no terminó. */
        private String ganadorNombre;
    }
}
