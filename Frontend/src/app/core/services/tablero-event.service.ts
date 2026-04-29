import { Injectable } from '@angular/core';
import { Observable, Subject } from 'rxjs';
import { GameEvent, GameEventTipo, PartidaEventWs } from '../models/interfaces/game-event.interface';

export interface HistorialEvento {
  texto: string;
  tipo: 'ataque' | 'ok' | 'normal';
}

/** Mapa de var CSS para colores de jugadores */
const COLOR_VAR_MAP: Record<string, string> = {
  ROJO:    'var(--player-rojo)',
  AZUL:    'var(--player-azul)',
  VERDE:   'var(--player-verde)',
  NARANJA: 'var(--player-naranja)',
  AMARILLO:'var(--player-dorado)',
  VIOLETA: 'var(--player-purpura)',
};

@Injectable({ providedIn: 'root' })
export class TableroEventService {

  private queue: GameEvent[] = [];
  private processing = false;
  private currentTipo: GameEventTipo | null = null;

  private currentEventSubject = new Subject<GameEvent | null>();
  /** Emite el evento actual (null cuando no hay nada mostrando). */
  readonly currentEvent$: Observable<GameEvent | null> = this.currentEventSubject.asObservable();

  /** Subject interno: la selección de dados emite el número elegido. */
  private diceResultSubject = new Subject<number>();
  readonly diceResult$: Observable<number> = this.diceResultSubject.asObservable();

  /** Canal independiente para el registro/historial: emite TODAS las acciones,
   *  incluso las del propio jugador. Las notificaciones efímeras siguen
   *  filtrando al autor (se procesan vía currentEvent$ con su propia política). */
  private historialEventSubject = new Subject<HistorialEvento>();
  readonly historialEvent$: Observable<HistorialEvento> = this.historialEventSubject.asObservable();

  /** Encola un evento genérico al final de la cola. */
  enqueue(event: GameEvent): void {
    this.queue.push(event);
    if (!this.processing) this.processNext();
  }

  /** Inserta un evento al FRENTE de la cola (tiene prioridad sobre los ya encolados). */
  enqueueAtFront(event: GameEvent): void {
    this.queue.unshift(event);
    if (!this.processing) this.processNext();
  }

  /**
   * Convierte un evento WS del backend en un GameEvent y lo encola.
   * Los eventos WS llegan a TODOS los jugadores (incluido el autor). Política:
   *   - Si el jugador local es el AUTOR de la acción → no se notifica (ya la ejecutó él).
   *   - Si es el DEFENSOR de un ataque → texto personalizado ("contra ti").
   *   - En otro caso → texto neutro de observador.
   */
  enqueueFromWs(ws: PartidaEventWs, miNombre: string | undefined): void {
    const colorKey = ws.jugadorColor?.toUpperCase() ?? '';
    const colorDefKey = ws.jugadorColorDefensor?.toUpperCase() ?? '';
    const color = COLOR_VAR_MAP[colorKey] ?? 'var(--player-rojo)';
    const colorDef = COLOR_VAR_MAP[colorDefKey] ?? '';
    const esAutor    = !!miNombre && ws.jugadorNombre === miNombre;
    const esDefensor = !!miNombre && !!ws.jugadorDefensor && ws.jugadorDefensor === miNombre;
    const esCombate = ws.tipo === 'ATAQUE_INICIADO' || ws.tipo === 'ATAQUE' || ws.tipo === 'CONQUISTA';

    // El registro/historial recibe TODAS las acciones (incluso las propias),
    // antes de cualquier filtro por autor. Las notificaciones efímeras quedan
    // gobernadas por la lógica de abajo (esAutor && !esCombate => return).
    this.emitirHistorialDesdeWs(ws);

    // En combate TODOS los roles ven el modal (atacante en "esperando",
    // defensor selecciona dados, espectador en modo lectura).
    if (esAutor && !esCombate) return;

    switch (ws.tipo) {
      case 'ATAQUE_INICIADO': {
        this.enqueue({
          tipo: 'ATAQUE_INICIADO',
          titulo: 'ATAQUE INICIADO',
          jugadorActivo: ws.jugadorNombre,
          colorJugador: color,
          colorJugadorKey: colorKey,
          jugadorDefensor: ws.jugadorDefensor ?? '',
          colorDefensor: colorDef,
          colorDefensorKey: colorDefKey,
          paisOrigen: ws.paisOrigen,
          paisDestino: ws.paisDestino,
          idAtacante: ws.idAtacante,
          idDefensor: ws.idDefensor,
          idPartida: ws.idPartida,
          diceSelection: {
            paisOrigen: ws.paisOrigen,
            paisDestino: ws.paisDestino,
            maxDados: ws.maxDadosDefensor ?? 3,
            timerSegundos: ws.timerSegundos ?? 5,
            cantDadosAtacante: ws.cantDadosAtacante,
          },
          duracionMs: 0,
        });
        break;
      }
      case 'ATAQUE':
      case 'CONQUISTA': {
        // Si el modal actual es ATAQUE_INICIADO (todos los roles esperando),
        // forzamos avance antes de encolar el resultado.
        if (this.currentTipo === 'ATAQUE_INICIADO') {
          this.processNext();
        }
        const tipo: GameEventTipo = ws.conquista ? 'CONQUISTA' : 'RESULTADO_DADOS';
        let titulo: string;
        if (esAutor) {
          titulo = ws.conquista ? '¡CONQUISTA!' : (ws.perdidasDefensor > ws.perdidasAtacante ? 'ATAQUE EXITOSO' : 'ATAQUE REPELIDO');
        } else if (esDefensor) {
          titulo = ws.conquista ? '¡TE CONQUISTARON!' : 'ATAQUE CONTRA TI';
        } else {
          titulo = ws.conquista ? '¡CONQUISTA!' : 'COMBATE';
        }
        const descripcion = esDefensor
          ? `${ws.jugadorNombre} atacó ${ws.paisDestino} desde ${ws.paisOrigen}`
          : undefined;
        this.enqueue({
          tipo,
          titulo,
          descripcion,
          jugadorActivo: ws.jugadorNombre,
          colorJugador: color,
          colorJugadorKey: colorKey,
          jugadorDefensor: ws.jugadorDefensor ?? '',
          colorDefensor: colorDef,
          colorDefensorKey: colorDefKey,
          paisOrigen: ws.paisOrigen,
          paisDestino: ws.paisDestino,
          idAtacante: ws.idAtacante,
          idDefensor: ws.idDefensor,
          dadosAtaque: ws.dadosAtaque,
          dadosDefensor: ws.dadosDefensor,
          conquista: ws.conquista,
          perdidasAtacante: ws.perdidasAtacante,
          perdidasDefensor: ws.perdidasDefensor,
          duracionMs: ws.conquista ? 3000 : 2700,
        });
        break;
      }
      case 'FIN_TURNO':
        this.enqueue({
          tipo: 'FIN_TURNO',
          titulo: 'FIN DE TURNO',
          descripcion: ws.descripcion,
          jugadorActivo: ws.jugadorNombre,
          colorJugador: color,
          duracionMs: 2000,
        });
        break;
      case 'INCORPORACION':
        this.enqueue({
          tipo: 'INCORPORACION',
          titulo: 'INCORPORACIÓN',
          descripcion: ws.descripcion,
          jugadorActivo: ws.jugadorNombre,
          colorJugador: color,
          duracionMs: 2500,
        });
        break;
      case 'REAGRUPAMIENTO':
        this.enqueue({
          tipo: 'REAGRUPAMIENTO',
          titulo: 'REAGRUPAMIENTO',
          descripcion: ws.descripcion,
          jugadorActivo: ws.jugadorNombre,
          colorJugador: color,
          duracionMs: 2500,
        });
        break;
      case 'TARJETA_OBTENIDA':
        this.enqueue({
          tipo: 'TARJETA_OBTENIDA',
          titulo: 'TARJETA OBTENIDA',
          descripcion: ws.descripcion,
          jugadorActivo: ws.jugadorNombre,
          colorJugador: color,
          duracionMs: 3500,
        });
        break;
      case 'TARJETA_CANJEADA':
        this.enqueue({
          tipo: 'TARJETA_CANJEADA',
          titulo: 'CANJE DE TARJETAS',
          descripcion: ws.descripcion,
          jugadorActivo: ws.jugadorNombre,
          colorJugador: color,
          duracionMs: 3500,
        });
        break;
      case 'FIN_PARTIDA':
        // No se encola como notificación efímera — el TableroComponent lo
        // maneja directamente con su animación de quemado + overlay.
        break;
    }
  }

  /** Llamado por el componente cuando el jugador eligió dados (o el timer expiró). */
  resolveDiceSelection(cantDados: number): void {
    this.diceResultSubject.next(cantDados);
  }

  /** Llamado después de procesar la selección de dados para avanzar la cola. */
  advanceAfterDice(): void {
    // Solo dismissar si aún estamos en el selector local. En la short-circuit
    // de bot-defensor, el WS ATAQUE puede llegar ANTES que la respuesta HTTP
    // y transicionar a RESULTADO_DADOS; en ese caso no debemos descartar el
    // resultado recién mostrado.
    if (this.currentTipo === 'ATAQUE_INICIADO') {
      this.processNext();
    }
  }

  /** Descarta el evento actual y procesa el siguiente. */
  dismissCurrent(): void {
    this.processNext();
  }

  /** Convierte un PartidaEventWs en una entrada de historial y la emite.
   *  Devuelve la cadena vacía / no emite si el tipo no aplica al historial
   *  (p.ej. ATAQUE_INICIADO, que es solo UI de combate en curso). */
  private emitirHistorialDesdeWs(ws: PartidaEventWs): void {
    let texto = '';
    let tipo: HistorialEvento['tipo'] = 'normal';
    switch (ws.tipo) {
      case 'CONQUISTA':
        texto = ws.conquista
          ? `${ws.jugadorNombre} conquistó ${ws.paisDestino}`
          : `${ws.jugadorNombre} atacó ${ws.paisDestino} desde ${ws.paisOrigen}`;
        tipo = ws.conquista ? 'ataque' : 'normal';
        break;
      case 'ATAQUE':
        texto = `${ws.jugadorNombre} atacó ${ws.paisDestino} desde ${ws.paisOrigen}`;
        tipo = 'normal';
        break;
      case 'FIN_TURNO':
        texto = ws.descripcion ?? `Fin de turno de ${ws.jugadorNombre}`;
        tipo = 'ok';
        break;
      case 'INCORPORACION':
        texto = ws.descripcion ?? `${ws.jugadorNombre} incorporó ejércitos`;
        tipo = 'ok';
        break;
      case 'REAGRUPAMIENTO':
        texto = ws.descripcion ?? `${ws.jugadorNombre} reagrupó tropas`;
        tipo = 'ok';
        break;
      case 'TARJETA_OBTENIDA':
        texto = ws.descripcion ?? `${ws.jugadorNombre} obtuvo una tarjeta`;
        tipo = 'ok';
        break;
      case 'TARJETA_CANJEADA':
        texto = ws.descripcion ?? `${ws.jugadorNombre} canjeó tarjetas`;
        tipo = 'ok';
        break;
    }
    if (texto) this.historialEventSubject.next({ texto, tipo });
  }

  /** Marca el evento siguiente con flag fastMode si la cola está saturada
   *  (>2 pendientes). El componente lo usa para acortar el slot machine. */
  private get backlogActivo(): boolean {
    return this.queue.length > 2;
  }

  private processNext(): void {
    if (this.queue.length === 0) {
      this.processing = false;
      this.currentTipo = null;
      this.currentEventSubject.next(null);
      return;
    }
    this.processing = true;
    const event = this.queue.shift()!;
    // Inyectar fastMode al evento — permite al componente decidir cómo abreviar.
    event.fastMode = this.backlogActivo;
    this.currentTipo = event.tipo;
    this.currentEventSubject.next(event);

    if (event.tipo === 'ATAQUE_INICIADO') {
      // El componente maneja el timer y llama resolveDiceSelection()
      // (o queda esperando el broadcast ATAQUE/CONQUISTA que lo avanza).
      return;
    }

    if (event.tipo === 'RESULTADO_DADOS' || event.tipo === 'CONQUISTA') {
      // En backlog (ráfaga de bots), reducimos la duración del combate a 1.5s
      // para que el jugador no quede 3s × N esperando combates ya resueltos.
      const duracion = event.fastMode
        ? 1500
        : event.duracionMs ?? 3000;
      setTimeout(() => this.processNext(), duracion);
      return;
    }

    // Notificaciones simples: si hay backlog, reducir a 900ms.
    const duracion = event.fastMode
      ? Math.min(event.duracionMs ?? 3500, 900)
      : event.duracionMs ?? 3500;

    setTimeout(() => this.processNext(), duracion);
  }
}
