package ar.edu.utn.frc.tup.piii.Entities;

import ar.edu.utn.frc.tup.piii.models.FaseTurno;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(exclude = {"jugador", "partida"})
@Table(name = "turnos")
public class TurnoEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idTurno;
    @Column
    private int nroTurno;

    @ManyToOne
    @JoinColumn(name = "idJugador")
    private JugadorEntity jugador;

    @ManyToOne
    @JoinColumn (referencedColumnName = "idPartida")
    private PartidaEntity partida;
    @Enumerated(EnumType.STRING)
    private FaseTurno fase;

    @Column
    private LocalDateTime inicio;

    @Column(columnDefinition = "boolean default false")
    private boolean reagrupado = false;

}
