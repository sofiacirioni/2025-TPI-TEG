package ar.edu.utn.frc.tup.piii.Entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "estadoPaises")
public class EstadoPaisEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idEstadoPais;

    @ManyToOne
    @JoinColumn(name = "idPais", nullable = false)
    private PaisEntity pais;

    @ManyToOne
    @JoinColumn(name = "idJugador")
    private JugadorEntity jugador;

    @ManyToOne
    @JoinColumn(name = "idEstadistica")
    private EstadisticaEntity estadistica;

    @Column
    private int cantidadTropas;

    @ManyToOne
    @JoinColumn(name = "idPartida")
    private PartidaEntity partida;

}
