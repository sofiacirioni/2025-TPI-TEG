package ar.edu.utn.frc.tup.piii.Services.ServicesImpl;

import ar.edu.utn.frc.tup.piii.Dtos.EstadoPaises.AgregarFichas;
import ar.edu.utn.frc.tup.piii.Dtos.EstadoPaises.EstadoPaisFicha;
import ar.edu.utn.frc.tup.piii.Entities.*;
import ar.edu.utn.frc.tup.piii.Repositories.*;
import ar.edu.utn.frc.tup.piii.Services.EstadoPaisService;
import ar.edu.utn.frc.tup.piii.Services.PaisService;
import ar.edu.utn.frc.tup.piii.Dtos.EstadoPaisDto;
import ar.edu.utn.frc.tup.piii.Dtos.EstadoPaises.MoverFichas;
import ar.edu.utn.frc.tup.piii.models.Estadistica;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class EstadoPaisServiceImpl implements EstadoPaisService {
    @Autowired
    EstadoPaisRepository estadoPaisRepository;
    @Autowired
    PaisService paisService;
    @Autowired
    PartidaRepository partidaRepository;
    @Autowired
    EstadisticaRepository estadisticaRepository;
    @Autowired
    JugadorRepository jugadorRepository;
    @Autowired
    ModelMapper modelMapper;
    @Autowired
    PaisRepository paisRepository;
    @Autowired
    LimiteRepository limiteRepository;

    @Override
    public EstadoPaisDto getEstadoPais(Long id) {
        EstadoPaisEntity estadoPaisEntity = estadoPaisRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("EstadoPais no encontrado"));

        return modelMapper.map(estadoPaisEntity, EstadoPaisDto.class);
    }

    public EstadoPaisEntity getEstadoPaisEntity(Long id) {
        EstadoPaisEntity estadoPaisEntity = estadoPaisRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("EstadoPais no encontrado"));

        return estadoPaisEntity;
    }

    @Override
    public EstadoPaisDto createEstadoPais(EstadoPaisDto estadoPaisDto)
    {
        EstadoPaisEntity estadoPaisEntity = modelMapper.map(estadoPaisDto, EstadoPaisEntity.class);
        EstadoPaisEntity savedEstadoPaisEntity = estadoPaisRepository.save(estadoPaisEntity);
        return modelMapper.map(savedEstadoPaisEntity, EstadoPaisDto.class);
    }

    @Override
    public void repartirPaises(Long partidaId){
        PartidaEntity partidaEntity = partidaRepository.findById(partidaId)
                .orElseThrow(() -> new RuntimeException("Partida no encontrada"));

        partidaEntity.getEstadoPaises().size();
        partidaEntity.getJugadores().size();
        List<EstadoPaisEntity> estadoPaisEntities = partidaEntity.getEstadoPaises();
        List<JugadorEntity> jugadorEntities = partidaEntity.getJugadores();


        Collections.shuffle(estadoPaisEntities); // Mezcla aleatoria

        int numJugadores = jugadorEntities.size();
        int index = 0;

        for (EstadoPaisEntity estado : estadoPaisEntities) {
            JugadorEntity jugador = jugadorEntities.get(index % numJugadores);
            estado.setJugador(jugador);
            estado.setCantidadTropas(1);
            index++;
            estadoPaisRepository.save(estado);
        }

    }

    @Override
    public EstadoPaisDto updateEstadoPais(EstadoPaisDto estadoPaisDto)
    {
        EstadoPaisEntity estadoPaisEntity = modelMapper.map(estadoPaisDto, EstadoPaisEntity.class);
        EstadoPaisEntity updatedEstadoPaisEntity = estadoPaisRepository.save(estadoPaisEntity);
        return modelMapper.map(updatedEstadoPaisEntity, EstadoPaisDto.class);
    }

    public void cambiarPropietario(Long idEstadoPaisObtenido, Long idEstadoPaisAtacante, Long idNuevoPropietario ){
        EstadoPaisEntity estadoPaisEntity = getEstadoPaisEntity(idEstadoPaisObtenido);
        EstadoPaisEntity estadoPaisEntityAt = getEstadoPaisEntity(idEstadoPaisAtacante);

        JugadorEntity jugadorEntity = jugadorRepository.findById(idNuevoPropietario)
                        .orElseThrow(() -> new RuntimeException("Jugador no encontrado"));

        estadoPaisEntity.setJugador(jugadorEntity);
        estadoPaisEntity.setCantidadTropas(1);
        estadoPaisEntityAt.setCantidadTropas(estadoPaisEntityAt.getCantidadTropas()-1);

        estadoPaisRepository.save(estadoPaisEntity);
        estadoPaisRepository.save(estadoPaisEntityAt);
    }

    @Override
    public List<EstadoPaisDto> getLimitesEstadoPais(Long idEstadoPais) {
        EstadoPaisEntity estadoPaisEntity = estadoPaisRepository.findById(idEstadoPais)
                .orElseThrow(() -> new RuntimeException("EstadoPais no encontrado"));

        List<LimiteEntity> limites = limiteRepository.findByPais1_IdPaisOrPais2_IdPais(estadoPaisEntity.getPais().getIdPais(), estadoPaisEntity.getPais().getIdPais());

        Set<Long> idsPaisesLimite = limites.stream()
                .map(limite -> {
                    if (limite.getPais1().getIdPais().equals(estadoPaisEntity.getPais().getIdPais())) {
                        return limite.getPais2().getIdPais();
                    } else {
                        return limite.getPais1().getIdPais();
                    }
                })
                .collect(Collectors.toSet());

        List<EstadoPaisEntity> estadosLimites = estadoPaisRepository.findAllByPais_IdPaisInAndPartida_IdPartida(idsPaisesLimite, estadoPaisEntity.getPartida().getIdPartida());

        return estadosLimites.stream()
                .map(estado -> modelMapper.map(estado, EstadoPaisDto.class))
                .collect(Collectors.toList());
    }

    @Override
    public List<EstadoPaisEntity> getLimitesEstadoPaisEntity(Long idEstadoPais) {
        EstadoPaisEntity estadoPaisEntity = estadoPaisRepository.findById(idEstadoPais)
                .orElseThrow(() -> new RuntimeException("EstadoPais no encontrado"));

        List<LimiteEntity> limites = limiteRepository.findByPais1_IdPaisOrPais2_IdPais(estadoPaisEntity.getPais().getIdPais(), estadoPaisEntity.getPais().getIdPais());

        Set<Long> idsPaisesLimite = limites.stream()
                .map(limite -> {
                    if (limite.getPais1().getIdPais().equals(estadoPaisEntity.getPais().getIdPais())) {
                        return limite.getPais2().getIdPais();
                    } else {
                        return limite.getPais1().getIdPais();
                    }
                })
                .collect(Collectors.toSet());

        List<EstadoPaisEntity> estadosLimites = estadoPaisRepository.findAllByPais_IdPaisInAndPartida_IdPartida(idsPaisesLimite, estadoPaisEntity.getPartida().getIdPartida());

        return estadosLimites;
    }


    @Override
    public boolean sonLimitrofes(Long idPais1, Long idPais2){
        return limiteRepository.existeLimiteEntre(idPais1, idPais2);
    }

    @Override
    public void inicializarEstadosPaises(Long partidaId) {
        List<PaisEntity> paises = paisService.obtenerTodos();
        List<EstadoPaisEntity> estados = new ArrayList<>();

        PartidaEntity partidaEntity = partidaRepository.findById(partidaId)
                .orElseThrow(() -> new RuntimeException("Partida no encontrada"));

        for (PaisEntity pais : paises) {
            EstadoPaisEntity estado = new EstadoPaisEntity();
            estado.setPais(pais);
            estado.setCantidadTropas(0);
            estado.setPartida(partidaEntity);
            estadoPaisRepository.save(estado);
            estados.add(estado);
        }

        partidaEntity.setEstadoPaises(estados);
        partidaRepository.save(partidaEntity);

    }

    @Override
    public boolean agregarFichasEstadosPaises(AgregarFichas agregarFichas) {
        //return List.of();
        // Por cada EstadoPaisFicha de agregarFichas
            // Validar que el jugador sea el propietario
            // Buscar el EstadoPais con el idPais correspondiente
            // Agregarle la fichas
            // Guardar


        JugadorEntity jugador = jugadorRepository.findById(agregarFichas.getIdJugador())
                .orElseThrow(() -> new RuntimeException("Jugador no encontrado"));

        int totalTropas = agregarFichas.getPaisesFichas().stream()
                .mapToInt(ficha -> ficha.getCantidadFichas().intValue())
                .sum();

        for (EstadoPaisFicha ficha : agregarFichas.getPaisesFichas()) {
            Optional<EstadoPaisEntity> optionalEstado = estadoPaisRepository
                    .findByPaisIdPaisAndJugadorIdJugador(ficha.getIdPais(), agregarFichas.getIdJugador());

            if (optionalEstado.isPresent()) {
                EstadoPaisEntity estado = optionalEstado.get();
                estado.setCantidadTropas(estado.getCantidadTropas() + ficha.getCantidadFichas().intValue());
                estadoPaisRepository.save(estado);
            } else {
                throw new IllegalArgumentException("El jugador no controla el país con ID: " + ficha.getIdPais());
            }
        }

        jugador.setEjercito((int) (jugador.getEjercito() - (long )totalTropas));
        jugadorRepository.save(jugador);

        return true;
    }

/*    // Puede ser llamado por turno, o puede ser llamado por front
    @Override
    public List<EstadoPaisDto> fichasInicialesEstadosPaises(AgregarFichas agregarFichas) {
        //return List.of();
        // Por cada EstadoPaisFicha de agregarFichas
            // Validar que el jugador sea el propietario
            // Buscar el EstadoPais con el idPais correspondiente
            // Agregarle la fichas
            // Guardar
        List<EstadoPaisDto> actualizados = new ArrayList<>();

        for (EstadoPaisFicha ficha : agregarFichas.getPaisesFichas()) {
            Optional<EstadoPaisEntity> optionalEstado = estadoPaisRepository
                    .findByPaisIdPaisAndJugadorIdJugador(ficha.getIdPais(), agregarFichas.getIdJugador());

            if (optionalEstado.isPresent()) {
                EstadoPaisEntity estado = optionalEstado.get();
                estado.setCantidadTropas(estado.getCantidadTropas() + ficha.getCantidadFichas().intValue());
                EstadoPaisEntity saved = estadoPaisRepository.save(estado);
                actualizados.add(modelMapper.map(saved, EstadoPaisDto.class));
            } else {
                throw new IllegalArgumentException("El jugador no controla el país con ID: " + ficha.getIdPais());
            }
        }

        return actualizados;
    }*/

    // Puede ser llamado por turno, o puede ser llamado por front
    @Override
    public boolean agrupacionFichas(MoverFichas moverFichas) {
        //return List.of();
        // Por cada EstadoPaisAgrupacion de moverFichas
            // Validar que el jugador sea propietario de origen y destino
            // que haya un camino, o sea limitrofes del jugador entre ellos
            // quitar las tropas del origen
            // agregarselas al destino
            // guardar

        EstadoPaisEntity estadoOrigen = estadoPaisRepository
                .findByPais_IdPaisAndJugador_IdJugador(moverFichas.getIdPaisOrigen(), moverFichas.getIdJugador())
                .orElseThrow(() -> new RuntimeException("Origen no encontrado"));


        EstadoPaisEntity estadoDestino = estadoPaisRepository
                .findByPais_IdPaisAndJugador_IdJugador(moverFichas.getIdPaisDestino(), moverFichas.getIdJugador())
                .orElseThrow(() -> new RuntimeException("Destino no encontrado"));

        // Validar que el origen tenga la cantidad de fichas
        if( (estadoOrigen.getCantidadTropas() -  moverFichas.getCantidadFichas()) <= 0 ){
            throw new RuntimeException("Las fichas no son suficientes");
        }

        // Validar que sean limitrofe
        if(!sonLimitrofes(estadoOrigen.getPais().getIdPais(), estadoDestino.getPais().getIdPais())){
            throw new RuntimeException("Los paises no son limitrofes");
        }


        estadoOrigen.setCantidadTropas(estadoOrigen.getCantidadTropas() - moverFichas.getCantidadFichas().intValue());
        estadoDestino.setCantidadTropas(estadoDestino.getCantidadTropas() + moverFichas.getCantidadFichas().intValue());


        estadoOrigen = estadoPaisRepository.save(estadoOrigen);
        estadoDestino = estadoPaisRepository.save(estadoDestino);


        List<EstadoPaisEntity> estadosEntities = new ArrayList<>();
        estadosEntities.add(estadoOrigen);
        estadosEntities.add(estadoDestino);

        return !estadosEntities.isEmpty();
    }

}