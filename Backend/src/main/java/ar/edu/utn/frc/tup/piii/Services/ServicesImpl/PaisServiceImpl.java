package ar.edu.utn.frc.tup.piii.Services.ServicesImpl;

import ar.edu.utn.frc.tup.piii.Entities.PaisEntity;
import ar.edu.utn.frc.tup.piii.Repositories.PaisRepository;
import ar.edu.utn.frc.tup.piii.Services.PaisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PaisServiceImpl implements PaisService {

    @Autowired
    private PaisRepository paisRepository;

    @Override
    public List<PaisEntity> obtenerTodos() {
        return paisRepository.findAll();
    }

    @Override
    public Optional<PaisEntity> buscarPorId(Long id) {
        return paisRepository.findById(id);
    }


}