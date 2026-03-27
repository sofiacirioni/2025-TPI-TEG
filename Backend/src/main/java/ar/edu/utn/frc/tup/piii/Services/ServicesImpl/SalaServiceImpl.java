package ar.edu.utn.frc.tup.piii.Services.ServicesImpl;

import ar.edu.utn.frc.tup.piii.Entities.SalaEntity;
import ar.edu.utn.frc.tup.piii.Repositories.SalaRepository;
import ar.edu.utn.frc.tup.piii.Services.SalaService;
import ar.edu.utn.frc.tup.piii.models.*;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Optional;

@Service
public class SalaServiceImpl implements SalaService {
    @Autowired
    public SalaRepository salaRepository;
    @Autowired
    public ModelMapper modelMapper;

    private static final String CODIGO_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    // Sin I, O, 0, 1 para evitar confusión visual al compartir verbalmente

    private String generarCodigoSala() {
        SecureRandom random = new SecureRandom();
        StringBuilder codigo = new StringBuilder(6);
        for (int i = 0; i < 6; i++) {
            codigo.append(CODIGO_CHARS.charAt(random.nextInt(CODIGO_CHARS.length())));
        }
        return codigo.toString();
    }

    public Sala crearSala(Sala sala, Usuario creador, String nombre) {

        sala.setUrl(generarCodigoSala());
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