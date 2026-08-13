package ar.edu.utn.frc.tup.piii.Repositories;

import ar.edu.utn.frc.tup.piii.Entities.JugadorEntity;
import ar.edu.utn.frc.tup.piii.Entities.SalaEntity;
import ar.edu.utn.frc.tup.piii.Entities.UsuarioEntity;
import ar.edu.utn.frc.tup.piii.models.Color;
import ar.edu.utn.frc.tup.piii.models.EstadoSala;
import ar.edu.utn.frc.tup.piii.models.TipoJugador;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class SalaRepositoryTest {
    @Autowired
    private SalaRepository salaRepository;

    @Autowired
    private TestEntityManager entityManager;

    private UsuarioEntity usuario;
    private SalaEntity sala;
    private JugadorEntity jugador;

    @BeforeEach
    void setUp() {
        usuario = new UsuarioEntity();
        usuario.setUsuario("Carlos");
        usuario.setCorreo("carlos@example.com");
        usuario.setContrasenia("Segura123@");
        usuario.setImagen("img.png");
        entityManager.persist(usuario);

        sala = new SalaEntity();
        sala.setNombreSala("Sala Europa");
        sala.setEstado(EstadoSala.ESPERANDO);
        sala.setUrl("sala-europa");
        sala.setCreador(usuario);
        sala.setJugadores(new ArrayList<>());
        entityManager.persist(sala);

        jugador = new JugadorEntity();
        jugador.setNombre("Carlos");
        jugador.setUsuario(usuario);
        jugador.setColor(Color.AZUL);
        jugador.setTipoJugador(TipoJugador.HUMANO);
        jugador.setSala(sala);
        jugador.setTarjetas(new ArrayList<>());

        sala.getJugadores().add(jugador);
        entityManager.persist(jugador);
        entityManager.merge(sala);

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void testFindByCreadorIdUsuario_whenExists_thenReturnsSala() {
        Optional<SalaEntity> resultado = salaRepository.findByCreadorIdUsuario(usuario.getIdUsuario());

        assertTrue(resultado.isPresent(), "La sala debería existir");
        assertEquals("Sala Europa", resultado.get().getNombreSala());
        assertEquals(usuario.getIdUsuario(), resultado.get().getCreador().getIdUsuario());
    }

    @Test
    void testFindByCreadorIdUsuario_whenNotExists_thenReturnsEmpty() {
        Optional<SalaEntity> resultado = salaRepository.findByCreadorIdUsuario(999L);

        assertFalse(resultado.isPresent(), "No debería encontrar sala para ID inexistente");
    }

    @Test
    void testFindByIdSalaWithJugadores_whenExists_thenReturnsSalaAndJugadores() {
        Optional<SalaEntity> resultado = salaRepository.findByIdSalaWithJugadores(sala.getIdSala());

        assertTrue(resultado.isPresent());
        SalaEntity encontrada = resultado.get();
        assertEquals("Sala Europa", encontrada.getNombreSala());
        assertEquals(1, encontrada.getJugadores().size());
        assertEquals("Carlos", encontrada.getJugadores().get(0).getNombre());
    }

    @Test
    void testFindByIdSalaWithJugadores_whenNotExists_thenReturnsEmpty() {
        Optional<SalaEntity> resultado = salaRepository.findByIdSalaWithJugadores(999L);

        assertFalse(resultado.isPresent());
    }

    @Test
    void testFindByUrl_whenExists_thenReturnsSalaWithJugadores() {
        Optional<SalaEntity> resultado = salaRepository.findByUrl("sala-europa");

        assertTrue(resultado.isPresent());
        SalaEntity encontrada = resultado.get();
        assertEquals("Sala Europa", encontrada.getNombreSala());
        assertEquals(1, encontrada.getJugadores().size());
        assertEquals("Carlos", encontrada.getJugadores().get(0).getNombre());
    }

    @Test
    void testFindByUrl_whenNotExists_thenReturnsEmpty() {
        Optional<SalaEntity> resultado = salaRepository.findByUrl("url-inexistente");

        assertFalse(resultado.isPresent());
    }

}
