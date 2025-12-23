package ar.edu.utn.frc.tup.piii.Services;




public interface JuegoService {
    void repartirPaisesYObjetivos(Long idPartida);
    void iniciarColocacionEjercitos(Long idPartida);
    void avanzarFase(Long idPartida);
    boolean comprobarObjetivoCumplido(Long idPartida);
}
