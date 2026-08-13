package ar.edu.utn.frc.tup.piii.Entities;

import ar.edu.utn.frc.tup.piii.models.EstadoPartida;
import ar.edu.utn.frc.tup.piii.models.FaseJuego;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "partidas")
public class PartidaEntity extends AuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idPartida;
    @Column
    private LocalDate fechaInicio;

    @OneToMany(mappedBy = "partida")
    @OrderBy("nroTurno ASC")
    private List<TurnoEntity> turnos;

    // Todo: Pensar como gestionar los turnos, debe haber un orden y de quien es el turno actual

    @OneToOne
    @JoinColumn(name = "idSala", nullable = false)
    private SalaEntity configuracion;

    @Column
    private Integer turnoActual;

    @OneToMany(mappedBy = "partida")
    private List<EstadoTarjetaEntity> mazo;

    /**
     * Sin {@code @OrderBy} esta lista sale en el orden físico de la tabla, y
     * Postgres reubica cada fila que se actualiza: como los jugadores cambian
     * de ejército y de contadores en cada turno, el panel de la mesa parecía
     * barajarse solo. El orden también decide quién juega primero, porque
     * {@code establecerOrdenJugadores} recorre esta lista para repartir los
     * números de turno.
     */
    @OneToMany(mappedBy = "partida", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("idJugador ASC")
    private List<JugadorEntity> jugadores;

    @Enumerated(EnumType.STRING)
    private EstadoPartida estadoPartida;

    @Enumerated(EnumType.STRING)
    private FaseJuego faseActual;

    private Boolean hostilidad = false;

    @OneToMany(mappedBy = "partida", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EstadoPaisEntity> estadoPaises = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "ganador_id", nullable = true)
    private JugadorEntity ganador;


}
