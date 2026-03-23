import { Component, inject } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { AuthService } from '../../core/services/auth.service';
import { Router, RouterLink } from '@angular/router';
import { StampComponent } from '../../components/index';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink, StampComponent],
  templateUrl: './inicio-sesion.component.html',
  styleUrls: ['./inicio-sesion.component.scss']
})
export class LoginComponent {
  private fb = inject(FormBuilder);
  private authService = inject(AuthService);
  private router = inject(Router);

  loginForm: FormGroup = this.fb.group({
    nombreUsuario: ['', Validators.required],
    contrasenia:   ['', Validators.required]
  });

  showPassword = false;

  togglePassword(): void {
    this.showPassword = !this.showPassword;
  }

  onSubmit(): void {
    if (this.loginForm.valid) {
      const { nombreUsuario, contrasenia } = this.loginForm.value;
      this.authService.login(nombreUsuario, contrasenia).subscribe({
        next: usuario => {
          localStorage.setItem('usuario', JSON.stringify(usuario));
          this.router.navigate(['/principal']);
        },
        error: () => alert('Usuario o contraseña incorrectos')
      });
    }
  }
}
