import { Component, Input, OnChanges, SimpleChanges, OnInit, EventEmitter, Output, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { EstadoPaisDto, JugadorDto } from '../../../../core/models/interfaces/partida.interface';
import { paisSVG } from '../../../../core/models/interfaces/pais-svg.interface';

export interface PaisClickEvent {
  estadoPais: EstadoPaisDto;
  event: MouseEvent;
}

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
    this.http.get('/assets/vectors/mapa-teg-vector-optimizado.svg', { responseType: 'text' }).subscribe({
      next: (svgText) => {
        const parser = new DOMParser();
        const svgDoc = parser.parseFromString(svgText, 'image/svg+xml');

        svgDoc.querySelectorAll('path').forEach(path => {
          const id = Number(path.id);
          if (!isNaN(id) && id > 0) {
            this.svgPathsCache.set(id, path.getAttribute('d') || '');
          }
        });

        const layer3 = svgDoc.getElementById('layer3');
        if (layer3) {
          layer3.querySelectorAll('path').forEach(path => {
            const d = path.getAttribute('d');
            if (d) this.labelPaths.push(d);
          });
        }

        svgDoc.querySelectorAll('path').forEach(el => {
          if (el.getAttribute('inkscape:label') === 'map-lines') {
            const d = el.getAttribute('d');
            if (d) this.mapBorderPath = d;
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
      return {
        id: estadoPais.pais.idPais,
        nombre: estadoPais.pais.nombre,
        color: this.getColorPais(estadoPais),
        colorSolido: this.getColorSolido(estadoPais),
        tropas: estadoPais.cantidadTropas,
        borde: 1,
        opacidad: 1,
        forma: this.svgPathsCache.get(estadoPais.pais.idPais) || '',
        cx: centroid.cx,
        cy: centroid.cy,
      };
    });
  }

  clickPais(id: number, event: MouseEvent) {
    const estadoPais = this.paises.find(p => p.pais.idPais === id);
    if (estadoPais) this.paisClickeado.emit({ estadoPais, event });
  }

  // Fill con opacidad para efecto papel
  getColorPais(estadoPais: EstadoPaisDto): string {
    const jugador = this.jugadores.find(j => j.idJugador === estadoPais.idJugador);
    if (!jugador) return 'rgba(180,150,80,0.25)';
    switch (jugador.color.toUpperCase()) {
      case 'ROJO':     return 'rgba(160,21,21,0.45)';
      case 'AZUL':     return 'rgba(26,64,128,0.45)';
      case 'VERDE':    return 'rgba(30,122,80,0.45)';
      case 'NARANJA':  return 'rgba(191,104,0,0.45)';
      case 'AMARILLO': return 'rgba(107,76,0,0.45)';
      case 'VIOLETA':  return 'rgba(107,36,144,0.45)';
      default:         return 'rgba(180,150,80,0.25)';
    }
  }

  // Color sólido para fichas (círculo + texto)
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
