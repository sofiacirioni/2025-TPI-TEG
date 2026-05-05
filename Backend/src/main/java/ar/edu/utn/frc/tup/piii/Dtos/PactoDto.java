package ar.edu.utn.frc.tup.piii.Dtos;

import ar.edu.utn.frc.tup.piii.models.EstadoPacto;
import ar.edu.utn.frc.tup.piii.models.TipoPacto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PactoDto {
    private Long id;
    private TipoPacto tipo;
    private EstadoPacto estado;

    private Long idJugadorA;
    private String nombreJugadorA;
    private String colorJugadorA;

    private Long idJugadorB;
    private String nombreJugadorB;
    private String colorJugadorB;

    private Long idPaisProtegidoA;
    private String nombrePaisProtegidoA;

    private Long idPaisProtegidoB;
    private String nombrePaisProtegidoB;

    private Long idPaisZona;
    private String nombrePaisZona;
    private String continenteZona;

    private Long idPartida;

    private LocalDateTime fechaPropuesta;
    private LocalDateTime fechaAceptacion;
    private LocalDateTime fechaRuptura;

    private Long turnoRupturaJugador;
    private Integer turnoNumeroAlRomper;
}
