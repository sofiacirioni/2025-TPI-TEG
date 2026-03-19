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

  private http = inject(HttpClient);
  private svgPathsCache: Map<number, string> = new Map();
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
        svgDoc.querySelectorAll('path').forEach(path => {
          const id = Number(path.id);
          if (!isNaN(id)) {
            this.svgPathsCache.set(id, path.getAttribute('d') || '');
          }
        });
        this.svgCargado = true;
        this.construirMapaPais();
      },
      error: (err) => console.error('Error al cargar el mapa SVG:', err)
    });
  }

  private construirMapaPais() {
    this.mapaPais = this.paises.map(estadoPais => ({
      id: estadoPais.pais.idPais,
      nombre: estadoPais.pais.nombre,
      color: this.obtenerColor(estadoPais.idJugador),
      tropas: estadoPais.cantidadTropas,
      borde: 1,
      opacidad: 0.8,
      forma: this.svgPathsCache.get(estadoPais.pais.idPais) || '',
    }));
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
