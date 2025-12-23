import { Component } from '@angular/core';
import {NgOptimizedImage} from '@angular/common';
import {Router, RouterLink} from "@angular/router";

@Component({
  selector: 'app-creditos',
  standalone: true,

  imports: [
    NgOptimizedImage,
    RouterLink
  ],
  templateUrl: './creditos.component.html',
  styleUrl: './creditos.component.css'
})
export class CreditosComponent {
  private router: Router
  volver() {
    window.history.back()
  }
}
