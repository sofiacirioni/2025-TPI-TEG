import { Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { StampComponent } from '../../shared/components/stamp/stamp.component';
import { AuthService } from '../../core/services/auth.service';
import {
  CampaniaHistorial,
  HistorialComandante,
  UsuarioService,
} from '../../core/services/perfilUsuario.service';
import { UserInfo } from '../../core/models/interfaces/auth.interfaces';

/** Los colores llegan del backend en mayúsculas y con nombres propios. */
const COLOR_VAR_MAP: Record<string, string> = {
  ROJO: 'var(--player-rojo)',
  AZUL: 'var(--player-azul)',
  NARANJA: 'var(--player-naranja)',
  VIOLETA: 'var(--player-purpura)',
  VERDE: 'var(--player-verde)',
  AMARILLO: 'var(--player-dorado)',
};

const NOMBRE_DIVISION: Record<string, string> = {
  ROJO: 'División Roja',
  AZUL: 'División Azul',
  NARANJA: 'División Naranja',
  VIOLETA: 'División Púrpura',
  VERDE: 'División Verde',
  AMARILLO: 'División Dorada',
};

const MESES_ABREVIADOS = [
  'ENE', 'FEB', 'MAR', 'ABR', 'MAY', 'JUN',
  'JUL', 'AGO', 'SEP', 'OCT', 'NOV', 'DIC',
];

const LARGO_MINIMO_CLAVE = 6;

@Component({
  selector: 'app-perfil-usuario',
  standalone: true,
  imports: [FormsModule, StampComponent],
  templateUrl: './perfil-usuario.component.html',
  styleUrl: './perfil-usuario.component.scss',
})
export class PerfilUsuario implements OnInit {
  private authService = inject(AuthService);
  private usuarioService = inject(UsuarioService);
  private router = inject(Router);

  /** Fotografías de legajo disponibles. */
  readonly avatares: string[] = [
    'assets/images/avatars/AgosCh.png',
    'assets/images/avatars/CandeArguello.png',
    'assets/images/avatars/CandeBlanco.png',
    'assets/images/avatars/LaraHeredia.png',
    'assets/images/avatars/MeliAbril.png',
    'assets/images/avatars/SofiCirioni.png',
    'assets/images/avatars/Maxi.png',
  ];

  usuario: UserInfo | null = null;
  historial: HistorialComandante | null = null;
  cargandoHistorial = true;
  errorHistorial = false;

  mostrarGaleria = false;
  mostrarRectificar = false;

  contraseniaActual = '';
  nuevaContrasenia = '';
  errorClave = '';
  /** Nota al margen: confirma la última operación hecha sobre el legajo. */
  aviso = '';

  ngOnInit(): void {
    this.usuario = this.authService.getCurrentUser();
    if (!this.usuario) {
      this.router.navigate(['/iniciar-sesion']);
      return;
    }

    this.usuarioService.getHistorial(this.usuario.idUsuario).subscribe({
      next: (h) => {
        this.historial = h;
        this.cargandoHistorial = false;
      },
      error: () => {
        this.errorHistorial = true;
        this.cargandoHistorial = false;
      },
    });
  }

  // ── Página izquierda: identificación ─────────────────────────────────

  /** El número impreso arriba a la derecha de la libreta. */
  get codigoLegajo(): string {
    const id = this.usuario?.idUsuario ?? 0;
    return String(id).padStart(6, '0');
  }

  /**
   * Destino: la división que más veces comandó. Lo calcula el backend sobre
   * todas sus participaciones, no sólo sobre el registro recortado que se
   * muestra, así que nombre y divisa siempre coinciden.
   */
  get destino(): string {
    return NOMBRE_DIVISION[this.historial?.divisaHabitual ?? ''] ?? 'Sin destino asignado';
  }

  get colorDestino(): string {
    return this.colorVarPara(this.historial?.divisaHabitual);
  }

  /**
   * Alta de la cuenta. Se mantiene el año censurado de la pizarra: el día y
   * el mes son reales, el año lo tacha la censura como en todo el juego.
   */
  get fechaAltaFormateada(): string {
    const f = this.historial?.fechaAlta;
    if (!f) return '—';
    if (Array.isArray(f)) return this.formatearDiaMes(f[1], f[2]);
    const d = new Date(f);
    return this.formatearDiaMes(d.getMonth() + 1, d.getDate());
  }

  // ── Página derecha: hoja de servicios ────────────────────────────────

  get sinCampanias(): boolean {
    return !!this.historial && this.historial.campaniasLibradas === 0;
  }

  fechaCampania(c: CampaniaHistorial): string {
    if (Array.isArray(c.fecha)) return this.formatearDiaMes(c.fecha[1], c.fecha[2]);
    if (!c.fecha) return '—';
    const d = new Date(c.fecha);
    return this.formatearDiaMes(d.getMonth() + 1, d.getDate());
  }

  resultadoDe(c: CampaniaHistorial): string {
    if (!c.terminada) return 'EN CURSO';
    if (c.ganador) return 'VICTORIA';
    if (c.eliminado) return 'ELIMINADO';
    return `${c.puesto}.º PUESTO`;
  }

  colorVarPara(color?: string): string {
    return COLOR_VAR_MAP[(color ?? '').toUpperCase()] ?? 'var(--color-oscuro)';
  }

  /** El parte de campaña de esa partida: la pizarra de estadísticas. */
  verParte(c: CampaniaHistorial): void {
    this.router.navigate(['/estadisticas', c.idPartida]);
  }

  // ── Trámites sobre el legajo ─────────────────────────────────────────

  seleccionarImagen(img: string): void {
    if (!this.usuario) return;
    this.mostrarGaleria = false;

    this.usuarioService
      .actualizarImagen({ correo: this.usuario.correo, imagen: img })
      .subscribe({
        next: (actualizado) => {
          if (this.usuario) this.usuario = { ...this.usuario, imagen: actualizado.imagen };
          this.aviso = 'Fotografía de legajo actualizada.';
        },
        error: () => {
          this.aviso = 'No se pudo actualizar la fotografía.';
        },
      });
  }

  rectificarClave(): void {
    this.errorClave = '';
    if (!this.usuario) return;

    if (this.contraseniaActual === this.nuevaContrasenia) {
      this.errorClave = 'La clave nueva no puede ser igual a la anterior.';
      return;
    }
    if (this.nuevaContrasenia.length < LARGO_MINIMO_CLAVE) {
      this.errorClave = `La clave nueva debe tener al menos ${LARGO_MINIMO_CLAVE} caracteres.`;
      return;
    }

    this.usuarioService
      .actualizarUsuario({
        correo: this.usuario.correo,
        contraseniaActual: this.contraseniaActual,
        nuevaContrasenia: this.nuevaContrasenia,
        imagen: this.usuario.imagen,
      })
      .subscribe({
        next: () => {
          this.contraseniaActual = '';
          this.nuevaContrasenia = '';
          this.mostrarRectificar = false;
          this.aviso = 'Clave rectificada.';
        },
        error: () => {
          this.errorClave = 'La clave actual no coincide con la del legajo.';
        },
      });
  }

  cerrarRectificar(): void {
    this.mostrarRectificar = false;
    this.errorClave = '';
    this.contraseniaActual = '';
    this.nuevaContrasenia = '';
  }

  cerrarSesion(): void {
    // logout() devuelve un Observable frío: sin subscribe no se llama al
    // backend ni se limpia el token en memoria.
    this.authService.logout().subscribe(() => {
      this.router.navigate(['/iniciar-sesion']);
    });
  }

  volver(): void {
    this.router.navigate(['/principal']);
  }

  private formatearDiaMes(mes: number, dia: number): string {
    const nombreMes = MESES_ABREVIADOS[mes - 1] ?? '';
    return `${String(dia).padStart(2, '0')} ${nombreMes} 194█`;
  }
}
