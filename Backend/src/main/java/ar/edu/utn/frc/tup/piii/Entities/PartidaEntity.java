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
    private List<TurnoEntity> turnos;

    // Todo: Pensar como gestionar los turnos, debe haber un orden y de quien es el turno actual

    @OneToOne
    @JoinColumn(name = "idSala", nullable = false)
    private SalaEntity configuracion;

    @Column
    private Integer turnoActual;

    @OneToMany(mappedBy = "partida")
    private List<EstadoTarjetaEntity> mazo;

    @OneToMany(mappedBy = "partida", cascade = CascadeType.ALL, orphanRemoval = true)
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
