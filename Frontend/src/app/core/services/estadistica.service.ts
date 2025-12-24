import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface Estadistica {
  id: number;
  colorJugador: string;
  idPartida: number;
  fecha: string;
  paisesConquistados: number;
  paisesPerdidos: number;
  porcentajeMundo: number;
  ganador: boolean;
}

@Injectable({ providedIn: 'root' })
export class EstadisticaService {
  private apiUrl = `${environment.apiUrl}/estadisticas`;

  constructor(private http: HttpClient) {}

  getEstadisticasPorUsuario(idUsuario: number): Observable<Estadistica[]> {
    return this.http.get<Estadistica[]>(`${this.apiUrl}/usuario/${idUsuario}`);
  }
}
