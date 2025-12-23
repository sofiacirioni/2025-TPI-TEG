package ar.edu.utn.frc.tup.piii.Dtos;

import ar.edu.utn.frc.tup.piii.models.EstadoPais;
import ar.edu.utn.frc.tup.piii.models.EstadoPartida;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PartidaDto {

        @NotNull(message = "El id de la partida es obligatorio")
        private Long idPartida;

        @NotNull(message = "El estado no puede estar vacío")
        private EstadoPartida estado;

        @NotNull(message = "La fecha de inicio es obligatoria")
        private LocalDate fechaInicio;

        @NotNull(message = "La configuración (sala) es obligatoria")
        private SalaDto configuracion;

        private JugadorDto ganador;

        private List<EstadoPaisDto> estadoPaises;

        private List<JugadorDto> jugadores;

        private List<TurnoDto> turnos;

        private List<EstadoTarjetaDto > estadoTarjetas;

        private int turnoActual;
}
