package ar.edu.utn.frc.tup.piii.Services.ServicesImpl;

import ar.edu.utn.frc.tup.piii.Entities.SalaEntity;
import ar.edu.utn.frc.tup.piii.Repositories.SalaRepository;
import ar.edu.utn.frc.tup.piii.Services.SalaService;
import ar.edu.utn.frc.tup.piii.models.*;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

@Service
public class SalaServiceImpl implements SalaService {
    @Autowired
    public SalaRepository salaRepository;
    @Autowired
    public ModelMapper modelMapper;

    public Sala crearSala(Sala sala, Usuario creador, String nombre) {

        String urlUnica = UUID.randomUUID().toString();
        sala.setUrl(urlUnica);
        sala.setNombreSala(nombre);
        sala.setCreador(creador);
        sala.setEstado(EstadoSala.ESPERANDO);

        if (sala.getJugadores() == null) {
            sala.setJugadores(new ArrayList<>());
        }

        for (Jugador jugador : sala.getJugadores()) {
            jugador.setSala(sala);
        }

        SalaEntity salaEntity = modelMapper.map(sala, SalaEntity.class);
        SalaEntity salaGuardada = salaRepository.save(salaEntity);

        return modelMapper.map(salaGuardada, Sala.class);
    }

    @Transactional
    public Sala obtenerSala(Long idSala) {
        Optional<SalaEntity> salaEntityOpt = salaRepository.findByIdSalaWithJugadores(idSala);

        if (salaEntityOpt.isEmpty()) {
            throw new IllegalArgumentException("No se encontró la sala con ID: " + idSala);
        }

        return modelMapper.map(salaEntityOpt.get(), Sala.class);
    }

    @Transactional
    public Sala obtenerSala(String url) {
        Optional<SalaEntity> salaEntityOpt = salaRepository.findByUrl(url);
        if (salaEntityOpt.isEmpty()) {
            throw new IllegalArgumentException("No se encontró la sala con URL: " + url);
        }
        SalaEntity salaEntity = salaEntityOpt.get();
        if (salaEntity.getEstado() == EstadoSala.INICIADA) {
            throw new IllegalArgumentException("La sala ya está empezada");
        }

        return modelMapper.map(salaEntityOpt.get(), Sala.class);
    }

}
