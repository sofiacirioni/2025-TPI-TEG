package ar.edu.utn.frc.tup.piii.Controller;

import ar.edu.utn.frc.tup.piii.Entities.EstadoPaisEntity;
import ar.edu.utn.frc.tup.piii.Entities.LimiteEntity;
import ar.edu.utn.frc.tup.piii.Services.EstadoPaisService;
import ar.edu.utn.frc.tup.piii.Dtos.EstadoPaisDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/estado/pais")
public class EstadoPaisController {
    @Autowired
    private EstadoPaisService estadoPaisService;

    public EstadoPaisController(EstadoPaisService estadoPaisService) {
        this.estadoPaisService = estadoPaisService;
    }

    @GetMapping("/{id}")
    public ResponseEntity <EstadoPaisDto> getEstadoPais(@PathVariable Long id) {
        EstadoPaisDto estadoPaisDto = estadoPaisService.getEstadoPais(id);
        return ResponseEntity.ok(estadoPaisDto);
    }

    @GetMapping("/limites/{id}")
    public ResponseEntity<List<EstadoPaisDto>> getLimitesEstadoPais(@PathVariable Long id) {
        return ResponseEntity.ok(estadoPaisService.getLimitesEstadoPais(id));
    }

    @PostMapping("")
    public ResponseEntity <EstadoPaisDto> createEstadoPais(@RequestBody EstadoPaisDto estadoPaisDto) {
        return ResponseEntity.ok(estadoPaisService.createEstadoPais(estadoPaisDto));
    }

    @PutMapping("")
    public ResponseEntity <EstadoPaisDto> updateEstadoPais(@RequestBody EstadoPaisDto estadoPaisDto) {
        return ResponseEntity.ok(estadoPaisService.updateEstadoPais(estadoPaisDto));
    }
}