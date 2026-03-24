import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Usuario } from '../../core/models/interfaces/usuario.interface';
import { ApiService } from '../../core/services/registrarse.service';
import { Router, RouterLink } from '@angular/router';
import { StampComponent } from '../../components/index';
import { SlideInDirective } from '../../shared/directives/index';

@Component({
  selector: 'app-registro',
  standalone: true,
  imports: [FormsModule, RouterLink, StampComponent, SlideInDirective],
  templateUrl: './registrarse.component.html',
  styleUrls: ['./registrarse.component.scss']
})
export class FormUsuarioComponent {

  imagen: string[] = [
    'assets/images/members/AgosCh.png',
    'assets/images/members/CandeArguello.png',
    'assets/images/members/CandeBlanco.png',
    'assets/images/members/LaraHeredia.png',
    'assets/images/members/MeliAbril.png',
    'assets/images/members/SofiCirioni.png',
    'assets/images/members/Maxi.png'
  ];

  mostrarGaleria = false;
  showPassword = false;
  showConfirmPassword = false;
  confirmarContrasenia = '';

  usuario: Usuario = {
    nombre: '',
    correo: '',
    contrasenia: '',
    imagen: ''
  };

  constructor(
    private apiService: ApiService,
    private router: Router
  ) {}

  toggleGaleria(): void {
    this.mostrarGaleria = !this.mostrarGaleria;
    if (this.mostrarGaleria) {
      this.usuario.imagen = '';
    }
  }

  seleccionarImagen(img: string): void {
    this.usuario.imagen = img;
    this.mostrarGaleria = false;
  }

  togglePassword(): void {
    this.showPassword = !this.showPassword;
  }

  toggleConfirmPassword(): void {
    this.showConfirmPassword = !this.showConfirmPassword;
  }

  onSubmit(): void {
    const payload = {
      nombre: this.usuario.nombre,
      correo: this.usuario.correo,
      contrasenia: this.usuario.contrasenia,
      imagen: this.usuario.imagen,
    };

    this.apiService.crearUsuario(payload).subscribe({
      next: () => {
        alert('Usuario creado con éxito');
        this.router.navigate(['/iniciar-sesion']);
      },
      error: (error) => {
        if (error.error && error.error.mensaje) {
          alert(error.error.mensaje);
        } else {
          alert('Ocurrió un error inesperado.');
        }
      }
    });
  }
}
