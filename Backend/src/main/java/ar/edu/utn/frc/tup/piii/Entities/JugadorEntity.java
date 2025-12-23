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

}
