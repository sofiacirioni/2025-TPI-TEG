export interface paisSVG {
  id: number;
  nombre: string;
  color: string;       // fill rgba con opacidad (ej: rgba(160,21,21,0.45))
  colorSolido: string; // color sólido hex para fichas y UI (ej: #A01515)
  tropas: number;
  borde: number;
  opacidad: number;
  forma: string;
  cx: number;
  cy: number;
}
