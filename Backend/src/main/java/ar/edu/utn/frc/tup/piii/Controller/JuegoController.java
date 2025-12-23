package ar.edu.utn.frc.tup.piii.Controller;

import ar.edu.utn.frc.tup.piii.Services.JuegoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/juego")
public class JuegoController {
    @Autowired
    private JuegoService juegoService;

    @PostMapping("/{idPartida}/repartir")
    public ResponseEntity<String> repartirPaisesYObjetivos(@PathVariable Long idPartida) {
        juegoService.repartirPaisesYObjetivos(idPartida);
        return ResponseEntity.ok("Países y objetivos repartidos");
    }

    @PostMapping("/{idPartida}/colocacion")
    public ResponseEntity<String> iniciarColocacionEjercitos(@PathVariable Long idPartida) {
        juegoService.iniciarColocacionEjercitos(idPartida);
        return ResponseEntity.ok("Colocación de ejércitos iniciada");
    }

    @PostMapping("/{idPartida}/fase")
    public ResponseEntity<String> avanzarFase(@PathVariable Long idPartida) {
        juegoService.avanzarFase(idPartida);
        return ResponseEntity.ok("Fase avanzada");
    }

    @GetMapping("/{idPartida}/verificar-objetivo")
    public ResponseEntity<Boolean> comprobarObjetivoCumplido(@PathVariable Long idPartida) {
        boolean cumplido = juegoService.comprobarObjetivoCumplido(idPartida);
        return ResponseEntity.ok(cumplido);
    }
}
