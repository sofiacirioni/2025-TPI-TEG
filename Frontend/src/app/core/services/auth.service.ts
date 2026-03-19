import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import {Observable, of, tap, throwError} from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import {Usuario} from '../models/interfaces/usuario.interface';
import { environment } from '../../../environments/environment';

export interface Credencial {
  correo: string;
  contrasenia: string;
}

export interface UsuarioDto {
  idUsuario: number;
  correo: string;
  imagen: string;

}

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private apiUrl = `${environment.apiUrl}/usuario`;

  constructor(private http: HttpClient) {}

  login(nombreUsuario: string, contrasenia: string): Observable<Usuario> {
    const credencial: Credencial = { correo: nombreUsuario, contrasenia };
    return this.http.post<Usuario>(`${this.apiUrl}/login`, credencial, { withCredentials: true }).pipe(
      //tap(usuario => {
      //   localStorage.setItem('usuario', JSON.stringify(usuario));
      //}),
      catchError(err => {
        console.error('Login fallido', err);
        return throwError(() => err);
      })
    );
  }

  logout(): void {
    localStorage.removeItem('usuario');
  }

  isLoggedIn(): boolean {
    return !!localStorage.getItem('usuario');
  }

  getUsuario(): UsuarioDto | null {
    const stored = localStorage.getItem('usuario');
    return stored ? JSON.parse(stored) : null;
  }
}
