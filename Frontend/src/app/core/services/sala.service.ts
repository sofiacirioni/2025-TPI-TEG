import { Injectable } from '@angular/core';
import {HttpClient, HttpParams} from '@angular/common/http';
import {Observable, of, pipe, throwError} from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import {Sala, SalaGet} from '../models/interfaces/sala.interface';
import { environment } from '../../../environments/environment';




@Injectable({
  providedIn: 'root'
})
export class SalaService {
  private apiUrlSala = `${environment.apiUrl}/sala`;
  private apiUrlPartida = `${environment.apiUrl}/partida`;


  constructor(private http: HttpClient) {
  }
  private salaActual: Sala | null = null;

  setSala(sala: Sala) {
    this.salaActual = sala;
    localStorage.setItem('salaActual', JSON.stringify(sala));
    console.log('Sala guardada en localStorage:', sala);
  }

  getSala(): Sala | null {
    if (!this.salaActual) {
      const salaString = localStorage.getItem('salaActual');
      console.log('Sala recuperada de localStorage:', salaString);
      if (salaString) {
        this.salaActual = JSON.parse(salaString);
      }
    }
    return this.salaActual;
  }
  crearSala(data: any): Observable<Sala> {
    return this.http.post<Sala>(`${this.apiUrlSala}/crear`, data, { withCredentials: true })
  .pipe(
      catchError(err => {
        console.error('Sala fallida', err);
        return throwError(() => err);
      })
    );
  }

  unirseSala(url: string): Observable<Sala> {
    let params = new HttpParams().set('url', url);
    return this.http.get<Sala>(`${this.apiUrlSala}/url`, { params, withCredentials: true }).pipe(
      catchError(err => {
        console.error('Sala fallida', err);
        return throwError(() => err);
      })
    );
  }

  unirsePartida(url: string, idUsuario: number): Observable<Sala> {
    const params = new HttpParams()
      .set('url', url)
      .set('idUsuario', idUsuario.toString());

    return this.http.get<Sala>(`${this.apiUrlPartida}/sala`, {
      params,
      withCredentials: true
    }).pipe(
      catchError(err => {
        console.error('Sala fallida', err);
        return throwError(() => err);
      })
    );
  }

}
