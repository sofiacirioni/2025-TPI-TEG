package ar.edu.utn.frc.tup.piii.Dtos;

import ar.edu.utn.frc.tup.piii.models.TipoPacto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProponerPactoDto {
    private Long idPartida;
    private Long idJugadorProponente;
    private Long idJugadorReceptor;
    private TipoPacto tipo;

    /** Solo para PACTO_PAISES: país del proponente que B no puede atacar. */
    private Long idPaisProtegidoA;
    /** Solo para PACTO_PAISES: país del receptor que A no puede atacar. */
    private Long idPaisProtegidoB;

    /** Solo para PACTO_ZONA_INTERNACIONAL. */
    private Long idContinenteZona;
}
