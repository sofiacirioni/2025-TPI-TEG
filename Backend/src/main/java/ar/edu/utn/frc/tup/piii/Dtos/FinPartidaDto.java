package ar.edu.utn.frc.tup.piii.Dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FinPartidaDto {
    private JugadorDto ganador;
    private ObjetivoProgresoDto objetivoCumplido;
    private List<JugadorResultadoDto> clasificacion;
    private LocalDateTime momentoFin;
}
