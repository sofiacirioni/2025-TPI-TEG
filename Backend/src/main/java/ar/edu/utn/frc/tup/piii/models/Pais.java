package ar.edu.utn.frc.tup.piii.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Pais {
    private Long idPais;
    private String nombre;
    private Continente continente;

}