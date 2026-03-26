import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../core/services/auth.service';
import { NotificationService } from '../../core/services/notification.service';
import { Router, RouterLink } from '@angular/router';
import { StampComponent } from '../../shared/components/index';
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
