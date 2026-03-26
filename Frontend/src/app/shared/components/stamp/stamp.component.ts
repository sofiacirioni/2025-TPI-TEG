import { Component, Input } from '@angular/core';

@Component({
  selector: 'app-stamp',
  standalone: true,
  templateUrl: './stamp.component.html',
  styleUrls: ['./stamp.component.scss']
})
export class StampComponent {
  @Input() type: 'top-secret' | 'clasificado' | 'teg-stamp' = 'top-secret';
  @Input() rotation: number = -8;
  @Input() opacity: number = 0.72;
  @Input() blend: boolean = false;

  get stampSrc(): string {
    switch (this.type) {
      case 'clasificado': return '/assets/vectors/clasificado-stamp.svg';
      case 'teg-stamp':   return '/assets/vectors/teg-stamp.svg';
      default:            return '/assets/vectors/top-secret-stamp.svg';
    }
  }
}