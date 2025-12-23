package ar.edu.utn.frc.tup.piii.Entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "limites")
public class LimiteEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idLimite;

    @ManyToOne
    @JoinColumn(name = "idPais1", nullable = false)
    private PaisEntity pais1;

    @ManyToOne
    @JoinColumn(name = "idPais2", nullable = false)
    private PaisEntity pais2;

}
