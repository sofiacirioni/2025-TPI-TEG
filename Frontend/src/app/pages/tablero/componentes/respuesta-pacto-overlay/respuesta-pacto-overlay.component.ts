import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, OnChanges, OnDestroy, Output, SimpleChanges } from '@angular/core';
import { PactoService } from '../../../../core/services/pacto.service';
import { NotificationService } from '../../../../core/services/notification.service';
import { PactoDto, TipoPacto } from '../../../../core/models/interfaces/pacto.interface';
import { JugadorDto } from '../../../../core/models/interfaces/partida.interface';

const TIMEOUT_SEGUNDOS = 30;
// El resultado (APROBADO/RECHAZADO) queda visible este tiempo antes de cerrar.
const TIEMPO_RESULTADO_MS = 3000;

const SIMBOLOS_PACTO: Record<TipoPacto, string> = {
  PACTO_PAISES: 'assets/images/tablero/countries-pact-symbol.png',
  PACTO_MUNDIAL: 'assets/images/tablero/global-pact-symbol.png',
  PACTO_ZONA_INTERNACIONAL: 'assets/images/tablero/continental-pact-symbol.png',
};

const AVATARES = [
  'assets/images/avatars/AgosCh.png', 'assets/images/avatars/CandeArguello.png',
  'assets/images/avatars/CandeBlanco.png', 'assets/images/avatars/LaraHeredia.png',
  'assets/images/avatars/MeliAbril.png', 'assets/images/avatars/SofiCirioni.png',
  'assets/images/avatars/Maxi.png',
];

@Component({
  selector: 'app-respuesta-pacto-overlay',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './respuesta-pacto-overlay.component.html',
  styleUrl: './respuesta-pacto-overlay.component.scss',
})
export class RespuestaPactoOverlayComponent implements OnChanges, OnDestroy {
  @Input() pacto!: PactoDto;
  @Input() jugadorLocalId!: number;
  @Input() jugadores: JugadorDto[] = [];
  /** Cuando llega 'ACEPTADO' o 'RECHAZADO', el overlay congela el countdown
   *  y muestra el resultado durante TIEMPO_RESULTADO_MS antes de emitir respondido. */
  @Input() resultado: 'ACEPTADO' | 'RECHAZADO' | null = null;

  @Output() respondido = new EventEmitter<void>();

  countdown = TIMEOUT_SEGUNDOS;
  enviando = false;
  private intervalId?: ReturnType<typeof setInterval>;
  private resultadoTimeoutId?: ReturnType<typeof setTimeout>;

  constructor(
    private pactoService: PactoService,
    private notificationService: NotificationService,
  ) {}

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['pacto'] && this.pacto) {
      this.iniciarCountdown();
    }
    // Cuando llega el resultado, detener countdown y programar cierre.
    if (changes['resultado'] && this.resultado) {
      this.detenerCountdown();
      this.programarCierreTrasResultado();
    }
  }

  ngOnDestroy(): void {
    this.detenerCountdown();
    if (this.resultadoTimeoutId) {
      clearTimeout(this.resultadoTimeoutId);
      this.resultadoTimeoutId = undefined;
    }
  }

  get simboloTipo(): string {
    return SIMBOLOS_PACTO[this.pacto.tipo];
  }

  /** Avatar del jugador A si es humano, o `null` si es bot (renderiza color en su lugar). */
  avatarJugadorA(): string | null {
    return this.esBot(this.pacto.idJugadorA) ? null : this.avatarFor(this.pacto.idJugadorA);
  }
  avatarJugadorB(): string | null {
    return this.esBot(this.pacto.idJugadorB) ? null : this.avatarFor(this.pacto.idJugadorB);
  }

  private esBot(idJugador: number): boolean {
    const j = this.jugadores.find(x => x.idJugador === idJugador);
    return (j?.tipoJugador ?? '').toUpperCase() === 'BOT';
  }

  private avatarFor(idJugador: number): string {
    return AVATARES[idJugador % AVATARES.length];
  }

  get esReceptor(): boolean {
    return this.pacto?.idJugadorB === this.jugadorLocalId;
  }

  get tituloTipo(): string {
    return this.tituloDe(this.pacto.tipo);
  }

  tituloDe(t: TipoPacto): string {
    switch (t) {
      case 'PACTO_PAISES': return 'PACTO ENTRE PAÍSES';
      case 'PACTO_MUNDIAL': return 'NO AGRESIÓN MUNDIAL';
      case 'PACTO_ZONA_INTERNACIONAL': return 'ZONA INTERNACIONAL';
    }
  }

  get descripcion(): string {
    const p = this.pacto;
    switch (p.tipo) {
      case 'PACTO_PAISES':
        return `${p.nombreJugadorA} protege ${p.nombrePaisProtegidoA ?? '—'} · ${p.nombreJugadorB} protege ${p.nombrePaisProtegidoB ?? '—'}`;
      case 'PACTO_MUNDIAL':
        return 'No agresión total entre ambos jugadores en cualquier país del mapa.';
      case 'PACTO_ZONA_INTERNACIONAL':
        return `Zona internacional sobre ${p.nombrePaisZona ?? '—'} (continente ${p.continenteZona ?? '—'}).`;
    }
  }

  aceptar(): void {
    if (this.enviando || !this.esReceptor) return;
    this.enviando = true;
    this.pactoService.aceptar(this.pacto.id, this.jugadorLocalId).subscribe({
      next: () => this.cerrar(),
      error: (err) => {
        this.enviando = false;
        this.notificationService.error(this.extraerError(err) ?? 'No se pudo aceptar el pacto.');
      },
    });
  }

  rechazar(): void {
    if (this.enviando || !this.esReceptor) return;
    this.enviando = true;
    this.pactoService.rechazar(this.pacto.id, this.jugadorLocalId).subscribe({
      next: () => this.cerrar(),
      error: (err) => {
        this.enviando = false;
        this.notificationService.error(this.extraerError(err) ?? 'No se pudo rechazar el pacto.');
      },
    });
  }

  private cerrar(): void {
    this.detenerCountdown();
    this.enviando = false;
    this.respondido.emit();
  }

  private iniciarCountdown(): void {
    this.detenerCountdown();
    this.countdown = TIMEOUT_SEGUNDOS;
    this.intervalId = setInterval(() => {
      this.countdown--;
      if (this.countdown <= 0) {
        if (this.esReceptor) {
          // Auto-rechazo por timeout
          this.rechazar();
        } else {
          this.cerrar();
        }
      }
    }, 1000);
  }

  private detenerCountdown(): void {
    if (this.intervalId) {
      clearInterval(this.intervalId);
      this.intervalId = undefined;
    }
  }

  private programarCierreTrasResultado(): void {
    if (this.resultadoTimeoutId) clearTimeout(this.resultadoTimeoutId);
    this.resultadoTimeoutId = setTimeout(() => {
      this.respondido.emit();
    }, TIEMPO_RESULTADO_MS);
  }

  colorVar(colorKey: string): string {
    const map: Record<string, string> = {
      ROJO: 'var(--player-rojo)',
      AZUL: 'var(--player-azul)',
      VERDE: 'var(--player-verde)',
      NARANJA: 'var(--player-naranja)',
      AMARILLO: 'var(--player-dorado)',
      VIOLETA: 'var(--player-purpura)',
    };
    return map[(colorKey ?? '').toUpperCase()] ?? 'var(--color-oscuro)';
  }

  private extraerError(err: any): string | null {
    if (!err) return null;
    if (typeof err.error === 'string') return err.error;
    if (err.error?.message) return err.error.message;
    if (err.message) return err.message;
    return null;
  }
}
