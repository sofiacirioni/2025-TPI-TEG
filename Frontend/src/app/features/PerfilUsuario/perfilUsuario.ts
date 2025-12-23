import {Component, OnInit} from '@angular/core';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {Router, RouterLink} from '@angular/router';
import {CommonModule, NgForOf, NgIf} from '@angular/common';
import {Usuario} from '../../core/models/interfaces/usuario.interface';
import {UsuarioService} from '../../core/services/perfilUsuarioService';
import {AuthService} from '../../core/services/auth.service';


@Component({
  selector: 'app-login',
  standalone: true,
  imports: [ReactiveFormsModule, FormsModule, CommonModule, RouterLink, NgForOf, NgIf],
  templateUrl: './perfilUsuario.html',
  styleUrls: ['./perfilUsuario.css']
})
export class PerfilUsuario implements OnInit {
  imagen: string[] =
    ['assets/miembros/AgosCh.png',
      'assets/miembros/CandeArguello.png',
      'assets/miembros/CandeBlanco.jpeg',
      'assets/miembros/LaraHeredia.png',
      'assets/miembros/MeliAbril.png',
      'assets/miembros/SofiCirioni.png',
      'assets/miembros/Maxi.png'];

  seleccionarImagen(img: string): void {
    this.mostrarGaleria = false;
    this.usuarioService
      .actualizarImagen({ correo: this.usuario.correo, imagen: img })
      .subscribe({
        next: updated => {

          this.usuario.imagen = updated.imagen;
          const stored = this.authService.getUsuario();
          if (stored) {
            stored.imagen = updated.imagen;
            localStorage.setItem('usuario', JSON.stringify(stored));
          }
          alert('Imagen actualizada con éxito.');
        },
        error: () => {
          alert('Error al actualizar imagen.');
        }
      });
  }

  usuario: Usuario = {
    nombre:"",
    apellido:"",
    correo:"",
    contrasenia:"",
    imagen:""
  };
  contraseniaActual = '';
  nuevaContrasenia = '';
  mostrarGaleria = false;
  errorContraseniaIgual = '';
  errorContraseniaCorta = '';

  constructor(
    private router: Router,
    private usuarioService: UsuarioService,
    private authService: AuthService) {}

  ngOnInit(): void {
    const user = this.authService.getUsuario();
    if (user) {
      this.usuario.correo = user.correo;
      this.usuario.imagen = user.imagen;
    } else {
      // Redirigir si no está logueado
      this.router.navigate(['/perfil-usuario']);
    }
  }

  cerrarSesion(): void {
    this.authService.logout();
    this.router.navigate(['/iniciar-sesion']);
  }

  cambiarContrasenia(): void {
    this.errorContraseniaIgual = '';
    this.errorContraseniaCorta = '';

    if (!this.usuario.correo) {
      alert('Correo de usuario no disponible.');
      return;
    }

    if (this.contraseniaActual === this.nuevaContrasenia) {
      this.errorContraseniaIgual = 'La nueva contraseña no puede ser igual a la actual.';
      return;
    }

    if (this.nuevaContrasenia.length < 6) {
      this.errorContraseniaCorta = 'La nueva contraseña debe tener al menos 6 caracteres.';
      return;
    }

    this.usuarioService.actualizarUsuario({
      correo: this.usuario.correo,
      contraseniaActual: this.contraseniaActual,
      nuevaContrasenia: this.nuevaContrasenia,
      imagen: this.usuario.imagen
    }).subscribe({
      next: res => {
        alert('Contraseña actualizada con éxito.');
        this.contraseniaActual = '';
        this.nuevaContrasenia = '';
      },
      error: err => {
        alert('Error al actualizar. Verifica los datos.');
        console.error(err);
      }
    });
  }
}
