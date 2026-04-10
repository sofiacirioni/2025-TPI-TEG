import {
  Component, OnInit, OnDestroy, inject, HostListener
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { Subscription } from 'rxjs';
import { TableroEventService } from '../../../../core/services/tablero-event.service';
import { GameEvent } from '../../../../core/models/interfaces/game-event.interface';

@Component({
  selector: 'app-tablero-event-display',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './tablero-event-display.component.html',
  styleUrl:    './tablero-event-display.component.scss',
})
export class TableroEventDisplayComponent implements OnInit, OnDestroy {

  private eventService = inject(TableroEventService);
  private sub?: Subscription;

  currentEvent: GameEvent | null = null;
  visible = false;
  dismissing = false;

  // Dados con estado de animación
  dadosAtaqueAnimados: { valor: number; perdedor: boolean; animado: boolean }[] = [];
  dadosDefensorAnimados: { valor: number; perdedor: boolean; animado: boolean }[] = [];

  // Selección de dados
  dadosSeleccionados = 1;
  timerValue = 5;
  private countdownInterval?: ReturnType<typeof setInterval>;

  // Post-resultado: fase conquista
  mostrandoConquista = false;

  // Identificador de dismiss — incrementar al llegar un evento nuevo cancela el dismiss anterior
  private dismissId = 0;

  ngOnInit(): void {
    this.sub = this.eventService.currentEvent$.subscribe(event => {
      if (!event) {
        this.startDismiss();
        return;
      }
      this.dismissId++; // cancela cualquier dismiss pendiente
      this.mostrandoConquista = false;
      this.currentEvent = event;
      this.dismissing = false;
      this.visible = true;

      if (event.tipo === 'ATAQUE_INICIADO' && event.diceSelection) {
        this.dadosSeleccionados = event.diceSelection.maxDados;
        this.startCountdown(event.diceSelection.timerSegundos ?? 5);
      }

      if (event.tipo === 'RESULTADO_DADOS' || event.tipo === 'CONQUISTA') {
        this.animarDados(event);
        // Si hubo conquista, transicionar a pantalla de conquista al final
        if (event.conquista && event.tipo !== 'CONQUISTA') {
          setTimeout(() => { this.mostrandoConquista = true; }, 2500);
        }
      }
    });
  }

  ngOnDestroy(): void {
    this.sub?.unsubscribe();
    this.clearCountdown();
  }

  private startDismiss(): void {
    if (!this.visible) return;
    this.dismissing = true;
    const myId = ++this.dismissId;
    setTimeout(() => {
      if (this.dismissId !== myId) return; // cancelado por evento nuevo
      this.visible = false;
      this.currentEvent = null;
      this.dismissing = false;
      this.mostrandoConquista = false;
    }, 400);
  }

  // ── Selección de dados ────────────────────────────────────────────

  get maxDados(): number {
    return this.currentEvent?.diceSelection?.maxDados ?? 3;
  }

  seleccionarDados(n: number): void {
    if (n >= 1 && n <= this.maxDados) {
      this.dadosSeleccionados = n;
    }
  }

  confirmarAtaque(): void {
    this.clearCountdown();
    this.eventService.resolveDiceSelection(this.dadosSeleccionados);
    // No llamar startDismiss aquí — advanceAfterDice() en tablero.component
    // disparará processNext() que emitirá el resultado y actualizará el panel.
  }

  /** ESC cierra el selector de dados para observadores */
  @HostListener('document:keydown.escape')
  onEscape(): void {
    if (this.currentEvent?.tipo === 'ATAQUE_INICIADO') return;
    if (this.visible) this.eventService.dismissCurrent();
  }

  private startCountdown(seconds: number): void {
    this.clearCountdown();
    this.timerValue = seconds;
    this.countdownInterval = setInterval(() => {
      this.timerValue--;
      if (this.timerValue <= 0) {
        this.clearCountdown();
        this.confirmarAtaque(); // auto-submit con max dados
      }
    }, 1000);
  }

  private clearCountdown(): void {
    if (this.countdownInterval) {
      clearInterval(this.countdownInterval);
      this.countdownInterval = undefined;
    }
  }

  // ── Animación de dados ────────────────────────────────────────────

  private animarDados(event: GameEvent): void {
    const atk = event.dadosAtaque ?? [];
    const def = event.dadosDefensor ?? [];
    const comparaciones = Math.min(atk.length, def.length);

    this.dadosAtaqueAnimados = atk.map((v, i) => ({
      valor: v,
      perdedor: i < comparaciones && v <= def[i],
      animado: false,
    }));
    this.dadosDefensorAnimados = def.map((v, i) => ({
      valor: v,
      perdedor: i < comparaciones && def[i] < atk[i],
      animado: false,
    }));

    // Lanzar animación escalonada
    [...this.dadosAtaqueAnimados, ...this.dadosDefensorAnimados].forEach((d, idx) => {
      setTimeout(() => { d.animado = true; }, 100 + idx * 120);
    });
  }

  // ── Helpers de UI ─────────────────────────────────────────────────

  getIconoEvento(): string {
    switch (this.currentEvent?.tipo) {
      case 'FIN_TURNO':        return '◈';
      case 'INCORPORACION':    return '✦';
      case 'REAGRUPAMIENTO':   return '↔';
      case 'TARJETA_OBTENIDA': return '▣';
      case 'TARJETA_CANJEADA': return '◆';
      case 'CONQUISTA':        return '★';
      case 'RESULTADO_DADOS':  return '⚔';
      case 'ATAQUE_INICIADO':  return '⚔';
      default:                  return '·';
    }
  }

  getEmojiDado(valor: number): string {
    return ['', '⚀', '⚁', '⚂', '⚃', '⚄', '⚅'][valor] ?? '?';
  }

  range(n: number): number[] {
    return Array.from({ length: n }, (_, i) => i + 1);
  }
}
