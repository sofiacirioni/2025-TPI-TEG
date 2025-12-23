import {Component, Input} from '@angular/core';
import {FormBuilder, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import {AuthService, UsuarioDto} from '../../core/services/auth.service';
import {Router, RouterLink} from '@angular/router';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './inicioSesion.component.html',
  styleUrls: ['./InicioSesion.component.css']
})
export class LoginComponent {loginForm: FormGroup;

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router
  ) {
    this.loginForm = this.fb.group({
      correo: ['', [Validators.required, Validators.email]],
      contrasenia: ['', Validators.required]
    });
  }

  onSubmit(): void {
    if (this.loginForm.valid) {
      const {correo, contrasenia} = this.loginForm.value;

      this.authService.login(correo, contrasenia).subscribe({
        next: usuario => {
          if (usuario) {
            localStorage.setItem('usuario', JSON.stringify(usuario));
            console.log('✅ Usuario logueado:', usuario);
            this.router.navigate(['/principal']);
          }
        },
        error: err => {
          alert('Correo o contraseña incorrectos');
        }
      });
    }
  }

}
