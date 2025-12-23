package ar.edu.utn.frc.tup.piii.Entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "tipoMensajes")
public class TipoMensajeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idTipoMensaje;
    @Column
    private String descripcion;
}
