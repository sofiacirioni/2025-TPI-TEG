package ar.edu.utn.frc.tup.piii.Dtos;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CanjeTarjetasDto {
    @NotNull(message = "Los IDs de las tarjetas son obligatorios")
    @Size(min = 3, max = 3, message = "Debes enviar exactamente 3 tarjetas")
    private List<@Positive(message = "El ID debe ser positivo") Long> idTarjetas;

    @NotNull(message = "El ID del jugador es obligatorio")
    @Positive(message = "El ID del jugador debe ser positivo")
    private Long idJugador;
}
