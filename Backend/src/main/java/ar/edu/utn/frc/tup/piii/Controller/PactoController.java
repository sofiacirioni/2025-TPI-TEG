package ar.edu.utn.frc.tup.piii.Controller;

import ar.edu.utn.frc.tup.piii.Dtos.PactoDto;
import ar.edu.utn.frc.tup.piii.Dtos.ProponerPactoDto;
import ar.edu.utn.frc.tup.piii.Services.PactoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/pacto")
public class PactoController {

    @Autowired
    private PactoService pactoService;

    @PostMapping("/proponer")
    public ResponseEntity<PactoDto> proponer(@RequestBody ProponerPactoDto dto) {
        return ResponseEntity.ok(pactoService.proponer(dto));
    }

    @PostMapping("/{id}/aceptar")
    public ResponseEntity<PactoDto> aceptar(@PathVariable Long id, @RequestParam Long idJugador) {
        return ResponseEntity.ok(pactoService.aceptar(id, idJugador));
    }

    @PostMapping("/{id}/rechazar")
    public ResponseEntity<PactoDto> rechazar(@PathVariable Long id, @RequestParam Long idJugador) {
        return ResponseEntity.ok(pactoService.rechazar(id, idJugador));
    }

    @PostMapping("/{id}/romper")
    public ResponseEntity<PactoDto> romper(@PathVariable Long id, @RequestParam Long idJugador) {
        return ResponseEntity.ok(pactoService.romperVoluntariamente(id, idJugador));
    }

    @GetMapping("/partida/{partidaId}/activos")
    public ResponseEntity<List<PactoDto>> listarActivos(@PathVariable Long partidaId) {
        return ResponseEntity.ok(pactoService.listarActivos(partidaId));
    }
}
