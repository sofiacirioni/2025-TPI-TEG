import {Component, Input, OnChanges, OnDestroy, OnInit} from '@angular/core';
import {NgOptimizedImage} from '@angular/common';
import {JugadorDto, PartidaDto} from '../../../../core/models/interfaces/partida.interface';
import {Subscription} from 'rxjs';
import {TableroServicio} from '../../../../core/services/tablero-servicio';
import {Router} from '@angular/router';
import {AuthService} from '../../../../core/services/auth.service';



@Component({
  selector: 'app-opciones-panel',
  imports: [

  ],
  templateUrl: 'opciones-panel.component.html',
})
export class OpcionesPanelComponent implements OnChanges, OnDestroy{
  jugadorActual: JugadorDto | null = null;
  private subscription?: Subscription;
  mostrarObjetivo = false;

  @Input() partida:PartidaDto | null = null;
  constructor(private tableroServicio: TableroServicio, private authService: AuthService, private router: Router) {}

  ngOnChanges() {
    this.jugadorActual = this.partida.jugadores.find(j => j.idUsuario === this.authService.getUsuario().idUsuario)
  }

  ngOnDestroy() {
    this.subscription?.unsubscribe();
    this.tableroServicio.stopPolling();
  }

  mostrarObjetivoSecreto() {
    this.mostrarObjetivo = !this.mostrarObjetivo;
  }

  RedireccionarAyuda() {
    this.router.navigate(['/ayuda']);

  }

}
