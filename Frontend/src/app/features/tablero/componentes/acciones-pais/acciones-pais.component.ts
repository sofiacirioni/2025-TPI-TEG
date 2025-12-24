import { Component, input, Input, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { NgbActiveModal, NgbModule } from '@ng-bootstrap/ng-bootstrap';
import { AtaqueDto, AtaqueResponseDto, EstadoPaisDto, JugadorDto, PartidaDto, TurnoDto, MoverFichas } from 'src/app/core/models/interfaces/partida.interface';
import { AuthService, UsuarioDto } from 'src/app/core/services/auth.service';
import { TableroServicio } from 'src/app/core/services/tablero.service';
import { FaseTurno } from 'src/app/core/models/interfaces/partida.interface';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { ViewChild } from '@angular/core';
import { Pais } from 'src/app/core/models/class/pais';
import { log } from '@angular-devkit/build-angular/src/builders/ssr-dev-server';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-acciones-pais',
  templateUrl: './acciones-pais.component.html',
  styleUrls: ['./acciones-pais.component.css'],
  imports: [NgbModule, FormsModule, CommonModule]
})
export class AccionesPaisComponent implements OnInit {

  @Input() paisSeleccionado!: EstadoPaisDto;
  paisesLimitrofes!: EstadoPaisDto[];
  paisesEnemigos: EstadoPaisDto[] = [];
  paisesAliados: EstadoPaisDto[] = [];
  jugador: JugadorDto | null = null;
  turnoActual: TurnoDto | null = null;
  esSuyo: boolean = false;
  estaJugando: boolean = false;

  idPaisSeleccionado: number = -1;
  inputTropas: number = 0;

  @ViewChild('modalAtaque') modalAtaque: any;

  ataqueResultado?: AtaqueResponseDto;
  paisOrigenNombre: string = '';
  paisDestinoNombre: string = '';


  constructor(public activeModal: NgbActiveModal,
              private modalService: NgbModal,
              private tableroServicio: TableroServicio,
              private authService: AuthService
  ) {
  }

  ngOnInit(): void {
      this.paisesEnemigos = [];
      this.paisesAliados = [];
    const usuarioLogeado: UsuarioDto = this.authService.getUsuario()

    this.tableroServicio.partidaObservable.subscribe({
      next: (p: PartidaDto) => {
        this.jugador = p.jugadores?.find(j => j.idUsuario === usuarioLogeado.idUsuario);
        this.turnoActual = p.turnos.find(t => t.nroTurno == p.turnoActual);

        if (this.turnoActual?.idJugador === this.jugador?.idJugador) {
          this.estaJugando = true;
        } else {
          this.estaJugando = false;
        }
        if (this.paisSeleccionado?.idJugador === this.jugador?.idJugador) {
          this.esSuyo = true;
        } else {
          this.esSuyo = false;
        }
      }
    })

    this.tableroServicio.getAllLimites(this.paisSeleccionado.id).subscribe({
      next: (limites) => {
        console.log('Límites del país obtenidos:', limites);
        this.paisesLimitrofes = limites;
        this.calcularPaisesAliadosYEnemigos();
      },
      error: (error) => {
        console.error('Error al obtener los límites del país:', error);
      }
    });
  }

  calcularPaisesAliadosYEnemigos() {
    this.paisesAliados = [];
    this.paisesEnemigos = [];

    const aliadosIds = new Set<number>();
    const enemigosIds = new Set<number>();

    for (const pais of this.paisesLimitrofes) {
      const id = pais.pais.idPais;

      if (pais.idJugador === this.paisSeleccionado.idJugador) {
        if (!aliadosIds.has(id)) {
          aliadosIds.add(id);
          this.paisesAliados.push(pais);
        }
      } else {
        if (!enemigosIds.has(id)) {
          enemigosIds.add(id);
          this.paisesEnemigos.push(pais);
        }
      }
    }
  }


  validarInputMover(event: Event) {
    const input = event.target as HTMLInputElement;
    const value = Number(input.value);
    const max = this.paisSeleccionado.cantidadTropas - 1;

    if (isNaN(value) || value < 0) {
      this.inputTropas = 0;
      input.value = '0';
    } else if (value > max) {
      this.inputTropas = max;
      input.value = String(max);
    } else {
      this.inputTropas = value;
    }
  }


  validarInput(event: Event) {
    const input = event.target as HTMLInputElement;
    const value = Number(input.value);
    const max = this.jugador.ejercito ?? 0;

    if (isNaN(value) || value < 0) {
      this.inputTropas = 0;
      input.value = '0';
    } else if (value > max) {
      this.inputTropas = max;
      input.value = String(max);
    } else {
      this.inputTropas = value;
    }
  }

  cerrar() {
    this.activeModal.close();
  }

  atacar() {
    if (!this.jugador) {
      console.error('Jugador no definido al atacar');
      return;
    }
    if (this.idPaisSeleccionado === -1) {
      alert('Debe seleccionar un país destino válido para atacar.');
      return;
    }
    const ataqueDto: AtaqueDto = {
      idJugador: this.jugador.idJugador,
      idPaisOrigen: this.paisSeleccionado.pais.idPais,
      idPaisDestino: this.idPaisSeleccionado
    };

    this.tableroServicio.atacarPais(ataqueDto).subscribe({
      next: (response: AtaqueResponseDto) => {
        this.ataqueResultado = response;

        this.paisOrigenNombre = this.paisSeleccionado.pais.nombre;
        this.paisDestinoNombre = this.paisesEnemigos.find(p => p.pais.idPais === ataqueDto.idPaisDestino)?.pais.nombre || '';

        this.modalService.open(this.modalAtaque);
        this.cerrar();
      },
      error: (error) => {
        console.error("Error al realizar el ataque:", error);
      }
    });
  }

  getEmojiDado(valor: number): string {
    switch (valor) {
      case 1:
        return '⚀';
      case 2:
        return '⚁';
      case 3:
        return '⚂';
      case 4:
        return '⚃';
      case 5:
        return '⚄';
      case 6:
        return '⚅';
      default:
        return '?';
    }
  }


  defender() {
    if (!this.jugador) {
      console.error('Jugador no definido al defender');
      return;
    }
    let body = {
      idJugador: this.jugador.idJugador,
      paisesFichas: [
        {
          idPais: this.paisSeleccionado.pais.idPais,
          cantidadFichas: this.inputTropas
        }
      ]
    };

    this.tableroServicio.defenderPais(body).subscribe({
      next: (resultado) => {
        if (resultado) {
          alert("Defensa exitosa");
          this.activeModal.close();
        } else {
          console.error("Error al defender el país");
        }
      },
      error: (error) => {
        console.error('Error en defenderPais:', error);
      }
    });
  }

  reagrupar() {
    if (!this.jugador) {
      console.error('Jugador no definido al reagrupar');
      return;
    }
    if (this.idPaisSeleccionado === -1) {
      alert('Debe seleccionar un país destino válido para reagrupar.');
      return;
    }
    let moverFichas: MoverFichas = {
      idJugador: this.jugador.idJugador,
      idPaisOrigen: this.paisSeleccionado.pais.idPais,
      idPaisDestino: this.idPaisSeleccionado,
      cantidadFichas: this.inputTropas
    };

    this.tableroServicio.reagruparFichas(moverFichas).subscribe({
      next: (resultado) => {
        if (resultado) {
          alert("Reagrupación exitosa");
          this.activeModal.close();
        } else {
          console.error("Error al reagrupar las fichas");
        }
      },
      error: (error) => {
        console.error('Error en reagruparFichas:', error);
      }
    });
  }
}
