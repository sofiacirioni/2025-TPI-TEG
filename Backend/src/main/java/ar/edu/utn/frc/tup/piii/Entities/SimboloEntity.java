package ar.edu.utn.frc.tup.piii.Entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "simbolos")
public class SimboloEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idSimbolo;
    @Column
    private String tipo;
}
