package ar.edu.utn.frc.tup.piii.Controller;

import ar.edu.utn.frc.tup.piii.Dtos.HistorialComandanteDto;
import ar.edu.utn.frc.tup.piii.Dtos.Login.UsuarioDto;
import ar.edu.utn.frc.tup.piii.Dtos.Login.UsuarioPutDto;
import ar.edu.utn.frc.tup.piii.Services.HistorialComandanteService;
import ar.edu.utn.frc.tup.piii.Services.UsuarioService;
import ar.edu.utn.frc.tup.piii.models.Usuario;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/usuario")
public class UsuarioController {
    @Autowired
    public UsuarioService usuarioService;
    @Autowired
    public ModelMapper modelMapper;
    @Autowired
    private HistorialComandanteService historialComandanteService;

    /** Hoja de servicios del comandante — la consume la libreta del perfil. */
    @GetMapping("/{idUsuario}/historial")
    public ResponseEntity<HistorialComandanteDto> obtenerHistorial(@PathVariable Long idUsuario) {
        return ResponseEntity.ok(historialComandanteService.obtenerHistorial(idUsuario));
    }

    @PutMapping("/actualizar")
    public ResponseEntity<UsuarioDto> actualizarUsuario(@RequestBody UsuarioPutDto request) {
        Usuario usuarioActualizado = usuarioService.actualizarUsuario(
                request.getCorreo(),
                request.getContraseniaActual(),
                request.getNuevaContrasenia(),
                request.getImagen());

        if (usuarioActualizado == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        UsuarioDto usuarioDto = modelMapper.map(usuarioActualizado, UsuarioDto.class);
        return ResponseEntity.ok(usuarioDto);
    }

    @PatchMapping("/imagen")
    public ResponseEntity<UsuarioDto> actualizarImagen(@RequestBody Map<String, String> body) {
        String correo = body.get("correo");
        String imagen = body.get("imagen");

        Usuario actualizado = usuarioService.actualizarImagen(correo, imagen);
        UsuarioDto dto = modelMapper.map(actualizado, UsuarioDto.class);
        return ResponseEntity.ok(dto);
    }

    @DeleteMapping("/eliminar/{id}")
    public ResponseEntity<UsuarioDto> eliminarUsuario(@PathVariable Long id) {
        Usuario usuarioEliminado = usuarioService.eliminarUsuario(id);
        if (usuarioEliminado == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        UsuarioDto usuarioDto = modelMapper.map(usuarioEliminado, UsuarioDto.class);
        return ResponseEntity.ok(usuarioDto);
    }
}
