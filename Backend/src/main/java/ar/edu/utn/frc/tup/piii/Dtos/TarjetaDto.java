package ar.edu.utn.frc.tup.piii.Dtos;
import ar.edu.utn.frc.tup.piii.models.Pais;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TarjetaDto {
    @JsonProperty("idtarjeta")
    private Long idTarjeta;
    private Pais pais;
    private String simbolo;

}
