import {Component, OnInit} from '@angular/core';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {Usuario} from '../../core/models/interfaces/usuario.interface';
import {ApiService} from '../../core/services/registrarse.service';
import {Router, RouterLink} from '@angular/router';
import { NgIf, NgFor, CommonModule } from '@angular/common';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [ReactiveFormsModule, FormsModule, RouterLink, NgIf,NgFor,CommonModule],
  templateUrl: './registrarse.component.html',
  styleUrls: ['./registrarse.component.css']
})
export class FormUsuarioComponent implements OnInit {

  imagen: string[] =
    ['assets/members/AgosCh.png',
      'assets/members/CandeArguello.png',
      'assets/members/CandeBlanco.png',
      'assets/members/LaraHeredia.png',
      'assets/members/MeliAbril.png',
      'assets/members/SofiCirioni.png',
      'assets/members/Maxi.png']

  mostrarGaleria = false;
  imagenSeleccionadaTemporal: string | null = null;

  toggleGaleria() {
    this.mostrarGaleria = !this.mostrarGaleria;

    if (this.mostrarGaleria) {
      this.usuario.imagen = "";
      this.imagenSeleccionadaTemporal = null;
    }
  }

  seleccionarImagen(img: string) {
    this.usuario.imagen = img;
    this.mostrarGaleria = false;
  }

  usuario: Usuario = {
    nombre:"",
    apellido:"",
    correo:"",
    contrasenia:"",
    imagen:""
  };



  constructor(private apiService: ApiService ,
  private router: Router
)  {
  }
  ngOnInit(): void {

  }

  onSubmit(): void {
    const payload = {
      nombre: this.usuario.nombre,
      apellido: this.usuario.apellido,
      correo: this.usuario.correo,
      contrasenia: this.usuario.contrasenia,
      imagen: this.usuario.imagen,
    };

    console.log('Payload a enviar:', payload);

    this.apiService.crearUsuario(payload).subscribe({
      next: (data) => {
        alert("Usuario creado con éxito");
        console.log('Usuario creado:', data);
        this.router.navigate(['/iniciar-sesion']);

      },

      error: (error) => {
        if (error.error && error.error.mensaje) {
          // ✅ Mostramos el mensaje exacto que vino del backend
          alert(error.error.mensaje);
        } else {
          // Otro error no esperado
          alert('Ocurrió un error inesperado.');
        }
        console.error('Detalle del error:', error);
      }
    });
  }
}
