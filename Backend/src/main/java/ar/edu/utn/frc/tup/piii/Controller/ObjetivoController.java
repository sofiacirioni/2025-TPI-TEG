package ar.edu.utn.frc.tup.piii.Controller;

import ar.edu.utn.frc.tup.piii.Dtos.ObjetivoDto;
import ar.edu.utn.frc.tup.piii.Dtos.ObjetivoProgresoDto;
import ar.edu.utn.frc.tup.piii.Services.ObjetivoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/objetivos")
public class ObjetivoController {
    @Autowired
    private ObjetivoService objetivoService;

    @GetMapping("/secretos")
    public List<ObjetivoDto> getObjetivosSecretos (){
        return objetivoService.obtenerObjetivosSecretos();
    }

    @GetMapping("/general")
    public ObjetivoDto getObjetivoGral (){
        return objetivoService.obtenerObjetivoGral();
    }

    @GetMapping("/{id}")
    public ObjetivoDto getObjetivoById(@PathVariable Long id){
        return objetivoService.obtenerObjetivoById(id);
    }

    /** Progreso en tiempo real del objetivo secreto del jugador. */
    @GetMapping("/progreso/{idJugador}")
    public ObjetivoProgresoDto getProgreso(@PathVariable Long idJugador) {
        return objetivoService.calcularProgreso(idJugador);
    }
}
