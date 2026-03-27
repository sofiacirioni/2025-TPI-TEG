package ar.edu.utn.frc.tup.piii.Controller;

import ar.edu.utn.frc.tup.piii.Dtos.PartidaDto;
import ar.edu.utn.frc.tup.piii.Services.PartidaService;
import ar.edu.utn.frc.tup.piii.Services.UsuarioService;
import ar.edu.utn.frc.tup.piii.models.*;
import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
@RestController
@RequestMapping("/api/v1/partida")
public class PartidaController {
    @Autowired
    private PartidaService partidaService;

    // @Autowired
    // private UsuarioRepository usuarioRepository;

    @Autowired
    private UsuarioService usuarioService;

    @PostMapping("/crear/{idSala}")
    public ResponseEntity<?> crearPartida(
            @PathVariable Long idSala,
            @AuthenticationPrincipal UserDetails userDetails) {

        Usuario usuarioActual = usuarioService.obtenerByCorreo(userDetails.getUsername());

        Partida partida = partidaService.crearPartidaYAsignarJugadores(idSala, usuarioActual);

        PartidaDto partidaDto = new PartidaDto();
        partidaDto.setEstado(EstadoPartida.EN_JUEGO);
        partidaDto.setFechaInicio(partida.getFechaInicio());
        partidaDto.setIdPartida(partida.getIdPartida());

        return ResponseEntity.ok(partidaDto);
    }

    // TODO: evaluar si se mantiene para futuras versiones (era usado por opción C "retomar partida", eliminada en 2026-03-27)
    @GetMapping("/disponibles")
    public ResponseEntity<List<Partida>> listarPartidasDisponibles() {
        return ResponseEntity.ok(partidaService.listarPartidasDisponibles());
    }

    // TODO: evaluar si se mantiene para futuras versiones (era usado por opción C "retomar partida", eliminada en 2026-03-27)
    @PostMapping("/{idPartida}/unirse/{idUsuario}")
    public ResponseEntity<Partida> unirseAPartida(@PathVariable Long idPartida, @PathVariable Long idUsuario) {
        return ResponseEntity.ok(partidaService.unirseAPartida(idUsuario, idPartida));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PartidaDto> cargarPartida(@PathVariable Long id) {
        return ResponseEntity.ok(partidaService.cargarPartida(id));
    }

    @GetMapping("/url/{url}")
    public ResponseEntity<PartidaDto> cargarPartidaBySala(@PathVariable String url, @RequestParam Long idUsuario) {
        return ResponseEntity.ok(partidaService.cargarPartidaBySala(url, idUsuario));
    }

    @PutMapping("/{idPartida}")
    public ResponseEntity<Partida> guardarPartida(@PathVariable Long idPartida,
            @Valid @RequestBody PartidaDto partidaDTO,
            BindingResult result) {

        if (result.hasErrors()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    result.getFieldErrors().stream()
                            .map(e -> e.getField() + ": " + e.getDefaultMessage())
                            .collect(Collectors.joining(", ")));
        }

        if (!idPartida.equals(partidaDTO.getIdPartida())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El ID de la URL y del cuerpo no coinciden");
        }

        Partida partida = new ModelMapper().map(partidaDTO, Partida.class);
        Partida partidaGuardada = partidaService.guardarPartida(partida);
        return ResponseEntity.ok(partidaGuardada);
    }

    @GetMapping("/{idPartida}/estado")
    public ResponseEntity<Partida> obtenerEstadoPartida(@PathVariable Long idPartida) {
        return ResponseEntity.ok(partidaService.obtenerEstadoPartida(idPartida));
    }

    @PostMapping("/{idPartida}/finalizar")
    public ResponseEntity<Void> finalizarPartida(@PathVariable Long idPartida) {
        boolean ok = partidaService.finalizarPartida(idPartida);
        if (ok) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

    @GetMapping("/sala")
    public ResponseEntity<PartidaDto> accederAPartidaPorUrl(
            @RequestParam String url,
            @RequestParam Long idUsuario) {
        PartidaDto dto = partidaService.obtenerPartidaActivaPorUrlYUsuario(url, idUsuario);
        ;

        return ResponseEntity.ok(dto);
    }
}
