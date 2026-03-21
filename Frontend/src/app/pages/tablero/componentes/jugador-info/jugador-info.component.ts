import {Component, Input, OnChanges, SimpleChanges, OnInit} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {CommonModule} from '@angular/common';
import {JugadorDto} from '../../../../core/models/interfaces/partida.interface';
import {AuthService, UsuarioDto} from '../../../../core/services/auth.service';

@Component({
  standalone: true,
  selector: 'app-jugador-info',
  templateUrl: './jugador-info.component.html',
  imports: [CommonModule]
  ,
})

export class JugadorInfoComponent {
  @Input() jugador: JugadorDto | null = null;
  @Input() imagen: string[] =
    ['assets/miembros/AgosCh.png',
      'assets/miembros/CandeArguello.png',
      'assets/miembros/CandeBlanco.jpeg',
      'assets/miembros/LaraHeredia.png',
      'assets/miembros/MeliAbril.png',
      'assets/miembros/SofiCirioni.png',
      'assets/miembros/Maxi.png']

  constructor(
    private authService: AuthService) {

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

  obtenerAvatar(idJugador: number): string {
    if (!idJugador || this.imagen.length === 0) {
      return 'assets/miembros/default.png';
    }
    const index = idJugador % this.imagen.length;
    return this.imagen[index];
  }
}
