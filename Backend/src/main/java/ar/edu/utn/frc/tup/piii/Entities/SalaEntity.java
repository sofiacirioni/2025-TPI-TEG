package ar.edu.utn.frc.tup.piii.Entities;

import ar.edu.utn.frc.tup.piii.models.EstadoSala;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

@Entity
@Data
@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "salas")
public class SalaEntity extends AuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idSala;

    @NotBlank(message = "El nombre de la sala no debe estar vacío")
    private String nombreSala;

    @ManyToOne
    @JoinColumn(name = "idUsuario")
    private UsuarioEntity creador;

    /** Orden explícito: ver la nota en {@link PartidaEntity#getJugadores()}. */
    @OneToMany(mappedBy = "sala", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("idJugador ASC")
    @JsonManagedReference
    @ToString.Exclude
    private List<JugadorEntity> jugadores;

    @Enumerated(EnumType.STRING)
    private EstadoSala estado;

    private String url;

}
