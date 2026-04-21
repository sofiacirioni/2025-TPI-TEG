import { Injectable } from '@angular/core';
import { Observable, Subject } from 'rxjs';
import { GameEvent, GameEventTipo, PartidaEventWs } from '../models/interfaces/game-event.interface';

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
          duracionMs: ws.conquista ? 5000 : 4000,
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

  private processNext(): void {
    if (this.queue.length === 0) {
      this.processing = false;
      this.currentTipo = null;
      this.currentEventSubject.next(null);
      return;
    }
    this.processing = true;
    const event = this.queue.shift()!;
    this.currentTipo = event.tipo;
    this.currentEventSubject.next(event);

    if (event.tipo === 'ATAQUE_INICIADO') {
      // El componente maneja el timer y llama resolveDiceSelection()
      // (o queda esperando el broadcast ATAQUE/CONQUISTA que lo avanza).
      return;
    }

    if (event.tipo === 'RESULTADO_DADOS' || event.tipo === 'CONQUISTA') {
      // Eventos de combate siempre con su duración completa
      setTimeout(() => this.processNext(), event.duracionMs ?? 4000);
      return;
    }

    // Notificaciones simples: si hay >2 eventos pendientes (bots actuando en ráfaga),
    // reducir a 900ms para que el jugador no quede bloqueado esperando notificaciones pasadas
    const duracion = this.queue.length > 2
      ? Math.min(event.duracionMs ?? 3500, 900)
      : event.duracionMs ?? 3500;

    setTimeout(() => this.processNext(), duracion);
  }
}
