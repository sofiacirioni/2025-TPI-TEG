package ar.edu.utn.frc.tup.piii;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main class.
 */
@SpringBootApplication
@EnableJpaRepositories(basePackages = "ar.edu.utn.frc.tup.piii.Repositories")
// Va acá y no en una @Configuration aparte a propósito: @DataJpaTest arma su
// contexto a partir de esta clase, así que las entidades auditadas también
// reciben sus fechas en los tests de repositorio.
@EnableJpaAuditing
@EnableScheduling
@OpenAPIDefinition(info = @Info(
        title = "TEG-grupo8 API",
        version = "0.0.1-SNAPSHOT",
        description = "TEG project for Spring Boot"
))
public class Application {

    /**
     * Main program.
     * @param args application args
     */
    public static void main(String[] args) {
        System.out.println(">>> INICIANDO SPRING BOOT...");
        SpringApplication.run(Application.class, args);
        System.out.println(">>> SPRING BOOT INICIALIZADO CORRECTAMENTE");
    }
}
