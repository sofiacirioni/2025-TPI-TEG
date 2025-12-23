package ar.edu.utn.frc.tup.piii.Entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "continentes")
public class ContinenteEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // o AUTO
    private Long idContinente;
    @NotBlank(message = "El nombre no puede ser nulo")
    @Column
    private String nombre;
}
