import {
  AfterViewInit, ChangeDetectionStrategy, Component, ElementRef, EventEmitter,
  Input, OnDestroy, Output, ViewChild,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { gsap } from 'gsap';
import { FinPartida, JugadorResultado } from '../../../../core/models/interfaces/partida.interface';

const COLOR_VAR_MAP: Record<string, string> = {
  ROJO: 'var(--player-rojo)',
  AZUL: 'var(--player-azul)',
  NARANJA: 'var(--player-naranja)',
  VIOLETA: 'var(--player-purpura)',
  VERDE: 'var(--player-verde)',
  AMARILLO: 'var(--player-dorado)',
};

type TipoMedalla = 'gold' | 'silver' | 'bronze';

const MEDALLA_MAP: Record<TipoMedalla, string> = {
  gold: 'assets/images/tablero/medallas/gold-medal.png',
  silver: 'assets/images/tablero/medallas/silver-medal.png',
  bronze: 'assets/images/tablero/medallas/bronze-medal.png',
};

const MESES_ABREVIADOS = [
  'ENE', 'FEB', 'MAR', 'ABR', 'MAY', 'JUN',
  'JUL', 'AGO', 'SEP', 'OCT', 'NOV', 'DIC',
];

/** Fila del ranking del diario: un jugador + su medalla (si corresponde). */
interface FilaRanking {
  resultado: JugadorResultado;
  medalla: TipoMedalla | null;
  esGanador: boolean;
}

const AUTO_AVANCE_MS = 30_000;

/**
 * Relleno tipográfico para los huecos de la hoja: notas de color que no dicen
 * nada del resultado real de la partida, solo dan volumen de "diario impreso".
 * Se recortan por overflow, así que no hace falta que entren enteras.
 */
const TEXTO_RELLENO: readonly string[] = [
  'Los despachos llegados desde el frente confirman que las líneas de suministro ' +
  'permanecieron abiertas durante toda la ofensiva. El estado mayor atribuye el ' +
  'resultado a la coordinación entre las divisiones de reserva y los cuerpos de ' +
  'ingenieros, que trabajaron sin descanso durante las últimas jornadas.',

  'En los cuarteles se comenta que la moral de la tropa se mantuvo alta pese al ' +
  'desgaste de las últimas semanas. Los corresponsales destacan el orden con que ' +
  'se ejecutaron los repliegues y la disciplina de las unidades acantonadas en la ' +
  'retaguardia.',

  'La oficina de prensa anticipa que en las próximas ediciones se publicarán los ' +
  'partes completos de cada continente, junto al detalle de las bajas y los ' +
  'territorios que cambiaron de mando durante el conflicto.',

  'Fuentes diplomáticas señalan que los tratados firmados en el transcurso de la ' +
  'campaña resultaron determinantes para el desenlace. Ninguna de las partes quiso ' +
  'referirse a las cláusulas acordadas en privado.',
];

@Component({
  selector: 'app-fin-partida-overlay',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './fin-partida-overlay.component.html',
  styleUrl: './fin-partida-overlay.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class FinPartidaOverlayComponent implements AfterViewInit, OnDestroy {
  @Input({ required: true }) datos!: FinPartida;
  @Output() verEstadisticas = new EventEmitter<void>();

  @ViewChild('diarioRoot') diarioRoot?: ElementRef<HTMLDivElement>;

  readonly fechaFormateada: string;
  readonly textoRelleno = TEXTO_RELLENO;

  private timeline?: gsap.core.Timeline;
  private autoAvanceTimeout?: ReturnType<typeof setTimeout>;

  constructor() {
    const ahora = new Date();
    const dia = ahora.getDate().toString().padStart(2, '0');
    const mes = MESES_ABREVIADOS[ahora.getMonth()];
    this.fechaFormateada = `${dia} ${mes} 194█`;
  }

  /** Ganador + resto del ranking (ya viene ordenado desc. por país desde el backend). */
  get ranking(): FilaRanking[] {
    const ganadorId = Number(this.datos.ganador.idJugador);
    const ganadorResultado = this.datos.clasificacion.find(j => j.id === ganadorId);
    const resto = this.datos.clasificacion.filter(j => j.id !== ganadorId);

    const filas: FilaRanking[] = [{
      resultado: ganadorResultado ?? {
        id: ganadorId,
        nombre: this.datos.ganador.nombre,
        color: this.datos.ganador.color,
        avatarUrl: this.datos.ganador.url,
        cantidadPaises: 0,
        cantidadEjercitos: this.datos.ganador.ejercito,
        eliminado: false,
      },
      medalla: 'gold',
      esGanador: true,
    }];

    resto.forEach((r, i) => {
      filas.push({ resultado: r, medalla: i === 0 ? 'silver' : i === 1 ? 'bronze' : null, esGanador: false });
    });
    return filas;
  }

  /** Nota principal del diario: el ganador. */
  get filaGanador(): FilaRanking {
    return this.ranking[0];
  }

  /** Segundo y tercer puesto — notas secundarias, a media columna. */
  get filasPodio(): FilaRanking[] {
    return this.ranking.filter(f => f.medalla === 'silver' || f.medalla === 'bronze');
  }

  /** El resto de los comandantes — breves al pie. */
  get filasResto(): FilaRanking[] {
    return this.ranking.filter(f => !f.esGanador && f.medalla === null);
  }


  /** true si a todos los rivales no les quedó ni un país — conquista total del mapa. */
  get esConquistaTotal(): boolean {
    const ganadorId = Number(this.datos.ganador.idJugador);
    const rivales = this.datos.clasificacion.filter(j => j.id !== ganadorId);
    return rivales.length > 0 && rivales.every(j => j.cantidadPaises === 0);
  }

  get titular(): string {
    const nombre = (this.datos.ganador.nombre || 'COMANDANTE').toUpperCase();
    return this.esConquistaTotal
      ? `EL IMPERIO DE ${nombre} CONQUISTÓ TODO EL MAPA`
      : `${nombre} PONE FIN A LA GUERRA Y SE ALZA CON LA VICTORIA`;
  }

  get colorGanadorVar(): string {
    return COLOR_VAR_MAP[this.datos.ganador.color?.toUpperCase()] ?? 'var(--player-rojo)';
  }

  medallaUrl(tipo: TipoMedalla): string {
    return MEDALLA_MAP[tipo];
  }

  colorVarPara(color: string): string {
    return COLOR_VAR_MAP[color?.toUpperCase()] ?? 'var(--player-rojo)';
  }

  /**
   * El backend manda la foto de cada comandante, bots incluidos. El respaldo es
   * genérico a propósito: antes se elegía una cara de una lista fija según
   * `idJugador % 7`, así que el diario ilustraba a un bot con la foto de
   * cualquier integrante del equipo.
   */
  avatarPara(j: JugadorResultado): string {
    return j.avatarUrl || 'assets/images/avatars/bot-avatar.png';
  }

  /** Frase editorial breve por jugador — heurística simple en base a los datos disponibles. */
  fraseParaFila(f: FilaRanking): string {
    if (f.esGanador) {
      return this.esConquistaTotal
        ? 'Sus tropas no dejaron un solo territorio libre en todo el mapa.'
        : `Cumplió su misión antes que ningún otro comandante: ${this.datos.objetivoCumplido.descripcion}.`;
    }
    if (f.resultado.eliminado) {
      return 'Sus fuerzas fueron aniquiladas antes del cese de hostilidades.';
    }
    if (f.medalla === 'silver') {
      return 'El comandante que más cerca estuvo de torcer el destino de la guerra.';
    }
    if (f.medalla === 'bronze') {
      return 'Resistió como una potencia regional hasta el último parte de guerra.';
    }
    if (f.resultado.cantidadEjercitos >= 15) {
      return 'Mantuvo un ejército numeroso pese a la pérdida de territorio.';
    }
    return 'Combatió con honor en el frente hasta el armisticio.';
  }

  ngAfterViewInit(): void {
    const root = this.diarioRoot?.nativeElement;
    if (root) {
      const overlay = root.closest('.fin-overlay');
      const escena = root.closest('.diario-escena');
      // El diario "cae" sobre la mesa y recién después le tiran las medallas
      // encima, una por una.
      this.timeline = gsap.timeline({ defaults: { ease: 'power2.out' } })
        .from(overlay, { opacity: 0, duration: 0.6 })
        .from(escena?.querySelector('.diario-pila') ?? null,
          { opacity: 0, y: -40, duration: 0.6 }, 0.1)
        .from(root, { opacity: 0, y: -70, rotate: -4, duration: 0.8, ease: 'power3.out' }, 0.2)
        .from(root.querySelector('.diario-titulo'),
          { opacity: 0, y: 12, duration: 0.5 }, 0.7)
        .from(root.querySelectorAll('.diario-nota'),
          { opacity: 0, y: 16, duration: 0.4, stagger: 0.09 }, 0.85)
        .from(root.querySelectorAll('.nota-medalla'),
          { opacity: 0, scale: 0.35, rotate: -40, duration: 0.55,
            stagger: 0.14, ease: 'back.out(1.7)' }, 1.15);
      // .btn-continuar no se anima: %btn-teg-base (global) trae su propio
      // `transition: all 0.3s ease` para el hover, que compite con la opacity
      // que setea GSAP sobre el mismo elemento y lo deja pisado en opacity:0.
    }

    this.autoAvanceTimeout = setTimeout(() => this.confirmar(), AUTO_AVANCE_MS);
  }

  ngOnDestroy(): void {
    this.timeline?.kill();
    if (this.autoAvanceTimeout) clearTimeout(this.autoAvanceTimeout);
  }

  onClickContinuar(): void {
    this.confirmar();
  }

  private confirmar(): void {
    if (this.autoAvanceTimeout) {
      clearTimeout(this.autoAvanceTimeout);
      this.autoAvanceTimeout = undefined;
    }
    this.verEstadisticas.emit();
  }
}
