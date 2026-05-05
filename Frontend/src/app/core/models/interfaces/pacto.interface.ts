export type TipoPacto = 'PACTO_PAISES' | 'PACTO_MUNDIAL' | 'PACTO_ZONA_INTERNACIONAL';

export type EstadoPacto =
  | 'PROPUESTO'
  | 'ACTIVO'
  | 'ROTO_VOLUNTARIO'
  | 'ROTO_AUTOMATICO'
  | 'RECHAZADO'
  | 'EXPIRADO';

export interface PactoDto {
  id: number;
  tipo: TipoPacto;
  estado: EstadoPacto;

  idJugadorA: number;
  nombreJugadorA: string;
  colorJugadorA: string;

  idJugadorB: number;
  nombreJugadorB: string;
  colorJugadorB: string;

  idPaisProtegidoA?: number | null;
  nombrePaisProtegidoA?: string | null;

  idPaisProtegidoB?: number | null;
  nombrePaisProtegidoB?: string | null;

  idPaisZona?: number | null;
  nombrePaisZona?: string | null;
  continenteZona?: string | null;

  idPartida: number;

  fechaPropuesta?: string;
  fechaAceptacion?: string;
  fechaRuptura?: string;

  turnoRupturaJugador?: number | null;
  turnoNumeroAlRomper?: number | null;
}

export interface ProponerPactoDto {
  idPartida: number;
  idJugadorProponente: number;
  idJugadorReceptor: number;
  tipo: TipoPacto;
  idPaisProtegidoA?: number | null;
  idPaisProtegidoB?: number | null;
  idContinenteZona?: number | null;
}
