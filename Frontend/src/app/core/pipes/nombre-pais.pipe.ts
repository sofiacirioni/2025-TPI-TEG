import { Pipe, PipeTransform } from '@angular/core';

/**
 * Corrige ortografía de nombres de países y continentes al mostrarse en la UI.
 * Los valores en la BD omiten tildes y caracteres especiales del español
 * por convención de encoding. Este pipe normaliza solo el display, nunca el dato.
 */
const CORRECCIONES: Record<string, string> = {
  // América del Norte
  'Yukon':            'Yukón',
  'Oregon':           'Oregón',
  'Mexico':           'México',
  'Canada':           'Canadá',
  // América del Sur
  'Peru':             'Perú',
  // Europa (fallback si H2 rompe UTF-8 con ñ)
  'Gran Bretana':     'Gran Bretaña',
  'Espana':           'España',
  // África
  'Etiopia':          'Etiopía',
  'Sudafrica':        'Sudáfrica',
  // Asia
  'Turquia':          'Turquía',
  'Japon':            'Japón',
  'Iran':             'Irán',
  // Continentes
  'America del Norte': 'América del Norte',
  'America del Sur':   'América del Sur',
  'Africa':            'África',
  'Oceania':           'Oceanía',
};

@Pipe({ name: 'nombrePais', standalone: true })
export class NombrePaisPipe implements PipeTransform {
  transform(value: string | null | undefined): string {
    if (!value) return '';
    return CORRECCIONES[value] ?? value;
  }
}
