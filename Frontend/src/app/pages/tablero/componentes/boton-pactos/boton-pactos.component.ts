import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, Output } from '@angular/core';

@Component({
  selector: 'app-boton-pactos',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './boton-pactos.component.html',
  styleUrl: './boton-pactos.component.scss',
})
export class BotonPactosComponent {
  @Input() esMiTurno = false;
  @Input() cantidadPactosActivos = 0;

  @Output() abrir = new EventEmitter<void>();

  onClick(): void {
    if (!this.esMiTurno) return;
    this.abrir.emit();
  }

  get ariaLabel(): string {
    const base = 'Pactos y tratados';
    if (this.cantidadPactosActivos > 0) {
      return `${base} — ${this.cantidadPactosActivos} activo${this.cantidadPactosActivos === 1 ? '' : 's'}`;
    }
    return base;
  }

  get tooltip(): string {
    return this.esMiTurno
      ? 'Pactos y tratados'
      : 'Solo podés proponer pactos en tu turno.';
  }
}
