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

  private currentEventSubject = new Subject<GameEvent | null>();
  /** Emite el evento actual (null cuando no hay nada mostrando). */
  readonly currentEvent$: Observable<GameEvent | null> = this.currentEventSubject.asObservable();

  /** Subject interno: la selección de dados emite el número elegido. */
  private diceResultSubject = new Subject<number>();
  readonly diceResult$: Observable<number> = this.diceResultSubject.asObservable();

  /** Encola un evento genérico. */
  enqueue(event: GameEvent): void {
    this.queue.push(event);
    if (!this.processing) this.processNext();
  }

  /**
   * Convierte un evento WS del backend en un GameEvent y lo encola.
   * Los eventos WS llegan a todos, incluido el atacante.
   * Para el atacante local (que ya recibió la respuesta HTTP) ignorar los tipos
   * ATAQUE / CONQUISTA porque ya los encoló directamente.
   */
  enqueueFromWs(ws: PartidaEventWs, esJugadorLocal: boolean): void {
    const color = COLOR_VAR_MAP[ws.jugadorColor?.toUpperCase()] ?? 'var(--player-rojo)';

    switch (ws.tipo) {
      case 'ATAQUE':
      case 'CONQUISTA': {
        if (esJugadorLocal) return; // el atacante ya encoló su propio resultado
        const tipo: GameEventTipo = ws.conquista ? 'CONQUISTA' : 'RESULTADO_DADOS';
        this.enqueue({
          tipo,
          titulo: ws.conquista ? '¡CONQUISTA!' : 'COMBATE',
          jugadorActivo: ws.jugadorNombre,
          colorJugador: color,
          paisOrigen: ws.paisOrigen,
          paisDestino: ws.paisDestino,
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
        if (esJugadorLocal) return;
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
    this.processNext();
  }

  /** Descarta el evento actual y procesa el siguiente. */
  dismissCurrent(): void {
    this.processNext();
  }

  private processNext(): void {
    if (this.queue.length === 0) {
      this.processing = false;
      this.currentEventSubject.next(null);
      return;
    }
    this.processing = true;
    const event = this.queue.shift()!;
    this.currentEventSubject.next(event);

    if (event.tipo === 'ATAQUE_INICIADO') {
      // El componente maneja el timer y llama resolveDiceSelection()
      return;
    }

    if (event.tipo === 'RESULTADO_DADOS' || event.tipo === 'CONQUISTA') {
      const duration = event.duracionMs ?? 4000;
      setTimeout(() => this.processNext(), duration);
      return;
    }

    setTimeout(() => this.processNext(), event.duracionMs ?? 3500);
  }
}
