import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { EstadisticaService, Estadistica} from '../../core/services/estadistica.service';

@Component({
  standalone: true,
  selector: 'app-estadisticas',
  templateUrl: './estadistica.component.html',
  styleUrls: ['./estadistica.component.css'],
  imports: [CommonModule, RouterLink]
})
export class EstadisticaComponent implements OnInit {
  estadisticas: Estadistica[] = [];

  constructor(private estadisticaService: EstadisticaService) {}

  ngOnInit(): void {
    const idUsuario = 1;
    this.estadisticaService.getEstadisticasPorUsuario(idUsuario).subscribe({
      next: (data) => {
        this.estadisticas = data;
      },
      error: (err) => {
        console.error('Error al obtener estadísticas', err);
      }
    });
  }
}
