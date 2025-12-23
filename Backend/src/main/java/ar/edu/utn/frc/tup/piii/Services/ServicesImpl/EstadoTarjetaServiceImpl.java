package ar.edu.utn.frc.tup.piii.Services.ServicesImpl;

import ar.edu.utn.frc.tup.piii.Dtos.EstadoTarjetaDto;
import ar.edu.utn.frc.tup.piii.Entities.EstadoTarjetaEntity;
import ar.edu.utn.frc.tup.piii.Entities.JugadorEntity;
import ar.edu.utn.frc.tup.piii.Entities.TarjetaEntity;
import ar.edu.utn.frc.tup.piii.Repositories.EstadoTarjetaRepository;
import ar.edu.utn.frc.tup.piii.Services.EstadoTarjetaService;
import ar.edu.utn.frc.tup.piii.Entities.*;
import ar.edu.utn.frc.tup.piii.Repositories.*;

import ar.edu.utn.frc.tup.piii.Dtos.TarjetaDto;
import ar.edu.utn.frc.tup.piii.Services.PaisService;
import ar.edu.utn.frc.tup.piii.models.Pais;
import ar.edu.utn.frc.tup.piii.models.Simbolo;
import jakarta.persistence.EntityNotFoundException;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class EstadoTarjetaServiceImpl implements EstadoTarjetaService {
    @Autowired
    private EstadoTarjetaRepository estadoTarjetaRepository;
    @Autowired
    private EstadoPaisRepository estadoPaisRepository;
    @Autowired
    private PaisService paisService;
    @Autowired
    private TarjetaRepository tarjetaRepository;
    @Autowired
    private PartidaRepository partidaRepository;
    @Autowired
    private JugadorRepository jugadorRepository;
    @Autowired
    private TurnoRepository turnoRepository;
    @Autowired
    private ModelMapper modelMapper;

    @Override
    @Transactional
    public TarjetaDto asignarTarjeta(EstadoTarjetaDto dto) {
        try {
            JugadorEntity jugador = jugadorRepository.findById((long) dto.getIdJugador())
                    .orElseThrow(() -> new EntityNotFoundException("Jugador no encontrado con ID: " + dto.getIdJugador()));

//            TurnoEntity turno = turnoRepository.findById((long) dto.getIdTurno())
//                    .orElseThrow(() -> new EntityNotFoundException("Turno no encontrado con ID: " + dto.getIdTurno()));

            EstadoTarjetaEntity estado = estadoTarjetaRepository.findById(dto.getIdEstadoTarjeta())
                    .orElseThrow(() -> new EntityNotFoundException("Estado de tarjeta no encontrado con ID: " + dto.getIdEstadoTarjeta()));
            if (estado.getJugador() != null) {
                throw new IllegalStateException("La tarjeta ya fue asignada a un jugador.");
            }
            estado.setJugador(jugador);
            //estado.setTurno(turno);
            estadoTarjetaRepository.save(estado);
            jugador.setConsquisto(false);
            jugadorRepository.save(jugador);

            validarMazo(estado.getPartida().getIdPartida());

            return new TarjetaDto().builder()
                    .idTarjeta(estado.getTarjeta().getIdTarjeta())
                    .pais(modelMapper.map(estado.getTarjeta().getPais(), Pais.class))
                    .simbolo(String.valueOf(estado.getTarjeta().getSimbolo()))
                    .build();
        } catch (EntityNotFoundException e) {
            throw new RuntimeException("Una o más entidades no fueron encontradas: " + e.getMessage());
        } catch (IllegalStateException e) {
            throw new RuntimeException("No se puede asignar la tarjeta: " + e.getMessage());
        }
    }

    private void validarMazo(Long partidaId){
        PartidaEntity partida = partidaRepository.findById(partidaId)
                .orElseThrow(() -> new EntityNotFoundException("Partida no encontrada con ID: " + partidaId));

        boolean tieneCartas = false;
        for (EstadoTarjetaEntity estadoTarjeta : partida.getMazo()) {
            if (estadoTarjeta.getJugador() == null && !estadoTarjeta.isCanjeada()) {
                tieneCartas = true;
                break;
            }
        }
        if(!tieneCartas){
            List<EstadoTarjetaEntity> mazo = estadoTarjetaRepository.findAllByPartida_IdPartida(partidaId);

            mazo.forEach(
                estadoTarjeta -> {
                    if(estadoTarjeta.isCanjeada()) {
                        estadoTarjeta.setUsada(false);
                        estadoTarjeta.setCanjeada(false);
                    }
                }
            );

            estadoTarjetaRepository.saveAll(mazo);
        }

    }

    @Override
    @Transactional
    public List<EstadoTarjetaEntity> obtenerEstadoTarjeta() {
        return estadoTarjetaRepository.findAll();
    }

    @Override
    @Transactional
    public EstadoTarjetaEntity obtenerPorId(Long id) {
        return estadoTarjetaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Estado de tarjeta no encontrado con ID: " + id));
    }

    @Override
    @Transactional
    public void eliminarEstadoTarjeta(Long id) {
        if (!estadoTarjetaRepository.existsById(id)) {
            throw new EntityNotFoundException("Estado de tarjeta no encontrado con ID: " + id);
        }
        estadoTarjetaRepository.deleteById(id);
    }

    @Override
    @Transactional
    public TarjetaDto obtenerTarjeta(Long jugadorId) {
        Optional<EstadoTarjetaEntity> estadoTarjetaEntity = estadoTarjetaRepository.findById(jugadorId);
        return estadoTarjetaEntity.map(tarjetaEntity -> modelMapper.map(tarjetaEntity, TarjetaDto.class)).orElse(null);
    }

    @Override
    @Transactional
    public void usarTarjetaEnPais(Long idTarjeta, Long idJugador) {
        EstadoTarjetaEntity estadoTarjeta = estadoTarjetaRepository.findById(idTarjeta)
                .orElseThrow(() -> new RuntimeException("Tarjeta no encontrada"));
        if (estadoTarjeta.isUsada()) {
            throw new RuntimeException("Esta tarjeta ya fue utilizada.");
        }

        PaisEntity pais = estadoTarjeta.getTarjeta().getPais();
        EstadoPaisEntity estadoPais = estadoPaisRepository
                .findByPaisIdPaisAndJugadorIdJugador(pais.getIdPais(), idJugador)
                .orElseThrow(() -> new RuntimeException("El jugador no es dueño del país representado por la tarjeta."));
        estadoPais.setCantidadTropas(estadoPais.getCantidadTropas() + 2);
        estadoTarjeta.setUsada(true);
        estadoTarjetaRepository.save(estadoTarjeta);
        estadoPaisRepository.save(estadoPais);
    }

    @Override
    @Transactional
    public Integer canjearTarjetas(List<Long> idTarjetas, Long idJugador) {
        if (idTarjetas.size() != 3) {
            throw new RuntimeException("Debes canjear exactamente 3 tarjetas.");
        }
        List<EstadoTarjetaEntity> tarjetas = estadoTarjetaRepository.findAllById(idTarjetas);
        if (tarjetas.size() != 3) {
            throw new RuntimeException("Una o más tarjetas no existen.");
        }
        for (EstadoTarjetaEntity tarjeta : tarjetas) {
            if (tarjeta.isCanjeada()) {
                throw new RuntimeException("Una de las tarjetas ya fue canjeada.");
            }
            if (!Objects.equals(tarjeta.getJugador().getIdJugador(), idJugador)) {
                throw new RuntimeException("Una de las tarjetas no pertenece al jugador.");
            }
        }
        List<Simbolo> simbolos = tarjetas.stream()
                .map(t -> t.getTarjeta().getSimbolo().name())
                .map(Simbolo::valueOf)
                .toList();
        boolean todosIguales = simbolos.stream().distinct().count() == 1;
        boolean todosDistintos = simbolos.stream().distinct().count() == 3;
        if (!todosIguales && !todosDistintos) {
            throw new RuntimeException("Debes canjear 3 tarjetas del mismo símbolo o 3 de símbolos distintos.");
        }
        List<EstadoTarjetaEntity> todasDelJugador = estadoTarjetaRepository.findByJugadorId(idJugador);
        long cantidadCanjesPrevios = todasDelJugador.stream().filter(EstadoTarjetaEntity::isCanjeada).count();
        int ejercitos;
        if (cantidadCanjesPrevios == 0) ejercitos = 4;
        else if (cantidadCanjesPrevios == 1) ejercitos = 7;
        else if (cantidadCanjesPrevios == 2) ejercitos = 10;
        else ejercitos = 10 + (int) (cantidadCanjesPrevios - 2) * 5;
        System.out.println("Se otorgan " + ejercitos + " ejércitos al jugador");
        tarjetas.forEach(t -> {
            t.setCanjeada(true);
            t.setJugador(null);
        });

        estadoTarjetaRepository.saveAll(tarjetas);

        JugadorEntity jugador = jugadorRepository.findById(idJugador)
                .orElseThrow(()-> new EntityNotFoundException("El jugador no fue encontrado"));

        jugador.setEjercito(jugador.getEjercito() + ejercitos);

        jugadorRepository.save(jugador);

        return ejercitos;
    }

    @Override
    @Transactional
    public void inicializarTarjetas(Long idPartida) {
        List<TarjetaEntity> tarjetaEntities = tarjetaRepository.findAll();
        List<EstadoTarjetaEntity> tarjetaEntityList = new ArrayList<>();

        PartidaEntity partidaEntity = partidaRepository.findById(idPartida)
                .orElseThrow(() -> new RuntimeException("Partida no encontrada"));

        for (TarjetaEntity tarjeta : tarjetaEntities) {
            EstadoTarjetaEntity estadoTarjetaEntity = new EstadoTarjetaEntity();
            estadoTarjetaEntity.setTarjeta(tarjeta);
            estadoTarjetaEntity.setPartida(partidaEntity);

            estadoTarjetaRepository.save(estadoTarjetaEntity);
            tarjetaEntityList.add(estadoTarjetaEntity);
        }

        partidaEntity.setMazo(tarjetaEntityList);
        partidaRepository.save(partidaEntity);
    }
}




