import { Component, OnInit, OnDestroy } from '@angular/core';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { Subscription } from 'rxjs';
import { SalaService } from '../../core/services/sala.service';
import { ConfigPartidaService } from '../../core/services/configPartida.service';
import { WebSocketService } from '../../core/services/socket.service';
import { AuthService } from '../../core/services/auth.service';
import { NotificationService } from '../../core/services/notification.service';
import { SlideInDirective } from '../../shared/directives/slide-in.directive';

interface JugadorLocal {
  id: number;
  nombre: string;
  esBot: boolean;
  idUsuario: number;
}

@Component({
  selector: 'app-config-partida',
  templateUrl: './config-partida.component.html',
  styleUrls: ['./config-partida.component.scss'],
  standalone: true,
  imports: [CommonModule, SlideInDirective]
})
export class ConfigPartidaComponent implements OnInit, OnDestroy {
  sala: any = null;
  jugadores: JugadorLocal[] = [];
  nombreUsuario: string = '';
  copiado: boolean = false;
  fechaOrden: string = '';

  private authSub?: Subscription;

  private readonly COLORES_JUGADOR = [
    'var(--player-rojo)',
    'var(--player-azul)',
    'var(--player-naranja)',
    'var(--player-purpura)',
    'var(--player-verde)',
    'var(--player-dorado)',
  ];

  private readonly mesesAbreviados = [
    'ENE','FEB','MAR','ABR','MAY','JUN',
    'JUL','AGO','SEP','OCT','NOV','DIC'
  ];

  constructor(
    private salaService: SalaService,
    private configService: ConfigPartidaService,
    private router: Router,
    private socketService: WebSocketService,
    private authService: AuthService,
    private notificationService: NotificationService
  ) {}

  ngOnInit() {
    this.sala = this.salaService.getSala();

    if (!this.sala || !this.sala.idSala) {
      this.notificationService.error('No hay sala seleccionada.');
      this.router.navigate(['/sala']);
      return;
    }

    // Fecha estilo 1944
    const ahora = new Date();
    const dia = ahora.getDate().toString().padStart(2, '0');
    const mes = this.mesesAbreviados[ahora.getMonth()];
    this.fechaOrden = `${dia}/${mes}/4█`;

    // Nombre del usuario logueado
    this.authSub = this.authService.currentUser$.subscribe(user => {
      if (user) this.nombreUsuario = user.usuario;
    });

    // Escuchar inicio de partida
    this.socketService.suscribirseInicioPartida(this.sala.idSala, (data: any) => {
      if (data?.url) {
        this.router.navigate(['/juego', data.url]);
      }
    });

    // Conectar al WebSocket — recargar lista cuando alguien se une
    const token = this.authService.getAccessToken() ?? undefined;
    this.socketService.conectar(this.sala.idSala, (_jugador: { nombre: string }) => {
      this.recargarJugadores();
    }, token);

    // Auto-crear jugador con el nombre del usuario si no hay uno activo
    if (!this.authService.getJugadorId()) {
      const usuario = this.authService.getCurrentUser();
      if (usuario) {
        this.configService.crearJugador(this.sala.idSala, usuario.usuario).subscribe({
          next: (jugadorCreado: any) => {
            this.authService.setJugadorId(jugadorCreado.idJugador);
            this.socketService.emitirNuevoJugador(this.sala.idSala, { nombre: usuario.usuario });
            this.recargarJugadores();
          },
          error: (err) => {
            console.error('Error al crear jugador:', err);
            this.recargarJugadores();
          }
        });
      }
    } else {
      this.recargarJugadores();
    }
  }

  ngOnDestroy() {
    this.authSub?.unsubscribe();
    this.socketService.desconectar();
  }

  private recargarJugadores() {
    this.configService.getJugadores(this.sala.idSala).subscribe({
      next: (jugadores: JugadorLocal[]) => { this.jugadores = jugadores; },
      error: (err) => console.error('Error al obtener jugadores', err)
    });
  }

  get puedeIniciar(): boolean {
    return this.jugadores.length >= 2;
  }

  get esAnfitrion(): boolean {
    return this.esUsuarioCreador();
  }

  get slotsVacios(): number[] {
    const total = 6;
    const vacios = Math.max(0, total - this.jugadores.length);
    return Array(vacios).fill(0);
  }

  getColorJugador(index: number): string {
    return this.COLORES_JUGADOR[index % this.COLORES_JUGADOR.length];
  }

  copiarCodigo() {
    if (!this.sala?.url) return;
    navigator.clipboard.writeText(this.sala.url).then(() => {
      this.copiado = true;
      setTimeout(() => { this.copiado = false; }, 2000);
    });
  }

  agregarBot() {
    const usuarioActual = this.authService.getCurrentUser();
    if (!usuarioActual || !usuarioActual.idUsuario) {
      this.notificationService.error('No hay usuario logueado.');
      return;
    }
    this.configService.crearBot(this.sala.idSala).subscribe({
      next: (botCreado: any) => {
        this.socketService.emitirNuevoJugador(this.sala.idSala, { nombre: botCreado.nombre });
        this.recargarJugadores();
      },
      error: (error) => {
        this.notificationService.error(error.error?.message || 'Ocurrió un error inesperado.');
        console.error('Detalle del error:', error);
      }
    });
  }

  eliminarJugador(id: number) {
    this.jugadores = this.jugadores.filter(j => j.id !== id);
  }

  iniciarPartida() {
    const usuarioActual = this.authService.getCurrentUser();
    if (!usuarioActual || !usuarioActual.idUsuario) {
      this.notificationService.error('No hay usuario logueado.');
      return;
    }
    this.configService.crearPartida(this.sala.idSala).subscribe({
      next: () => {
        this.socketService.emitirInicioPartida(this.sala.idSala, { url: this.sala.url });
        this.router.navigate(['/juego', this.sala.url]);
      },
      error: (error) => {
        this.notificationService.error(error.error?.message || error.error?.error || 'Ocurrió un error inesperado.');
        console.error('Detalle del error:', error);
      }
    });
  }

  abandonarSala() {
    this.router.navigate(['/sala']);
  }

  esUsuarioCreador(): boolean {
    const usuarioActual = this.authService.getCurrentUser();
    return !!usuarioActual && !!this.sala?.creador && this.sala.creador.idUsuario === usuarioActual.idUsuario;
  }
}
