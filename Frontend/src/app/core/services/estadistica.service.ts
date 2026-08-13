import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

/** Fila del parte de campaña: estado final de un comandante + su combate. */
export interface ComandanteResumen {
  idJugador: number;
  nombre: string;
  color: string;
  avatarUrl?: string;
  esBot: boolean;

  paises: number;
  ejercitos: number;
  porcentajeMapa: number;
  eliminado: boolean;
  /** Color de quien lo eliminó; ausente si sobrevivió. */
  eliminadoPor?: string;
  ganador: boolean;

  ataquesLanzados: number;
  conquistas: number;
  defensasResistidas: number;
  tropasAbatidas: number;
  tropasPerdidas: number;
  canjesRealizados: number;
  /** conquistas / ataques, en porcentaje. */
  efectividad: number;
  /** abatidas / perdidas. */
  ratioBajas: number;
}

export interface ResumenPartida {
  idPartida: number;
  /** Jackson serializa LocalDate como [año, mes, día]. */
  fecha: number[] | string;
  turnosJugados: number;
  terminada: boolean;
  ganadorNombre?: string;
  ganadorColor?: string;
  objetivoCumplido?: string;
  tratadosFirmados: number;
  tratadosRotos: number;
  comandantes: ComandanteResumen[];
}

@Injectable({ providedIn: 'root' })
export class EstadisticaService {
  private http = inject(HttpClient);
  private apiUrl = environment.apiUrl;

  /** Parte de campaña de una partida concreta. */
  getResumenPartida(idPartida: number): Observable<ResumenPartida> {
    return this.http.get<ResumenPartida>(`${this.apiUrl}/partida/${idPartida}/resumen`);
  }
}
