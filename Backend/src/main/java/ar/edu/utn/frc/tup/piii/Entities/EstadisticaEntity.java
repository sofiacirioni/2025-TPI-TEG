package ar.edu.utn.frc.tup.piii.Entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "estadisticas")
public class EstadisticaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idEstadistica;

    @ManyToOne
    @JoinColumn(name = "idJugador", nullable = false)
    private JugadorEntity jugador;

    @ManyToOne
    @JoinColumn(name = "idPartida", nullable = false)
    private PartidaEntity partida;

    // Todo, agregar bien las relaciones bidireccionales

    @Column
    private int paisesConquistados;
    @Column
    private int paisesPerdidos;
    @Column
    private int rondasJugadas;
    @Column
    private boolean ganador;
    @Column
    private boolean eliminado;
    @Column
    private String fechaPartida;
    @Column
    private String evento;
}
