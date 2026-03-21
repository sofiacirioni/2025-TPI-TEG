import { Component } from '@angular/core';
import {Router, RouterLink} from '@angular/router';

@Component({
  selector: 'app-principal',
  standalone: true,
  templateUrl: './principal.component.html',
  imports: [
    RouterLink
  ],
  styleUrls: ['./principal.component.css']
})
export class PrincipalComponent {
  constructor(private router: Router) {}


  verificarYJugar() {
    const usuarioStr = localStorage.getItem('usuario');
    console.log('usuarioStr:', usuarioStr);

    if (!usuarioStr) {
      console.log('No hay usuario en localStorage, redirigiendo a registrarse');
      this.router.navigate(['/registrarse']);
      return;
    }

    try {
      const usuario = JSON.parse(usuarioStr);
      console.log('usuario parseado:', usuario);

      if (usuario && typeof usuario.correo === 'string' && usuario.correo.trim() !== '') {
        console.log('Usuario válido, redirigiendo a sala');
        this.router.navigate(['/entrarCrearSala']);
      } else {
        console.log('Usuario inválido, redirigiendo a registrarse');
        this.router.navigate(['/registrarse']);
      }
    } catch (error) {
      console.log('Error parseando usuario, redirigiendo a registrarse');
      this.router.navigate(['/registrarse']);
    }
  }
  verificarUsuario(): void {
    const usuarioStr = localStorage.getItem('usuario');

    if (!usuarioStr) {
      // No hay sesión iniciada → ir a registro
      this.router.navigate(['/registrarse']);
      return;
    }

    try {
      const usuario = JSON.parse(usuarioStr);

      // Validamos que tenga correo válido
      if (usuario && typeof usuario.correo === 'string' && usuario.correo.trim() !== '') {
        this.router.navigate(['/perfilUsuario']);
      } else {
        this.router.navigate(['/registrarse']);
      }
    } catch {
      this.router.navigate(['/registrarse']);
    }
  }

  RedireccionarCreditos() {
    this.router.navigate(['/creditos']);

  }

  RedireccionarAyuda() {
    this.router.navigate(['/ayuda']);

  }}
