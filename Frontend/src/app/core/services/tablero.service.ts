import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, interval, NEVER, Observable, startWith, Subscription, switchMap } from 'rxjs';
import { AtaqueDto, AtaqueResponseDto, CanjeTarjetasDto, EstadoPaisDto, PartidaDto, TarjetaDto, UsarTarjetaEnPaisDto, VerificacionObjetivo } from '../models/interfaces/partida.interface';
import { AuthService } from './auth.service';
import { log } from '@angular-devkit/build-angular/src/builders/ssr-dev-server';
import { map } from 'rxjs/operators';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root',
})


export class TableroServicio {
  private apiUrl = environment.apiUrl;
  private pollingSubscription?: Subscription;

  private _polling = new BehaviorSubject<boolean>(true);

  getAllLimites(id: number): Observable<EstadoPaisDto[]> {
    return this.http.get<EstadoPaisDto[]>(`${this.apiUrl}/estado/pais/limites/${id}`);
  }

  startPolling(url: string) {
    this._polling.next(true)
    this.startSubscription(url);
  }

  stopPolling() {
    this._polling.next(false)

  }

  //Subject
  private _partida = new BehaviorSubject<PartidaDto | null>(null)

  //Observables públicos
  readonly partidaObservable: Observable<PartidaDto | null> = this._partida.asObservable();

  constructor(private http: HttpClient,
    private authService: AuthService,) {
  }

  cleanInterval() {
    this.pollingSubscription?.unsubscribe()
  }

  startSubscription(url: string) {
    this.cleanInterval();
    this.getMatchById(url);
  }

  private getMatchById(url: string) {
    this.pollingSubscription = this._polling.pipe(
      switchMap(isOn =>
        isOn
          ? interval(1000).pipe(startWith(0)) //
          : NEVER
      ),
      switchMap(() => this.obtenerPartidaByUrl(url))
    ).subscribe({
      next: (partida: PartidaDto) => {
        this._partida.next(partida);
        // console.log(partida);
        // console.log("polling activo!");
      },
      error: err => console.error("Error en polling:", err)
    });
  }

  //obtener partida
  obtenerPartidaByUrl(url: string): Observable<PartidaDto> {
    return this.http.get<PartidaDto>(`${this.apiUrl}/partida/url/${url}?idUsuario=${this.authService.getUsuario().idUsuario}`);
  }

  // Defenser / Agregar
  defenderPais(body: any): Observable<boolean> {
    return this.http.put<boolean>(`${this.apiUrl}/turno/defender`, body
    );
  }

  // MOVER TROPAS / REAGRUPAR
  reagruparFichas(body: any): Observable<boolean> {
    return this.http.put<boolean>(`${this.apiUrl}/turno/reagrupar`, body
    );
  }

  atacarPais(body: AtaqueDto): Observable<AtaqueResponseDto> {
    return this.http.put<AtaqueResponseDto>(`${this.apiUrl}/turno/ataque`, body
    );
  }

  // Cambiar de turno
  cambiarTurno(idPartida: number): Observable<boolean> {
    return this.http.put<boolean>(`${this.apiUrl}/turno/fase?idPartida=${idPartida}`, {});
  }

  obtenerTarjeta(idJugador: number, idPartida: number): Observable<TarjetaDto> {
    return this.http.put<TarjetaDto>(
      `${this.apiUrl}/turno/tarjeta/obtener?idJugador=${idJugador}&idPartida=${idPartida}`,
      {}
    );
  }

  usarTarjetaEnPais(dto: UsarTarjetaEnPaisDto): Observable<void> {
    return this.http.put<void>(`${this.apiUrl}/turno/usarTarjeta`, dto);
  }

  realizarCanje(canjeDto: CanjeTarjetasDto): Observable<number> {
    return this.http.put<number>(`${this.apiUrl}/turno/canje/realizar`, canjeDto);
  }


  consultarMotivoGanador(idJugador: number): Observable<VerificacionObjetivo> {
    return this.http.put<VerificacionObjetivo>(
      `${this.apiUrl}/turno/verificarGanador?idJugador=${idJugador}`,
      {}
    );
  }




}
