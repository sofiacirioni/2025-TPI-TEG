package ar.edu.utn.frc.tup.piii.Services;

import ar.edu.utn.frc.tup.piii.models.Sala;
import ar.edu.utn.frc.tup.piii.models.Usuario;

import java.util.Optional;

public interface SalaService {
    Sala crearSala(Sala sala,Usuario creador, String nombre);
    Sala obtenerSala(Long idSala);
    Sala obtenerSala(String url);
}
