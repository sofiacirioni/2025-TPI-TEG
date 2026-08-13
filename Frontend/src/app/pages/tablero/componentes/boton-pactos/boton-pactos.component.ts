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

  /**
   * Abre siempre, sea o no tu turno. Lo que el turno restringe es **proponer**,
   * y de eso ya se encarga el overlay deshabilitando su propio botón. Con el
   * candado acá, romper un tratado —que no depende del turno— era inalcanzable
   * casi todo el tiempo, porque la única puerta a la lista estaba cerrada.
   */
  onClick(): void {
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
      : 'Pactos y tratados — proponer, sólo en tu turno.';
  }
}
