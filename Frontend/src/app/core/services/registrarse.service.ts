import {Usuario} from '../models/interfaces/usuario.interface';
import {Observable} from 'rxjs';
import { HttpClient } from '@angular/common/http';

import {Injectable} from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class ApiService {

  private apiUrlUsuarios = 'http://localhost:8080/api/v1/usuario';

  constructor(private http: HttpClient) {}

  crearUsuario(usuario: Usuario): Observable<Usuario> {
    return this.http.post<Usuario>(`${this.apiUrlUsuarios}`, usuario);
  }
}
