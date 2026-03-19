import { Component, Input, OnChanges, SimpleChanges, OnInit, EventEmitter, Output, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { EstadoPaisDto, JugadorDto } from '../../../../core/models/interfaces/partida.interface';
import { paisSVG } from '../../../../core/models/interfaces/pais-svg.interface';

@Component({
  selector: 'app-mapa-svg',
  imports: [],
  templateUrl: './mapa-svg.component.html',
  styleUrl: './mapa-svg.component.css'
})
export class MapaSvgComponent implements OnChanges, OnInit {
  @Input() paises: EstadoPaisDto[] = [];
  @Input() jugadores: JugadorDto[] = [];
  @Output() paisClickeado = new EventEmitter<EstadoPaisDto>();

  mapaPais: paisSVG[] = [];
  labelPaths: string[] = [];
  mapBorderPath: string = '';

  private http = inject(HttpClient);
  private svgPathsCache: Map<number, string> = new Map();
  private svgCentroidsCache: Map<number, { cx: number; cy: number }> = new Map();
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
    this.http.get('mapa-teg-vector-optimizado.svg', { responseType: 'text' }).subscribe({
      next: (svgText) => {
        const parser = new DOMParser();
        const svgDoc = parser.parseFromString(svgText, 'image/svg+xml');

        // Extraer paths de países (IDs numéricos 1-50)
        svgDoc.querySelectorAll('path').forEach(path => {
          const id = Number(path.id);
          if (!isNaN(id) && id > 0) {
            this.svgPathsCache.set(id, path.getAttribute('d') || '');
          }
        });

        // Extraer etiquetas de layer3
        const layer3 = svgDoc.getElementById('layer3');
        if (layer3) {
          layer3.querySelectorAll('path').forEach(path => {
            const d = path.getAttribute('d');
            if (d) this.labelPaths.push(d);
          });
        }

        // Extraer borde del mapa (map-lines) - buscar por atributo inkscape:label
        svgDoc.querySelectorAll('path').forEach(el => {
          if (el.getAttribute('inkscape:label') === 'map-lines') {
            const d = el.getAttribute('d');
            if (d) this.mapBorderPath = d;
          }
        });

        // Computar centroides usando SVG temporal en el DOM
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
      return {
        id: estadoPais.pais.idPais,
        nombre: estadoPais.pais.nombre,
        color: this.obtenerColor(estadoPais.idJugador),
        tropas: estadoPais.cantidadTropas,
        borde: 1,
        opacidad: 0.8,
        forma: this.svgPathsCache.get(estadoPais.pais.idPais) || '',
        cx: centroid.cx,
        cy: centroid.cy,
      };
    });
  }

  clickPais(id: number) {
    const estadoPais = this.paises.find(p => p.pais.idPais === id);
    if (estadoPais) this.paisClickeado.emit(estadoPais);
  }

  obtenerColor(idJugador: number): string {
    const jugador = this.jugadores.find(j => j.idJugador === idJugador);
    if (!jugador) return '#888';
    switch (jugador.color.toLowerCase()) {
      case 'rojo':     return '#c0392b';
      case 'verde':    return '#1a7a4a';
      case 'azul':     return '#2c5f8a';
      case 'amarillo': return '#c9a84c';
      case 'violeta':  return '#7d3c98';
      case 'naranja':  return '#c0621a';
      default:         return '#888';
    }
  }
}
