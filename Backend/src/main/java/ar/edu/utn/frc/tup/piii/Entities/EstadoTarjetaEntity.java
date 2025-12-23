package ar.edu.utn.frc.tup.piii.Entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "estado_tarjetas")
public class EstadoTarjetaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_estado_tarjeta")
    private Long idEstadoTarjeta;

    @ManyToOne()
    @JoinColumn(name = "id_tarjeta", nullable = false)
    private TarjetaEntity tarjeta;

    @ManyToOne
    @JoinColumn(name = "id_partida")
    private PartidaEntity partida;

    @ManyToOne()
    @JoinColumn(name = "id_jugador")
    private JugadorEntity jugador;

    @ManyToOne()
    @JoinColumn(name = "id_turno")
    private TurnoEntity turno;

    private boolean usada;

    private boolean canjeada;

    @Column(name = "fecha_asignacion")
    private LocalDateTime fechaAsignacion;

    @PrePersist
    protected void onCreate() {
        fechaAsignacion = LocalDateTime.now();
    }
}
