import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Usuario } from '../models/class/usuario';

@Injectable({ providedIn: 'root' })
export class UsuarioService {

  private apiUrl = 'http://localhost:8080/api/v1/usuario';

  constructor(private http: HttpClient) {}

  actualizarUsuario(request: {
    correo: string;
    contraseniaActual: string;
    nuevaContrasenia: string;
    imagen: string;
  }): Observable<Usuario> {

    return this.http.put<Usuario>(
      `${this.apiUrl}/actualizar`,
      request
    );
  }

  actualizarImagen(payload: {
    correo: string;
    imagen: string;
  }): Observable<Usuario> {

    return this.http.patch<Usuario>(
      `${this.apiUrl}/imagen`,
      payload
    );
  }
}
