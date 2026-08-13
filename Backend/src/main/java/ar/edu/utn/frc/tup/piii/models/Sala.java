
package ar.edu.utn.frc.tup.piii.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Sala {
    private Long idSala;
    private String nombreSala;
    private String url;
    private List<Usuario> usuarios;
    private List<Jugador> jugadores;
    private Usuario creador;
    private EstadoSala estado = EstadoSala.ESPERANDO;

    public boolean puedeAgregarJugador() {
        return estado == EstadoSala.ESPERANDO && jugadores != null && jugadores.size() < 6;
    }

    public boolean listaJugadoresCompleta() {
        return jugadores != null && jugadores.size() >= 2 && jugadores.size() <= 6;
    }

    public boolean esCreador(Usuario usuario) {
        return creador != null && creador.getIdUsuario() == usuario.getIdUsuario();
    }
}
