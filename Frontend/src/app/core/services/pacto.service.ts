import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { PactoDto, ProponerPactoDto } from '../models/interfaces/pacto.interface';

@Injectable({ providedIn: 'root' })
export class PactoService {
  private readonly base = `${environment.apiUrl}/pacto`;

  constructor(private http: HttpClient) {}

  proponer(dto: ProponerPactoDto): Observable<PactoDto> {
    return this.http.post<PactoDto>(`${this.base}/proponer`, dto);
  }

  aceptar(idPacto: number, idJugador: number): Observable<PactoDto> {
    return this.http.post<PactoDto>(`${this.base}/${idPacto}/aceptar`, null, {
      params: { idJugador: String(idJugador) },
    });
  }

  rechazar(idPacto: number, idJugador: number): Observable<PactoDto> {
    return this.http.post<PactoDto>(`${this.base}/${idPacto}/rechazar`, null, {
      params: { idJugador: String(idJugador) },
    });
  }

  romper(idPacto: number, idJugador: number): Observable<PactoDto> {
    return this.http.post<PactoDto>(`${this.base}/${idPacto}/romper`, null, {
      params: { idJugador: String(idJugador) },
    });
  }

  listarActivos(idPartida: number): Observable<PactoDto[]> {
    return this.http.get<PactoDto[]>(`${this.base}/partida/${idPartida}/activos`);
  }
}
