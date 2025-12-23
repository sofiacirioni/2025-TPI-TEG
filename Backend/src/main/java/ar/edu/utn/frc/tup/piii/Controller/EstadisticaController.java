package ar.edu.utn.frc.tup.piii.Controller;

import ar.edu.utn.frc.tup.piii.Dtos.EstadisticaDto;
import ar.edu.utn.frc.tup.piii.Services.EstadisticaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/estadisticas")
@RequiredArgsConstructor
public class EstadisticaController {

    private final EstadisticaService estadisticaService;

    @GetMapping("/usuario/{id}")
    public ResponseEntity<List<EstadisticaDto>> getEstadisticasPorUsuario(@PathVariable Long id) {
        try{
            List<EstadisticaDto> estadisticas = estadisticaService.getEstadisticas(id);
            if(estadisticas.isEmpty()){
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(estadisticas);
        }catch (Exception e){
            return ResponseEntity.internalServerError().build();
        }
    }

}
