export interface paisSVG {
  id: number;
  nombre: string;
  color: string;        // fill rgba con opacidad (ej: rgba(160,21,21,0.4))
  colorSolido: string;  // color sólido hex para fichas y UI (ej: #A01515)
  colorBorde: string;   // color de borde de continente para capa de stroke
  tropas: number;
  borde: number;
  opacidad: number;
  forma: string;
  cx: number;
  cy: number;
  continente: string;
}
