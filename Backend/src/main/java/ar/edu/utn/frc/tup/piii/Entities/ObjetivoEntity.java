package ar.edu.utn.frc.tup.piii.Entities;

import ar.edu.utn.frc.tup.piii.models.Color;
import ar.edu.utn.frc.tup.piii.models.TipoObjetivo;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "objetivos")
public class ObjetivoEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "descripcion")
    private String descripcion;

    @Enumerated(EnumType.STRING)
    @Column(name = "id_tipo_objetivo")
    private TipoObjetivo tipoObjetivo; //general/secreto

    private Integer cantidadPaisesObjetivo;

    private Integer limitrofe;
    private Integer africa;
    private Integer asia;
    private Integer europa;
    private Integer americaNorte;
    private Integer americaSur;
    private Integer oceania;
    @Enumerated(EnumType.STRING)
    private Color colorEnemigo;

}
