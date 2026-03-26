import { Component, inject } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { AuthService } from '../../core/services/auth.service';
import { NotificationService } from '../../core/services/notification.service';
import { Router, RouterLink } from '@angular/router';
import { StampComponent } from '../../shared/components/index';
import { SlideInDirective } from '../../shared/directives/index';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink, StampComponent, SlideInDirective],
  templateUrl: './inicio-sesion.component.html',
  styleUrls: ['./inicio-sesion.component.scss']
})
export class LoginComponent {
  private fb                   = inject(FormBuilder);
  private authService          = inject(AuthService);
  private notificationService  = inject(NotificationService);
  private router               = inject(Router);

  loginForm: FormGroup = this.fb.group({
    nombreUsuario: ['', Validators.required],
    contrasenia:   ['', Validators.required]
  });

  showPassword = false;

  togglePassword(): void {
    this.showPassword = !this.showPassword;
  }

  onSubmit(): void {
    if (this.loginForm.invalid) return;
    const { nombreUsuario, contrasenia } = this.loginForm.value;
    this.authService.login(nombreUsuario, contrasenia).subscribe({
      next: () => {
        this.notificationService.success('Acceso autorizado. Bienvenido, comandante.');
        this.router.navigate(['/principal']);
      },
      error: (err) => {
        const msg = err.status === 401
          ? 'Credenciales inválidas. Acceso denegado.'
          : 'Error de conexión con el cuartel general.';
        this.notificationService.error(msg);
      }
    });
  }
}
