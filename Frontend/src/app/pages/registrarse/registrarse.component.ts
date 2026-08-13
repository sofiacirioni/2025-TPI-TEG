import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../core/services/auth.service';
import { NotificationService } from '../../core/services/notification.service';
import { Router, RouterLink } from '@angular/router';
import { StampComponent } from '../../shared/components/stamp/stamp.component';
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
    'assets/images/avatars/AgosCh.png',
    'assets/images/avatars/CandeArguello.png',
    'assets/images/avatars/CandeBlanco.png',
    'assets/images/avatars/LaraHeredia.png',
    'assets/images/avatars/MeliAbril.png',
    'assets/images/avatars/SofiCirioni.png',
    'assets/images/avatars/Maxi.png'
  ];

  mostrarGaleria      = false;
  showPassword        = false;
  showConfirmPassword = false;
  confirmarContrasenia = '';

  usuario = {
    usuario:     '',
    correo:      '',
    contrasenia: '',
    imagen:      ''
  };

  constructor(
    private authService: AuthService,
    private notificationService: NotificationService,
    private router: Router
  ) {}

  toggleGaleria(): void {
    this.mostrarGaleria = !this.mostrarGaleria;
    if (this.mostrarGaleria) this.usuario.imagen = '';
  }

  seleccionarImagen(img: string): void {
    this.usuario.imagen = img;
    this.mostrarGaleria = false;
  }

  togglePassword(): void        { this.showPassword = !this.showPassword; }
  toggleConfirmPassword(): void { this.showConfirmPassword = !this.showConfirmPassword; }

  onSubmit(): void {
    this.authService.register({
      usuario:     this.usuario.usuario,
      correo:      this.usuario.correo,
      contrasenia: this.usuario.contrasenia,
      imagen:      this.usuario.imagen
    }).subscribe({
      next: () => {
        this.notificationService.success('Acreditación aprobada. Acceso concedido.');
        this.router.navigate(['/principal']);
      },
      error: (err) => {
        const msg = err.status === 409 || err.status === 400
          ? 'El usuario ya existe en el registro.'
          : 'Error al procesar la solicitud.';
        this.notificationService.error(msg);
      }
    });
  }
}
