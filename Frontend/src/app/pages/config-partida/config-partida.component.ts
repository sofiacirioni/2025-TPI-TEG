import { Component, OnInit, OnDestroy } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { SalaService } from '../../core/services/sala.service';
import { ConfigPartidaService } from '../../core/services/configPartida.service';
import { CommonModule, NgFor, NgIf } from '@angular/common';
import { WebSocketService } from '../../core/services/socket.service';
import { AuthService } from '../../core/services/auth.service';
import {JugadorDto} from '../../core/models/interfaces/partida.interface';

@Component({
  selector: 'app-config-partida',
  templateUrl: './config-partida.component.html',
  styleUrls: ['./config-partida.component.css'],
  standalone: true,
  imports: [
    ReactiveFormsModule,
    RouterLink, NgFor, NgIf,
    CommonModule
  ]
})
export class ConfigPartidaComponent implements OnInit, OnDestroy {
  sala: any = null;
  crearJugadorForm!: FormGroup;
  mostrarModal = false;
  jugadores: { nombreJugador: string }[] = [];

  constructor(
    private salaService: SalaService,
    private configService: ConfigPartidaService,
    private fb: FormBuilder,
    private router: Router,
    private socketService: WebSocketService,
    private authService: AuthService
  ) {}

  ngOnInit() {
    this.sala = this.salaService.getSala();

    if (!this.sala || !this.sala.idSala) {
      alert('No hay sala seleccionada.');
      this.router.navigate(['/sala']);
      return;
    }

    this.crearJugadorForm = this.fb.group({
      nombreJugador: ['', Validators.required]
    });

    // Cargar jugadores iniciales
    this.configService.getJugadores(this.sala.idSala).subscribe({
      next: jugadores => {
        this.jugadores = jugadores;
      },
      error: (err) => {
        console.error('Error al obtener jugadores', err);
      }
    });

    // Escuchar inicio de partida 🔥 (esto va AFUERA del conectar)
    this.socketService.suscribirseInicioPartida(this.sala.idSala, (data: any) => {
      if (data?.url) {
        this.router.navigate(['/juego', data.url]);
      }
    });

    // Conectar al WebSocket
    this.socketService.conectar(this.sala.idSala, (jugador: { nombre: string }) => {
      if (!this.jugadores.some(j => j.nombreJugador === jugador.nombre)) {
        this.jugadores.push({ nombreJugador: jugador.nombre });
      }
    });
  }

  ngOnDestroy() {
    this.socketService.desconectar();
  }

  abrirModal() {
    this.mostrarModal = true;
  }

  cerrarModal(event?: MouseEvent) {
    this.mostrarModal = false;
  }

  crearJugador() {
    if (this.crearJugadorForm.invalid) {
      alert('Debés ingresar el nombre del jugador.');
      return;
    }

    const nombreJugador = this.crearJugadorForm.value.nombreJugador;
    const usuarioActual = this.authService.getUsuario();

    if (!usuarioActual) {
      alert('No hay usuario logueado.');
      return;
    }

    this.configService.crearJugador(this.sala.idSala, nombreJugador).subscribe({
      next: (jugadorCreado: JugadorDto) => {
        this.authService.setJugadorId(jugadorCreado.idJugador);
        this.socketService.emitirNuevoJugador(this.sala.idSala, {
          nombre: nombreJugador
        });
        this.jugadores.push({ nombreJugador });
        this.crearJugadorForm.reset();
        this.cerrarModal();
        alert('Jugador creado con éxito');
      },
      error: (error) => {
        alert(error.error?.mensaje || 'Ocurrió un error inesperado.');
        console.error('Detalle del error:', error);
      }
    });
  }

  eliminarJugador(jugador: { nombreJugador: string }) {
    this.jugadores = this.jugadores.filter(j => j !== jugador);
  }

  crearBot() {
    const usuarioActual = this.authService.getUsuario();
    if (!usuarioActual || !usuarioActual.idUsuario) {
      alert('No hay usuario logueado.');
      return;
    }

    const idSala = this.sala.idSala;

    this.configService.crearBot(idSala).subscribe({
      next: (botCreado) => {
        this.socketService.emitirNuevoJugador(idSala, {
          nombre: botCreado.nombre
        });
        this.jugadores.push({ nombreJugador: botCreado.nombre });
      },
      error: (error) => {
        alert(error.error?.message || 'Ocurrió un error inesperado.');
        console.error('Detalle del error:', error);
      }
    });
  }

  crearPartida() {
    const usuarioActual = this.authService.getUsuario();

    if (!usuarioActual || !usuarioActual.idUsuario) {
      alert('No hay usuario logueado.');
      return;
    }

    const idSala = this.sala.idSala;

    this.configService.crearPartida(idSala).subscribe({
      next: () => {
        this.socketService.emitirInicioPartida(idSala, {
          url: this.sala.url
        });

        alert('Partida creada correctamente.');
        this.router.navigate(['/juego', this.sala.url]);
      },
      error: (error) => {
        alert(error.error?.message || error.error?.error || 'Ocurrió un error inesperado.');
        console.error('Detalle del error:', error);
      }
    });
  }

  esUsuarioCreador(): boolean {
    const usuarioActual = this.authService.getUsuario();
    return !!usuarioActual && !!this.sala?.creador && this.sala.creador.idUsuario === usuarioActual.idUsuario;
  }
}
