import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Usuario } from '../models/class/usuario';
import { environment } from '../../../environments/environment';

/** Una línea del registro de campañas: enlaza al parte de esa partida. */
export interface CampaniaHistorial {
  idPartida: number;
  /** Jackson serializa LocalDate como [año, mes, día]. */
  fecha: number[] | string;
  /** Sólo las terminadas suman al acumulado. */
  terminada: boolean;
  /** EN_JUEGO, PAUSADA, TERMINADA o ABANDONADA. */
  estado?: string;
  /** 1 = vencedor. 0 si la campaña no llegó a un desenlace. */
  puesto: number;
  comandantes: number;
  turnosJugados: number;
  color?: string;
  ganador: boolean;
  eliminado: boolean;
  ganadorNombre?: string;
}

/** Hoja de servicios acumulada: el histórico de todas las campañas concluidas. */
export interface HistorialComandante {
  idUsuario: number;
  usuario: string;
  correo: string;
  imagen: string;
  /** LocalDateTime → [año, mes, día, hora, minuto, segundo, nanos]. */
  fechaAlta?: number[] | string;
  /** Color con el que más veces jugó. */
  divisaHabitual?: string;

  campaniasLibradas: number;
  victorias: number;
  segundosPuestos: number;
  terceroPuestos: number;
  tasaVictoria: number;

  ataquesLanzados: number;
  conquistas: number;
  efectividad: number;
  defensasResistidas: number;
  tropasAbatidas: number;
  tropasPerdidas: number;
  ratioBajas: number;
  canjesRealizados: number;
  comandantesAbatidos: number;

  tratadosFirmados: number;
  /** Tratados rotos por este usuario. */
  tratadosRotos: number;

  campanias: CampaniaHistorial[];
}

@Injectable({ providedIn: 'root' })
export class UsuarioService {

  private apiUrl = `${environment.apiUrl}/usuario`;

  constructor(private http: HttpClient) {}

  getHistorial(idUsuario: number): Observable<HistorialComandante> {
    return this.http.get<HistorialComandante>(`${this.apiUrl}/${idUsuario}/historial`);
  }

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
