package ar.edu.utn.frc.tup.piii.Entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Data
@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "mensajes")
public class MensajeEntity extends AuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idMensaje;

    @ManyToOne
    @JoinColumn(name = "idJugador", nullable = false)
    private JugadorEntity jugador;

    @Column
    private String contenido;

    @ManyToOne
    @JoinColumn(name="idTipoMensaje", nullable = false)
    private TipoMensajeEntity tipoMensaje;

    @ManyToOne
    @JoinColumn(name = "idPartida", nullable = false)
    private PartidaEntity partida;
}
