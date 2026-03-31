import { Component, Input, OnChanges, SimpleChanges, OnInit, EventEmitter, Output, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { EstadoPaisDto, JugadorDto } from '../../../../core/models/interfaces/partida.interface';
import { paisSVG } from '../../../../core/models/interfaces/pais-svg.interface';

export interface PaisClickEvent {
  estadoPais: EstadoPaisDto;
  event: MouseEvent;
}

/** Colores de borde por continente para distinguir regiones en el mapa */
const CONTINENT_BORDER_COLORS: Record<string, string> = {
  'America del norte': 'rgba(191,104,0,0.55)',
  'America del sur':  'rgba(160,21,21,0.55)',
  'Europa':           'rgba(26,64,128,0.55)',
  'Africa':           'rgba(107,76,0,0.55)',
  'Asia':             'rgba(107,36,144,0.55)',
  'Oceania':          'rgba(30,122,80,0.55)',
};

/** Tintes de relleno por continente para países neutros */
const CONTINENT_NEUTRAL_COLORS: Record<string, string> = {
  'America del norte': 'rgba(191,104,0,0.13)',
  'America del sur':   'rgba(160,21,21,0.13)',
  'Europa':            'rgba(26,64,128,0.13)',
  'Africa':            'rgba(107,76,0,0.13)',
  'Asia':              'rgba(107,36,144,0.13)',
  'Oceania':           'rgba(30,122,80,0.13)',
};

@Component({
  selector: 'app-mapa-svg',
  imports: [],
  templateUrl: './mapa-svg.component.html',
  styleUrl: './mapa-svg.component.css'
})
export class MapaSvgComponent implements OnChanges, OnInit {
  @Input() paises: EstadoPaisDto[] = [];
  @Input() jugadores: JugadorDto[] = [];
  @Output() paisClickeado = new EventEmitter<PaisClickEvent>();

  mapaPais: paisSVG[] = [];

  /** URL del SVG estático — se usa como <image> para preservar decoraciones y estilos originales */
  readonly svgAssetUrl = '/assets/vectors/mapa-teg-vector-optimizado.svg';

  private http = inject(HttpClient);
  private svgPathsCache: Map<number, string> = new Map();
  private svgCentroidsCache: Map<number, { cx: number; cy: number }> = new Map();
  private svgContinentCache: Map<number, string> = new Map();
  private svgCargado = false;

  ngOnInit() {
    this.cargarMapa();
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['paises'] && this.svgCargado) {
      this.construirMapaPais();
    }
  }

  cargarMapa() {
    this.http.get(this.svgAssetUrl, { responseType: 'text' }).subscribe({
      next: (svgText) => {
        const parser = new DOMParser();
        const svgDoc = parser.parseFromString(svgText, 'image/svg+xml');

        // Extraer paths de países (layer2): solo IDs numéricos válidos.
        // Los IDs de continentes (cont-norte, etc.) no son numéricos y se ignoran.
        svgDoc.querySelectorAll('path').forEach(path => {
          const rawId = path.id;
          const numId = Number(rawId);

          if (!isNaN(numId) && numId > 0) {
            this.svgPathsCache.set(numId, path.getAttribute('d') || '');

            // Leer el continente del grupo padre (atributo continent="...")
            const parentGroup = path.parentElement;
            const continent = parentGroup?.getAttribute('continent') ?? '';
            this.svgContinentCache.set(numId, continent);
          }
        });

        this.computarCentroides();
        this.svgCargado = true;
        this.construirMapaPais();
      },
      error: (err) => console.error('Error al cargar el mapa SVG:', err)
    });
  }

  private computarCentroides(): void {
    const tempSvg = document.createElementNS('http://www.w3.org/2000/svg', 'svg');
    tempSvg.style.cssText = 'position:absolute;left:-9999px;top:-9999px;width:1920px;height:1080px;visibility:hidden';
    document.body.appendChild(tempSvg);

    for (const [id, d] of this.svgPathsCache) {
      try {
        const path = document.createElementNS('http://www.w3.org/2000/svg', 'path');
        path.setAttribute('d', d);
        tempSvg.appendChild(path);
        const bbox = path.getBBox();
        this.svgCentroidsCache.set(id, {
          cx: bbox.x + bbox.width / 2,
          cy: bbox.y + bbox.height / 2,
        });
        tempSvg.removeChild(path);
      } catch (_) {}
    }

    document.body.removeChild(tempSvg);
  }

  private construirMapaPais() {
    this.mapaPais = this.paises.map(estadoPais => {
      const centroid = this.svgCentroidsCache.get(estadoPais.pais.idPais) ?? { cx: 0, cy: 0 };
      const continente = this.svgContinentCache.get(estadoPais.pais.idPais) ?? '';
      return {
        id: estadoPais.pais.idPais,
        nombre: estadoPais.pais.nombre,
        color: this.getColorPais(estadoPais, continente),
        colorSolido: this.getColorSolido(estadoPais),
        colorBorde: CONTINENT_BORDER_COLORS[continente] ?? 'rgba(67,42,30,0.35)',
        tropas: estadoPais.cantidadTropas,
        borde: 1,
        opacidad: 1,
        forma: this.svgPathsCache.get(estadoPais.pais.idPais) || '',
        cx: centroid.cx,
        cy: centroid.cy,
        continente,
      };
    });
  }

  clickPais(id: number, event: MouseEvent) {
    const estadoPais = this.paises.find(p => p.pais.idPais === id);
    if (estadoPais) this.paisClickeado.emit({ estadoPais, event });
  }

  /**
   * Fill RGBA con opacidad ~0.4 para efecto papel conquistado.
   * Países sin dueño usan tinte sutil del continente para distinguir regiones.
   */
  getColorPais(estadoPais: EstadoPaisDto, continente?: string): string {
    const cont = continente ?? this.svgContinentCache.get(estadoPais.pais.idPais) ?? '';
    const jugador = this.jugadores.find(j => j.idJugador === estadoPais.idJugador);
    if (!jugador) {
      return CONTINENT_NEUTRAL_COLORS[cont] ?? 'rgba(239,232,206,0.18)';
    }
    switch (jugador.color.toUpperCase()) {
      case 'ROJO':     return 'rgba(160,21,21,0.4)';
      case 'AZUL':     return 'rgba(26,64,128,0.4)';
      case 'VERDE':    return 'rgba(30,122,80,0.4)';
      case 'NARANJA':  return 'rgba(191,104,0,0.4)';
      case 'AMARILLO': return 'rgba(107,76,0,0.4)';
      case 'VIOLETA':  return 'rgba(107,36,144,0.4)';
      default:         return CONTINENT_NEUTRAL_COLORS[cont] ?? 'rgba(239,232,206,0.18)';
    }
  }

  /** Color sólido para fichas (círculo + texto). */
  getColorSolido(estadoPais: EstadoPaisDto): string {
    const jugador = this.jugadores.find(j => j.idJugador === estadoPais.idJugador);
    if (!jugador) return 'rgba(180,150,80,0.5)';
    switch (jugador.color.toUpperCase()) {
      case 'ROJO':     return '#A01515';
      case 'AZUL':     return '#1A4080';
      case 'VERDE':    return '#1E7A50';
      case 'NARANJA':  return '#BF6800';
      case 'AMARILLO': return '#6B4C00';
      case 'VIOLETA':  return '#6B2490';
      default:         return '#555';
    }
  }
}
