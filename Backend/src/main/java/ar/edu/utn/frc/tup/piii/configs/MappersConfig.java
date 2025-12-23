package ar.edu.utn.frc.tup.piii.configs;

import ar.edu.utn.frc.tup.piii.Entities.JugadorEntity;
import ar.edu.utn.frc.tup.piii.Entities.SalaEntity;
import ar.edu.utn.frc.tup.piii.models.Jugador;
import ar.edu.utn.frc.tup.piii.models.Sala;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.modelmapper.Conditions;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * ModelMapper and ObjectMapper configuration class.
 */
@Configuration
public class MappersConfig {

    /**
     * The ModelMapper bean by default.
     * @return the ModelMapper by default.
     */
    @Bean
    public ModelMapper modelMapper() {
        ModelMapper mapper = new ModelMapper();

        // Asegura que no se salten campos nulos o no inicializados
        mapper.getConfiguration()
                .setFieldMatchingEnabled(true)
                .setFieldAccessLevel(org.modelmapper.config.Configuration.AccessLevel.PRIVATE)
                .setSkipNullEnabled(false);

        // Mapea Jugador a JugadorEntity correctamente
        mapper.createTypeMap(Jugador.class, JugadorEntity.class);
        mapper.createTypeMap(Sala.class, SalaEntity.class);

        return mapper;
    }
    /**
     * The ModelMapper bean to merge objects.
     * @return the ModelMapper to use in updates.
     */
//    @Bean("mergerMapper")
//    public ModelMapper mergerMapper() {
//        ModelMapper mapper =  new ModelMapper();
//        mapper.getConfiguration()
//                .setPropertyCondition(Conditions.isNotNull());
//        return mapper;
//    }

    /**
     * The ObjectMapper bean.
     * @return the ObjectMapper with JavaTimeModule included.
     */
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        return objectMapper;
    }

}
