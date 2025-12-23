package ar.edu.utn.frc.tup.piii.Repositories;

import ar.edu.utn.frc.tup.piii.Entities.UsuarioEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
@DataJpaTest
public class UsuariosRepositoryTest {
    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private TestEntityManager entityManager;

    private UsuarioEntity usuario;

    @BeforeEach
    void setUp() {
        usuario = new UsuarioEntity();
        usuario.setNombre("Carlos");
        usuario.setApellido("Lopez");
        usuario.setCorreo("carlos@gmail.com");
        usuario.setContrasenia("Segura123@");
        usuario.setImagen("img.png");

        entityManager.persist(usuario);
        entityManager.flush();
    }

    @Test
    void testFindByCorreoAndContrasenia_found() {
        Optional<UsuarioEntity> resultado = usuarioRepository.findByCorreoAndContrasenia("carlos@gmail.com", "Segura123@");

        assertTrue(resultado.isPresent(), "El usuario debería estar presente");
        assertEquals("Carlos", resultado.get().getNombre());
    }

    @Test
    void testFindByCorreoAndContrasenia_notFound() {
        Optional<UsuarioEntity> resultado = usuarioRepository.findByCorreoAndContrasenia("otro@mail.com", "123");
        assertTrue(resultado.isEmpty(), "No debería encontrar un usuario con credenciales incorrectas");
    }

    @Test
    void testFindByCorreo_found() {
        Optional<UsuarioEntity> resultado = usuarioRepository.findByCorreo("carlos@gmail.com");

        assertTrue(resultado.isPresent());
        assertEquals("Lopez", resultado.get().getApellido());
    }

    @Test
    void testFindByCorreo_notFound() {
        Optional<UsuarioEntity> resultado = usuarioRepository.findByCorreo("marta@gmail.com");

        assertTrue(resultado.isEmpty());
    }

    @Test
    void testFindByIdUsuario_found() {
        Optional<UsuarioEntity> resultado = usuarioRepository.findByIdUsuario(usuario.getIdUsuario());

        assertTrue(resultado.isPresent());
        assertEquals("Carlos", resultado.get().getNombre());
    }

    @Test
    void testFindByIdUsuario_notFound() {
        Optional<UsuarioEntity> resultado = usuarioRepository.findByIdUsuario(999L);

        assertTrue(resultado.isEmpty());
    }
}


