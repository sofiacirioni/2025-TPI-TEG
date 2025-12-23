package ar.edu.utn.frc.tup.piii.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Tarjeta {

    private Long idTarjeta;
    private Pais pais;
    private Simbolo simbolo;


}