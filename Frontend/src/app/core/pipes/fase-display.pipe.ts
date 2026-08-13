import { Pipe, PipeTransform } from '@angular/core';

const FASES: Record<string, string> = {
  'INCORPORACION': 'INCORPORACIÓN',
  'ATAQUE':        'ATAQUE',
  'REAGRUPACION':  'REAGRUPACIÓN',
};

@Pipe({ name: 'faseDisplay', standalone: true })
export class FaseDisplayPipe implements PipeTransform {
  transform(value: string | null | undefined): string {
    if (!value) return '—';
    return FASES[value] ?? value.replace(/_/g, ' ');
  }
}
