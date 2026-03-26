import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';

export type PlayerColor = 'rojo' | 'azul' | 'naranja' | 'purpura' | 'verde' | 'dorado';

const PLAYER_SVG: Record<PlayerColor, string> = {
  rojo:    '/assets/vectors/red-player.svg',
  azul:    '/assets/vectors/blue-player.svg',
  naranja: '/assets/vectors/orange-player.svg',
  purpura: '/assets/vectors/purple-player.svg',
  verde:   '/assets/vectors/green-player.svg',
  dorado:  '/assets/vectors/gold-player.svg',
};

@Component({
  selector: 'app-player-token',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './player-token.component.html',
  styleUrls: ['./player-token.component.scss']
})
export class PlayerTokenComponent {
  @Input() player!: PlayerColor;
  @Input() count: number = 0;
  @Input() size: 'sm' | 'md' | 'lg' = 'md';

  get tokenSrc(): string {
    return PLAYER_SVG[this.player] ?? '';
  }
}