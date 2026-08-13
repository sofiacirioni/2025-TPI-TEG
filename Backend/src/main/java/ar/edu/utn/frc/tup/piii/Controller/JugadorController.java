package ar.edu.utn.frc.tup.piii.Controller;

import ar.edu.utn.frc.tup.piii.Dtos.JugadorDto;
import ar.edu.utn.frc.tup.piii.Repositories.UsuarioRepository;
import ar.edu.utn.frc.tup.piii.Services.JugadorService;
import ar.edu.utn.frc.tup.piii.Services.PartidaService;
import ar.edu.utn.frc.tup.piii.Services.SalaService;
import ar.edu.utn.frc.tup.piii.Services.UsuarioService;
import ar.edu.utn.frc.tup.piii.models.*;
import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/jugador")
public class JugadorController {

    @Autowired
    public JugadorService jugadorService;
    @Autowired
    public SalaService salaService;
    @Autowired
    public ModelMapper modelMapper;
    @Autowired
    public UsuarioService usuarioService;
    @Autowired
    public UsuarioRepository usuarioRepository;
    @Autowired
    public PartidaService partidaService;

    @MessageMapping("/sala.{salaId}")
    @SendTo("/topic/sala.{salaId}")
    public JugadorDto notificarNuevoJugador(@DestinationVariable String salaId, JugadorDto jugadorDto) {
        return jugadorDto;
    }

    @PostMapping("/sala/{idSala}/jugador")
    public ResponseEntity<JugadorDto> guardarJugador(
            @PathVariable Long idSala,
            @RequestBody @Valid Jugador jugador,
            @AuthenticationPrincipal UserDetails userDetails) {

        Usuario usuarioActual = usuarioService.obtenerByCorreo(userDetails.getUsername());

        Sala sala = salaService.obtenerSala(idSala);
        if (sala == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        Jugador jugadorGuardado = jugadorService.crearJugador(jugador, usuarioActual, sala);
        if (jugadorGuardado == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        JugadorDto jugadorDto = modelMapper.map(jugadorGuardado, JugadorDto.class);
        return ResponseEntity.ok(jugadorDto);
    }

    @GetMapping("/crearBot/{idSala}")
    public ResponseEntity<JugadorDto> crearBot(
            @PathVariable Long idSala,
            @AuthenticationPrincipal UserDetails userDetails) {

        Sala sala = salaService.obtenerSala(idSala);
        if (sala == null)
            return ResponseEntity.notFound().build();

        Usuario usuarioActual = usuarioService.obtenerByCorreo(userDetails.getUsername());

        Jugador bot = jugadorService.crearBot(sala, usuarioActual);
        JugadorDto dto = new JugadorDto();
        dto.setIdJugador(bot.getIdJugador());
        dto.setNombre(bot.getNombre());
        dto.setColor(bot.getColor().toString());
        dto.setTipoJugador(bot.getTipoJugador().toString());
        dto.setIdUsuario(bot.getUsuario() != null ? bot.getUsuario().getIdUsuario() : null);
        dto.setIdSala(bot.getSala().getIdSala());
        return ResponseEntity.ok(dto);
    }

    @DeleteMapping("/{idJugador}")
    public ResponseEntity<Void> eliminarJugador(
            @PathVariable Long idJugador,
            @AuthenticationPrincipal UserDetails userDetails) {

        Usuario solicitante = usuarioService.obtenerByCorreo(userDetails.getUsername());
        jugadorService.eliminarJugadorDeSala(idJugador, solicitante);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{idJugador}/votar-pausa")
    public ResponseEntity<String> votarPausa(@PathVariable Long idJugador) {
        jugadorService.votarPausa(idJugador);
        return ResponseEntity.ok("Voto de pausa registrado");
    }

    @PostMapping("/{idJugador}/votar-reanudar")
    public ResponseEntity<String> votarReanudar(@PathVariable Long idJugador) {
        jugadorService.votarReanudar(idJugador);
        return ResponseEntity.ok("Voto de reanudación registrado");
    }

    /**
     * El jugador se retira de la campaña. Cuando todos los humanos se
     * retiraron, la partida se cierra como ABANDONADA y deja de figurar como
     * en curso.
     */
    @PostMapping("/{idJugador}/finalizarPartida")
    public ResponseEntity<String> finalizarPartida(@PathVariable Long idJugador) {
        jugadorService.finalizarPartida(idJugador);
        return ResponseEntity.ok("Retiro registrado");
    }

    @GetMapping("/sala/{idSala}/jugadores")
    public ResponseEntity<List<JugadorDto>> getJugadoresPorSala(@PathVariable Long idSala) {
        List<JugadorDto> jugadores = jugadorService.obtenerJugadoresPorSala(idSala);
        return ResponseEntity.ok(jugadores);
    }
}
