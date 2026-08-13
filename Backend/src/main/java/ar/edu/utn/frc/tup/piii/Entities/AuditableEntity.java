package ar.edu.utn.frc.tup.piii.Entities;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Marcas de tiempo de alta y última modificación, el par de auditoría que
 * cualquier registro con vida propia debería tener.
 *
 * <p>Las escribe Spring Data (ver {@code @EnableJpaAuditing} en Application),
 * no la aplicación: son metadatos de la fila, no datos de dominio. Por eso
 * quedan fuera de equals/hashCode de las entidades que la heredan.
 *
 * <p>No la heredan las entidades cuyo ciclo de vida ya está contado por otra
 * cosa: los pactos llevan sus propias fechas de propuesta, aceptación y
 * ruptura; los turnos, su inicio; y jugadores, estado_paises y estado_tarjetas
 * nacen y mueren con la partida que los contiene. Tampoco los datos de
 * referencia (países, objetivos, tarjetas), que son semilla inmutable.
 */
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public abstract class AuditableEntity {

    @CreatedDate
    @Column(name = "fecha_alta", nullable = false, updatable = false)
    private LocalDateTime fechaAlta;

    @LastModifiedDate
    @Column(name = "fecha_actualizacion", nullable = false)
    private LocalDateTime fechaActualizacion;
}
