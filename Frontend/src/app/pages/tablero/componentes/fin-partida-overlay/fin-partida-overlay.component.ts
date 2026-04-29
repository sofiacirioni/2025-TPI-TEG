import {
  AfterViewInit, ChangeDetectionStrategy, Component, ElementRef, EventEmitter,
  Input, OnDestroy, Output, ViewChild, inject,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { gsap } from 'gsap';
import { FinPartida, JugadorResultado } from '../../../../core/models/interfaces/partida.interface';

const COLOR_VAR_MAP: Record<string, string> = {
  ROJO: 'var(--player-rojo)',
  AZUL: 'var(--player-azul)',
  NARANJA: 'var(--player-naranja)',
  VIOLETA: 'var(--player-purpura)',
  VERDE: 'var(--player-verde)',
  AMARILLO: 'var(--player-dorado)',
};

const COLOR_INSIGNIA_MAP: Record<string, string> = {
  ROJO: 'assets/images/tablero/insignias/red-insignia.png',
  AZUL: 'assets/images/tablero/insignias/blue-insignia.png',
  VERDE: 'assets/images/tablero/insignias/green-insignia.png',
  NARANJA: 'assets/images/tablero/insignias/orange-insignia.png',
  AMARILLO: 'assets/images/tablero/insignias/gold-insignia.png',
  VIOLETA: 'assets/images/tablero/insignias/purple-insignia.png',
};

@Component({
  selector: 'app-fin-partida-overlay',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './fin-partida-overlay.component.html',
  styleUrl: './fin-partida-overlay.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class FinPartidaOverlayComponent implements AfterViewInit, OnDestroy {
  @Input({ required: true }) datos!: FinPartida;
  @Output() verEstadisticas = new EventEmitter<void>();

  @ViewChild('overlayRoot') overlayRoot?: ElementRef<HTMLDivElement>;

  /** Botón visible recién después de la animación escalonada. */
  botonHabilitado = false;

  private timeline?: gsap.core.Timeline;
  private botonTimeout?: ReturnType<typeof setTimeout>;
  private cdr = inject(ElementRef);

  get colorGanadorVar(): string {
    return COLOR_VAR_MAP[this.datos.ganador.color?.toUpperCase()] ?? 'var(--player-rojo)';
  }

  get insigniaUrl(): string {
    return COLOR_INSIGNIA_MAP[this.datos.ganador.color?.toUpperCase()]
      ?? 'assets/images/tablero/insignias/red-insignia.png';
  }

  get avatarGanador(): string {
    return this.datos.ganador.url || this.avatarFallback(Number(this.datos.ganador.idJugador ?? 0));
  }

  colorVarPara(color: string): string {
    return COLOR_VAR_MAP[color?.toUpperCase()] ?? 'var(--player-rojo)';
  }

  insigniaUrlPara(color: string): string {
    return COLOR_INSIGNIA_MAP[color?.toUpperCase()]
      ?? 'assets/images/tablero/insignias/red-insignia.png';
  }

  avatarPara(j: JugadorResultado): string {
    return j.avatarUrl || this.avatarFallback(j.id);
  }

  private avatarFallback(idJugador: number): string {
    const avatares = [
      'assets/images/avatars/AgosCh.png', 'assets/images/avatars/CandeArguello.png',
      'assets/images/avatars/CandeBlanco.png', 'assets/images/avatars/LaraHeredia.png',
      'assets/images/avatars/MeliAbril.png', 'assets/images/avatars/SofiCirioni.png',
      'assets/images/avatars/Maxi.png',
    ];
    return avatares[Math.abs(idJugador) % avatares.length];
  }

  ngAfterViewInit(): void {
    const root = this.overlayRoot?.nativeElement;
    if (!root) return;

    // Timeline escalonado de entrada. Usamos GSAP (ya en el bundle) en lugar de
    // animation-delay CSS porque al ser muchos elementos con cascada larga (3.5s)
    // un timeline es más fácil de leer y de pausar/cancelar.
    this.timeline = gsap.timeline({ defaults: { ease: 'power2.out' } })
      .from(root, { opacity: 0, duration: 0.8 })
      .from(root.querySelector('.zona-insignia'),
        { opacity: 0, scale: 0.4, duration: 0.7, ease: 'back.out(1.7)' }, 0.4)
      .from(root.querySelector('.zona-identidad'),
        { opacity: 0, y: 40, duration: 0.6 }, 0.9)
      .from(root.querySelector('.subtitulo-comandante'),
        { opacity: 0, duration: 0.5 }, 1.2)
      .from(root.querySelector('.zona-objetivo'),
        { opacity: 0, y: 16, duration: 0.7 }, 1.6)
      .from(root.querySelectorAll('.objetivo-item'),
        { opacity: 0, y: 8, duration: 0.4, stagger: 0.12 }, 1.8)
      .from(root.querySelector('.zona-bajas'),
        { opacity: 0, duration: 0.6 }, 2.3);

    // Botón se habilita y aparece a los 3.5s desde el inicio
    this.botonTimeout = setTimeout(() => {
      this.botonHabilitado = true;
      const btn = root.querySelector('.btn-ver-estadisticas');
      if (btn) gsap.fromTo(btn, { opacity: 0 }, { opacity: 1, duration: 0.6 });
    }, 3500);
  }

  ngOnDestroy(): void {
    this.timeline?.kill();
    if (this.botonTimeout) clearTimeout(this.botonTimeout);
  }

  onClickEstadisticas(): void {
    if (!this.botonHabilitado) return;
    this.verEstadisticas.emit();
  }
}
