package ar.edu.utn.frc.tup.piii.Dtos;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EstadoTarjetaDto {

    @NotNull(message = "El ID de la tarjeta es obligatorio")
    @Positive(message = "El ID de la tarjeta debe ser positivo")
    private Long idEstadoTarjeta;

    private TarjetaDto tarjeta;

    @NotNull(message = "El ID del jugador es obligatorio")
    @Positive(message = "El ID del jugador debe ser positivo")
    private Long idJugador;

    @NotNull(message = "El ID del turno es obligatorio")
    @Positive(message = "El ID del turno debe ser positivo")
    private Long idTurno;

    private boolean usada;

    private boolean canjeada;

    public EstadoTarjetaDto(long l, long l1, long l2) {
    }
}
