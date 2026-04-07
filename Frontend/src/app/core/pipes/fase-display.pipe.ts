import { Pipe, PipeTransform } from '@angular/core';

const FASES: Record<string, string> = {
  'COLOCACION':   'COLOCACIÓN',
  'ATACAR':       'ATACAR',
  'MOVER_TROPAS': 'MOVER TROPAS',
};

@Pipe({ name: 'faseDisplay', standalone: true })
export class FaseDisplayPipe implements PipeTransform {
  transform(value: string | null | undefined): string {
    if (!value) return '—';
    return FASES[value] ?? value.replace(/_/g, ' ');
  }
}
