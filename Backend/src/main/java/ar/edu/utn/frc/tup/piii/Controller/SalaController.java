package ar.edu.utn.frc.tup.piii.Controller;

import ar.edu.utn.frc.tup.piii.Dtos.SalaDto;
import ar.edu.utn.frc.tup.piii.Services.SalaService;
import ar.edu.utn.frc.tup.piii.models.Sala;
import ar.edu.utn.frc.tup.piii.models.Usuario;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.CrossOrigin;

import java.util.Map;

@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")

@RestController
@RequestMapping("/api/v1/sala")
public class SalaController {
    @Autowired
    public SalaService salaService;

    @Autowired
    public ModelMapper modelMapper;

    @MessageMapping("/sala/{idSala}/inicio")
    @SendTo("/topic/sala.{idSala}.inicio")
    public Map<String, String> enviarInicioPartida(@DestinationVariable Long idSala, Map<String, String> data) {
        return data;
    }

    @PostMapping("/crear")
    public ResponseEntity<SalaDto> crearSala(@RequestBody @Valid Sala sala, HttpSession session) {
        Usuario usuarioActual = (Usuario) session.getAttribute("usuarioActual");
        if (usuarioActual == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Sala salaGuardada = salaService.crearSala(sala, usuarioActual, sala.getNombreSala());
        if (salaGuardada == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        SalaDto salaDto = modelMapper.map(salaGuardada, SalaDto.class);
        return ResponseEntity.ok(salaDto);
    }

    @GetMapping("/id/{idSala}")
    public ResponseEntity<SalaDto> obtenerSala(@PathVariable Long idSala) {
        Sala sala = salaService.obtenerSala(idSala);
        if (sala == null) {
            return ResponseEntity.notFound().build();
        }
        SalaDto salaDto = modelMapper.map(sala, SalaDto.class);
        return ResponseEntity.ok(salaDto);
    }

    @GetMapping("/url")
    public ResponseEntity<SalaDto> obtenerSalaPorUrl(@RequestParam String url) {
        Sala sala = salaService.obtenerSala(url);
        if (sala == null) {
            return ResponseEntity.notFound().build();
        }
        SalaDto dto = modelMapper.map(sala, SalaDto.class);
        return ResponseEntity.ok(dto);
    }

}
