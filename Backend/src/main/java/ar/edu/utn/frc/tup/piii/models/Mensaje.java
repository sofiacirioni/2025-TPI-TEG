package ar.edu.utn.frc.tup.piii.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Mensaje {
        private Long idMensaje;
        private Jugador jugador;
        private String contenido;
        private TipoMensaje tipoMensaje;
        private Partida partida;


}

