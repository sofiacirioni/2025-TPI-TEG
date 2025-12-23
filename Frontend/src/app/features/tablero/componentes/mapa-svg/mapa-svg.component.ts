import {Component, Input, OnChanges, SimpleChanges, OnInit, EventEmitter, Output} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {EstadoPaisDto, JugadorDto, PartidaDto} from '../../../../core/models/interfaces/partida.interface';
import {paisSVG} from '../../../../core/models/interfaces/pais-svg.interface';


@Component({
  selector: 'app-mapa-svg',
  imports: [],
  templateUrl: './mapa-svg.component.html',
  styleUrl: './mapa-svg.component.css'
})
export class MapaSvgComponent implements OnChanges, OnInit, OnChanges {
  @Input() paises: EstadoPaisDto[] = [];
  @Input() jugadores: JugadorDto[] = [];

  @Output() paisClickeado: EventEmitter<EstadoPaisDto> = new EventEmitter();

  mapaPais: paisSVG[] = [];

  constructor(private http: HttpClient) {

  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['paises']) {
      this.cargarMapa();
    }
  }

  ngOnInit() {
    this.cargarMapa();
  }

  cargarMapa() {
    this.http.get('mapaa.svg', {responseType: 'text'}).subscribe((svgText) => {
      const parser = new DOMParser();
      const svgDoc = parser.parseFromString(svgText, 'image/svg+xml');
      const paths = svgDoc.querySelectorAll('path');
      this.mapaPais = [];
      this.paises.forEach(estadoPais => {
        const path = Array.from(paths).find(p => Number (p.id) === estadoPais.pais.idPais);
        this.mapaPais.push({
          id: estadoPais.pais.idPais,
          nombre: estadoPais.pais.nombre,
          color: this.obtenerColor(estadoPais.idJugador),
          tropas: estadoPais.cantidadTropas,
          borde: 1,
          opacidad: 0.8,
          forma: path?.getAttribute('d') || '',
        });
      });
    })
  }

  clickPais(id: any) {
    const estadoPais: EstadoPaisDto | undefined = this.paises.find(p => p.pais.idPais === id);
    this.paisClickeado.emit(estadoPais);
  }

  obtenerColor(idJugador: number): string {
    const color = this.jugadores.find(j => j.idJugador == idJugador).color

    switch (color.toLowerCase()) {
      case 'rojo':
        return '#f44336';
      case 'verde':
        return '#009688';
      case 'azul':
        return '#3f51b5';
      case 'amarillo':
        return '#ffc107';
      case 'violeta':
        return '#9c27b0';
      case 'naranja':
        return '#ff9800';
      default:
        return '#000';
    }
  }
}
