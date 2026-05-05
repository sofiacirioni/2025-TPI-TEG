import {
  Component, EventEmitter, Input, OnChanges, OnDestroy, OnInit, Output, SimpleChanges
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { JugadorDto, ObjetivoDto, ObjetivoProgreso } from '../../../../core/models/interfaces/partida.interface';
import { NombrePaisPipe } from '../../../../core/pipes/nombre-pais.pipe';

interface ItemDisplay {
  texto: string;
  valorActual: number;
  valorObjetivo: number;
  completado: boolean;
  /** Color hex del jugador enemigo, para ELIMINAR_JUGADOR */
  colorHex?: string;
  /** true → objetivo de eliminación: mostrar "X países" (valorActual) en vez de "X/N" */
  esEliminacion?: boolean;
}

/** Mapa de enum Color (backend) → CSS hex del design system */
const COLOR_MAP: Record<string, string> = {
  ROJO:     '#A01515',
  AZUL:     '#1A4080',
  NARANJA:  '#BF6800',
  VIOLETA:  '#6B2490',
  VERDE:    '#1E7A50',
  AMARILLO: '#6B4C00',
};

@Component({
  selector: 'app-objetivo-revelacion',
  standalone: true,
  imports: [CommonModule, NombrePaisPipe],
  templateUrl: './objetivo-revelacion.component.html',
  styleUrl: './objetivo-revelacion.component.scss',
})
export class ObjetivoRevelacionComponent implements OnInit, OnChanges, OnDestroy {
  @Input() objetivo!: ObjetivoDto;
  @Input() jugadores: JugadorDto[] = [];
  /** Progreso real del objetivo (viene del backend). Cuando llega, se recalcula
   *  la lista de items — permite mostrar el número real de países restantes del
   *  enemigo en objetivos de eliminación en lugar de un "0 por defecto". */
  @Input() progresoObjetivo: ObjetivoProgreso | null = null;
  @Output() confirmado = new EventEmitter<void>();
  /** Avisa al padre que la animación de cierre (vuelo hacia top-left) empezó.
   *  Permite al tablero mostrar el sobre-objetivo justo antes del aterrizaje. */
  @Output() cerrandoStart = new EventEmitter<void>();

  /** 'cerrado' → 'abriendo' → 'abierto' → 'cerrando' */
  estado: 'cerrado' | 'abriendo' | 'abierto' | 'cerrando' = 'cerrado';

  numeroOrden = Math.floor(1000 + Math.random() * 9000);
  items: ItemDisplay[] = [];
  descripcion = '';

  private autoTimeout?: ReturnType<typeof setTimeout>;
  private animTimeout?: ReturnType<typeof setTimeout>;

  ngOnInit(): void {
    this.buildItems();

    // Iniciar animación de apertura inmediatamente — un frame es suficiente
    // para que Angular asiente el estado "cerrado" antes de transicionar.
    this.animTimeout = setTimeout(() => {
      this.estado = 'abriendo';
      setTimeout(() => { this.estado = 'abierto'; }, 350);
    }, 20);

    // Auto-confirmar a los 12 s (bajado de 15s para no bloquear al jugador)
    this.autoTimeout = setTimeout(() => this.confirmar(), 12_000);
  }

  ngOnChanges(changes: SimpleChanges): void {
    // El progreso REST llega poco después de que el componente se monta. Cuando
    // cambia, recalculamos items para reflejar el valor real (países restantes
    // del enemigo) en lugar del placeholder 0.
    if (changes['progresoObjetivo'] && !changes['progresoObjetivo'].firstChange) {
      this.buildItems();
    }
  }

  ngOnDestroy(): void {
    clearTimeout(this.autoTimeout);
    clearTimeout(this.animTimeout);
  }

  confirmar(): void {
    clearTimeout(this.autoTimeout);
    this.estado = 'cerrando';
    // Aviso inmediato para que el tablero muestre el sobre-objetivo con fade-in
    // mientras este sobre vuela a su posición (evita el "pop" al final).
    this.cerrandoStart.emit();
    // 180ms delay (solapa se cierra + hoja se retrae) + 600ms fly
    setTimeout(() => this.confirmado.emit(), 800);
  }

  private buildItems(): void {
    const obj = this.objetivo;

    if (obj.colorEnemigo) {
      const enemigo = this.jugadores.find(
        j => j.color?.toUpperCase() === obj.colorEnemigo?.toUpperCase()
      );
      const nombre = enemigo?.nombre ?? obj.colorEnemigo;
      const colorHex = COLOR_MAP[obj.colorEnemigo.toUpperCase()] ?? '#432A1E';

      // Países restantes actuales del enemigo: toma el valor real del progreso
      // (valorActual del primer item que el backend calcula en calcularProgreso).
      // Si el progreso aún no llegó, fallback a 0 para no romper el render.
      const paisesRestantes = this.progresoObjetivo?.items?.[0]?.valorActual ?? 0;
      const completado     = this.progresoObjetivo?.items?.[0]?.completado    ?? false;

      this.descripcion = `Destruir al ejército de ${nombre}`;
      this.items = [{
        texto: `Países restantes de ${nombre}`,
        valorActual: paisesRestantes,
        valorObjetivo: 0,
        completado,
        colorHex,
        esEliminacion: true,
      }];
      return;
    }

    this.descripcion = obj.descripcion ?? 'Misión desconocida';
    const parsed: ItemDisplay[] = [];

    if (obj.cantidadPaisesObjetivo && obj.cantidadPaisesObjetivo > 0) {
      parsed.push({ texto: 'Países en dominio total', valorActual: 0, valorObjetivo: obj.cantidadPaisesObjetivo, completado: false });
    }
    this.addContinenteItem(parsed, obj.africa,       'África');
    this.addContinenteItem(parsed, obj.asia,         'Asia');
    this.addContinenteItem(parsed, obj.europa,       'Europa');
    this.addContinenteItem(parsed, obj.americaNorte, 'América del Norte');
    this.addContinenteItem(parsed, obj.americaSur,   'América del Sur');
    this.addContinenteItem(parsed, obj.oceania,      'Oceanía');

    this.items = parsed;
  }

  private addContinenteItem(list: ItemDisplay[], req: number | undefined, nombre: string): void {
    if (!req || req === 0) return;
    list.push({ texto: `Países de ${nombre}`, valorActual: 0, valorObjetivo: req, completado: false });
  }
}
