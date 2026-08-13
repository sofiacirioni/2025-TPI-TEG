package ar.edu.utn.frc.tup.piii.Services;

import ar.edu.utn.frc.tup.piii.Dtos.PactoDto;
import ar.edu.utn.frc.tup.piii.Dtos.ProponerPactoDto;

import java.util.List;

public interface PactoService {

    PactoDto proponer(ProponerPactoDto dto);

    PactoDto aceptar(Long pactoId, Long jugadorReceptorId);

    PactoDto rechazar(Long pactoId, Long jugadorReceptorId);

    PactoDto romperVoluntariamente(Long pactoId, Long jugadorId);

    /**
     * Los pactos que la mesa tiene que ver: propuestas sin responder, tratados
     * vigentes y los rotos que siguen en período de gracia. No sirve para decidir
     * si un ataque es legal — para eso está {@link #ataqueViolaPactoActivo}.
     */
    List<PactoDto> listarActivos(Long partidaId);

    /** True si el ataque del jugador atacante al país atacado violaría algún pacto activo. */
    boolean ataqueViolaPactoActivo(Long jugadorAtacanteId, Long paisAtacadoId, Long partidaId);

    /** Marca como ROTO_AUTOMATICO los pactos cuyo paisProtegido o paisZona haya sido conquistado. */
    void verificarRupturaAutomaticaPorConquista(Long paisConquistadoId, Long nuevoDueñoId, Long partidaId);

    /** Cuando un jugador queda eliminado, marca todos sus pactos como EXPIRADO. */
    void expirarPactosDeJugadorEliminado(Long jugadorId, Long partidaId);

    /** Procesa pactos en estado ROTO_VOLUNTARIO al inicio de un turno: marca EXPIRADO los que cumplen el período de gracia. */
    void procesarPactosEnGracia(Long partidaId, Integer turnoNumeroQueInicia);
}
