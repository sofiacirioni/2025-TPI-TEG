package ar.edu.utn.frc.tup.piii.Controller;

import ar.edu.utn.frc.tup.piii.Dtos.CanjeTarjetasDto;
import ar.edu.utn.frc.tup.piii.Dtos.EstadoTarjetaDto;
import ar.edu.utn.frc.tup.piii.Dtos.TarjetaDto;
import ar.edu.utn.frc.tup.piii.Entities.EstadoTarjetaEntity;
import ar.edu.utn.frc.tup.piii.Services.EstadoTarjetaService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/estado/tarjeta")
public class EstadoTarjetaController {
@Autowired
private EstadoTarjetaService estadoTarjetaService;

    @PostMapping("/asignar")
    public ResponseEntity<?> asignarTarjeta(@Valid @RequestBody EstadoTarjetaDto dto) {
        try {
            estadoTarjetaService.asignarTarjeta(dto);
            return ResponseEntity.ok().body("Tarjeta asignada exitosamente");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Error al asignar tarjeta: " + e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<List<EstadoTarjetaEntity>> obtenerEstados() {
        try {
            List<EstadoTarjetaEntity> estados = estadoTarjetaService.obtenerEstadoTarjeta();
            return ResponseEntity.ok(estados);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}")
        public ResponseEntity<?> obtenerEstadoPorId(@PathVariable Long id) {
        try {
            EstadoTarjetaEntity estado = estadoTarjetaService.obtenerPorId(id);
            return ResponseEntity.ok(estado);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarEstado(@PathVariable Long id) {
        try {
            estadoTarjetaService.eliminarEstadoTarjeta(id);
            return ResponseEntity.ok().body("Estado eliminado exitosamente");
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/tarjeta/{idJugador}")
    public ResponseEntity<?> obtenerTarjeta(@PathVariable Long idJugador) {

        TarjetaDto tarjeta = estadoTarjetaService.obtenerTarjeta(idJugador);

        if (tarjeta != null) {
            return ResponseEntity.ok(tarjeta);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{idTarjeta}/usar/{idJugador}")
    public ResponseEntity<?> usarTarjeta(@PathVariable Long idTarjeta, @PathVariable Long idJugador) {
        try {
            estadoTarjetaService.usarTarjetaEnPais(idTarjeta, idJugador);
            return ResponseEntity.ok("Tarjeta usada exitosamente en el país.");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Error al usar la tarjeta: " + e.getMessage());
        }
    }


    @PostMapping("/canjear")
    public ResponseEntity<?> canjearTarjetas(@Valid @RequestBody CanjeTarjetasDto dto) {
        try {
            int ejercitosOtorgados = estadoTarjetaService.canjearTarjetas(dto.getIdTarjetas(), dto.getIdJugador());
            return ResponseEntity.ok("Tarjetas canjeadas exitosamente. Ejércitos otorgados: " + ejercitosOtorgados);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Error al canjear tarjetas: " + e.getMessage());
        }
    }
}
