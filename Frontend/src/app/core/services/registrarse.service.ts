import {Usuario} from '../models/interfaces/usuario.interface';
import {Observable} from 'rxjs';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';
import {Injectable} from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class ApiService {

  private apiUrlUsuarios = `${environment.apiUrl}/usuario`;

  constructor(private http: HttpClient) {}

  crearUsuario(usuario: Usuario): Observable<Usuario> {
    return this.http.post<Usuario>(`${this.apiUrlUsuarios}`, usuario);
  }
}
