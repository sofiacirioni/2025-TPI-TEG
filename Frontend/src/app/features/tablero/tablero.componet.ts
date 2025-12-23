import { Component, OnDestroy, OnInit } from '@angular/core';
import {ActivatedRoute, Router, RouterOutlet} from '@angular/router';
import { JugadorPanelComponent } from './componentes/jugadores-panel/jugador-panel.component';
import { OpcionesPanelComponent } from './componentes/opciones-panel/opciones-panel.component';
import { TableroServicio } from '../../core/services/tablero-servicio';
import { MapaSvgComponent } from './componentes/mapa-svg/mapa-svg.component';
import { CanjeTarjetasDto, EstadoPaisDto, EstadoPartida, EstadoTarjetaDto, FaseTurno, JugadorDto, PartidaDto, TurnoDto, UsarTarjetaEnPaisDto, VerificacionObjetivo } from '../../core/models/interfaces/partida.interface';
import { AccionesPaisComponent } from './componentes/acciones-pais/acciones-pais.component';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { log } from '@angular-devkit/build-angular/src/builders/ssr-dev-server';
import { Turno } from 'src/app/core/models/class/turno';
import { AuthService } from '../../core/services/auth.service';
import { CommonModule, NgIf, NgStyle } from '@angular/common';
import { EstadoTarjeta } from 'src/app/core/models/class/estado_tarjeta';

@Component({
  selector: 'app-tablero',
  imports: [JugadorPanelComponent, OpcionesPanelComponent, MapaSvgComponent, CommonModule, NgStyle],
  templateUrl: 'tablero.component.html',
  styleUrl: 'tablero.component.css',
})

export class TableroComponent implements OnInit, OnDestroy {

  url!: string;
  partida!: PartidaDto;
  turnoInfo: string = '';
  esMiTurno: boolean = false;
  jugadorConsquisto: JugadorDto = null;
  cartasJugador: EstadoTarjetaDto[] = [];
  puedeCanjear: boolean = false;
  combinacionesPosibles: EstadoTarjetaDto[][] = [];
  combinacionSeleccionada: number | null = null;
  jugadorId: number = 0;

  constructor(private tableroServicio: TableroServicio,
    private route: ActivatedRoute,
    private modalService: NgbModal,
    private authService: AuthService,
              private router:Router) {
  }

  ngOnInit() {
    this.url = this.route.snapshot.params['url'];
    this.tableroServicio.startPolling(this.url);
    this.tableroServicio.partidaObservable.subscribe({
      next: (result: PartidaDto) => {
        if (result.estado === EstadoPartida.TERMINADA) {
          console.log('Partida terminada');

          this.tableroServicio.consultarMotivoGanador(result.ganador.idJugador).subscribe({
            next: (verificacion: VerificacionObjetivo) => {
              if (verificacion.gano) {
                this.tableroServicio.stopPolling();
                alert(`El color ${result.ganador.color} ha ganado la partida! 🎉\nObjetivo cumplido: ${verificacion.objetivoCumplido}`);
                this.router.navigate(['/principal']);
              }
            },
            error: (err) => {
              console.error('Error al consultar el motivo del ganador:', err);
            }
          });

          return;
        }
        // console.log('Partida recibida:', result);
        // console.log('Jugadores recibidos:', result.jugadores);
        this.partida = result;
        this.turnoInfo = "Turno:\n";
        const usuarioActual = this.authService.getUsuario();
        const idJugadorActual = Number(localStorage.getItem('idJugador'));

        const turnoActualNum = Number(this.partida.turnoActual);
        const turno = this.partida.turnos.find(t => Number(t.nroTurno) === turnoActualNum);
        // console.log('ID de Turno actual:', this.partida.turnoActual);
        if (!turno) {
          console.warn('No se encontró el turno actual:', this.partida.turnoActual);
          this.turnoInfo = "No hay turno actual";
          this.esMiTurno = false;
          return;
        }

        const jugador: JugadorDto | undefined = this.partida.jugadores?.find(
          j => j.idJugador === turno.idJugador
        );

        if (!jugador) {
          console.warn(`Jugador con id ${turno.idJugador} no encontrado en la lista de jugadores.`);
          this.turnoInfo = "No hay información del jugador para el turno actual";
          this.esMiTurno = false;
          return;
        }

        this.turnoInfo = `Jugador: ${jugador.nombre} | ${jugador.color}\nFase: ${turno.fase}\n`;

        if ((jugador.idUsuario === usuarioActual?.idUsuario) && (jugador.idJugador === idJugadorActual)) {
          this.turnoInfo += "→ ¡Es tu turno! 🎉";
          this.esMiTurno = true;
          this.jugadorId = jugador.idJugador;
        } else {
          this.turnoInfo += "Esperando turno...";
          this.esMiTurno = false;
        }

        // Usuario conquisto
        this.jugadorConsquisto = this.partida.jugadores?.find(j => j.consquisto && j.idUsuario === usuarioActual?.idUsuario && turno.fase === FaseTurno.MOVER_TROPAS);

        let jugadorUsuario = this.partida.jugadores?.find(j => j.idUsuario === usuarioActual.idUsuario);

        this.cartasJugador = this.partida.estadoTarjetas?.filter(t => t.idJugador === jugadorUsuario.idJugador) || [];

        for (const tarjeta of this.cartasJugador) {
          tarjeta.jugadorTienePais = this.partida.estadoPaises.some(p => p.idJugador === jugadorUsuario.idJugador && p.pais.idPais === tarjeta.tarjeta.pais.idPais && !tarjeta.usada);
        }

        this.calcularCombinacionesCanje(this.cartasJugador);
      }
    })
  }

  avanzarFaseTurno() {
    this.tableroServicio.cambiarTurno(this.partida.idPartida).subscribe({
      next: (response) => {
        console.log('Fase del turno avanzada correctamente');
      },
      error: (err) => {
        console.error('Error al avanzar la fase del turno:', err);
      }
    });
  }

  ngOnDestroy() {
    this.tableroServicio.stopPolling();
  }

  paisClickeado(id: EstadoPaisDto) {
    const modalRef = this.modalService.open(AccionesPaisComponent, {
      centered: true,
      size: 'm', // opcional
      backdrop: 'static', // opcional: evita cerrar haciendo clic fuera
    });

    modalRef.componentInstance.paisSeleccionado = id;
  }

  private calcularCombinacionesCanje(cartas: EstadoTarjetaDto[]): void {
    this.puedeCanjear = false;
    this.combinacionesPosibles = [];

    if (!cartas || cartas.length < 3) return;

    const disponibles = cartas.filter(c => !c.canjeada);

    // Generar todas las combinaciones posibles de 3 cartas
    for (let i = 0; i < disponibles.length - 2; i++) {
      for (let j = i + 1; j < disponibles.length - 1; j++) {
        for (let k = j + 1; k < disponibles.length; k++) {
          const combo = [disponibles[i], disponibles[j], disponibles[k]];
          const simbolos = combo.map(c => c.tarjeta?.simbolo).filter(Boolean);

          if (simbolos.length === 3) {
            const set = new Set(simbolos);
            if (set.size === 1 || set.size === 3) {
              this.combinacionesPosibles.push(combo);
            }
          }
        }
      }
    }

    this.puedeCanjear = this.combinacionesPosibles.length > 0;
  }

  abrirModalCanje(content: any): void {
    console.log('Abriendo modal de canje');
    console.log("--------------------------------------------------------------");

    this.combinacionSeleccionada = null;
    this.modalService.open(content, { centered: true, size: 'lg' });
  }

  seleccionarCombinacion(index: number): void {
    this.combinacionSeleccionada = index;
  }

  confirmarCanje(modalRef: any): void {
    if (this.combinacionSeleccionada === null) return;

    const cartasSeleccionadas = this.combinacionesPosibles[this.combinacionSeleccionada];
    const dto: CanjeTarjetasDto = {
      idTarjetas: cartasSeleccionadas.map(c => c.idEstadoTarjeta),
      idJugador: this.jugadorId
    };

    this.tableroServicio.realizarCanje(dto).subscribe({
      next: (tropas: number) => {
        alert(`Canje exitoso. Tropas obtenidas: ${tropas}`);
        modalRef.close();
        // Podés recargar el estado de tarjetas si querés
      },
      error: (error) => {
        alert("Error al realizar el canje");
        console.error(error);
      }
    });
  }



  obtenerColorJugadorActual(): string {
    const usuario = this.authService.getUsuario();
    const jugador = this.partida?.jugadores.find(j => j.idUsuario === usuario.idUsuario);
    return this.obtenerColor(jugador?.color);
  }

  obtenerTarjeta() {
    if (this.jugadorConsquisto) {
      this.tableroServicio.obtenerTarjeta(this.jugadorConsquisto.idJugador, this.partida.idPartida).subscribe({
        next: (tarjeta) => {
          console.log('Tarjeta obtenida:', tarjeta);
        },
        error: (err) => {
          console.error('Error al obtener la tarjeta:', err);
          alert('Error al obtener la tarjeta');
        }
      });
    } else {
      alert('No has conquistado un país en este turno.');
    }
  }

  usarTarjeta(tarjeta: EstadoTarjetaDto) {

    const dto: UsarTarjetaEnPaisDto = {
      idTarjeta: tarjeta.idEstadoTarjeta,
      idJugador: tarjeta.idJugador
    };

    this.tableroServicio.usarTarjetaEnPais(dto).subscribe({
      next: () => {
        console.log("Tarjeta usada correctamente");
      },
      error: (err) => {
        console.error("Error al usar tarjeta", err);
      }
    });

  }

  obtenerColor(color: string): string {

    switch (color.toLowerCase()) {
      case 'rojo':
        return '#f44336';
      case 'verde':
        return '#009688';
      case 'azul':
        return '#3f51b5';
      case 'amarillo':
        return '#ffc107';
      case 'violeta':
        return '#9c27b0';
      case 'naranja':
        return '#ff9800';
      default:
        return '#000';
    }
  }
}
