package ar.edu.utn.frc.tup.piii.Entities;

import ar.edu.utn.frc.tup.piii.models.EstadoPacto;
import ar.edu.utn.frc.tup.piii.models.TipoPacto;
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
@EqualsAndHashCode(exclude = {"jugadorA", "jugadorB", "paisProtegidoA", "paisProtegidoB", "paisZona", "partida"})
@Table(name = "pactos")
public class PactoEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private TipoPacto tipo;

    @ManyToOne
    @JoinColumn(name = "id_jugador_a")
    private JugadorEntity jugadorA;

    @ManyToOne
    @JoinColumn(name = "id_jugador_b")
    private JugadorEntity jugadorB;

    @ManyToOne
    @JoinColumn(name = "id_pais_protegido_a")
    private PaisEntity paisProtegidoA;

    @ManyToOne
    @JoinColumn(name = "id_pais_protegido_b")
    private PaisEntity paisProtegidoB;

    @ManyToOne
    @JoinColumn(name = "id_pais_zona")
    private PaisEntity paisZona;

    @Column
    private String continenteZona;

    @Enumerated(EnumType.STRING)
    private EstadoPacto estado;

    @Column
    private LocalDateTime fechaPropuesta;

    @Column
    private LocalDateTime fechaAceptacion;

    @Column
    private LocalDateTime fechaRuptura;

    @Column(name = "id_jugador_ruptura")
    private Long turnoRupturaJugador;

    @Column
    private Integer turnoNumeroAlRomper;

    @ManyToOne
    @JoinColumn(name = "id_partida")
    private PartidaEntity partida;
}
