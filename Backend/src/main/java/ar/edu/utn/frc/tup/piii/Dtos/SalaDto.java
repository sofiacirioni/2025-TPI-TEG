package ar.edu.utn.frc.tup.piii.Dtos;
import ar.edu.utn.frc.tup.piii.Dtos.Login.UsuarioDto;
import ar.edu.utn.frc.tup.piii.models.Usuario;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SalaDto {
    @NotNull(message = "El id de la sala es obligatorio")
    private Long idSala;

    @NotBlank(message = "El nombre de la sala no puede estar vacío")
    private String nombreSala;

    private String url;

    private UsuarioDto creador;
}

