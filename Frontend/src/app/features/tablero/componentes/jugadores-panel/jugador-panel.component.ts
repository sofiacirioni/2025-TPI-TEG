import {Component, Input, OnChanges, SimpleChanges, OnInit} from '@angular/core';
import {JugadorInfoComponent} from '../jugador-info/jugador-info.component';
import {JugadorDto} from '../../../../core/models/interfaces/partida.interface';
import {UsuarioDto} from '../../../../core/services/auth.service';


@Component({
  selector: 'app-jugador-panel',
  imports: [
    JugadorInfoComponent
  ],
  templateUrl: './jugador-panel.component.html',
})

export class JugadorPanelComponent {
  @Input() jugadores: JugadorDto[] = []
  @Input() usuarios: UsuarioDto[] = []



}
