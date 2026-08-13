package ar.edu.utn.frc.tup.piii.configs;

import ar.edu.utn.frc.tup.piii.Entities.ObjetivoEntity;
import ar.edu.utn.frc.tup.piii.Repositories.ObjetivoRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
public class MojibakeFixer implements CommandLineRunner {

    private final ObjetivoRepository objetivoRepository;

    public MojibakeFixer(ObjetivoRepository objetivoRepository) {
        this.objetivoRepository = objetivoRepository;
    }

    @Override
    public void run(String... args) {
        List<ObjetivoEntity> todos = objetivoRepository.findAll();
        int arreglados = 0;
        for (ObjetivoEntity o : todos) {
            String desc = o.getDescripcion();
            if (desc == null) continue;
            if (!esMojibake(desc)) continue;
            String corregido = new String(desc.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
            if (!corregido.equals(desc)) {
                o.setDescripcion(corregido);
                objetivoRepository.save(o);
                arreglados++;
            }
        }
        if (arreglados > 0) {
            System.out.println(">>> MojibakeFixer: corregidas " + arreglados + " descripciones de objetivos a UTF-8");
        }
    }

    private boolean esMojibake(String s) {
        return s.indexOf('Ã') >= 0 || s.indexOf('Â') >= 0 || s.indexOf('\uFFFD') >= 0;
    }
}
