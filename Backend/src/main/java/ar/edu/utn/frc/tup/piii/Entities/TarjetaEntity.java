package ar.edu.utn.frc.tup.piii.Entities;

import ar.edu.utn.frc.tup.piii.models.Simbolo;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "tarjetas")
public class TarjetaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idTarjeta;

    @ManyToOne
    @JoinColumn(name = "idPais", nullable = false)
    private PaisEntity pais;

    @Enumerated(EnumType.STRING) // Guarda como "GLOBO", "GALEON", "CANION"
    @Column(name = "simbolo", nullable = false)
    private Simbolo simbolo;

}
