import { CommonModule } from '@angular/common';
import { Component, EventEmitter, HostListener, Input, OnChanges, OnInit, Output, SimpleChanges } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { PactoService } from '../../../../core/services/pacto.service';
import { NotificationService } from '../../../../core/services/notification.service';
import { EstadoPaisDto, JugadorDto, PartidaDto } from '../../../../core/models/interfaces/partida.interface';
import { PactoDto, ProponerPactoDto, TipoPacto } from '../../../../core/models/interfaces/pacto.interface';

type Vista = 'lista' | 'wizard-tipo' | 'wizard-receptor' | 'wizard-detalles';

interface ContinenteDisponible {
  idContinente: number;
  nombre: string;
  paisAislado: EstadoPaisDto;
}

@Component({
  selector: 'app-pactos-overlay',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './pactos-overlay.component.html',
  styleUrl: './pactos-overlay.component.scss',
})
export class PactosOverlayComponent implements OnInit, OnChanges {
  @Input() partida!: PartidaDto;
  @Input() jugadorLocal!: JugadorDto;
  @Input() esMiTurno = false;
  @Input() pactosActivos: PactoDto[] = [];

  @Output() cerrar = new EventEmitter<void>();
  @Output() pactoCambiado = new EventEmitter<void>();

  vista: Vista = 'lista';

  // Wizard state
  tipoSeleccionado: TipoPacto | null = null;
  receptorSeleccionado: JugadorDto | null = null;
  paisProtegidoLocalId: number | null = null;
  paisProtegidoReceptorId: number | null = null;
  continenteSeleccionadoId: number | null = null;

  enviando = false;

  constructor(
    private pactoService: PactoService,
    private notificationService: NotificationService,
  ) {}

  ngOnInit(): void {
    this.recalcularEstado();
  }

  ngOnChanges(_changes: SimpleChanges): void {
    this.recalcularEstado();
  }

  private recalcularEstado(): void { /* hook for derived state if needed */ }

  @HostListener('document:keydown.escape')
  onEscape(): void {
    this.cerrar.emit();
  }

  onBackdropClick(): void {
    this.cerrar.emit();
  }

  // ───────── Lista de activos ─────────

  esParteDelPacto(p: PactoDto): boolean {
    return p.idJugadorA === this.jugadorLocal.idJugador
        || p.idJugadorB === this.jugadorLocal.idJugador;
  }

  descripcionPacto(p: PactoDto): string {
    switch (p.tipo) {
      case 'PACTO_PAISES':
        return `${p.nombrePaisProtegidoA ?? '—'} ⟷ ${p.nombrePaisProtegidoB ?? '—'}`;
      case 'PACTO_MUNDIAL':
        return 'Sin restricción geográfica';
      case 'PACTO_ZONA_INTERNACIONAL':
        return `${p.nombrePaisZona ?? '—'} — Continente ${p.continenteZona ?? '—'}`;
    }
  }

  tituloTipoPacto(t: TipoPacto): string {
    switch (t) {
      case 'PACTO_PAISES': return 'PACTO ENTRE PAÍSES';
      case 'PACTO_MUNDIAL': return 'NO AGRESIÓN MUNDIAL';
      case 'PACTO_ZONA_INTERNACIONAL': return 'ZONA INTERNACIONAL';
    }
  }

  romperPacto(p: PactoDto): void {
    if (this.enviando) return;
    this.enviando = true;
    this.pactoService.romper(p.id, this.jugadorLocal.idJugador).subscribe({
      next: () => {
        this.enviando = false;
        this.pactoCambiado.emit();
      },
      error: (err) => {
        this.enviando = false;
        this.notificationService.error(this.extraerError(err) ?? 'No se pudo romper el pacto.');
      },
    });
  }

  // ───────── Wizard ─────────

  irAWizard(): void {
    if (!this.esMiTurno) return;
    this.vista = 'wizard-tipo';
    this.tipoSeleccionado = null;
    this.receptorSeleccionado = null;
    this.paisProtegidoLocalId = null;
    this.paisProtegidoReceptorId = null;
    this.continenteSeleccionadoId = null;
  }

  volverAlInicioWizard(): void {
    this.vista = 'lista';
  }

  seleccionarTipo(t: TipoPacto): void {
    if (t === 'PACTO_ZONA_INTERNACIONAL' && this.continentesDisponibles.length === 0) return;
    this.tipoSeleccionado = t;
  }

  avanzarASelecionReceptor(): void {
    if (!this.tipoSeleccionado) return;
    this.vista = 'wizard-receptor';
  }

  jugadoresDisponibles(): JugadorDto[] {
    return (this.partida.jugadores ?? []).filter(j =>
      j.idJugador !== this.jugadorLocal.idJugador
    );
  }

  yaHayPactoActivoCon(jugador: JugadorDto): boolean {
    if (!this.tipoSeleccionado) return false;
    return this.pactosActivos.some(p =>
      p.tipo === this.tipoSeleccionado &&
      (p.estado === 'ACTIVO' || p.estado === 'ROTO_VOLUNTARIO') &&
      ((p.idJugadorA === this.jugadorLocal.idJugador && p.idJugadorB === jugador.idJugador) ||
       (p.idJugadorB === this.jugadorLocal.idJugador && p.idJugadorA === jugador.idJugador))
    );
  }

  paisesPropios(): EstadoPaisDto[] {
    return (this.partida.estadoPaises ?? []).filter(e => e.idJugador === this.jugadorLocal.idJugador);
  }

  paisesDelReceptor(): EstadoPaisDto[] {
    if (!this.receptorSeleccionado) return [];
    return (this.partida.estadoPaises ?? []).filter(e => e.idJugador === this.receptorSeleccionado!.idJugador);
  }

  cantidadPaisesJugador(jugador: JugadorDto): number {
    return (this.partida.estadoPaises ?? []).filter(e => e.idJugador === jugador.idJugador).length;
  }

  cantidadEjercitosJugador(jugador: JugadorDto): number {
    return (this.partida.estadoPaises ?? [])
      .filter(e => e.idJugador === jugador.idJugador)
      .reduce((sum, e) => sum + (e.cantidadTropas ?? 0), 0);
  }

  /** Continentes en los que el jugador local tiene todos los países menos uno. */
  get continentesDisponibles(): ContinenteDisponible[] {
    const map = new Map<number, { nombre: string; paises: EstadoPaisDto[] }>();
    for (const e of this.partida.estadoPaises ?? []) {
      const c = e.pais.continente;
      if (!c) continue;
      if (!map.has(c.idContinente)) {
        map.set(c.idContinente, { nombre: c.nombre, paises: [] });
      }
      map.get(c.idContinente)!.paises.push(e);
    }

    const result: ContinenteDisponible[] = [];
    for (const [idContinente, { nombre, paises }] of map.entries()) {
      const propios = paises.filter(p => p.idJugador === this.jugadorLocal.idJugador);
      const ajenos = paises.filter(p => p.idJugador !== this.jugadorLocal.idJugador);
      if (propios.length === paises.length - 1 && ajenos.length === 1) {
        result.push({ idContinente, nombre, paisAislado: ajenos[0] });
      }
    }
    return result;
  }

  duenoDelPaisAislado(continenteId: number): JugadorDto | null {
    const cont = this.continentesDisponibles.find(c => c.idContinente === continenteId);
    if (!cont) return null;
    return (this.partida.jugadores ?? []).find(j => j.idJugador === cont.paisAislado.idJugador) ?? null;
  }

  jugadoresVisiblesParaReceptor(): JugadorDto[] {
    if (this.tipoSeleccionado !== 'PACTO_ZONA_INTERNACIONAL') {
      return this.jugadoresDisponibles();
    }
    // Solo los dueños del país aislado en algún continente disponible.
    const ids = new Set(this.continentesDisponibles.map(c => c.paisAislado.idJugador));
    return this.jugadoresDisponibles().filter(j => ids.has(j.idJugador));
  }

  seleccionarReceptor(j: JugadorDto): void {
    if (this.yaHayPactoActivoCon(j)) return;
    this.receptorSeleccionado = j;
    this.vista = 'wizard-detalles';

    // Pre-selección para zona internacional
    if (this.tipoSeleccionado === 'PACTO_ZONA_INTERNACIONAL') {
      const cont = this.continentesDisponibles.find(c => c.paisAislado.idJugador === j.idJugador);
      if (cont) this.continenteSeleccionadoId = cont.idContinente;
    }
  }

  volverASeleccionTipo(): void {
    this.vista = 'wizard-tipo';
  }

  volverASeleccionReceptor(): void {
    this.vista = 'wizard-receptor';
  }

  // ───────── Vista previa ─────────

  get nombrePaisProtegidoLocal(): string {
    return this.paisesPropios().find(e => e.pais.idPais === this.paisProtegidoLocalId)?.pais.nombre ?? '—';
  }
  get nombrePaisProtegidoReceptor(): string {
    return this.paisesDelReceptor().find(e => e.pais.idPais === this.paisProtegidoReceptorId)?.pais.nombre ?? '—';
  }
  get continenteSeleccionado(): ContinenteDisponible | null {
    return this.continentesDisponibles.find(c => c.idContinente === this.continenteSeleccionadoId) ?? null;
  }

  puedeEnviar(): boolean {
    if (!this.tipoSeleccionado || !this.receptorSeleccionado) return false;
    switch (this.tipoSeleccionado) {
      case 'PACTO_PAISES':
        return !!this.paisProtegidoLocalId && !!this.paisProtegidoReceptorId;
      case 'PACTO_MUNDIAL':
        return true;
      case 'PACTO_ZONA_INTERNACIONAL':
        return !!this.continenteSeleccionadoId;
    }
  }

  enviarPropuesta(): void {
    if (!this.puedeEnviar() || this.enviando) return;
    this.enviando = true;

    const dto: ProponerPactoDto = {
      idPartida: this.partida.idPartida,
      idJugadorProponente: this.jugadorLocal.idJugador,
      idJugadorReceptor: this.receptorSeleccionado!.idJugador,
      tipo: this.tipoSeleccionado!,
      idPaisProtegidoA: this.tipoSeleccionado === 'PACTO_PAISES' ? this.paisProtegidoLocalId : null,
      idPaisProtegidoB: this.tipoSeleccionado === 'PACTO_PAISES' ? this.paisProtegidoReceptorId : null,
      idContinenteZona: this.tipoSeleccionado === 'PACTO_ZONA_INTERNACIONAL' ? this.continenteSeleccionadoId : null,
    };

    this.pactoService.proponer(dto).subscribe({
      next: () => {
        this.enviando = false;
        this.notificationService.success(`Propuesta enviada a ${this.receptorSeleccionado!.nombre}.`);
        this.pactoCambiado.emit();
        this.cerrar.emit();
      },
      error: (err) => {
        this.enviando = false;
        this.notificationService.error(this.extraerError(err) ?? 'No se pudo enviar la propuesta.');
      },
    });
  }

  private extraerError(err: any): string | null {
    if (!err) return null;
    if (typeof err.error === 'string') return err.error;
    if (err.error?.message) return err.error.message;
    if (err.message) return err.message;
    return null;
  }

  colorVar(colorKey: string): string {
    const map: Record<string, string> = {
      ROJO: 'var(--player-rojo)',
      AZUL: 'var(--player-azul)',
      VERDE: 'var(--player-verde)',
      NARANJA: 'var(--player-naranja)',
      AMARILLO: 'var(--player-dorado)',
      VIOLETA: 'var(--player-purpura)',
    };
    return map[(colorKey ?? '').toUpperCase()] ?? 'var(--color-oscuro)';
  }
}
