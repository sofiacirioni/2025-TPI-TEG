package ar.edu.utn.frc.tup.piii.Services;

import ar.edu.utn.frc.tup.piii.Dtos.PartidaDto;
import ar.edu.utn.frc.tup.piii.models.Partida;
import ar.edu.utn.frc.tup.piii.models.Usuario;

import java.util.List;

public interface PartidaService {
    // Partida crearPartida(List<UsuarioEntity> usuarios, Long idSala, String
    // configInicial);

    List<Partida> listarPartidasDisponibles(); // GET

    Partida unirseAPartida(Long idUsuario, Long idPartida);

    PartidaDto cargarPartida(Long idPartida);

    Partida guardarPartida(Partida partida); // PUT

    Partida obtenerEstadoPartida(Long idPartida); // GET

    boolean finalizarPartida(Long idPartida);

    // Partida crearPartida(List<Usuario> usuarios, String configInicial); //POST
    // boolean unirseAPartida(Usuario usuario, int idPartida); //PATCH?
    // List<Partida> listarPartidasDisponibles(); //GET
    // Partida cargarPartida(int idPartida); //GET/PUT?
    // void guardarPartida(Partida partida); //PUT
    // Partida obtenerEstadoPartida(int idPartida); //GET
    void verificarCondicionVictoria(Partida partida);

    Partida crearPartidaYAsignarJugadores(Long idSala, Usuario usuarioActual);

    PartidaDto obtenerPartidaActivaPorUrlYUsuario(String urlSala, Long idUsuario);

    PartidaDto cargarPartidaBySala(String url, Long idUsuario);
}
