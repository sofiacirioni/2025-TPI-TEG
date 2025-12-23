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
public class UsarTarjetaEnPaisDto {
    @NotNull(message = "El ID de la tarjeta es obligatorio")
    @Positive(message = "El ID de la tarjeta debe ser positivo")
    private Long idTarjeta;

    @NotNull(message = "El ID del jugador es obligatorio")
    @Positive(message = "El ID del jugador debe ser positivo")
    private Long idJugador;
}
