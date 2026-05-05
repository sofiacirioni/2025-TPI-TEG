import {
  Component, OnInit, OnDestroy, inject, HostListener
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { Subscription } from 'rxjs';
import { TableroEventService } from '../../../../core/services/tablero-event.service';
import { TableroServicio } from '../../../../core/services/tablero.service';
import { AuthService } from '../../../../core/services/auth.service';
import { GameEvent } from '../../../../core/models/interfaces/game-event.interface';
import { NombrePaisPipe } from '../../../../core/pipes/nombre-pais.pipe';

type RolCombate = 'atacante' | 'defensor' | 'espectador' | 'local';

/** Color enum → nombre de archivo SVG de la ficha (en /assets/vectors/tablero/fichas/) */
const FICHA_MAP: Record<string, string> = {
  ROJO:     'red-player',
  AZUL:     'blue-player',
  VERDE:    'green-player',
  NARANJA:  'orange-player',
  AMARILLO: 'gold-player',
  VIOLETA:  'purple-player',
};

/** Color enum → hex del design system para texto/bordes (del CLAUDE.md) */
const COLOR_HEX_MAP: Record<string, string> = {
  ROJO:     '#A01515',
  AZUL:     '#1A4080',
  VERDE:    '#1E7A50',
  NARANJA:  '#BF6800',
  AMARILLO: '#6B4C00',
  VIOLETA:  '#6B2490',
};

@Component({
  selector: 'app-tablero-event-display',
  standalone: true,
  imports: [CommonModule, NombrePaisPipe],
  templateUrl: './tablero-event-display.component.html',
  styleUrl:    './tablero-event-display.component.scss',
})
export class TableroEventDisplayComponent implements OnInit, OnDestroy {

  private eventService = inject(TableroEventService);
  private tableroServicio = inject(TableroServicio);
  private authService = inject(AuthService);
  private sub?: Subscription;

  currentEvent: GameEvent | null = null;
  visible = false;
  dismissing = false;

  rol: RolCombate = 'local';

  dadosAtaqueAnimados: { valor: number; perdedor: boolean; animado: boolean }[] = [];
  dadosDefensorAnimados: { valor: number; perdedor: boolean; animado: boolean }[] = [];

  dadosSeleccionados = 1;
  timerValue = 5;
  private countdownInterval?: ReturnType<typeof setInterval>;

  atacanteLanzo = false;
  defensorLanzo = false;

  slotMachineActive = false;
  slotDadosAtk: number[] = [];
  slotDadosDef: number[] = [];
  private slotInterval?: ReturnType<typeof setInterval>;
  private slotTimeout?: ReturnType<typeof setTimeout>;

  mostrandoConquista = false;

  private dismissId = 0;

  ngOnInit(): void {
    this.sub = this.eventService.currentEvent$.subscribe(event => {
      if (!event) {
        this.startDismiss();
        return;
      }
      this.dismissId++;
      this.mostrandoConquista = false;
      this.currentEvent = event;
      this.dismissing = false;
      this.visible = true;

      if (event.tipo === 'ATAQUE_INICIADO') {
        this.rol = this.computeRol(event);
        this.atacanteLanzo = false;
        this.defensorLanzo = false;
        this.slotMachineActive = false;
        if (event.diceSelection) {
          this.dadosSeleccionados = this.rol === 'atacante'
            ? (event.diceSelection.cantDadosAtacante ?? event.diceSelection.maxDados)
            : event.diceSelection.maxDados;
          this.startCountdown(event.diceSelection.timerSegundos ?? 5);
        }
      }

      if (event.tipo === 'RESULTADO_DADOS' || event.tipo === 'CONQUISTA') {
        this.startSlotMachine(event);
      }
    });
  }

  ngOnDestroy(): void {
    this.sub?.unsubscribe();
    this.clearCountdown();
    this.clearSlotMachine();
  }

  private computeRol(event: GameEvent): RolCombate {
    if (event.idAtacante == null) return 'local';
    const miId = this.authService.getJugadorId();
    if (miId != null && miId === event.idAtacante) return 'atacante';
    if (miId != null && miId === event.idDefensor) return 'defensor';
    return 'espectador';
  }

  private startDismiss(): void {
    if (!this.visible) return;
    this.dismissing = true;
    const myId = ++this.dismissId;
    setTimeout(() => {
      if (this.dismissId !== myId) return;
      this.visible = false;
      this.currentEvent = null;
      this.dismissing = false;
      this.mostrandoConquista = false;
    }, 400);
  }

  get maxDados(): number {
    return this.currentEvent?.diceSelection?.maxDados ?? 3;
  }

  get dadosAtacanteSlots(): number {
    const pre = this.currentEvent?.diceSelection?.cantDadosAtacante;
    return pre ?? this.dadosSeleccionados;
  }

  /** Selecciona la cantidad de dados Y lanza inmediatamente.
   *  Reemplaza al flujo previo de "elegir + presionar LANZAR" por una sola
   *  acción del usuario. El timeout del countdown sigue auto-disparando con
   *  la selección actual si no se elige nada. */
  seleccionarDados(n: number): void {
    if (!this.puedeSeleccionarDados() || n < 1 || n > this.maxDados) return;
    this.dadosSeleccionados = n;
    this.lanzarSegunRol();
  }

  puedeSeleccionarDados(): boolean {
    if (this.rol === 'local') return true;
    if (this.rol === 'defensor') return !this.defensorLanzo;
    return false;
  }

  /** Dispara la acción correspondiente al rol. Llamado desde
   *  `seleccionarDados` y desde el timeout del countdown. */
  private lanzarSegunRol(): void {
    switch (this.rol) {
      case 'local':
        this.confirmarAtaque();
        break;
      case 'atacante':
        this.atacanteLanzo = true;
        break;
      case 'defensor':
        this.lanzarDefensor();
        break;
    }
  }

  private lanzarDefensor(): void {
    if (!this.currentEvent) return;
    const idPartida = this.currentEvent.idPartida;
    const miId = this.authService.getJugadorId();
    if (idPartida == null || miId == null) {
      this.defensorLanzo = true;
      return;
    }
    this.defensorLanzo = true;
    this.clearCountdown();
    this.tableroServicio.defenderAtaque({
      idPartida,
      idJugador: miId,
      cantDadosDefensor: this.dadosSeleccionados,
    }).subscribe({
      next: () => this.tableroServicio.forceRefresh(),
      error: err => console.error('Error defender ataque:', err),
    });
  }

  confirmarAtaque(): void {
    this.clearCountdown();
    this.eventService.resolveDiceSelection(this.dadosSeleccionados);
  }

  @HostListener('document:keydown.escape')
  onEscape(): void {
    this.cerrarManual();
  }

  cerrarManual(): void {
    if (!this.visible) return;
    if (this.currentEvent?.tipo === 'ATAQUE_INICIADO') return;
    this.eventService.dismissCurrent();
  }

  onBackdropClick(): void {
    this.cerrarManual();
  }

  private startCountdown(seconds: number): void {
    this.clearCountdown();
    this.timerValue = seconds;
    this.countdownInterval = setInterval(() => {
      this.timerValue--;
      if (this.timerValue <= 0) {
        this.clearCountdown();
        // Auto-lanzar con la selección vigente cuando expira el timer.
        if ((this.rol === 'defensor' && !this.defensorLanzo)
            || (this.rol === 'atacante' && !this.atacanteLanzo)
            || this.rol === 'local') {
          this.lanzarSegunRol();
        }
      }
    }, 1000);
  }

  private clearCountdown(): void {
    if (this.countdownInterval) {
      clearInterval(this.countdownInterval);
      this.countdownInterval = undefined;
    }
  }

  private startSlotMachine(event: GameEvent): void {
    this.clearSlotMachine();
    const atk = event.dadosAtaque ?? [];
    const def = event.dadosDefensor ?? [];
    this.slotMachineActive = true;
    this.slotDadosAtk = atk.map(() => 1 + Math.floor(Math.random() * 6));
    this.slotDadosDef = def.map(() => 1 + Math.floor(Math.random() * 6));

    this.slotInterval = setInterval(() => {
      this.slotDadosAtk = this.slotDadosAtk.map(() => 1 + Math.floor(Math.random() * 6));
      this.slotDadosDef = this.slotDadosDef.map(() => 1 + Math.floor(Math.random() * 6));
    }, 80);

    // Slot acortado: 1.6s en flujo normal, 600ms cuando hay ráfaga de bots.
    // El servicio sigue marcando processNext con duracionMs ≤3s, así el modal
    // total cierra en torno a 2.7-3s (slot + lectura del resultado).
    const slotDuration = event.fastMode ? 600 : 1600;
    this.slotTimeout = setTimeout(() => {
      this.clearSlotMachine();
      this.slotMachineActive = false;
      this.animarDados(event);
    }, slotDuration);
  }

  private clearSlotMachine(): void {
    if (this.slotInterval) clearInterval(this.slotInterval);
    if (this.slotTimeout) clearTimeout(this.slotTimeout);
    this.slotInterval = undefined;
    this.slotTimeout = undefined;
  }

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

    [...this.dadosAtaqueAnimados, ...this.dadosDefensorAnimados].forEach((d, idx) => {
      setTimeout(() => { d.animado = true; }, 100 + idx * 120);
    });
  }

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
      case 'PACTO_PROPUESTO':
      case 'PACTO_ACEPTADO':   return '✍';
      case 'PACTO_RECHAZADO':
      case 'PACTO_ROTO':       return '✕';
      default:                  return '·';
    }
  }

  /** PNG del dado: white = atacante, black = defensor. */
  getDadoImg(valor: number, variante: 'white' | 'black'): string {
    const v = Math.min(6, Math.max(1, valor));
    return `assets/images/tablero/dice/${variante}-dice-${v}.png`;
  }

  getEmojiDado(valor: number): string {
    return ['', '⚀', '⚁', '⚂', '⚃', '⚄', '⚅'][valor] ?? '?';
  }

  /** SVG de la ficha según el color key (ROJO, AZUL, ...). Fallback: rojo. */
  getFichaPath(colorKey: string | undefined): string {
    const name = FICHA_MAP[(colorKey ?? '').toUpperCase()] ?? 'red-player';
    return `assets/vectors/tablero/fichas/${name}.svg`;
  }

  /** Hex color del jugador según su key. Fallback: color oscuro del tema. */
  getColorHex(colorKey: string | undefined): string {
    return COLOR_HEX_MAP[(colorKey ?? '').toUpperCase()] ?? '#432A1E';
  }

  range(n: number): number[] {
    return Array.from({ length: n }, (_, i) => i + 1);
  }

  get ganadorColor(): string {
    if (!this.currentEvent) return '';
    const atacanteHex = this.getColorHex(this.currentEvent.colorJugadorKey);
    const defensorHex = this.getColorHex(this.currentEvent.colorDefensorKey);
    if (this.currentEvent.conquista) return atacanteHex;
    const pa = this.currentEvent.perdidasAtacante ?? 0;
    const pd = this.currentEvent.perdidasDefensor ?? 0;
    if (pd > pa) return atacanteHex;
    return defensorHex;
  }

  get textoResultado(): string {
    if (!this.currentEvent) return '';
    if (this.currentEvent.conquista) return '¡CONQUISTA!';
    const pa = this.currentEvent.perdidasAtacante ?? 0;
    const pd = this.currentEvent.perdidasDefensor ?? 0;
    if (pd > pa) return 'ATAQUE EXITOSO';
    if (pd < pa) return 'ATAQUE REPELIDO';
    return 'COMBATE';
  }
}
