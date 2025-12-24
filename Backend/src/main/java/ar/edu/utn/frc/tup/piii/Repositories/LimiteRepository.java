package ar.edu.utn.frc.tup.piii.Repositories;

import ar.edu.utn.frc.tup.piii.Entities.LimiteEntity;
import ar.edu.utn.frc.tup.piii.Entities.PaisEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LimiteRepository extends JpaRepository<LimiteEntity, Long> {
    LimiteEntity findLimiteByPais1AndPais2(PaisEntity pais1, PaisEntity pais2);

    @Query("""
                SELECT CASE
                    WHEN COUNT(l) > 0 THEN true
                    ELSE false
                END
                FROM LimiteEntity l
                WHERE (l.pais1.idPais = :id1 AND l.pais2.idPais = :id2)
                   OR (l.pais1.idPais = :id2 AND l.pais2.idPais = :id1)
            """)
    boolean existeLimiteEntre(@Param("id1") Long id1, @Param("id2") Long id2);

    List<LimiteEntity> findByPais1_IdPaisOrPais2_IdPais(Long IdPais1, Long IdPais2);
}
