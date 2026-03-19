import { Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { DIALOG_DATA, DialogRef } from '@angular/cdk/dialog';
import { AtaqueDto, AtaqueResponseDto, EstadoPaisDto, JugadorDto, PartidaDto, TurnoDto, MoverFichas } from 'src/app/core/models/interfaces/partida.interface';
import { AuthService, UsuarioDto } from 'src/app/core/services/auth.service';
import { TableroServicio } from 'src/app/core/services/tablero.service';
import { FaseTurno } from 'src/app/core/models/interfaces/partida.interface';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-acciones-pais',
  templateUrl: './acciones-pais.component.html',
  styleUrls: ['./acciones-pais.component.css'],
  imports: [FormsModule, CommonModule]
})
export class AccionesPaisComponent implements OnInit {

  dialogRef = inject(DialogRef);
  data = inject<{ paisSeleccionado: EstadoPaisDto }>(DIALOG_DATA);

  private tableroServicio = inject(TableroServicio);
  private authService = inject(AuthService);

  paisSeleccionado!: EstadoPaisDto;
  paisesLimitrofes!: EstadoPaisDto[];
  paisesEnemigos: EstadoPaisDto[] = [];
  paisesAliados: EstadoPaisDto[] = [];
  jugador: JugadorDto | null = null;
  turnoActual: TurnoDto | null = null;
  esSuyo: boolean = false;
  estaJugando: boolean = false;

  idPaisSeleccionado: number = -1;
  inputTropas: number = 0;

  ataqueResultado?: AtaqueResponseDto;
  paisOrigenNombre: string = '';
  paisDestinoNombre: string = '';
  showAtaqueModal: boolean = false;

  ngOnInit(): void {
    this.paisSeleccionado = this.data.paisSeleccionado;
    this.paisesEnemigos = [];
    this.paisesAliados = [];

    const usuarioLogeado: UsuarioDto = this.authService.getUsuario();

    this.tableroServicio.partidaObservable.subscribe({
      next: (p: PartidaDto) => {
        this.jugador = p.jugadores?.find(j => j.idUsuario === usuarioLogeado.idUsuario);
        this.turnoActual = p.turnos.find(t => t.nroTurno == p.turnoActual);
        this.estaJugando = this.turnoActual?.idJugador === this.jugador?.idJugador;
        this.esSuyo = this.paisSeleccionado?.idJugador === this.jugador?.idJugador;
      }
    });

    this.tableroServicio.getAllLimites(this.paisSeleccionado.id).subscribe({
      next: (limites) => {
        this.paisesLimitrofes = limites;
        this.calcularPaisesAliadosYEnemigos();
      },
      error: (error) => console.error('Error al obtener los límites del país:', error)
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
        if (!aliadosIds.has(id)) { aliadosIds.add(id); this.paisesAliados.push(pais); }
      } else {
        if (!enemigosIds.has(id)) { enemigosIds.add(id); this.paisesEnemigos.push(pais); }
      }
    }
  }

  validarInputMover(event: Event) {
    const input = event.target as HTMLInputElement;
    const value = Number(input.value);
    const max = this.paisSeleccionado.cantidadTropas - 1;
    if (isNaN(value) || value < 0) { this.inputTropas = 0; input.value = '0'; }
    else if (value > max) { this.inputTropas = max; input.value = String(max); }
    else { this.inputTropas = value; }
  }

  validarInput(event: Event) {
    const input = event.target as HTMLInputElement;
    const value = Number(input.value);
    const max = this.jugador?.ejercito ?? 0;
    if (isNaN(value) || value < 0) { this.inputTropas = 0; input.value = '0'; }
    else if (value > max) { this.inputTropas = max; input.value = String(max); }
    else { this.inputTropas = value; }
  }

  cerrar() {
    this.dialogRef.close();
  }

  atacar() {
    if (!this.jugador) { console.error('Jugador no definido al atacar'); return; }
    if (this.idPaisSeleccionado === -1) { alert('Debe seleccionar un país destino válido para atacar.'); return; }

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
        this.dialogRef.close();
        this.showAtaqueModal = true;
      },
      error: (error) => console.error("Error al realizar el ataque:", error)
    });
  }

  cerrarAtaqueModal() {
    this.showAtaqueModal = false;
  }

  getEmojiDado(valor: number): string {
    return ['', '⚀', '⚁', '⚂', '⚃', '⚄', '⚅'][valor] ?? '?';
  }

  defender() {
    if (!this.jugador) { console.error('Jugador no definido al defender'); return; }
    const body = {
      idJugador: this.jugador.idJugador,
      paisesFichas: [{ idPais: this.paisSeleccionado.pais.idPais, cantidadFichas: this.inputTropas }]
    };
    this.tableroServicio.defenderPais(body).subscribe({
      next: (resultado) => {
        if (resultado) { alert("Defensa exitosa"); this.dialogRef.close(); }
        else { console.error("Error al defender el país"); }
      },
      error: (error) => console.error('Error en defenderPais:', error)
    });
  }

  reagrupar() {
    if (!this.jugador) { console.error('Jugador no definido al reagrupar'); return; }
    if (this.idPaisSeleccionado === -1) { alert('Debe seleccionar un país destino válido para reagrupar.'); return; }

    const moverFichas: MoverFichas = {
      idJugador: this.jugador.idJugador,
      idPaisOrigen: this.paisSeleccionado.pais.idPais,
      idPaisDestino: this.idPaisSeleccionado,
      cantidadFichas: this.inputTropas
    };
    this.tableroServicio.reagruparFichas(moverFichas).subscribe({
      next: (resultado) => {
        if (resultado) { alert("Reagrupación exitosa"); this.dialogRef.close(); }
        else { console.error("Error al reagrupar las fichas"); }
      },
      error: (error) => console.error('Error en reagruparFichas:', error)
    });
  }
}
