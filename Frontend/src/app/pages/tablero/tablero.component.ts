import { Component, ElementRef, HostListener, OnDestroy, OnInit, ViewChild, inject } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { take } from 'rxjs';
import { StompSubscription } from '@stomp/stompjs';
import { TableroServicio } from '../../core/services/tablero.service';
import { WebSocketService } from '../../core/services/socket.service';
import { TableroEventService } from '../../core/services/tablero-event.service';
import { MapaSvgComponent, PaisClickEvent } from './componentes/mapa-svg/mapa-svg.component';
import { TableroEventDisplayComponent } from './componentes/tablero-event-display/tablero-event-display.component';
import { ObjetivoRevelacionComponent } from './componentes/objetivo-revelacion/objetivo-revelacion.component';
import { ObjetivoQuemadoComponent } from './componentes/objetivo-quemado/objetivo-quemado.component';
import { FinPartidaOverlayComponent } from './componentes/fin-partida-overlay/fin-partida-overlay.component';
import { BotonPactosComponent } from './componentes/boton-pactos/boton-pactos.component';
import { PactosOverlayComponent } from './componentes/pactos-overlay/pactos-overlay.component';
import { RespuestaPactoOverlayComponent } from './componentes/respuesta-pacto-overlay/respuesta-pacto-overlay.component';
import {
  AtaqueDto,
  AtaqueResponseDto,
  CanjeTarjetasDto,
  EstadoPaisDto,
  EstadoTarjetaDto,
  EstadoPartida,
  FaseTurno,
  FinPartida,
  JugadorDto,
  MoverFichas,
  ObjetivoProgreso,
  PartidaDto,
  TurnoDto,
  UsarTarjetaEnPaisDto,
} from '../../core/models/interfaces/partida.interface';
import { PactoDto } from '../../core/models/interfaces/pacto.interface';
import { PactoService } from '../../core/services/pacto.service';
import { AuthService } from '../../core/services/auth.service';
import { NotificationService } from '../../core/services/notification.service';
import { NombrePaisPipe } from '../../core/pipes/nombre-pais.pipe';
import { FaseDisplayPipe } from '../../core/pipes/fase-display.pipe';

interface ChatMensaje {
  actor: string;
  texto: string;
  colorSolido: string;
  colorVar: string;
  timestamp: Date;
  esPropio: boolean;
}

interface HistorialItem {
  texto: string;
  tipo: 'ataque' | 'ok' | 'normal' | 'pacto';
}

@Component({
  selector: 'app-tablero',
  standalone: true,
  imports: [MapaSvgComponent, CommonModule, FormsModule, NombrePaisPipe, FaseDisplayPipe, TableroEventDisplayComponent, ObjetivoRevelacionComponent, ObjetivoQuemadoComponent, FinPartidaOverlayComponent, BotonPactosComponent, PactosOverlayComponent, RespuestaPactoOverlayComponent],
  templateUrl: 'tablero.component.html',
  styleUrl: 'tablero.component.scss',
})
export class TableroComponent implements OnInit, OnDestroy {

  private tableroServicio = inject(TableroServicio);
  private route = inject(ActivatedRoute);
  private authService = inject(AuthService);
  private router = inject(Router);
  private notificationService = inject(NotificationService);
  private wsService = inject(WebSocketService);
  private pactoService = inject(PactoService);
  readonly tableroEventService = inject(TableroEventService);

  /** true cuando ya nos suscribimos al topic de la partida (solo 1 vez) */
  private wsPartidaSuscripto = false;

  // ── Estado de partida ──────────────────────────────────────
  url!: string;
  partida!: PartidaDto;
  esMiTurno = false;
  jugadorId = 0;

  // ── Tarjetas ───────────────────────────────────────────────
  cartasJugador: EstadoTarjetaDto[] = [];
  puedeCanjear = false;
  combinacionesPosibles: EstadoTarjetaDto[][] = [];
  showCanjeModal = false;
  /** Ids de EstadoTarjeta seleccionados manualmente en el modal de detalle. */
  tarjetasSeleccionadasIds = new Set<number>();

  // ── Fin de partida ────────────────────────────────────────
  /** Datos del fin de partida recibidos por WS (visibles para todos los jugadores). */
  datosFinPartida: FinPartida | null = null;
  /** true → la partida terminó: se congela la UI hasta el overlay. */
  partidaFinalizada = false;
  /** true solo para el jugador ganador: muestra la animación de quemado. */
  mostrarAnimacionQuemado = false;
  /** true para todos los jugadores: muestra el overlay con el resultado. */
  mostrarFinPartida = false;
  private finPartidaTimeout?: ReturnType<typeof setTimeout>;

  // ── Objetivo secreto ───────────────────────────────────────
  objetivoVisible = false;
  showRevelacion = false;
  /** Oculto por defecto: aparece solo cuando la revelación está finalizando
   *  (evento cerrandoStart del componente) o cuando se entra a una partida sin
   *  revelación pendiente. Esto evita el flash inicial de aparición+desaparición. */
  sobreObjetivoVisible = false;
  private revelacionMostrada = false;
  progresoObjetivo: ObjetivoProgreso | null = null;
  private objetivoWsSub: StompSubscription | null = null;

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

  // ── Vista de continentes ───────────────────────────────────
  modoContinente = false;

  // ── Pactos ─────────────────────────────────────────────────
  pactosActivos: PactoDto[] = [];
  showPactosOverlay = false;
  /** Propuesta pendiente: si soy receptor, abre overlay bloqueante;
   *  si soy proponente o tercero, muestra overlay no bloqueante hasta que el receptor responda. */
  propuestaPendiente: PactoDto | null = null;
  /** Resultado de la propuesta vigente — `null` mientras se espera respuesta;
   *  `'ACEPTADO'` o `'RECHAZADO'` cuando llega el WS. El overlay queda visible
   *  unos segundos mostrando el resultado antes de cerrarse. */
  resultadoPropuesta: 'ACEPTADO' | 'RECHAZADO' | null = null;

  /** Referencias para la leyenda inferior cuando el modo overlay está activo.
   *  Los colores se mantienen sincronizados con CONTINENT_FILL_COLORS de
   *  mapa-svg.component.ts — si cambian allá, actualizar aquí también. */
  readonly leyendaContinentes: ReadonlyArray<{ nombre: string; color: string }> = [
    { nombre: 'América del Norte', color: 'rgba(191,104,0,0.55)' },
    { nombre: 'América del Sur',   color: 'rgba(160,21,21,0.55)' },
    { nombre: 'Europa',            color: 'rgba(26,64,128,0.55)' },
    { nombre: 'África',            color: 'rgba(107,76,0,0.55)' },
    { nombre: 'Asia',              color: 'rgba(107,36,144,0.55)' },
    { nombre: 'Oceanía',           color: 'rgba(30,122,80,0.55)' },
  ];

  toggleContinentes(): void {
    this.modoContinente = !this.modoContinente;
  }

  // ── Zoom / Pan ─────────────────────────────────────────────
  scale = 1;
  translateX = 0;
  translateY = 0;
  isDragging = false;
  private lastMouseX = 0;
  private lastMouseY = 0;

  // ── Timer de turno (5 min total) ──────────────────────────────
  tiempoRestante = 300;
  private timerInterval: ReturnType<typeof setInterval> | null = null;
  private lastTurnoActual = -1;
  /** Id del jugador activo en el último ciclo de polling — detecta cambio de turno incluso con bots síncronos */
  private lastJugadorActualId = -1;
  /** Flag: timer arrancó al menos una vez. Evita que el polling o el else-if
   *  de "partida en curso" re-arranquen el timer en cada tick. */
  private timerIniciado = false;
  /** true → el timer y el timer de inactividad NO decrementan su contador en
   *  este tick. Se activa mientras hay un evento en cola del sistema de
   *  notificaciones, para no robarle segundos al jugador. */
  private timerPausado = false;

  // ── Inactividad (30 s) ─────────────────────────────────────────
  inactividadRestante = 30;
  showInactividad = false;
  private inactividadInterval: ReturnType<typeof setInterval> | null = null;
  // Mousemove: acumulador de desplazamiento en ventana de 2 s
  private mouseAccumDist = 0;
  private mouseWindowStart = 0;
  private mousePrevX = 0;
  private mousePrevY = 0;

  // ── Reloj decorativo ───────────────────────────────────────
  horaActual = new Date();
  private clockInterval: ReturnType<typeof setInterval> | null = null;
  @ViewChild('relojObj') private relojObj?: ElementRef<HTMLObjectElement>;
  private svgDoc: Document | null = null;
  // Ángulos base de las agujas en el SVG original (medidos via getBBox).
  // Nota: el grupo id="aguja-horas" en el SVG es visualmente la aguja LARGA (minutos),
  // y id="aguja-minutos" es la aguja CORTA (horas). Los IDs quedaron invertidos al agregarlos.
  private readonly MINUTE_HAND_BASE_DEG  = 100;  // base de id="aguja-horas" (mano larga)
  private readonly HOUR_HAND_BASE_DEG    = 135;  // base de id="aguja-minutos" (mano corta)
  private readonly SECOND_HAND_BASE_DEG  = 0;

  // ── Historial de operaciones ───────────────────────────────
  historial: HistorialItem[] = [];

  // ── Chat ───────────────────────────────────────────────────
  chatMensajes: ChatMensaje[] = [];
  chatInput = '';
  readonly chatMaxLen = 120;
  @ViewChild('chatMensajesRef') private chatMensajesRef?: ElementRef<HTMLDivElement>;
  /** true si el scroll del chat está pegado al fondo — controla el auto-scroll */
  private chatPegadoAlFondo = true;

  // Anti-spam: tras N mensajes en una ventana corta el usuario queda bloqueado.
  private mensajesRecientes: number[] = [];
  private readonly LIMITE_MENSAJES = 4;
  private readonly VENTANA_MS = 10_000;
  private readonly COOLDOWN_MS = 15_000;
  enCooldown = false;
  cooldownRestanteSeg = 0;
  private cooldownInterval: ReturnType<typeof setInterval> | null = null;

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
    const minuteHand = this.svgDoc.getElementById('aguja-horas');   // grupo largo = minutos
    const hourHand   = this.svgDoc.getElementById('aguja-minutos'); // grupo corto = horas
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
    return this.faseActual === FaseTurno.ATAQUE && this.esSuyoPaisModal && this.estaJugandoModal;
  }
  // Reglamento: hacen falta ≥2 ejércitos en el país para iniciar ataque
  // (uno se queda defendiendo). El botón se muestra deshabilitado si no se cumple.
  get tropasInsuficientesParaAtacar(): boolean {
    return (this.paisModalSeleccionado?.cantidadTropas ?? 0) <= 1;
  }
  get puedoDefenderDesdeModal(): boolean {
    return this.faseActual === FaseTurno.INCORPORACION && this.esSuyoPaisModal && this.estaJugandoModal;
  }
  get puedoReagruparDesdeModal(): boolean {
    return this.faseActual === FaseTurno.REAGRUPACION && this.esSuyoPaisModal && this.estaJugandoModal;
  }

  // ── Ciclo de vida ──────────────────────────────────────────
  ngOnInit() {
    this.url = this.route.snapshot.params['url'];
    this.tableroServicio.startPolling(this.url);

    this.clockInterval = setInterval(() => { this.horaActual = new Date(); this.updateClockHands(); }, 1000);

    // currentEvent$: pausa el timer mientras haya una notificación efímera en
    // curso, y refresca el progreso del objetivo cuando hay una conquista.
    // El registro del historial vive en otro canal (historialEvent$) que recibe
    // TODAS las acciones, incluidas las propias.
    this.tableroEventService.currentEvent$.subscribe(event => {
      this.timerPausado = event !== null;
      if (!event) return;
      if (event.tipo === 'CONQUISTA') {
        const jId = this.jugadorUsuario?.idJugador;
        if (jId) {
          this.tableroServicio.obtenerProgresoObjetivo(jId).pipe(take(1)).subscribe({
            next: p => { this.progresoObjetivo = p; },
            error: () => {}
          });
        }
      }
    });

    // historialEvent$: una entrada por cada acción del juego (todos los jugadores,
    // incluido el local). Lo emite el TableroEventService antes del filtro por
    // autor para que las acciones propias también queden registradas.
    this.tableroEventService.historialEvent$.subscribe(({ texto, tipo }) => {
      this.agregarHistorial(texto, tipo);
    });

    this.tableroServicio.partidaObservable.subscribe({
      next: (result: PartidaDto | null) => {
        if (!result) return;

        // La partida ya terminó pero no recibimos el WS (recarga, llegamos tarde):
        // detenemos el polling silenciosamente y esperamos al evento si llega.
        // El overlay se muestra cuando llega el WS FIN_PARTIDA, que dispara
        // alRecibirFinPartida(). No navegamos aquí — eso era el comportamiento
        // viejo que saltaba a estadísticas sin mostrar al ganador.
        if (result.estado === EstadoPartida.TERMINADA && !this.partidaFinalizada) {
          this.tableroServicio.stopPolling();
          this.partidaFinalizada = true;
          this.detenerInactividad();
          if (this.timerInterval) clearInterval(this.timerInterval);
        }

        // Suscribir al topic WS de la partida la primera vez que cargue
        if (!this.wsPartidaSuscripto && result.idPartida) {
          this.wsPartidaSuscripto = true;
          this.wsService.asegurarConexion();
          this.wsService.suscribirseEventosPartida(result.idPartida, evento => {
            // Capturamos FIN_PARTIDA acá: no es una notificación efímera.
            if (evento.tipo === 'FIN_PARTIDA' && evento.finPartida) {
              this.alRecibirFinPartida(evento.finPartida);
              return;
            }
            // Pactos: actualizar estado local y manejar overlays antes de pasar al servicio de eventos
            // (que se encarga del registro/notificaciones).
            if (evento.tipo?.startsWith('PACTO_')) {
              this.alRecibirEventoPacto(evento);
            }
            this.tableroEventService.enqueueFromWs(evento, this.jugadorUsuario?.nombre);
          });

          // Carga inicial de pactos activos para esta partida.
          this.recargarPactos();

          // Suscribir al topic personal de progreso de objetivo
          const jugadorId = this.authService.getJugadorId();
          if (jugadorId) {
            this.objetivoWsSub = this.wsService.suscribirseProgresoObjetivo(
              result.idPartida, jugadorId,
              (progreso: ObjetivoProgreso) => { this.progresoObjetivo = progreso; }
            );
          }
        }

        // Mostrar revelación la primera vez que tengamos objetivo cargado
        if (!this.revelacionMostrada && this.jugadorUsuario?.objetivo) {
          this.revelacionMostrada = true;
          this.showRevelacion = true;
          this.sobreObjetivoVisible = false;  // se re-muestra al confirmar/aterrizar
          // Cargar progreso inicial desde REST
          const jId = this.jugadorUsuario.idJugador;
          this.tableroServicio.obtenerProgresoObjetivo(jId).subscribe({
            next: p => { this.progresoObjetivo = p; },
            error: () => {}
          });
        } else if (!this.showRevelacion && this.jugadorUsuario?.objetivo) {
          // Partida ya en curso (sin revelación pendiente): asegurar sobre visible
          // y arrancar el timer si todavía no arrancó (recargas, reconexión, etc.).
          this.sobreObjetivoVisible = true;
          if (!this.timerIniciado) this.iniciarTimer();
        }

        const turnoAnteriorNum = this.lastTurnoActual;
        this.partida = result;
        const turnoActualNum = Number(result.turnoActual);

        if (turnoAnteriorNum !== -1 && turnoAnteriorNum !== turnoActualNum) {
          this.iniciarTimer();
          const jNuevo = this.jugadorActualTurno;
          if (jNuevo) this.agregarHistorial(`Turno de ${jNuevo.nombre} — ${this.faseActual}`, 'ok');
          // Refrescar progreso al iniciar cada nuevo turno
          const jId = this.jugadorUsuario?.idJugador;
          if (jId) {
            this.tableroServicio.obtenerProgresoObjetivo(jId).pipe(take(1)).subscribe({
              next: p => { this.progresoObjetivo = p; },
              error: () => {}
            });
          }
        }
        this.lastTurnoActual = turnoActualNum;

        const usuario = this.authService.getCurrentUser();
        const turno = this.getTurnoActual();
        if (!turno) { this.esMiTurno = false; this.lastJugadorActualId = -1; return; }

        const jugador = result.jugadores?.find(j => j.idJugador === turno.idJugador);
        if (!jugador) { this.esMiTurno = false; this.lastJugadorActualId = -1; return; }

        const idJugadorActual = this.authService.getJugadorId() ?? 0;
        // turnoChanged es true cuando cambia el jugador activo — funciona aunque los bots sean síncronos
        const turnoChanged = this.lastJugadorActualId !== turno.idJugador;
        this.lastJugadorActualId = turno.idJugador;

        if (jugador.idUsuario === usuario?.idUsuario && jugador.idJugador === idJugadorActual) {
          this.esMiTurno = true;
          this.jugadorId = jugador.idJugador;
          if (turnoChanged) this.iniciarInactividad();
        } else {
          this.esMiTurno = false;
          this.detenerInactividad();
        }

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

        // Canje obligatorio: si es mi turno, estoy en INCORPORACION y tengo 5+ cartas,
        // abrir el panel de canje automáticamente para que el jugador seleccione
        if (this.esMiTurno && this.faseActual === FaseTurno.INCORPORACION
            && this.cartasJugador.length >= 5 && !this.showCanjeModal) {
          this.showCanjeModal = true;
        }
      }
    });

    // El timer NO arranca acá: espera a que el jugador confirme la revelación
    // del objetivo (o al auto-close del sobre). Ver onRevelacionConfirmada y el
    // branch else-if de "partida en curso" para los puntos de arranque.
  }

  ngOnDestroy() {
    this.tableroServicio.stopPolling();
    this.wsService.desuscribirsePartida();
    this.objetivoWsSub?.unsubscribe();
    if (this.timerInterval) clearInterval(this.timerInterval);
    if (this.clockInterval) clearInterval(this.clockInterval);
    if (this.cooldownInterval) clearInterval(this.cooldownInterval);
    if (this.finPartidaTimeout) clearTimeout(this.finPartidaTimeout);
    this.detenerInactividad();
  }

  /** Disparado cuando el usuario confirma (o el timer expira) y empieza el vuelo.
   *  Mostramos el sobre-objetivo con fade-in para que "aterrice" el sobre grande. */
  onRevelacionCerrandoStart(): void {
    this.sobreObjetivoVisible = true;
  }

  onRevelacionConfirmada(): void {
    this.showRevelacion = false;
    // Idempotente — por si el vuelo se saltó (dev hot-reload, etc.)
    this.sobreObjetivoVisible = true;
    // Recién ahora el jugador ya vio su objetivo — arrancar el timer de turno.
    if (!this.timerIniciado) this.iniciarTimer();
  }

  // ── Fin de partida ────────────────────────────────────────────
  /**
   * Disparado cuando llega el evento WS FIN_PARTIDA. Congela la UI, frena
   * polling/timers y muestra el overlay (con animación de quemado previa para
   * el ganador). Para sincronizar la pantalla de resultado entre todos los
   * jugadores, los no-ganadores también esperan ~3s antes de ver el overlay
   * (lo que dura la animación de quemado del ganador) — así nadie ve antes
   * el resultado.
   */
  alRecibirFinPartida(dto: FinPartida): void {
    if (this.partidaFinalizada && this.datosFinPartida) return; // idempotente

    this.partidaFinalizada = true;
    this.datosFinPartida = dto;

    // Detener polling REST y todos los timers — la partida terminó.
    this.tableroServicio.stopPolling();
    if (this.timerInterval) { clearInterval(this.timerInterval); this.timerInterval = null; }
    this.detenerInactividad();
    this.timerPausado = true;

    const idLocal = this.authService.getJugadorId() ?? this.jugadorUsuario?.idJugador;
    const esGanador = idLocal != null && Number(dto.ganador.idJugador) === Number(idLocal);

    if (esGanador) {
      this.mostrarAnimacionQuemado = true;
      // El overlay se mostrará cuando la animación emita animacionCompleta.
    } else {
      // Los demás ven la pantalla congelada durante la duración de la animación
      // del ganador (3s) y luego el overlay — así llega sincronizado.
      this.finPartidaTimeout = setTimeout(() => {
        this.mostrarFinPartida = true;
      }, 3000);
    }
  }

  /** Disparado por ObjetivoQuemadoComponent al terminar (~3s). */
  alCompletarseAnimacionQuemado(): void {
    this.mostrarAnimacionQuemado = false;
    this.mostrarFinPartida = true;
  }

  irAEstadisticas(): void {
    this.router.navigate(['/estadisticas']);
  }

  // ── Pactos ────────────────────────────────────────────────
  recargarPactos(): void {
    if (!this.partida?.idPartida) return;
    this.pactoService.listarActivos(this.partida.idPartida).subscribe({
      next: (lista) => { this.pactosActivos = lista; },
      error: () => {},
    });
  }

  /** Cantidad de pactos activos en los que el jugador local participa. */
  get cantidadPactosLocales(): number {
    const myId = this.jugadorUsuario?.idJugador;
    if (!myId) return 0;
    return this.pactosActivos.filter(p =>
      p.estado === 'ACTIVO' && (p.idJugadorA === myId || p.idJugadorB === myId)
    ).length;
  }

  abrirPactosOverlay(): void {
    this.recargarPactos();
    this.showPactosOverlay = true;
  }

  cerrarPactosOverlay(): void {
    this.showPactosOverlay = false;
  }

  onPactoCambiado(): void {
    this.recargarPactos();
  }

  onRespuestaPacto(): void {
    this.propuestaPendiente = null;
    this.resultadoPropuesta = null;
    this.recargarPactos();
  }

  /** Maneja todos los eventos WS PACTO_*. */
  alRecibirEventoPacto(ws: any): void {
    if (!ws.pacto) return;
    const myId = this.jugadorUsuario?.idJugador;

    switch (ws.tipo) {
      case 'PACTO_PROPUESTO':
        // Mostrar overlay de respuesta a TODOS los jugadores; el receptor verá modo bloqueante.
        this.propuestaPendiente = ws.pacto;
        this.resultadoPropuesta = null;
        break;
      case 'PACTO_ACEPTADO':
      case 'PACTO_RECHAZADO':
        // Mantener el overlay unos segundos mostrando APROBADO/RECHAZADO antes de cerrar.
        if (this.propuestaPendiente?.id === ws.pacto.id) {
          this.resultadoPropuesta = ws.tipo === 'PACTO_ACEPTADO' ? 'ACEPTADO' : 'RECHAZADO';
        }
        if (ws.tipo === 'PACTO_ACEPTADO' && myId
            && (ws.pacto.idJugadorA === myId || ws.pacto.idJugadorB === myId)) {
          this.notificationService.success('Pacto firmado.');
        }
        break;
      case 'PACTO_ROTO':
        // Si era una propuesta abierta, cerrar.
        if (this.propuestaPendiente?.id === ws.pacto.id) {
          this.propuestaPendiente = null;
          this.resultadoPropuesta = null;
        }
        break;
    }

    // Actualizar estado local de pactos: el WS no contiene la lista completa,
    // pedimos refresh al backend.
    this.recargarPactos();
  }

  // ── HostListeners para detectar actividad ─────────────────────
  @HostListener('click')
  @HostListener('keydown')
  onActivityEvent(): void {
    this.registrarActividad();
  }

  /** Cierra el modal de canje al presionar Escape. */
  @HostListener('document:keydown.escape')
  onEscapeKey(): void {
    if (this.showCanjeModal) this.cerrarModalCanje();
  }

  @HostListener('mousemove', ['$event'])
  onMouseMoveActivity(event: MouseEvent): void {
    if (!this.esMiTurno) return;
    const now = Date.now();
    if (now - this.mouseWindowStart > 2000) {
      this.mouseAccumDist = 0;
      this.mouseWindowStart = now;
      this.mousePrevX = event.clientX;
      this.mousePrevY = event.clientY;
      return;
    }
    const dx = Math.abs(event.clientX - this.mousePrevX);
    const dy = Math.abs(event.clientY - this.mousePrevY);
    this.mouseAccumDist += Math.sqrt(dx * dx + dy * dy);
    this.mousePrevX = event.clientX;
    this.mousePrevY = event.clientY;
    if (this.mouseAccumDist > 50) {
      this.registrarActividad();
      this.mouseAccumDist = 0;
      this.mouseWindowStart = now;
    }
  }

  registrarActividad(): void {
    if (!this.esMiTurno) return;
    this.inactividadRestante = 30;
    this.showInactividad = false;
  }

  // ── Timer ──────────────────────────────────────────────────
  iniciarTimer() {
    if (this.timerInterval) clearInterval(this.timerInterval);
    this.timerIniciado = true;
    this.tiempoRestante = 300;
    this.timerInterval = setInterval(() => {
      // Pausa mientras haya notificaciones en cola: no robar tiempo al jugador.
      if (this.timerPausado) return;
      if (this.tiempoRestante > 0) {
        this.tiempoRestante--;
      } else {
        if (this.timerInterval) clearInterval(this.timerInterval);
        this.detenerInactividad();
        if (this.esMiTurno) this.saltarTurnoCompleto();
      }
    }, 1000);
  }

  private iniciarInactividad(): void {
    this.detenerInactividad();
    if (!this.esMiTurno) return;
    const turno = this.getTurnoActual();
    const jugadorActivo = this.partida?.jugadores?.find(j => j.idJugador === turno?.idJugador);
    if (jugadorActivo?.tipoJugador === 'BOT') return;
    this.inactividadRestante = 60;
    this.showInactividad = false;
    this.inactividadInterval = setInterval(() => {
      // Pausa mientras haya notificaciones: no marcar al jugador como inactivo.
      if (this.timerPausado) return;
      if (this.inactividadRestante > 0) {
        this.inactividadRestante--;
        if (this.inactividadRestante <= 15) this.showInactividad = true;
      } else {
        // Pasa el turno completo al siguiente jugador
        this.detenerInactividad();
        if (this.esMiTurno) this.saltarTurnoCompleto();
      }
    }, 1000);
  }

  /** Avanza todas las fases restantes del turno actual (máx 3 llamadas encadenadas). */
  private saltarTurnoCompleto(intentosRestantes = 3): void {
    if (intentosRestantes <= 0) { this.tableroServicio.forceRefresh(); return; }

    // Si hay canje obligatorio pendiente (5+ cartas en INCORPORACION), canjear antes de avanzar
    if (this.faseActual === FaseTurno.INCORPORACION
        && this.cartasJugador.length >= 5
        && this.combinacionesPosibles.length > 0) {
      const dto: CanjeTarjetasDto = {
        idTarjetas: this.combinacionesPosibles[0].map(c => c.idEstadoTarjeta),
        idJugador: this.jugadorId
      };
      this.tableroServicio.realizarCanje(dto).subscribe({
        next: () => this.tableroServicio.forceRefresh(),
        error: () => {}
      });
    }

    this.tableroServicio.cambiarTurno(this.partida.idPartida).subscribe({
      next: () => {
        this.tableroServicio.forceRefresh();
        // Esperar a que el estado actualice y verificar si aún es mi turno
        setTimeout(() => {
          if (this.esMiTurno) this.saltarTurnoCompleto(intentosRestantes - 1);
        }, 400);
      },
      error: err => { console.error('Error al saltar turno:', err); this.tableroServicio.forceRefresh(); }
    });
  }

  private detenerInactividad(): void {
    if (this.inactividadInterval) {
      clearInterval(this.inactividadInterval);
      this.inactividadInterval = null;
    }
    this.showInactividad = false;
  }

  // ── Acciones de turno ──────────────────────────────────────
  avanzarFaseTurno() {
    this.tableroServicio.cambiarTurno(this.partida.idPartida).subscribe({
      next: () => this.tableroServicio.forceRefresh(),
      error: err => console.error('Error al avanzar fase:', err)
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
    this.tarjetasSeleccionadasIds = new Set<number>();
    this.showCanjeModal = true;
  }

  cerrarModalCanje(): void {
    this.showCanjeModal = false;
    this.tarjetasSeleccionadasIds = new Set<number>();
  }

  /** Indica si una tarjeta puede ser seleccionada para canje en el modal. */
  get canjeHabilitado(): boolean {
    return this.esMiTurno && this.faseActual === FaseTurno.INCORPORACION;
  }

  /** Toggle de selección de una tarjeta en el modal (máx. 3, no canjeadas). */
  toggleTarjetaCanje(t: EstadoTarjetaDto): void {
    if (!this.canjeHabilitado || t.canjeada) return;
    const id = t.idEstadoTarjeta;
    if (this.tarjetasSeleccionadasIds.has(id)) {
      this.tarjetasSeleccionadasIds.delete(id);
      return;
    }
    if (this.tarjetasSeleccionadasIds.size >= 3) return;
    this.tarjetasSeleccionadasIds.add(id);
  }

  estaSeleccionada(t: EstadoTarjetaDto): boolean {
    return this.tarjetasSeleccionadasIds.has(t.idEstadoTarjeta);
  }

  /**
   * Combinación válida: 3 tarjetas y los símbolos son todos iguales (size 1)
   * o todos distintos (size 3). Replica la regla aplicada en
   * calcularCombinacionesCanje() para validar la selección manual.
   */
  get combinacionValida(): boolean {
    if (this.tarjetasSeleccionadasIds.size !== 3) return false;
    const seleccionadas = this.cartasJugador.filter(c => this.tarjetasSeleccionadasIds.has(c.idEstadoTarjeta));
    if (seleccionadas.length !== 3) return false;
    const simbolos = seleccionadas.map(c => c.tarjeta?.simbolo).filter(Boolean) as string[];
    if (simbolos.length !== 3) return false;
    const set = new Set(simbolos);
    return set.size === 1 || set.size === 3;
  }

  /** Devuelve la clave normalizada del símbolo para selección de SVG/clase CSS. */
  getSimboloKey(simbolo: string | undefined | null): 'galeon' | 'globo' | 'canion' | 'comodin' {
    const s = (simbolo ?? '').toUpperCase();
    if (s === 'GALEON') return 'galeon';
    if (s === 'GLOBO')  return 'globo';
    if (s === 'CANION') return 'canion';
    return 'comodin';
  }

  confirmarCanje(): void {
    if (!this.combinacionValida) return;
    const ids = Array.from(this.tarjetasSeleccionadasIds);
    const dto: CanjeTarjetasDto = {
      idTarjetas: ids,
      idJugador: this.jugadorId
    };
    this.tableroServicio.realizarCanje(dto).subscribe({
      next: (tropas: number) => {
        this.cerrarModalCanje();
        this.tableroEventService.enqueue({
          tipo: 'TARJETA_CANJEADA',
          titulo: 'CANJE DE TARJETAS',
          descripcion: `${this.jugadorUsuario?.nombre ?? 'Jugador'} obtuvo ${tropas} ejércitos`,
          jugadorActivo: this.jugadorUsuario?.nombre,
          colorJugador: this.getColorVarJugador(this.jugadorUsuario?.color ?? ''),
          duracionMs: 3500,
        });
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
      const MODAL_W = 226; // 210px ancho + 8px padding exterior × 2
      const MODAL_H = 300; // altura estimada generosa (sin acciones: ~200px, con: ~300px)
      const clickX = event.clientX - rect.left;
      const clickY = event.clientY - rect.top;

      // Abrir a la izquierda del click si no cabe a la derecha dentro del mapa
      let x = clickX + 12;
      if (clickX + MODAL_W + 12 > rect.width) {
        x = clickX - MODAL_W - 12;
      }

      // Abrir hacia arriba si el click está en el tercio inferior del mapa
      let y = clickY - 30;
      if (clickY + MODAL_H - 30 > rect.height) {
        y = clickY - MODAL_H;
      }

      this.modalX = Math.max(0, Math.min(x, rect.width  - MODAL_W));
      this.modalY = Math.max(0, Math.min(y, rect.height - MODAL_H));
    }

    this.tableroServicio.getAllLimites(estadoPais.id).subscribe({
      next: limites => { this.paisesLimitrofes = limites; this.calcularLimitrofes(); },
      error: err => console.error('Error al obtener límites:', err)
    });
  }

  cerrarModalPais() { this.paisModalSeleccionado = null; }

  private calcularLimitrofes() {
    // Países enemigos: limítrofes directos de otro jugador (para atacar)
    this.paisesEnemigos = [];
    const enemigosIds = new Set<number>();
    for (const p of this.paisesLimitrofes) {
      if (p.idJugador !== this.paisModalSeleccionado?.idJugador) {
        if (!enemigosIds.has(p.pais.idPais)) { enemigosIds.add(p.pais.idPais); this.paisesEnemigos.push(p); }
      }
    }

    // Para reagrupar: pedir al backend los destinos válidos vía BFS
    this.paisesAliados = [];
    const idOrigen = this.paisModalSeleccionado?.pais.idPais;
    const idJugador = this.jugadorUsuario?.idJugador;
    if (idOrigen == null || idJugador == null) return;

    this.tableroServicio.getDestinosReagrupamiento(idOrigen, idJugador, this.partida.idPartida).subscribe({
      next: destinos => { this.paisesAliados = destinos; },
      error: () => {
        // Fallback: mostrar solo limítrofes propios directos
        this.paisesAliados = this.paisesLimitrofes.filter(p => p.idJugador === idJugador);
      }
    });
  }

  atacarDesdeModal() {
    if (!this.jugadorUsuario || !this.paisModalSeleccionado) return;
    if (this.idPaisDestinoModal === -1) { this.notificationService.warning('Seleccioná un país destino.'); return; }

    const paisOrigenNombre  = this.paisModalSeleccionado.pais.nombre;
    const idDestino         = Number(this.idPaisDestinoModal);
    const paisDestinoEstado = this.paisesEnemigos.find(p => p.pais.idPais === idDestino);
    const paisDestinoNombre = paisDestinoEstado?.pais.nombre ?? '';
    const jugadorDefensorObj = this.partida?.jugadores?.find(j => Number(j.idJugador) === Number(paisDestinoEstado?.idJugador));
    const jugadorDefensor   = jugadorDefensorObj?.nombre ?? '';
    const colorDefensor     = this.getColorVarJugador(jugadorDefensorObj?.color ?? '');
    const maxDados          = Math.min(3, Math.max(1, this.paisModalSeleccionado.cantidadTropas - 1));
    const colorJugador      = this.getColorVarJugador(this.jugadorUsuario.color);
    // Guardar ID origen ANTES de cerrar el modal (cerrarModalPais() lo pone en null)
    const idOrigen          = this.paisModalSeleccionado.pais.idPais;

    // Cerrar modal primero, luego mostrar selección de dados
    this.cerrarModalPais();

    // Suscribirse al resultado de dados ANTES de encolar el evento
    this.tableroEventService.diceResult$.pipe(take(1)).subscribe(cantDados => {
      const dto: AtaqueDto = {
        idJugador: this.jugadorUsuario!.idJugador,
        idPaisOrigen: idOrigen,
        idPaisDestino: idDestino,
        cantDadosAtacante: cantDados,
      };

      // Flujo dual: iniciarAtaque registra pendiente en backend y dispara WS
      // ATAQUE_INICIADO. El resultado llega por WS ATAQUE/CONQUISTA.
      this.tableroServicio.iniciarAtaque(dto).subscribe({
        next: () => {
          this.tableroEventService.advanceAfterDice();
          this.tableroServicio.forceRefresh();
        },
        error: err => {
          this.tableroEventService.advanceAfterDice();
          console.error('Error ataque:', err);
          this.notificationService.error('Error al iniciar ataque.');
        }
      });
    });

    // Encolar selector de dados LOCAL (solo el atacante lo ve antes de disparar
    // iniciarAtaque). Sin idAtacante/idDefensor → el componente lo trata como
    // "atacante-selector" (no como broadcast).
    this.tableroEventService.enqueue({
      tipo: 'ATAQUE_INICIADO',
      titulo: 'ELECCIÓN DE DADOS',
      jugadorActivo: this.jugadorUsuario.nombre,
      colorJugador,
      colorJugadorKey: (this.jugadorUsuario.color ?? '').toUpperCase(),
      jugadorDefensor,
      colorDefensor,
      colorDefensorKey: (jugadorDefensorObj?.color ?? '').toUpperCase(),
      paisOrigen: paisOrigenNombre,
      paisDestino: paisDestinoNombre,
      diceSelection: { paisOrigen: paisOrigenNombre, paisDestino: paisDestinoNombre, maxDados, timerSegundos: 5 },
    });
  }

  defenderDesdeModal() {
    if (!this.jugadorUsuario || !this.paisModalSeleccionado) return;
    const tropas = Math.floor(this.inputTropasModal);
    if (tropas < 1 || tropas > this.ejercitosDisponibles) {
      this.notificationService.warning(`Ingresá entre 1 y ${this.ejercitosDisponibles} ejércitos.`);
      return;
    }
    const body = {
      idJugador: this.jugadorUsuario.idJugador,
      paisesFichas: [{ idPais: this.paisModalSeleccionado.pais.idPais, cantidadFichas: tropas }]
    };
    this.tableroServicio.defenderPais(body).subscribe({
      next: resultado => {
        if (resultado) {
          this.notificationService.success('Ejércitos colocados.');
          // El registro de historial viene del evento WS INCORPORACION (ya incluye país y cantidad)
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
    const paisDest = this.paisesAliados.find(p => p.pais.idPais === Number(this.idPaisDestinoModal))?.pais.nombre ?? '';
    this.tableroServicio.reagruparFichas(dto).subscribe({
      next: resultado => {
        if (resultado) {
          this.cerrarModalPais();
          this.tableroEventService.enqueue({
            tipo: 'REAGRUPAMIENTO',
            titulo: 'REAGRUPAMIENTO',
            descripcion: `${this.jugadorUsuario!.nombre} movió tropas hacia ${paisDest}`,
            jugadorActivo: this.jugadorUsuario!.nombre,
            colorJugador: this.getColorVarJugador(this.jugadorUsuario!.color),
            duracionMs: 2500,
          });
        }
      },
      error: err => { console.error('Error reagrupar:', err); this.notificationService.error('Error al reagrupar.'); }
    });
  }

  cerrarResultadoAtaque() { this.showAtaqueResultado = false; }

  // ── Zoom / Pan ─────────────────────────────────────────────
  private clampTranslate(): void {
    const el = document.querySelector('.mapa-zona') as HTMLElement;
    if (!el) return;
    // Con transform-origin: top left, el rango válido de translate es:
    // X: [-(scale-1)*width, 0]   Y: [-(scale-1)*height, 0]
    this.translateX = Math.max(-(this.scale - 1) * el.clientWidth,  Math.min(0, this.translateX));
    this.translateY = Math.max(-(this.scale - 1) * el.clientHeight, Math.min(0, this.translateY));
  }

  onWheel(event: WheelEvent) {
    event.preventDefault();
    const delta = event.deltaY > 0 ? 0.9 : 1.1;
    this.scale = Math.min(Math.max(this.scale * delta, 1), 3);
    if (this.scale === 1) { this.translateX = 0; this.translateY = 0; }
    else { this.clampTranslate(); }
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
    this.clampTranslate();
    this.lastMouseX = event.clientX;
    this.lastMouseY = event.clientY;
  }

  onMouseUpMapa() { this.isDragging = false; }

  zoomIn() {
    this.scale = Math.min(this.scale * 1.25, 3);
    this.clampTranslate();
  }
  zoomOut() {
    this.scale = Math.max(this.scale / 1.25, 1);
    if (this.scale === 1) { this.translateX = 0; this.translateY = 0; }
    else { this.clampTranslate(); }
  }
  resetZoom() { this.scale = 1; this.translateX = 0; this.translateY = 0; }

  // ── Historial ──────────────────────────────────────────────
  // unshift inserta al principio: el evento más reciente queda en el índice 0
  // y por ende aparece arriba en la lista renderizada — el jugador siempre lee
  // lo último sin necesidad de scrollear. Los eventos viejos caen al final y
  // se descartan al superar 20 entradas.
  agregarHistorial(texto: string, tipo: 'ataque' | 'ok' | 'normal' | 'pacto' = 'normal') {
    this.historial.unshift({ texto, tipo });
    if (this.historial.length > 20) this.historial.pop();
  }

  // ── Chat ───────────────────────────────────────────────────
  enviarChat() {
    if (this.enCooldown) return;
    const texto = this.chatInput.trim();
    if (!texto) return;
    // El maxlength del input es la primera barrera; este recorte protege contra
    // pegado masivo o manipulación del DOM.
    const textoAcotado = texto.slice(0, this.chatMaxLen);

    const ahora = Date.now();
    this.mensajesRecientes = this.mensajesRecientes.filter(t => ahora - t < this.VENTANA_MS);

    if (this.mensajesRecientes.length >= this.LIMITE_MENSAJES) {
      this.activarCooldown();
      return;
    }
    this.mensajesRecientes.push(ahora);

    const usuario = this.authService.getCurrentUser();
    const color = this.jugadorUsuario?.color ?? '';
    this.chatMensajes.push({
      actor: usuario?.usuario ?? this.jugadorUsuario?.nombre ?? 'Yo',
      texto: textoAcotado,
      colorSolido: this.getColorSolido(color),
      colorVar: this.getColorVarJugador(color),
      timestamp: new Date(),
      esPropio: true,
    });
    this.chatInput = '';
    this.chatPegadoAlFondo = true;
    this.scrollChatAlFondoSiCorresponde();
    // TODO: WebSocket chat no implementado — sin topic en backend
  }

  private activarCooldown(): void {
    this.enCooldown = true;
    this.cooldownRestanteSeg = Math.ceil(this.COOLDOWN_MS / 1000);
    this.cooldownInterval = setInterval(() => {
      this.cooldownRestanteSeg--;
      if (this.cooldownRestanteSeg <= 0) {
        this.terminarCooldown();
      }
    }, 1000);
  }

  private terminarCooldown(): void {
    this.enCooldown = false;
    this.cooldownRestanteSeg = 0;
    this.mensajesRecientes = [];
    if (this.cooldownInterval) {
      clearInterval(this.cooldownInterval);
      this.cooldownInterval = null;
    }
  }

  formatearHora(d: Date): string {
    if (!(d instanceof Date)) d = new Date(d);
    const h = d.getHours().toString().padStart(2, '0');
    const m = d.getMinutes().toString().padStart(2, '0');
    return `${h}:${m}`;
  }

  onChatMensajesScroll(): void {
    const el = this.chatMensajesRef?.nativeElement;
    if (!el) return;
    // Margen de 6px para tolerar redondeos de subpíxeles
    this.chatPegadoAlFondo = el.scrollHeight - el.scrollTop - el.clientHeight < 6;
  }

  /** Auto-scroll al último mensaje, respetando si el usuario ya estaba en el fondo.
   *  Se invoca diferido (microtask) para esperar el render del nuevo mensaje. */
  private scrollChatAlFondoSiCorresponde(): void {
    queueMicrotask(() => {
      if (!this.chatPegadoAlFondo) return;
      const el = this.chatMensajesRef?.nativeElement;
      if (!el) return;
      el.scrollTop = el.scrollHeight;
    });
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

  getColorVarJugador(color: string): string {
    switch (color?.toUpperCase()) {
      case 'ROJO':     return 'var(--player-rojo)';
      case 'AZUL':     return 'var(--player-azul)';
      case 'VERDE':    return 'var(--player-verde)';
      case 'NARANJA':  return 'var(--player-naranja)';
      case 'AMARILLO': return 'var(--player-dorado)';
      case 'VIOLETA':  return 'var(--player-purpura)';
      default:         return 'var(--player-rojo)';
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
      case 'ROJO':     return 'assets/vectors/tablero/fichas/red-player.svg';
      case 'AZUL':     return 'assets/vectors/tablero/fichas/blue-player.svg';
      case 'VERDE':    return 'assets/vectors/tablero/fichas/green-player.svg';
      case 'NARANJA':  return 'assets/vectors/tablero/fichas/orange-player.svg';
      case 'AMARILLO': return 'assets/vectors/tablero/fichas/gold-player.svg';
      case 'VIOLETA':  return 'assets/vectors/tablero/fichas/purple-player.svg';
      default:         return 'assets/vectors/tablero/fichas/red-player.svg';
    }
  }

  getInsigniaUrl(color: string): string {
    switch (color?.toUpperCase()) {
      case 'ROJO':     return 'assets/images/tablero/insignias/red-insignia.png';
      case 'AZUL':     return 'assets/images/tablero/insignias/blue-insignia.png';
      case 'VERDE':    return 'assets/images/tablero/insignias/green-insignia.png';
      case 'NARANJA':  return 'assets/images/tablero/insignias/orange-insignia.png';
      case 'AMARILLO': return 'assets/images/tablero/insignias/gold-insignia.png';
      case 'VIOLETA':  return 'assets/images/tablero/insignias/purple-insignia.png';
      default:         return 'assets/images/tablero/insignias/red-insignia.png';
    }
  }

  getAvatarJugador(idJugador: number): string {
    const avatares = [
      'assets/images/avatars/AgosCh.png', 'assets/images/avatars/CandeArguello.png',
      'assets/images/avatars/CandeBlanco.png', 'assets/images/avatars/LaraHeredia.png',
      'assets/images/avatars/MeliAbril.png', 'assets/images/avatars/SofiCirioni.png',
      'assets/images/avatars/Maxi.png'
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
