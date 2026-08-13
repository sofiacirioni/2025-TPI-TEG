import { Component } from '@angular/core';
import { NgOptimizedImage } from '@angular/common';
import { PaperCardComponent } from '../../shared/components/paper-card/paper-card.component';
import { StampComponent } from '../../shared/components/stamp/stamp.component';

@Component({
  selector: 'app-creditos',
  standalone: true,
  imports: [NgOptimizedImage, PaperCardComponent, StampComponent],
  templateUrl: './creditos.component.html',
  styleUrl: './creditos.component.scss'
})
export class CreditosComponent {
  volver() {
    window.history.back();
  }
}