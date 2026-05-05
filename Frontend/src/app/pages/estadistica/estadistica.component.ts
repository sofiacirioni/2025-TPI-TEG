import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { EstadisticaService, Estadistica} from '../../core/services/estadistica.service';
import { AuthService } from '../../core/services/auth.service';

@Component({
  standalone: true,
  selector: 'app-estadisticas',
  templateUrl: './estadistica.component.html',
  styleUrls: ['./estadistica.component.css'],
  imports: [CommonModule, RouterLink]
})
export class EstadisticaComponent implements OnInit {
  estadisticas: Estadistica[] = [];

  private estadisticaService = inject(EstadisticaService);
  private authService = inject(AuthService);

  ngOnInit(): void {
    const idUsuario = this.authService.getCurrentUser()?.idUsuario ?? 0;
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
