import {
  Component, EventEmitter, Input, OnDestroy, OnInit, Output
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { JugadorDto, ObjetivoDto } from '../../../../core/models/interfaces/partida.interface';

interface ItemDisplay {
  texto: string;
  valorActual: number;
  valorObjetivo: number;
  completado: boolean;
  /** Color hex del jugador enemigo, para ELIMINAR_JUGADOR */
  colorHex?: string;
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
  imports: [CommonModule],
  templateUrl: './objetivo-revelacion.component.html',
  styleUrl: './objetivo-revelacion.component.scss',
})
export class ObjetivoRevelacionComponent implements OnInit, OnDestroy {
  @Input() objetivo!: ObjetivoDto;
  @Input() jugadores: JugadorDto[] = [];
  @Output() confirmado = new EventEmitter<void>();

  /** 'cerrado' → 'abriendo' → 'abierto' → 'cerrando' */
  estado: 'cerrado' | 'abriendo' | 'abierto' | 'cerrando' = 'cerrado';

  numeroOrden = Math.floor(1000 + Math.random() * 9000);
  items: ItemDisplay[] = [];
  descripcion = '';

  private autoTimeout?: ReturnType<typeof setTimeout>;
  private animTimeout?: ReturnType<typeof setTimeout>;

  ngOnInit(): void {
    this.buildItems();

    // Iniciar animación de apertura — delay mínimo para que el DOM esté listo
    this.animTimeout = setTimeout(() => {
      this.estado = 'abriendo';
      setTimeout(() => { this.estado = 'abierto'; }, 600);
    }, 60);

    // Auto-confirmar a los 15 s
    this.autoTimeout = setTimeout(() => this.confirmar(), 15_000);
  }

  ngOnDestroy(): void {
    clearTimeout(this.autoTimeout);
    clearTimeout(this.animTimeout);
  }

  confirmar(): void {
    clearTimeout(this.autoTimeout);
    this.estado = 'cerrando';
    setTimeout(() => this.confirmado.emit(), 700);
  }

  private buildItems(): void {
    const obj = this.objetivo;

    if (obj.colorEnemigo) {
      const enemigo = this.jugadores.find(
        j => j.color?.toUpperCase() === obj.colorEnemigo?.toUpperCase()
      );
      const nombre = enemigo?.nombre ?? obj.colorEnemigo;
      const colorHex = COLOR_MAP[obj.colorEnemigo.toUpperCase()] ?? '#432A1E';

      this.descripcion = `Destruir al ejército de ${nombre}`;
      this.items = [{
        texto: `Países restantes de ${nombre}`,
        valorActual: 0,
        valorObjetivo: 0,
        completado: false,
        colorHex,
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
