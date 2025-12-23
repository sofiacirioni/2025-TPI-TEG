package ar.edu.utn.frc.tup.piii.Services.ServicesImpl;

import ar.edu.utn.frc.tup.piii.Entities.LimiteEntity;
import ar.edu.utn.frc.tup.piii.Entities.PaisEntity;
import ar.edu.utn.frc.tup.piii.Repositories.LimiteRepository;
import ar.edu.utn.frc.tup.piii.Services.LimiteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class LimiteServiceImpl implements LimiteService {

    @Autowired
    private LimiteRepository limiteRepository;


    @Override
    public List<LimiteEntity> obtenerTodos() {
        return limiteRepository.findAll();
    }

    @Override
    public List<PaisEntity> obtenerVecinos(PaisEntity pais) {
        List<LimiteEntity> limites = limiteRepository.findAll();
        List<PaisEntity> vecinos = new ArrayList<>();

        for (LimiteEntity limite : limites) {
            if (limite.getPais1().equals(pais)) {
                vecinos.add(limite.getPais2());
            } else if (limite.getPais2().equals(pais)) {
                vecinos.add(limite.getPais1());
            }
        }

        return vecinos;
    }
}
