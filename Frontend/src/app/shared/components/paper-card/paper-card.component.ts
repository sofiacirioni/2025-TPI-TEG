import { Component, Input } from '@angular/core';

@Component({
  selector: 'app-paper-card',
  standalone: true,
  templateUrl: './paper-card.component.html',
  styleUrls: ['./paper-card.component.scss'],
  host: { '[style.--tilt]': 'tilt + "deg"' }
})
export class PaperCardComponent {
  @Input() tilt: number = 0;
  @Input() size: 'sm' | 'md' | 'lg' = 'md';
  @Input() elevated: boolean = false;
}