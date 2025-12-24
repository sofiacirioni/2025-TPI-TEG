import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {catchError, map} from 'rxjs/operators';
import {throwError} from 'rxjs';
import {SalaService} from './sala.service';
import {UsuarioDto} from './auth.service';
import { environment } from '../../../environments/environment';


@Injectable({
  providedIn: 'root'
})
export class ConfigPartidaService {
  private apiUrlConfig = `${environment.apiUrl}/jugador/sala`;

  constructor(private http: HttpClient) {

  }
  crearJugador(idSala: number, nombre: string ) {
    return this.http.post(
      `${environment.apiUrl}/jugador/sala/${idSala}/jugador`,
      {"nombre":nombre},
      { withCredentials: true }
    );
  }
  crearBot(idSala: number, idUsuario: number) {
    return this.http.get<any>(
      `${environment.apiUrl}/jugador/crearBot/${idSala}?idUsuario=${idUsuario}`,
      { withCredentials: true }
    );
  }
  crearPartida(idSala: number, idUsuario: number) {
    return this.http.post<any>(
      `${environment.apiUrl}/partida/crear/${idSala}?idUsuario=${idUsuario}`,
      { withCredentials: true }
    );
  }

  mostrarModal = false;


  abrirModal() {
    this.mostrarModal = true;
  }

  cerrarModal(event?: MouseEvent) {
    this.mostrarModal = false;

  }

  getJugadores(idSala: number) {
    return this.http.get<any[]>(`${environment.apiUrl}/jugador/sala/${idSala}/jugadores`, { withCredentials: true }).pipe(
      map(jugadores => jugadores.map(j => ({ nombreJugador: j.nombre })))
    );
  }

}
