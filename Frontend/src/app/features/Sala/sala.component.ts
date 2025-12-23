
(window as any).global = window;
import {Router, RouterLink} from '@angular/router';
import {FormBuilder, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import {SalaService} from '../../core/services/sala.service';
import {Component, ViewEncapsulation} from '@angular/core';
import {NgFor, NgIf} from '@angular/common';
import {Sala, SalaGet} from '../../core/models/interfaces/sala.interface';
import {AuthService} from '../../core/services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink, NgIf],
  templateUrl: './sala.component.html',
  styleUrls: ['./sala.component.css'],
  encapsulation: ViewEncapsulation.None
})
export class SalaComponent {
  crearSala!: FormGroup;
  unirseSala!: FormGroup;
  unirsePartida!: FormGroup;


  constructor(
    private router: Router,
    private fb: FormBuilder,
    private salaService: SalaService,
  private authService: AuthService

) {
    this.crearSala = this.fb.group({
      nombreSala: ['', Validators.required]
    });
    this.unirseSala = this.fb.group({
      url: ['', Validators.required]
    })
    this.unirsePartida=this.fb.group({
      url: ['', Validators.required]
    }

  )
  }
  mostrarModal = false;

  mostrarModalUnirse = false;

  mostrarModalUnirsePartida=false;

  abrirModal() {
    this.mostrarModal = true;
  }

  abrirModalUnirse() {
    this.mostrarModalUnirse = true;
  }
  abrirModalUnirsePartida()
  {
    this.mostrarModalUnirsePartida = true;

  }
  cerrarModal(event?: MouseEvent) {
    this.mostrarModal = false;
    this.mostrarModalUnirse = false;
    this.mostrarModalUnirsePartida = false;

  }

  onCrearSala(): void {
    if (this.crearSala.invalid) {
      alert('El nombre de la sala es obligatorio.');
      return;
    }
    const payload = this.crearSala.value;
    console.log('Payload a enviar:', payload);

    this.salaService.crearSala(payload).subscribe({
      next: (data: Sala) => {
        this.salaService.setSala(data);
        this.router.navigate(['/configPartida']);
      },
      error: (error) => {
        if (error.error && error.error.mensaje) {
          alert(error.error.mensaje);
        } else {
          // Otro error no esperado
          alert('Ocurrió un error inesperado.');
        }
        console.error('Detalle del error:', error);
      }
    });
  }

  onUnirseSala(): void {
    if (this.unirseSala.invalid) {
      alert('El url de la sala es obligatorio.');
      return;
    }

    const url = this.unirseSala.value.url;

    this.salaService.unirseSala(url).subscribe({
      next: (data: Sala) => {
        console.log('Uniéndose a la sala:', data);

        localStorage.setItem('urlSala', url);

        this.salaService.setSala(data);

        this.router.navigate(['/configPartida']);
      },
      error: (error) => {
        if (error.error && error.error.mensaje) {
          alert(error.error.mensaje);
        } else {
          alert('Ocurrió un error inesperado.');
        }
        console.error('Detalle del error:', error);
      }
    });
  }
  onUnirsePartida(): void {
    if (this.unirsePartida.invalid) {
      alert('La URL es obligatoria');
      return;
    }

    const url = this.unirsePartida.value.url;
    const usuarioActual = this.authService.getUsuario();

    if (!usuarioActual || !usuarioActual.idUsuario) {
      alert('No hay usuario logueado.');
      return;
    }

    this.salaService.unirsePartida(url, usuarioActual.idUsuario).subscribe({
      next: (data: any) => {
        const sala = data.configuracion;

        if (!sala) {
          alert('No se recibió configuración de sala válida.');
          return;
        }

        this.salaService.setSala(sala);
        this.router.navigate(['/juego', url]);
      },
      error: error => this.manejarError(error)
    });
  }

  private manejarError(error: any) {
    if (error.error?.mensaje) {
      alert(error.error.mensaje);
    } else if (error.error?.error) {
      alert(error.error.error);
    } else {
      alert('Ocurrió un error inesperado.');
    }
    console.error('Detalle del error:', error);
  }
}
