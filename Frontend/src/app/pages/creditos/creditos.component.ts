import { Component } from '@angular/core';
import { NgOptimizedImage } from '@angular/common';
import { PaperCardComponent } from '../../components/paper-card/paper-card.component';
import { StampComponent } from '../../components/stamp/stamp.component';

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