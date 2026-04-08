package ar.edu.utn.frc.tup.piii.Dtos.EstadoPaises;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AtaqueResponseDto {
    boolean ataqueExitoso;
    boolean conquista;
    List<Integer> dadosAtaque;
    List<Integer> dadosDefensor;
}
