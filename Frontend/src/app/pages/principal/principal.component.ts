import { Component, AfterViewInit, OnDestroy, ViewChild, ElementRef } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';

interface Particle {
  x: number; y: number; r: number;
  vx: number; vy: number;
  a: number; ph: number; spd: number;
}

@Component({
  selector: 'app-principal',
  standalone: true,
  templateUrl: './principal.component.html',
  imports: [],
  styleUrls: ['./principal.component.scss']
})
export class PrincipalComponent implements AfterViewInit, OnDestroy {
  @ViewChild('particleCanvas') canvasRef!: ElementRef<HTMLCanvasElement>;

  uiFading = false;
  bgZooming = false;
  darkening = false;
  zoomActive = false;
  isTransitioning = false;

  private animFrameId: number | null = null;
  private particles: Particle[] = [];
  private resizeHandler!: () => void;

  constructor(private router: Router, private authService: AuthService) {}

  ngAfterViewInit(): void {
    this.initParticles();
    this.animate();
    this.resizeHandler = () => this.resizeCanvas();
    window.addEventListener('resize', this.resizeHandler);
  }

  ngOnDestroy(): void {
    if (this.animFrameId !== null) {
      cancelAnimationFrame(this.animFrameId);
    }
    window.removeEventListener('resize', this.resizeHandler);
  }

  private initParticles(): void {
    const canvas = this.canvasRef.nativeElement;
    canvas.width = window.innerWidth;
    canvas.height = window.innerHeight;
    this.particles = Array.from({ length: 130 }, () =>
      this.createParticle(canvas.width, canvas.height)
    );
  }

  private createParticle(w: number, h: number): Particle {
    return {
      x: Math.random() * w,
      y: Math.random() * h,
      r: 0.4 + Math.random() * 1.6,
      vx: (Math.random() - 0.5) * 0.3,
      vy: -(0.03 + Math.random() * 0.1),
      a: 0.12 + Math.random() * 0.40,
      ph: Math.random() * Math.PI * 2,
      spd: 0.01 + Math.random() * 0.018
    };
  }

  private resizeCanvas(): void {
    const canvas = this.canvasRef.nativeElement;
    canvas.width = window.innerWidth;
    canvas.height = window.innerHeight;
  }

  private animate(): void {
    const canvas = this.canvasRef.nativeElement;
    const ctx = canvas.getContext('2d')!;

    ctx.clearRect(0, 0, canvas.width, canvas.height);

    for (const p of this.particles) {
      p.x += p.vx;
      p.y += p.vy;
      p.ph += p.spd;

      if (p.x < 0) p.x = canvas.width;
      if (p.x > canvas.width) p.x = 0;
      if (p.y < 0) {
        p.y = canvas.height;
        p.x = Math.random() * canvas.width;
      }

      const alpha = p.a * (0.55 + 0.45 * Math.sin(p.ph));
      ctx.beginPath();
      ctx.arc(p.x, p.y, p.r, 0, Math.PI * 2);
      ctx.fillStyle = `rgba(255,218,135,${alpha})`;
      ctx.fill();
    }

    this.animFrameId = requestAnimationFrame(() => this.animate());
  }

  private delay(ms: number): Promise<void> {
    return new Promise(resolve => setTimeout(resolve, ms));
  }

  private getTargetRoute(): string {
    return this.authService.isAuthenticated() ? '/entrarCrearSala' : '/iniciar-sesion';
  }

  async onJugar(): Promise<void> {
    if (this.isTransitioning) return;
    this.isTransitioning = true;

    const route = this.getTargetRoute();

    // 0ms: UI fade out
    this.uiFading = true;

    // 200ms: zoom in del fondo (se aprecia solo, sin oscuridad)
    await this.delay(200);
    this.bgZooming = true;

    // 700ms: empieza a oscurecer (500ms de zoom ya visible)
    await this.delay(500);
    this.darkening = true;

    // 1600ms: pantalla negra completa (700 + ~900ms de transicion)
    await this.delay(900);
    this.zoomActive = true;

    // ~3200ms: navegar
    await this.delay(1600);
    this.router.navigate([route]);
  }

  verificarUsuario(): void {
    if (this.authService.isAuthenticated()) {
      this.router.navigate(['/perfilUsuario']);
    } else {
      this.router.navigate(['/registrarse']);
    }
  }

  RedireccionarCreditos(): void {
    this.router.navigate(['/creditos']);
  }

  RedireccionarAyuda(): void {
    this.router.navigate(['/ayuda']);
  }
}