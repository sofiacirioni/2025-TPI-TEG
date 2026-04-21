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

// Regex word-boundary precomputado para sustituir los nombres dentro de
// textos más largos (ej. descripciones de objetivo).
const REPLACEMENTS: [RegExp, string][] = Object.entries(CORRECCIONES).map(
  ([from, to]) => [new RegExp(`\\b${from}\\b`, 'g'), to]
);

@Pipe({ name: 'nombrePais', standalone: true })
export class NombrePaisPipe implements PipeTransform {
  transform(value: string | null | undefined): string {
    if (!value) return '';
    // Match exacto: resolución rápida para nombres puros ("Mexico").
    if (CORRECCIONES[value]) return CORRECCIONES[value];
    // Fallback: sustituir dentro del string ("Conquistar Mexico y Peru").
    let result = value;
    for (const [re, to] of REPLACEMENTS) result = result.replace(re, to);
    return result;
  }
}
