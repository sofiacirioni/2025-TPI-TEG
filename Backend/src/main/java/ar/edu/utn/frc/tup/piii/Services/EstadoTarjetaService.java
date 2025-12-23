package ar.edu.utn.frc.tup.piii.Services;

import ar.edu.utn.frc.tup.piii.Dtos.TarjetaDto;
import ar.edu.utn.frc.tup.piii.Dtos.EstadoTarjetaDto;
import ar.edu.utn.frc.tup.piii.Entities.EstadoTarjetaEntity;



import java.util.List;



public interface EstadoTarjetaService {
    TarjetaDto asignarTarjeta(EstadoTarjetaDto dto);
    List<EstadoTarjetaEntity> obtenerEstadoTarjeta();
    EstadoTarjetaEntity obtenerPorId(Long id);
    void eliminarEstadoTarjeta(Long id);
    TarjetaDto obtenerTarjeta(Long jugadorId);
    void usarTarjetaEnPais(Long idTarjeta, Long idJugador);
    Integer canjearTarjetas(List<Long> idTarjetas, Long idJugador);
    void inicializarTarjetas(Long idPartida);
}
