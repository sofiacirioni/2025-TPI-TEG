package ar.edu.utn.frc.tup.piii.Services.ServicesImpl;

import ar.edu.utn.frc.tup.piii.Dtos.ObjetivoDto;
import ar.edu.utn.frc.tup.piii.Dtos.ObjetivoItemDto;
import ar.edu.utn.frc.tup.piii.Dtos.ObjetivoProgresoDto;
import ar.edu.utn.frc.tup.piii.Dtos.VerificacionObjetivoDto;
import ar.edu.utn.frc.tup.piii.Entities.*;
import ar.edu.utn.frc.tup.piii.Repositories.*;
import ar.edu.utn.frc.tup.piii.Services.EstadoPaisService;
import ar.edu.utn.frc.tup.piii.Services.LimiteService;
import ar.edu.utn.frc.tup.piii.Services.ObjetivoService;
import ar.edu.utn.frc.tup.piii.models.Color;
import ar.edu.utn.frc.tup.piii.models.EstadoPartida;
import ar.edu.utn.frc.tup.piii.models.TipoObjetivo;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.*;
import java.util.stream.Collectors;


@Service
public class ObjetivoServiceImpl implements ObjetivoService {
    @Autowired
    private ObjetivoRepository objetivoRepository;

    @Autowired
    private EstadoPaisRepository estadoPaisRepository;

    @Autowired
    private EstadoPaisService estadoPaisService;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private JugadorRepository jugadorRepository;


    @Autowired
    private PartidaRepository partidaRepository;

    @Autowired
    private LimiteService limiteService;

    @Override
    public List<ObjetivoDto> obtenerObjetivosSecretos() {
        return objetivoRepository.findByTipoObjetivo(TipoObjetivo.SECRETO)
                .stream()
                .map(obj -> modelMapper.map(obj, ObjetivoDto.class))
                .collect(Collectors.toList());
    }

    @Override
    public ObjetivoDto obtenerObjetivoGral() {
        ObjetivoEntity objetivoEntity = objetivoRepository.findFirstByTipoObjetivo(TipoObjetivo.GENERAL)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Objetivo general no encontrado."));
        return modelMapper.map(objetivoEntity, ObjetivoDto.class);
    }

    @Override
    public ObjetivoDto obtenerObjetivoById(Long id) {
        ObjetivoEntity objetivoEntity = objetivoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Objetivo no encontrado."));
        return modelMapper.map(objetivoEntity, ObjetivoDto.class);
    }

    @Override
    public void asignarObjetivosSecretos(List<JugadorEntity> jugadores) {
        List<ObjetivoEntity> objetivosSecretos = objetivoRepository.findByTipoObjetivo(TipoObjetivo.SECRETO);

        if (jugadores.size() > objetivosSecretos.size()) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "No hay suficientes objetivos para asignar a jugadores.");
        }

        Random random = new Random();

        for (int i = 0; i < jugadores.size(); i++) {
            JugadorEntity jugador = jugadores.get(i);

            List<ObjetivoEntity> objetivos = new ArrayList<>(objetivosSecretos);

            objetivos = objetivos.stream()
                    .filter(obj -> {
                        Color colorObjetivo = obj.getColorEnemigo();

                        // Permitimos color null (objetivo sin color)
                        if (colorObjetivo == null) return true;

                        // No permitir que el objetivo sea del mismo color que el jugador
                        if (colorObjetivo.equals(jugador.getColor())) return false;

                        // Solo permitir si el color existe en otro jugador
                        return jugadores.stream()
                                .anyMatch(j -> !j.equals(jugador) && colorObjetivo.equals(j.getColor()));
                    })
                    .toList();

            ObjetivoEntity objetivoAsignado = objetivos.get(random.nextInt(objetivos.size()));

            jugador.setObjetivo(objetivoAsignado);
            jugadorRepository.save(jugador);
        }

    }

//    //funciones auxiliares para limitrofes
//    private boolean tieneGrupoLimitrofe(List<PaisEntity> paisesJugador, int cantidadRequerida) {
//        // Mapeo de país a sus vecinos (entre los países que posee el jugador)
//        Map<Long, List<Long>> grafo = new HashMap<>();
//
//        for (PaisEntity pais : paisesJugador) {
//            List<PaisEntity> vecinos = limiteService.obtenerVecinos(pais);
//            List<Long> vecinosIds = vecinos.stream()
//                    .filter(p -> paisesJugador.contains(p)) // solo si el jugador también lo posee
//                    .map(PaisEntity::getIdPais)
//                    .toList();
//
//            grafo.put(pais.getIdPais(), vecinosIds);
//        }
//
//        // Algoritmo de búsqueda (DFS o BFS)
//        Set<Long> visitados = new HashSet<>();
//
//        for (PaisEntity pais : paisesJugador) {
//            if (!visitados.contains(pais.getIdPais())) {
//                int sizeGrupo = contarConexos(pais.getIdPais(), grafo, visitados);
//                if (sizeGrupo >= cantidadRequerida) return true;
//            }
//        }
//
//        return false;
//    }
//
//    private int contarConexos(Long idActual, Map<Long, List<Long>> grafo, Set<Long> visitados) {
//        Stack<Long> stack = new Stack<>();
//        stack.push(idActual);
//        int count = 0;
//
//        while (!stack.isEmpty()) {
//            Long actual = stack.pop();
//            if (visitados.contains(actual)) continue;
//            visitados.add(actual);
//            count++;
//
//            List<Long> vecinos = grafo.getOrDefault(actual, List.of());
//            for (Long vecino : vecinos) {
//                if (!visitados.contains(vecino)) {
//                    stack.push(vecino);
//                }
//            }
//        }
//
//        return count;
//    }
    
    @Override
    public VerificacionObjetivoDto verificarObjetivos(Long idJugador) {

        JugadorEntity jugador = jugadorRepository.findById(idJugador).
                orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No se encontro al jugador."));

        List<EstadoPaisEntity> paises = estadoPaisRepository.findEstadoPaisEntitiesByJugador_IdJugador(idJugador);

        ObjetivoEntity objetivo = objetivoRepository.findById(jugador.getObjetivo().getId()).
                orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No se encontro el objetivo."));

        List<ObjetivoEntity> objetivoGeneral = objetivoRepository.findByTipoObjetivo(TipoObjetivo.GENERAL);

        PartidaEntity partidaEntity = partidaRepository.findById(jugador.getPartida().getIdPartida()).
                orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No se encontro la partida."));

        if (objetivo.getColorEnemigo() != null) {

            JugadorEntity jugadorEnemigo = partidaEntity.getJugadores()
                    .stream()
                    .filter(j -> j.getColor() == objetivo.getColorEnemigo())
                    .findFirst()
                    .orElse(null);

            if (jugadorEnemigo != null && jugadorEnemigo.isPerdio()) {
                Color eliminadoPor = jugadorEnemigo.getEliminadoPorColor();
                boolean yoLoElimine = eliminadoPor == null || eliminadoPor == jugador.getColor();

                if (yoLoElimine) {
                    // El portador eliminó al enemigo → gana
                    partidaEntity.setGanador(jugador);
                    partidaEntity.setEstadoPartida(EstadoPartida.TERMINADA);
                    for (JugadorEntity j : partidaEntity.getJugadores()) {
                        j.setPerdio(true);
                    }
                    partidaRepository.save(partidaEntity);

                    return VerificacionObjetivoDto.builder()
                            .gano(true)
                            .objetivoCumplido(objetivo.getDescripcion())
                            .build();
                }
                // Enemigo fue eliminado por otro → objetivo se convierte: cae al check de 30 países
            }

        } else {

            if (objetivo.getCantidadPaisesObjetivo() != null
                    && paises.size() >= objetivo.getCantidadPaisesObjetivo()) {

                Map<String, Long> paisesPorContinente = paises.stream()
                        .map(e -> e.getPais().getContinente().getNombre())
                        .collect(Collectors.groupingBy(nombre -> nombre, Collectors.counting()));

                boolean cumple = true;

                if (objetivo.getAfrica() != null && //si es menor que la cant requerida no cumple
                        paisesPorContinente.getOrDefault("Africa", 0L) < objetivo.getAfrica())
                    cumple = false;
                if (objetivo.getAsia() != null &&
                        paisesPorContinente.getOrDefault("Asia", 0L) < objetivo.getAsia())
                    cumple = false;
                if (objetivo.getEuropa() != null &&
                        paisesPorContinente.getOrDefault("Europa", 0L) < objetivo.getEuropa())
                    cumple = false;
                if (objetivo.getAmericaNorte() != null &&
                        paisesPorContinente.getOrDefault("America del Norte", 0L) < objetivo.getAmericaNorte())
                    cumple = false;
                if (objetivo.getAmericaSur() != null &&
                        paisesPorContinente.getOrDefault("America del Sur", 0L) < objetivo.getAmericaSur())
                    cumple = false;
                if (objetivo.getOceania() != null &&
                        paisesPorContinente.getOrDefault("Oceania", 0L) < objetivo.getOceania())
                    cumple = false;

                if (cumple) {

                    partidaEntity.setGanador(jugador);
                    partidaEntity.setEstadoPartida(EstadoPartida.TERMINADA);

                    for(JugadorEntity j : partidaEntity.getJugadores()) {
                        j.setPerdio(true);
                    }

                    partidaRepository.save(partidaEntity);

                    return VerificacionObjetivoDto.builder()
                            .gano(true)
                            .objetivoCumplido(objetivo.getDescripcion())
                            .build();
                }
            }
        }
        if (paises.size() >= 30) {

            partidaEntity.setGanador(jugador);
            partidaEntity.setEstadoPartida(EstadoPartida.TERMINADA);

            for(JugadorEntity j : partidaEntity.getJugadores()) {
                j.setPerdio(true);
            }

            partidaRepository.save(partidaEntity);

            return VerificacionObjetivoDto.builder()
                    .gano(true)
                    .objetivoCumplido(objetivoGeneral.get(0).getDescripcion())
                    .build();
        }


        return VerificacionObjetivoDto.builder()
                .gano(false)
                .objetivoCumplido("Ninguno")
                .build();
    }

    // ── Progreso del objetivo ──────────────────────────────────────────────────────

    @Override
    public ObjetivoProgresoDto calcularProgreso(Long idJugador) {
        JugadorEntity jugador = jugadorRepository.findById(idJugador)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Jugador no encontrado."));

        ObjetivoEntity objetivo = objetivoRepository.findById(jugador.getObjetivo().getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Objetivo no encontrado."));

        PartidaEntity partida = partidaRepository.findById(jugador.getPartida().getIdPartida())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Partida no encontrada."));

        List<EstadoPaisEntity> misPaises = estadoPaisRepository
                .findEstadoPaisEntitiesByJugador_IdJugador(idJugador);

        // Detectar si el objetivo de eliminación fue convertido (enemigo eliminado por otro jugador)
        boolean objetivoConvertido = false;
        if (objetivo.getColorEnemigo() != null) {
            JugadorEntity enemigo = partida.getJugadores().stream()
                    .filter(j -> j.getColor() == objetivo.getColorEnemigo())
                    .findFirst().orElse(null);
            if (enemigo != null && enemigo.isPerdio()) {
                Color killedBy = enemigo.getEliminadoPorColor();
                if (killedBy != null && killedBy != jugador.getColor()) {
                    objetivoConvertido = true;
                }
            }
        }

        List<ObjetivoItemDto> items = new ArrayList<>();
        String descripcion;
        boolean completado;

        if (objetivoConvertido) {
            // El enemigo fue eliminado por otro jugador → objetivo se convierte a 30 países
            int actual = misPaises.size();
            items.add(ObjetivoItemDto.builder()
                    .descripcion("Objetivo convertido — Países en dominio")
                    .valorActual(actual)
                    .valorObjetivo(30)
                    .completado(actual >= 30)
                    .build());
            descripcion = "Conquistar 30 países (objetivo convertido)";
            completado = actual >= 30;

        } else if (objetivo.getColorEnemigo() != null) {
            // Objetivo de eliminación activo: mostrar cuántos países le quedan al enemigo
            JugadorEntity enemigo = partida.getJugadores().stream()
                    .filter(j -> j.getColor() == objetivo.getColorEnemigo())
                    .findFirst().orElse(null);

            String nombreEnemigo = enemigo != null ? enemigo.getNombre() : objetivo.getColorEnemigo().name();
            boolean eliminado = enemigo != null && enemigo.isPerdio();
            int paisesEnemigo = eliminado ? 0
                    : (enemigo != null
                        ? estadoPaisRepository.findEstadoPaisEntitiesByJugador_IdJugador(enemigo.getIdJugador()).size()
                        : 0);

            // valorActual = 0 cuando está eliminado (objetivo: llegar a 0 países del enemigo)
            items.add(ObjetivoItemDto.builder()
                    .descripcion("Países restantes de " + nombreEnemigo)
                    .valorActual(paisesEnemigo)
                    .valorObjetivo(0)
                    .completado(eliminado)
                    .build());

            descripcion = "Destruir al ejército de " + nombreEnemigo + " (" + objetivo.getColorEnemigo().name() + ")";
            completado = eliminado;

        } else {
            // Objetivo territorial: X/N por categoría
            Map<String, Long> porContinente = misPaises.stream()
                    .map(e -> e.getPais().getContinente().getNombre())
                    .collect(Collectors.groupingBy(n -> n, Collectors.counting()));

            descripcion = objetivo.getDescripcion();

            if (objetivo.getCantidadPaisesObjetivo() != null && objetivo.getCantidadPaisesObjetivo() > 0) {
                int actual = misPaises.size();
                int req = objetivo.getCantidadPaisesObjetivo();
                items.add(ObjetivoItemDto.builder()
                        .descripcion("Países en dominio total")
                        .valorActual(actual)
                        .valorObjetivo(req)
                        .completado(actual >= req)
                        .build());
            }

            addContinenteItem(items, objetivo.getAfrica(),       "Africa",           porContinente);
            addContinenteItem(items, objetivo.getAsia(),         "Asia",             porContinente);
            addContinenteItem(items, objetivo.getEuropa(),       "Europa",           porContinente);
            addContinenteItem(items, objetivo.getAmericaNorte(), "America del Norte", porContinente);
            addContinenteItem(items, objetivo.getAmericaSur(),   "America del Sur",  porContinente);
            addContinenteItem(items, objetivo.getOceania(),      "Oceania",          porContinente);

            completado = items.stream().allMatch(ObjetivoItemDto::isCompletado);
        }

        return ObjetivoProgresoDto.builder()
                .descripcion(descripcion)
                .items(items)
                .completado(completado)
                .objetivoConvertido(objetivoConvertido)
                .build();
    }

    private void addContinenteItem(List<ObjetivoItemDto> items, Integer requerido,
                                   String nombre, Map<String, Long> porContinente) {
        if (requerido == null || requerido == 0) return;
        int actual = porContinente.getOrDefault(nombre, 0L).intValue();
        items.add(ObjetivoItemDto.builder()
                .descripcion("Países de " + nombre)
                .valorActual(actual)
                .valorObjetivo(requerido)
                .completado(actual >= requerido)
                .build());
    }
}
