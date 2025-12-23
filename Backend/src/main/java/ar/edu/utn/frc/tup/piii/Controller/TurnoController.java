package ar.edu.utn.frc.tup.piii.Controller;

import ar.edu.utn.frc.tup.piii.Dtos.*;
import ar.edu.utn.frc.tup.piii.Dtos.EstadoPaises.AgregarFichas;
import ar.edu.utn.frc.tup.piii.Dtos.EstadoPaises.Ataque;
import ar.edu.utn.frc.tup.piii.Dtos.EstadoPaises.AtaqueResponseDto;
import ar.edu.utn.frc.tup.piii.Dtos.EstadoPaises.MoverFichas;
import ar.edu.utn.frc.tup.piii.Entities.JugadorEntity;
import ar.edu.utn.frc.tup.piii.Entities.PartidaEntity;
import ar.edu.utn.frc.tup.piii.Entities.TurnoEntity;
import ar.edu.utn.frc.tup.piii.Services.TurnoService;
import ar.edu.utn.frc.tup.piii.models.*;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/turno")

public class TurnoController {
    @Autowired
    private TurnoService turnoService;

    @GetMapping("/{id}")
    public ResponseEntity<Turno> ObtenerTurno(@PathVariable Long id) {
        try {
            Turno turno = turnoService.obtenerTurno(id);
            if (turno != null) {
                return ResponseEntity.ok(turno);
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PutMapping("/fase")
    public ResponseEntity<Boolean> cambiarFase(@RequestParam Long idPartida) {
        return ResponseEntity.ok(turnoService.cambiarFaseTurno(idPartida));
    }

    @PutMapping("/defender")
    public ResponseEntity<Boolean> defenderFichas(@RequestBody AgregarFichas agregarFichas) {
        return ResponseEntity.ok(turnoService.agregarFichas(agregarFichas));
    }

    @PutMapping("/reagrupar")
    public ResponseEntity<Boolean> reagruparFichas(@RequestBody MoverFichas moverFichas) {
        return ResponseEntity.ok(turnoService.moverFichas(moverFichas));
    }

    @PutMapping("/ataque")
    public ResponseEntity<AtaqueResponseDto> atacar(@RequestBody Ataque ataque) {
        return ResponseEntity.ok(turnoService.ataque(ataque));
    }

    @PutMapping("/pasarTurno")
    public ResponseEntity<Void> pasarTurno(@RequestParam Long idPartida) {
        turnoService.pasarTurno(idPartida);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/validar-ataque")
    public ResponseEntity<?> ValidarAtaque(@RequestBody Pais pais1, @RequestBody Pais pais2, @RequestBody Jugador jugador) {
        try {
            turnoService.validarAtaque(pais1, pais2, jugador);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/validar-movimiento")
    public ResponseEntity<?> ValidarMovimiento(@RequestBody Pais pais1, @RequestBody Pais pais2, @RequestBody Jugador jugador) {
        try {
            turnoService.validarMovimiento(pais1, pais2, jugador);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/verificar-accion")
    public ResponseEntity<?> VerificarOrdenAcciones(@RequestBody Jugador jugador, @PathVariable String accion) {
        try {
            turnoService.verificarOrdenAcciones(jugador, accion);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/timeout")
    public ResponseEntity<?> GestionarTimeout(@RequestParam Long idTurno) {
        try {
            turnoService.gestionarTimeoutTurno(idTurno);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PutMapping("/verificarGanador")
    public ResponseEntity<VerificacionObjetivoDto> verificarGanador(@RequestParam Long idJugador) {
        return ResponseEntity.ok(turnoService.verificarGanador(idJugador));
    }

    @PutMapping ("/tarjeta/obtener")
    public ResponseEntity<TarjetaDto> obtenerTarjeta(@RequestParam Long idJugador, @RequestParam Long idPartida) {
        return ResponseEntity.ok( turnoService.entregarTarjetaSiCorresponde(idJugador, idPartida));
    }

    @PutMapping("/canje/realizar")
    public ResponseEntity<Integer> validarCanjeTarjetas(@Valid @RequestBody CanjeTarjetasDto canjeTarjetasDto) {
        return ResponseEntity.ok(turnoService.validarCanjeTarjetas(canjeTarjetasDto));
    }

    @PutMapping("/usarTarjeta")
    public ResponseEntity<Void> validarUsarTarjetaEnPais(@RequestBody UsarTarjetaEnPaisDto usarTarjetaEnPaisDto) {
        turnoService.validarUsarTarjetaEnPais(usarTarjetaEnPaisDto);
        return ResponseEntity.ok().build();

    }
}
