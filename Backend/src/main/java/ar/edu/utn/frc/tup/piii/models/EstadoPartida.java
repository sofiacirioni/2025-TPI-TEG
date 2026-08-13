package ar.edu.utn.frc.tup.piii.models;

public enum EstadoPartida {
    /** Llegó a un desenlace: hay un vencedor. Es la única que suma al historial. */
    TERMINADA,
    PAUSADA,
    EN_JUEGO,
    /**
     * Los comandantes se retiraron antes del desenlace. Figura en el registro
     * de campañas pero no cuenta para las estadísticas: no se puede saber
     * quién habría ganado.
     */
    ABANDONADA
}
