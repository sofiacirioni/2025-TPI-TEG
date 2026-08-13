import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, interval, NEVER, Observable, startWith, Subscription, switchMap } from 'rxjs';
import { AtaqueDefenderDto, AtaqueDto, AtaqueResponseDto, CanjeTarjetasDto, EstadoPaisDto, ObjetivoProgreso, PartidaDto, TarjetaDto, UsarTarjetaEnPaisDto, VerificacionObjetivo } from '../models/interfaces/partida.interface';
import { AuthService } from './auth.service';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root',
})


export class TableroServicio {
  private apiUrl = environment.apiUrl;
  private pollingSubscription?: Subscription;
  private currentUrl = '';

  private _polling = new BehaviorSubject<boolean>(true);

  getAllLimites(id: number): Observable<EstadoPaisDto[]> {
    return this.http.get<EstadoPaisDto[]>(`${this.apiUrl}/estado/pais/limites/${id}`);
  }

  getDestinosReagrupamiento(idEstadoPaisOrigen: number, idJugador: number, idPartida: number): Observable<EstadoPaisDto[]> {
    return this.http.get<EstadoPaisDto[]>(
      `${this.apiUrl}/turno/reagrupar/destinos?idPaisOrigen=${idEstadoPaisOrigen}&idJugador=${idJugador}&idPartida=${idPartida}`
    );
  }

  startPolling(url: string) {
    this.currentUrl = url;
    this._polling.next(true);
    this.startSubscription(url);
  }

  stopPolling() {
    this._polling.next(false);
  }

  /** Dispara una consulta inmediata sin esperar el próximo tick del intervalo. */
  forceRefresh(): void {
    if (!this.currentUrl) return;
    this.obtenerPartidaByUrl(this.currentUrl).subscribe({
      next: p => this._partida.next(p),
      error: () => {}
    });
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
          ? interval(3000).pipe(startWith(0))
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

  /**
   * Paso 1 flujo dual: registra la intención de ataque. El backend emite WS
   * ATAQUE_INICIADO y el frontend aguarda la resolución por WS (salvo que
   * el defensor sea bot, en cuyo caso la resolución llega en el response).
   */
  iniciarAtaque(body: AtaqueDto): Observable<AtaqueResponseDto> {
    return this.http.put<AtaqueResponseDto>(`${this.apiUrl}/turno/ataque/iniciar`, body);
  }

  /**
   * Paso 2 flujo dual: el defensor confirma dados y dispara la resolución.
   */
  defenderAtaque(body: AtaqueDefenderDto): Observable<AtaqueResponseDto> {
    return this.http.put<AtaqueResponseDto>(`${this.apiUrl}/turno/ataque/defender`, body);
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

  obtenerProgresoObjetivo(idJugador: number): Observable<ObjetivoProgreso> {
    return this.http.get<ObjetivoProgreso>(
      `${this.apiUrl}/objetivos/progreso/${idJugador}`
    );
  }

  /** Envía un mensaje de chat; el backend lo difunde por WS a todos los jugadores de la partida. */
  enviarChat(idPartida: number, idJugador: number, texto: string): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/chat/enviar`, { idPartida, idJugador, texto });
  }

  /**
   * El jugador se retira de la campaña. Cuando todos los humanos se retiraron
   * la partida se cierra como ABANDONADA y deja de figurar como en curso.
   */
  retirarseDeLaPartida(idJugador: number): Observable<string> {
    return this.http.post(`${this.apiUrl}/jugador/${idJugador}/finalizarPartida`, {},
      { responseType: 'text' });
  }




}
