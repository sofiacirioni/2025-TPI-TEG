package ar.edu.utn.frc.tup.piii.Entities;

import ar.edu.utn.frc.tup.piii.models.*;
import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(exclude = {"turno", "partida", "sala", "tarjetas", "objetivo"})
@Table(name = "jugadores")
public class JugadorEntity {

    /**
     * Fotografía de legajo de los bots. Todos comparten la misma: son tropa
     * genérica, no personajes.
     *
     * <p>Hace falta porque los bots se graban con el usuario de quien creó la
     * partida, así que sin esto saldrían con la foto de ese jugador.
     */
    public static final String AVATAR_BOT = "assets/images/avatars/bot-avatar.png";
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idJugador;

    @NotBlank(message = "El nombre no puede estar vacío")
//    @Pattern(regexp = "^[a-zA-ZáéíóúÁÉÍÓÚñÑ ]+$", message = "El nombre solo puede contener letras y espacios")
    @Column
    private String nombre;

    @Column
    private boolean consquisto = false;

    @ManyToOne
    @JoinColumn(name = "idUsuario")
    private UsuarioEntity usuario;

    @Enumerated(EnumType.STRING)
    private TipoJugador tipoJugador;

    @Column
    private boolean perdio;

    @ManyToOne
    @JoinColumn(name = "idTurno")
    private TurnoEntity turno;

    @Enumerated(EnumType.STRING)
    private Color color;

    @ManyToOne
    @JoinColumn(name = "idSala", nullable = false)
    @JsonBackReference
    @ToString.Exclude
    private SalaEntity sala;

    @ManyToOne
    @JoinColumn(name = "idPartida")
    private PartidaEntity partida;

    @ManyToOne
    @JoinColumn(name="idObjetivo")
    private ObjetivoEntity objetivo;

    @Column
    private boolean aceptoPausa;

    @Column
    private boolean aceptoRenudar;

    @Column
    private boolean finalizarPartida;

    @ManyToMany
    private List<EstadoTarjetaEntity> tarjetas;

    @Enumerated(EnumType.STRING)
    private EstadoJugador estadoJugador;

    @Column
    private Integer ejercito = 0;

    /** Color del jugador que eliminó a este jugador (null si aún no fue eliminado o si se rindió). */
    @Enumerated(EnumType.STRING)
    @Column
    private Color eliminadoPorColor;

    // ── Contadores de combate ─────────────────────────────────────────────
    // Cada fila de jugadores es por partida, así que estos contadores son el
    // parte de campaña de este comandante. Se incrementan en vivo durante el
    // combate (TurnoServiceImpl.resolverInterno) y los lee el resumen de
    // partida. Ver migración V4.

    @Column(nullable = false)
    private Integer ataquesLanzados = 0;

    @Column(nullable = false)
    private Integer conquistas = 0;

    @Column(nullable = false)
    private Integer tropasAbatidas = 0;

    @Column(nullable = false)
    private Integer tropasPerdidas = 0;

    @Column(nullable = false)
    private Integer defensasResistidas = 0;

    @Column(nullable = false)
    private Integer canjesRealizados = 0;

    /**
     * Avatar que se muestra para este jugador. Los bots llevan siempre el
     * suyo; los humanos, el que eligieron en su perfil.
     */
    public String avatarUrl() {
        if (tipoJugador == TipoJugador.BOT) {
            return AVATAR_BOT;
        }
        return usuario != null ? usuario.getImagen() : null;
    }
}
