import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import {
  ComandanteResumen,
  EstadisticaService,
  ResumenPartida,
} from '../../core/services/estadistica.service';

const COLOR_VAR_MAP: Record<string, string> = {
  ROJO: 'var(--player-rojo)',
  AZUL: 'var(--player-azul)',
  NARANJA: 'var(--player-naranja)',
  VIOLETA: 'var(--player-purpura)',
  VERDE: 'var(--player-verde)',
  AMARILLO: 'var(--player-dorado)',
};

const MESES_ABREVIADOS = [
  'ENE', 'FEB', 'MAR', 'ABR', 'MAY', 'JUN',
  'JUL', 'AGO', 'SEP', 'OCT', 'NOV', 'DIC',
];

@Component({
  standalone: true,
  selector: 'app-estadisticas',
  templateUrl: './estadistica.component.html',
  styleUrl: './estadistica.component.scss',
  imports: [CommonModule],
})
export class EstadisticaComponent implements OnInit {
  private estadisticaService = inject(EstadisticaService);
  private route = inject(ActivatedRoute);
  private router = inject(Router);

  resumen: ResumenPartida | null = null;
  cargando = true;
  error = false;

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('idPartida'));
    if (!id) {
      // Se entró sin partida (URL a mano): no hay parte que mostrar.
      this.cargando = false;
      this.error = true;
      return;
    }

    this.estadisticaService.getResumenPartida(id).subscribe({
      next: (r) => { this.resumen = r; this.cargando = false; },
      error: () => { this.error = true; this.cargando = false; },
    });
  }

  /** Jackson manda LocalDate como [año, mes, día]. */
  get fechaFormateada(): string {
    const f = this.resumen?.fecha;
    if (!f) return '';
    let dia: number, mes: number;
    if (Array.isArray(f)) {
      mes = f[1]; dia = f[2];
    } else {
      const d = new Date(f);
      mes = d.getMonth() + 1; dia = d.getDate();
    }
    return `${String(dia).padStart(2, '0')} ${MESES_ABREVIADOS[mes - 1]} 194█`;
  }

  colorVarPara(color?: string): string {
    return COLOR_VAR_MAP[(color ?? '').toUpperCase()] ?? 'var(--color-claro)';
  }

  /** Nombre del color en minúsculas, para el texto de "eliminado por". */
  nombreColor(color?: string): string {
    if (!color) return '';
    const c = color.toLowerCase();
    return c === 'violeta' ? 'púrpura' : c;
  }

  estadoDe(c: ComandanteResumen): string {
    if (c.ganador) return 'VICTORIA';
    if (c.eliminado) return 'ELIMINADO';
    return 'EN PIE';
  }

  volver(): void {
    this.router.navigate(['/principal']);
  }
}

