import { Component, ElementRef, OnDestroy, OnInit, ViewChild, inject } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { CommonModule, DecimalPipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { TableroServicio } from '../../core/services/tablero.service';
import { MapaSvgComponent, PaisClickEvent } from './componentes/mapa-svg/mapa-svg.component';
import {
  AtaqueDto,
  AtaqueResponseDto,
  CanjeTarjetasDto,
  EstadoPaisDto,
  EstadoTarjetaDto,
  EstadoPartida,
  FaseTurno,
  JugadorDto,
  MoverFichas,
  PartidaDto,
  TurnoDto,
  UsarTarjetaEnPaisDto,
  VerificacionObjetivo,
} from '../../core/models/interfaces/partida.interface';
import { AuthService } from '../../core/services/auth.service';
import { NotificationService } from '../../core/services/notification.service';

interface ChatMensaje {
  actor: string;
  texto: string;
  colorSolido: string;
}

interface HistorialItem {
  texto: string;
  tipo: 'ataque' | 'ok' | 'normal';
}

@Component({
  selector: 'app-tablero',
  standalone: true,
  imports: [MapaSvgComponent, CommonModule, FormsModule, DecimalPipe],
  templateUrl: 'tablero.component.html',
  styleUrl: 'tablero.component.scss',
})
export class TableroComponent implements OnInit, OnDestroy {

  private tableroServicio = inject(TableroServicio);
  private route = inject(ActivatedRoute);
  private authService = inject(AuthService);
  private router = inject(Router);
  private notificationService = inject(NotificationService);

  // ── Estado de partida ──────────────────────────────────────
  url!: string;
  partida!: PartidaDto;
  esMiTurno = false;
  jugadorId = 0;
  jugadorConsquisto: JugadorDto | null = null;

  // ── Tarjetas ───────────────────────────────────────────────
  cartasJugador: EstadoTarjetaDto[] = [];
  puedeCanjear = false;
  combinacionesPosibles: EstadoTarjetaDto[][] = [];
  combinacionSeleccionada: number | null = null;
  showCanjeModal = false;

  // ── Objetivo secreto ───────────────────────────────────────
  objetivoVisible = false;

  // ── Modal de país ──────────────────────────────────────────
  paisModalSeleccionado: EstadoPaisDto | null = null;
  modalX = 0;
  modalY = 0;
  paisesLimitrofes: EstadoPaisDto[] = [];
  paisesEnemigos: EstadoPaisDto[] = [];
  paisesAliados: EstadoPaisDto[] = [];
  idPaisDestinoModal = -1;
  inputTropasModal = 0;
  ataqueResultado?: AtaqueResponseDto;
  showAtaqueResultado = false;
  paisOrigenNombreModal = '';
  paisDestinoNombreModal = '';

  // ── Zoom / Pan ─────────────────────────────────────────────
  scale = 1;
  translateX = 0;
  translateY = 0;
  isDragging = false;
  private lastMouseX = 0;
  private lastMouseY = 0;

  // ── Timer de turno ─────────────────────────────────────────
  tiempoRestante = 120;
  private timerInterval: ReturnType<typeof setInterval> | null = null;
  private lastTurnoActual = -1;

  // ── Reloj decorativo ───────────────────────────────────────
  horaActual = new Date();
  private clockInterval: ReturnType<typeof setInterval> | null = null;
  @ViewChild('relojObj') private relojObj?: ElementRef<HTMLObjectElement>;
  private svgDoc: Document | null = null;
  // Ángulos base de las agujas en el SVG original (medidos via getBBox)
  private readonly MINUTE_HAND_BASE_DEG  = 135;
  private readonly HOUR_HAND_BASE_DEG    = 100;
  private readonly SECOND_HAND_BASE_DEG  = 0;

  // ── Historial de operaciones ───────────────────────────────
  historial: HistorialItem[] = [];

  // ── Chat ───────────────────────────────────────────────────
  chatMensajes: ChatMensaje[] = [];
  chatInput = '';

  // ── Getters ────────────────────────────────────────────────
  private getTurnoActual(): TurnoDto | undefined {
    return this.partida?.turnos?.find(t => Number(t.nroTurno) === Number(this.partida.turnoActual));
  }

  get faseActual(): string {
    return this.getTurnoActual()?.fase ?? '';
  }

  get jugadorActualTurno(): JugadorDto | undefined {
    const turno = this.getTurnoActual();
    return this.partida?.jugadores?.find(j => j.idJugador === turno?.idJugador);
  }

  get numeroTurno(): number {
    return this.partida?.turnoActual ?? 0;
  }

  get jugadorUsuario(): JugadorDto | undefined {
    const usuario = this.authService.getCurrentUser();
    return this.partida?.jugadores?.find(j => j.idUsuario === usuario?.idUsuario);
  }

  get ejercitosDisponibles(): number {
    return this.jugadorUsuario?.ejercito ?? 0;
  }

  get objetivoTexto(): string {
    return this.jugadorUsuario?.objetivo?.descripcion ?? '';
  }

  get timerDisplay(): string {
    const m = Math.floor(this.tiempoRestante / 60);
    const s = this.tiempoRestante % 60;
    return `${m.toString().padStart(2, '0')} : ${s.toString().padStart(2, '0')}`;
  }

  get horasDeg(): number {
    const h = this.horaActual.getHours() % 12;
    return (h + this.horaActual.getMinutes() / 60) * 30;
  }
  get minutosDeg(): number  { return this.horaActual.getMinutes() * 6; }
  get segundosDeg(): number { return this.horaActual.getSeconds() * 6; }

  onRelojLoaded(): void {
    const obj = this.relojObj?.nativeElement;
    if (obj) {
      this.svgDoc = (obj as HTMLObjectElement).contentDocument;
      this.updateClockHands();
    }
  }

  private updateClockHands(): void {
    if (!this.svgDoc) return;
    const cx = '576.99', cy = '576.99';
    const minuteHand = this.svgDoc.getElementById('aguja-minutos');
    const hourHand   = this.svgDoc.getElementById('aguja-horas');
    const secondHand = this.svgDoc.getElementById('aguja-segundos');
    if (minuteHand) {
      minuteHand.setAttribute('transform',
        `rotate(${this.minutosDeg - this.MINUTE_HAND_BASE_DEG}, ${cx}, ${cy})`);
    }
    if (hourHand) {
      hourHand.setAttribute('transform',
        `rotate(${this.horasDeg - this.HOUR_HAND_BASE_DEG}, ${cx}, ${cy})`);
    }
    if (secondHand) {
      secondHand.setAttribute('transform',
        `rotate(${this.segundosDeg - this.SECOND_HAND_BASE_DEG}, ${cx}, ${cy})`);
    }
  }

  get mapTransform(): string {
    return `translate(${this.translateX}px, ${this.translateY}px) scale(${this.scale})`;
  }

  // Modal pais helpers
  get esSuyoPaisModal(): boolean {
    return this.paisModalSeleccionado?.idJugador === this.jugadorUsuario?.idJugador;
  }
  get estaJugandoModal(): boolean {
    return this.getTurnoActual()?.idJugador === this.jugadorUsuario?.idJugador;
  }
  get puedoAtacarDesdeModal(): boolean {
    return this.faseActual === FaseTurno.ATACAR && this.esSuyoPaisModal && this.estaJugandoModal;
  }
  get puedoDefenderDesdeModal(): boolean {
    return this.faseActual === FaseTurno.COLOCACION && this.esSuyoPaisModal && this.estaJugandoModal;
  }
  get puedoReagruparDesdeModal(): boolean {
    return this.faseActual === FaseTurno.MOVER_TROPAS && this.esSuyoPaisModal && this.estaJugandoModal;
  }

  // ── Ciclo de vida ──────────────────────────────────────────
  ngOnInit() {
    this.url = this.route.snapshot.params['url'];
    this.tableroServicio.startPolling(this.url);

    this.clockInterval = setInterval(() => { this.horaActual = new Date(); this.updateClockHands(); }, 1000);

    this.tableroServicio.partidaObservable.subscribe({
      next: (result: PartidaDto) => {
        if (!result) return;

        if (result.estado === EstadoPartida.TERMINADA) {
          this.tableroServicio.consultarMotivoGanador(result.ganador!.idJugador).subscribe({
            next: (verificacion: VerificacionObjetivo) => {
              if (verificacion.gano) {
                this.tableroServicio.stopPolling();
                this.notificationService.success(
                  `¡${result.ganador!.color} ganó la partida! Objetivo: ${verificacion.objetivoCumplido}`
                );
                this.router.navigate(['/principal']);
              }
            },
            error: err => console.error('Error consultarMotivoGanador:', err)
          });
          return;
        }

        const turnoAnteriorNum = this.lastTurnoActual;
        this.partida = result;
        const turnoActualNum = Number(result.turnoActual);

        if (turnoAnteriorNum !== -1 && turnoAnteriorNum !== turnoActualNum) {
          this.iniciarTimer();
          const jNuevo = this.jugadorActualTurno;
          if (jNuevo) this.agregarHistorial(`Turno de ${jNuevo.nombre} — ${this.faseActual}`, 'ok');
        }
        this.lastTurnoActual = turnoActualNum;

        const usuario = this.authService.getCurrentUser();
        const turno = this.getTurnoActual();
        if (!turno) { this.esMiTurno = false; return; }

        const jugador = result.jugadores?.find(j => j.idJugador === turno.idJugador);
        if (!jugador) { this.esMiTurno = false; return; }

        const idJugadorActual = this.authService.getJugadorId() ?? 0;
        if (jugador.idUsuario === usuario?.idUsuario && jugador.idJugador === idJugadorActual) {
          this.esMiTurno = true;
          this.jugadorId = jugador.idJugador;
        } else {
          this.esMiTurno = false;
        }

        this.jugadorConsquisto = result.jugadores?.find(
          j => j.consquisto && j.idUsuario === usuario?.idUsuario && turno.fase === FaseTurno.MOVER_TROPAS
        ) ?? null;

        const jugadorUsuario = result.jugadores?.find(j => j.idUsuario === usuario?.idUsuario);
        this.cartasJugador = result.estadoTarjetas?.filter(t => t.idJugador === jugadorUsuario?.idJugador) ?? [];

        for (const tarjeta of this.cartasJugador) {
          tarjeta.jugadorTienePais = result.estadoPaises?.some(
            p => p.idJugador === jugadorUsuario?.idJugador
              && p.pais.idPais === tarjeta.tarjeta?.pais.idPais
              && !tarjeta.usada
          ) ?? false;
        }

        this.calcularCombinacionesCanje(this.cartasJugador);
      }
    });

    this.iniciarTimer();
  }

  ngOnDestroy() {
    this.tableroServicio.stopPolling();
    if (this.timerInterval) clearInterval(this.timerInterval);
    if (this.clockInterval) clearInterval(this.clockInterval);
  }

  // ── Timer ──────────────────────────────────────────────────
  iniciarTimer() {
    if (this.timerInterval) clearInterval(this.timerInterval);
    this.tiempoRestante = 120;
    this.timerInterval = setInterval(() => {
      if (this.tiempoRestante > 0) {
        this.tiempoRestante--;
      } else {
        if (this.timerInterval) clearInterval(this.timerInterval);
        if (this.esMiTurno) this.avanzarFaseTurno();
      }
    }, 1000);
  }

  // ── Acciones de turno ──────────────────────────────────────
  avanzarFaseTurno() {
    this.tableroServicio.cambiarTurno(this.partida.idPartida).subscribe({
      error: err => console.error('Error al avanzar fase:', err)
    });
  }

  obtenerTarjeta() {
    if (!this.jugadorConsquisto) {
      this.notificationService.info('No conquistaste ningún país en este turno.');
      return;
    }
    this.tableroServicio.obtenerTarjeta(this.jugadorConsquisto.idJugador, this.partida.idPartida).subscribe({
      next: () => this.notificationService.success('¡Tarjeta obtenida!'),
      error: err => { console.error('Error al obtener tarjeta:', err); this.notificationService.error('Error al obtener la tarjeta.'); }
    });
  }

  usarTarjeta(tarjeta: EstadoTarjetaDto) {
    const dto: UsarTarjetaEnPaisDto = { idTarjeta: tarjeta.idEstadoTarjeta, idJugador: tarjeta.idJugador };
    this.tableroServicio.usarTarjetaEnPais(dto).subscribe({
      next: () => this.notificationService.success('Tarjeta usada.'),
      error: err => console.error('Error al usar tarjeta', err)
    });
  }

  // ── Canje de tarjetas ──────────────────────────────────────
  private calcularCombinacionesCanje(cartas: EstadoTarjetaDto[]): void {
    this.puedeCanjear = false;
    this.combinacionesPosibles = [];
    if (!cartas || cartas.length < 3) return;

    const disponibles = cartas.filter(c => !c.canjeada);
    for (let i = 0; i < disponibles.length - 2; i++) {
      for (let j = i + 1; j < disponibles.length - 1; j++) {
        for (let k = j + 1; k < disponibles.length; k++) {
          const combo = [disponibles[i], disponibles[j], disponibles[k]];
          const simbolos = combo.map(c => c.tarjeta?.simbolo).filter(Boolean);
          if (simbolos.length === 3) {
            const set = new Set(simbolos);
            if (set.size === 1 || set.size === 3) this.combinacionesPosibles.push(combo);
          }
        }
      }
    }
    this.puedeCanjear = this.combinacionesPosibles.length > 0;
  }

  abrirModalCanje(): void {
    this.combinacionSeleccionada = null;
    this.showCanjeModal = true;
  }

  cerrarModalCanje(): void {
    this.showCanjeModal = false;
  }

  seleccionarCombinacion(index: number): void {
    this.combinacionSeleccionada = index;
  }

  confirmarCanje(): void {
    if (this.combinacionSeleccionada === null) return;
    const cartasSeleccionadas = this.combinacionesPosibles[this.combinacionSeleccionada];
    const dto: CanjeTarjetasDto = {
      idTarjetas: cartasSeleccionadas.map(c => c.idEstadoTarjeta),
      idJugador: this.jugadorId
    };
    this.tableroServicio.realizarCanje(dto).subscribe({
      next: (tropas: number) => {
        this.notificationService.success(`Canje exitoso. Tropas obtenidas: ${tropas}`);
        this.cerrarModalCanje();
      },
      error: () => this.notificationService.error('Error al realizar el canje.')
    });
  }

  // ── Modal de país ──────────────────────────────────────────
  paisClickeado({ estadoPais, event }: PaisClickEvent) {
    this.paisModalSeleccionado = estadoPais;
    this.idPaisDestinoModal = -1;
    this.inputTropasModal = 0;
    this.showAtaqueResultado = false;
    this.ataqueResultado = undefined;

    const mapaEl = document.querySelector('.mapa-zona') as HTMLElement;
    if (mapaEl) {
      const rect = mapaEl.getBoundingClientRect();
      this.modalX = Math.max(0, Math.min(event.clientX - rect.left + 12, rect.width - 180));
      this.modalY = Math.max(0, Math.min(event.clientY - rect.top - 30, rect.height - 220));
    }

    this.tableroServicio.getAllLimites(estadoPais.id).subscribe({
      next: limites => { this.paisesLimitrofes = limites; this.calcularLimitrofes(); },
      error: err => console.error('Error al obtener límites:', err)
    });
  }

  cerrarModalPais() { this.paisModalSeleccionado = null; }

  private calcularLimitrofes() {
    this.paisesAliados = [];
    this.paisesEnemigos = [];
    const aliadosIds = new Set<number>();
    const enemigosIds = new Set<number>();
    for (const p of this.paisesLimitrofes) {
      if (p.idJugador === this.paisModalSeleccionado?.idJugador) {
        if (!aliadosIds.has(p.pais.idPais)) { aliadosIds.add(p.pais.idPais); this.paisesAliados.push(p); }
      } else {
        if (!enemigosIds.has(p.pais.idPais)) { enemigosIds.add(p.pais.idPais); this.paisesEnemigos.push(p); }
      }
    }
  }

  atacarDesdeModal() {
    if (!this.jugadorUsuario || !this.paisModalSeleccionado) return;
    if (this.idPaisDestinoModal === -1) { this.notificationService.warning('Seleccioná un país destino.'); return; }
    const dto: AtaqueDto = {
      idJugador: this.jugadorUsuario.idJugador,
      idPaisOrigen: this.paisModalSeleccionado.pais.idPais,
      idPaisDestino: Number(this.idPaisDestinoModal)
    };
    this.tableroServicio.atacarPais(dto).subscribe({
      next: response => {
        this.ataqueResultado = response;
        this.paisOrigenNombreModal = this.paisModalSeleccionado!.pais.nombre;
        this.paisDestinoNombreModal = this.paisesEnemigos.find(p => p.pais.idPais === dto.idPaisDestino)?.pais.nombre ?? '';
        this.showAtaqueResultado = true;
        this.agregarHistorial(
          `${this.jugadorUsuario!.nombre} ${response.ataqueExitoso ? 'conquistó' : 'atacó'} ${this.paisDestinoNombreModal}`,
          response.ataqueExitoso ? 'ataque' : 'normal'
        );
        this.cerrarModalPais();
      },
      error: err => { console.error('Error ataque:', err); this.notificationService.error('Error al atacar.'); }
    });
  }

  defenderDesdeModal() {
    if (!this.jugadorUsuario || !this.paisModalSeleccionado) return;
    const body = {
      idJugador: this.jugadorUsuario.idJugador,
      paisesFichas: [{ idPais: this.paisModalSeleccionado.pais.idPais, cantidadFichas: this.inputTropasModal }]
    };
    this.tableroServicio.defenderPais(body).subscribe({
      next: resultado => {
        if (resultado) {
          this.notificationService.success('Ejércitos colocados.');
          this.agregarHistorial(`${this.jugadorUsuario!.nombre} colocó en ${this.paisModalSeleccionado!.pais.nombre}`, 'ok');
          this.cerrarModalPais();
        }
      },
      error: err => { console.error('Error defender:', err); this.notificationService.error('Error al colocar ejércitos.'); }
    });
  }

  reagruparDesdeModal() {
    if (!this.jugadorUsuario || !this.paisModalSeleccionado) return;
    if (this.idPaisDestinoModal === -1) { this.notificationService.warning('Seleccioná un país destino.'); return; }
    const dto: MoverFichas = {
      idJugador: this.jugadorUsuario.idJugador,
      idPaisOrigen: this.paisModalSeleccionado.pais.idPais,
      idPaisDestino: Number(this.idPaisDestinoModal),
      cantidadFichas: this.inputTropasModal
    };
    this.tableroServicio.reagruparFichas(dto).subscribe({
      next: resultado => {
        if (resultado) {
          this.notificationService.success('Reagrupación exitosa.');
          this.agregarHistorial(`${this.jugadorUsuario!.nombre} reagrupó tropas`, 'ok');
          this.cerrarModalPais();
        }
      },
      error: err => { console.error('Error reagrupar:', err); this.notificationService.error('Error al reagrupar.'); }
    });
  }

  cerrarResultadoAtaque() { this.showAtaqueResultado = false; }

  // ── Zoom / Pan ─────────────────────────────────────────────
  onWheel(event: WheelEvent) {
    event.preventDefault();
    const delta = event.deltaY > 0 ? 0.9 : 1.1;
    this.scale = Math.min(Math.max(this.scale * delta, 1), 4);
    if (this.scale === 1) { this.translateX = 0; this.translateY = 0; }
  }

  onMouseDownMapa(event: MouseEvent) {
    if (this.scale <= 1) return;
    this.isDragging = true;
    this.lastMouseX = event.clientX;
    this.lastMouseY = event.clientY;
  }

  onMouseMoveMapa(event: MouseEvent) {
    if (!this.isDragging) return;
    this.translateX += event.clientX - this.lastMouseX;
    this.translateY += event.clientY - this.lastMouseY;
    this.lastMouseX = event.clientX;
    this.lastMouseY = event.clientY;
  }

  onMouseUpMapa() { this.isDragging = false; }

  zoomIn() { this.scale = Math.min(this.scale * 1.25, 4); }
  zoomOut() {
    this.scale = Math.max(this.scale / 1.25, 1);
    if (this.scale === 1) { this.translateX = 0; this.translateY = 0; }
  }
  resetZoom() { this.scale = 1; this.translateX = 0; this.translateY = 0; }

  // ── Historial ──────────────────────────────────────────────
  agregarHistorial(texto: string, tipo: 'ataque' | 'ok' | 'normal' = 'normal') {
    this.historial.unshift({ texto, tipo });
    if (this.historial.length > 20) this.historial.pop();
  }

  // ── Chat ───────────────────────────────────────────────────
  enviarChat() {
    if (!this.chatInput.trim()) return;
    const usuario = this.authService.getCurrentUser();
    this.chatMensajes.push({
      actor: usuario?.usuario ?? 'Yo',
      texto: this.chatInput.trim(),
      colorSolido: this.getColorSolido(this.jugadorUsuario?.color ?? '')
    });
    this.chatInput = '';
    // TODO: WebSocket chat no implementado — sin topic en backend
  }

  // ── Helpers de color / UI ──────────────────────────────────
  getColorSolido(color: string): string {
    switch (color?.toUpperCase()) {
      case 'ROJO':     return '#A01515';
      case 'AZUL':     return '#1A4080';
      case 'VERDE':    return '#1E7A50';
      case 'NARANJA':  return '#BF6800';
      case 'AMARILLO': return '#6B4C00';
      case 'VIOLETA':  return '#6B2490';
      default:         return '#888';
    }
  }

  getColorSolidoPaisModal(): string {
    if (!this.paisModalSeleccionado) return '#888';
    const jugador = this.partida?.jugadores?.find(j => j.idJugador === this.paisModalSeleccionado!.idJugador);
    return jugador ? this.getColorSolido(jugador.color) : '#888';
  }

  getNombreJugadorPais(idJugador: number): string {
    return this.partida?.jugadores?.find(j => j.idJugador === idJugador)?.nombre ?? 'Sin dueño';
  }

  getPlayerSvg(color: string): string {
    switch (color?.toUpperCase()) {
      case 'ROJO':     return 'assets/vectors/red-player.svg';
      case 'AZUL':     return 'assets/vectors/blue-player.svg';
      case 'VERDE':    return 'assets/vectors/green-player.svg';
      case 'NARANJA':  return 'assets/vectors/orange-player.svg';
      case 'AMARILLO': return 'assets/vectors/gold-player.svg';
      case 'VIOLETA':  return 'assets/vectors/purple-player.svg';
      default:         return 'assets/vectors/red-player.svg';
    }
  }

  getAvatarJugador(idJugador: number): string {
    const avatares = [
      'assets/miembros/AgosCh.png', 'assets/miembros/CandeArguello.png',
      'assets/miembros/CandeBlanco.jpeg', 'assets/miembros/LaraHeredia.png',
      'assets/miembros/MeliAbril.png', 'assets/miembros/SofiCirioni.png',
      'assets/miembros/Maxi.png'
    ];
    return avatares[idJugador % avatares.length];
  }

  getEmojiDado(valor: number): string {
    return ['', '⚀', '⚁', '⚂', '⚃', '⚄', '⚅'][valor] ?? '?';
  }

  esEsBot(jugador: JugadorDto): boolean {
    return jugador.tipoJugador?.toUpperCase() === 'BOT';
  }

  countriasDeJugador(idJugador: number): number {
    return this.partida?.estadoPaises?.filter(p => p.idJugador === idJugador).length ?? 0;
  }
}
