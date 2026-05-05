// Estrategia: Opción A del plan (GSAP + filtro SVG turbulence + canvas con
// partículas). Justificación: GSAP 3.14.2 ya está instalado en el proyecto
// (package.json), entonces no hay costo de bundle adicional. La técnica
// combina (1) mask-image radial-gradient animada por GSAP, (2) filtro SVG
// fractalNoise + displacementMap para borde irregular, (3) canvas vanilla
// con requestAnimationFrame para 25 partículas de brasa, (4) drop-shadow
// animado para resplandor. Sin librería adicional.

import {
  AfterViewInit, ChangeDetectionStrategy, Component, ElementRef, EventEmitter,
  Input, OnDestroy, Output, ViewChild,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { gsap } from 'gsap';

interface Brasa {
  x: number;
  y: number;
  vx: number;
  vy: number;
  vida: number;
  size: number;
  color: string;
}

const COLORES_BRASA = ['#ffcc00', '#ff8800', '#ff5500'];
const MAX_BRASAS = 25;

@Component({
  selector: 'app-objetivo-quemado',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './objetivo-quemado.component.html',
  styleUrl: './objetivo-quemado.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ObjetivoQuemadoComponent implements AfterViewInit, OnDestroy {
  @Input() activar = false;
  @Output() animacionCompleta = new EventEmitter<void>();

  @ViewChild('panelHost', { static: true }) panelHost!: ElementRef<HTMLDivElement>;
  @ViewChild('canvasBrasas', { static: true }) canvasBrasas!: ElementRef<HTMLCanvasElement>;

  private maskState = { transparente: 0, borde: 5 };
  private glowState = { intensidad: 0 };
  private displacementState = { scale: 8 };

  private brasas: Brasa[] = [];
  private radioActual = 0;
  private rafId = 0;
  private inicioMs = 0;
  private animActiva = false;

  private timeline?: gsap.core.Timeline;
  private finishTimeout?: ReturnType<typeof setTimeout>;

  /** Id único del filtro SVG por instancia (evita colisiones si hay varios montados) */
  readonly filterId = 'burnEdge_' + Math.random().toString(36).slice(2, 9);

  ngAfterViewInit(): void {
    if (this.activar) this.iniciar();
  }

  ngOnDestroy(): void {
    this.timeline?.kill();
    if (this.rafId) cancelAnimationFrame(this.rafId);
    if (this.finishTimeout) clearTimeout(this.finishTimeout);
    this.brasas = [];
    this.animActiva = false;
  }

  private iniciar(): void {
    const host = this.panelHost.nativeElement;
    const canvas = this.canvasBrasas.nativeElement;

    // Sincronizar canvas con dimensiones del host (devicePixelRatio para HiDPI)
    const rect = host.getBoundingClientRect();
    const dpr = Math.min(window.devicePixelRatio || 1, 2);
    canvas.width = Math.max(1, Math.floor(rect.width * dpr));
    canvas.height = Math.max(1, Math.floor(rect.height * dpr));
    canvas.style.width = rect.width + 'px';
    canvas.style.height = rect.height + 'px';
    const ctx = canvas.getContext('2d');
    if (!ctx) return;
    ctx.scale(dpr, dpr);

    this.inicioMs = performance.now();
    this.animActiva = true;
    this.rafId = requestAnimationFrame(() => this.tickBrasas(ctx, rect.width, rect.height));

    // Timeline GSAP para mask + glow + displacement (sincronizadas con las fases del plan)
    this.timeline = gsap.timeline({
      onUpdate: () => this.aplicarEstado(),
      onComplete: () => this.completar(),
    });

    // Fase 1: encendido (0-200ms)
    this.timeline.to(this.glowState, { intensidad: 0.2, duration: 0.2, ease: 'power1.in' }, 0);

    // Fase 2: propagación inicial (200-800ms) — círculo crece a ~30%
    this.timeline.to(this.maskState, { transparente: 30, borde: 38, duration: 0.6, ease: 'power1.in' }, 0.2);
    this.timeline.to(this.glowState, { intensidad: 0.6, duration: 0.6 }, 0.2);

    // Fase 3: combustión activa (800-1800ms) — 30%-80%, glow máximo, displacement respira
    this.timeline.to(this.maskState, { transparente: 80, borde: 88, duration: 1.0, ease: 'power1.inOut' }, 0.8);
    this.timeline.to(this.glowState, { intensidad: 1.0, duration: 0.5 }, 0.8);
    this.timeline.to(this.displacementState, { scale: 14, duration: 0.5, yoyo: true, repeat: 1 }, 0.8);

    // Fase 4: consumición (1800-2500ms) — 80%-110%, glow decrece
    this.timeline.to(this.maskState, { transparente: 110, borde: 118, duration: 0.7, ease: 'power1.out' }, 1.8);
    this.timeline.to(this.glowState, { intensidad: 0.4, duration: 0.7 }, 1.8);

    // Fase 5: desvanecimiento (2500-3000ms) — fade out total
    this.timeline.to(host, { opacity: 0, duration: 0.5, ease: 'power2.out' }, 2.5);
    this.timeline.to(this.glowState, { intensidad: 0, duration: 0.5 }, 2.5);

    // Hard fallback por si onComplete no dispara (caso GSAP killed externamente)
    this.finishTimeout = setTimeout(() => this.completar(), 3100);
  }

  private aplicarEstado(): void {
    const host = this.panelHost.nativeElement;
    const t = this.maskState.transparente;
    const b = Math.max(t + 2, this.maskState.borde);
    // Reescribimos la mask en cada frame (Chrome/Edge la soportan vía vendor + estándar).
    const mask = `radial-gradient(circle at 50% 50%, transparent ${t}%, black ${b}%)`;
    host.style.maskImage = mask;
    host.style.webkitMaskImage = mask;

    const i = this.glowState.intensidad;
    host.style.filter = `url(#${this.filterId}) ` +
      `drop-shadow(0 0 ${6 * i}px #ff9500) ` +
      `drop-shadow(0 0 ${14 * i}px rgba(255, 58, 0, ${0.6 * i}))`;

    // Sincronizar el feDisplacementMap dinámicamente
    const fdm = host.parentElement?.querySelector(`#${this.filterId} feDisplacementMap`);
    fdm?.setAttribute('scale', this.displacementState.scale.toFixed(1));

    // Radio del círculo de combustión (en pixels) para el spawn de brasas
    const rect = host.getBoundingClientRect();
    const maxR = Math.hypot(rect.width, rect.height) / 2;
    this.radioActual = (this.maskState.transparente / 100) * maxR;
  }

  private spawnBrasa(width: number, height: number): void {
    if (this.brasas.length >= MAX_BRASAS) return;
    // Spawn desde el borde del círculo de combustión activa
    const cx = width / 2;
    const cy = height / 2;
    const angulo = Math.random() * Math.PI * 2;
    const r = Math.max(8, this.radioActual * (0.85 + Math.random() * 0.2));
    this.brasas.push({
      x: cx + Math.cos(angulo) * r,
      y: cy + Math.sin(angulo) * r,
      // Vertical inicial negativa (asciende), horizontal aleatoria pequeña
      vx: (Math.random() - 0.5) * 1.5,
      vy: -(2 + Math.random() * 2),
      vida: 1,
      size: 1.5 + Math.random() * 2,
      color: COLORES_BRASA[Math.floor(Math.random() * COLORES_BRASA.length)],
    });
  }

  private tickBrasas(ctx: CanvasRenderingContext2D, width: number, height: number): void {
    if (!this.animActiva) return;

    ctx.clearRect(0, 0, width, height);

    const elapsed = performance.now() - this.inicioMs;
    // Spawn rate variable según la fase: máximo durante combustión activa (800-1800ms)
    let spawnPorFrame = 0;
    if (elapsed < 200) spawnPorFrame = 0;
    else if (elapsed < 800) spawnPorFrame = 0.4;
    else if (elapsed < 1800) spawnPorFrame = 1.2;
    else if (elapsed < 2500) spawnPorFrame = 0.5;
    else spawnPorFrame = 0;

    // Spawn fraccional acumulativo
    if (Math.random() < spawnPorFrame) this.spawnBrasa(width, height);

    // Update + render
    for (const b of this.brasas) {
      b.x += b.vx;
      b.y += b.vy;
      b.vy += 0.05;        // gravedad leve hacia abajo
      b.vida -= 0.015;
      ctx.fillStyle = b.color;
      ctx.globalAlpha = Math.max(0, b.vida);
      // fillRect — más rápido que arc()/fill() para 25 partículas a 60fps
      ctx.fillRect(b.x, b.y, b.size, b.size);
    }
    ctx.globalAlpha = 1;

    this.brasas = this.brasas.filter(b => b.vida > 0);

    this.rafId = requestAnimationFrame(() => this.tickBrasas(ctx, width, height));
  }

  private completar(): void {
    if (!this.animActiva) return;
    this.animActiva = false;
    if (this.rafId) cancelAnimationFrame(this.rafId);
    this.animacionCompleta.emit();
  }
}
