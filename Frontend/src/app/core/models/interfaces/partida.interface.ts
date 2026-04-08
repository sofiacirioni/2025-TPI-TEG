

export enum EstadoPartida {
  TERMINADA = 'TERMINADA',
  PAUSADA = 'PAUSADA',
  EN_JUEGO = 'EN JUEGO',
}

export enum TipoObjetivo {
  SECRETO = 1,
  GENERAL = 2,
}

export enum FaseTurno {
  ATACAR = 'ATACAR',
  MOVER_TROPAS = 'MOVER_TROPAS',
  COLOCACION = 'COLOCACION',
}


export interface PartidaDto {
  idPartida: number;
  estado: EstadoPartida;
  fechaInicio: string;
  configuracion: SalaDto;
  estadoPaises?: EstadoPaisDto[];
  jugadores?: JugadorDto[];
  ganador?: JugadorDto;
  turnos?: TurnoDto[];
  estadoTarjetas?: EstadoTarjetaDto[];
  turnoActual: number;
}

export interface SalaDto {
  idSala: number;
  nombreSala: string;
  url?: string;
}

export interface Continente {
  idContinente: number;
  nombre: string;
}

export interface Pais {
  idPais: number;
  nombre: string;
  continente: Continente;
}

export interface EstadoPaisDto {
  id: number;
  pais: Pais;
  idJugador: number;
  cantidadTropas: number;
}

export interface JugadorDto {
  idJugador: number;
  nombre: string;
  color: string;
  tipoJugador: string;
  idUsuario: number;
  idSala: number;
  objetivo: ObjetivoDto;
  ejercito: number;
  consquisto: boolean;
  url?: string;
}

export interface ObjetivoDto {
  descripcion: string;
  tipoObjetivo: TipoObjetivo;
}

export interface TurnoDto {
  idTurno: number;
  nroTurno: number;
  fase: FaseTurno;
  idJugador: number;
}

export interface EstadoTarjetaDto {
  idEstadoTarjeta: number;
  tarjeta?: TarjetaDto;
  idJugador: number;
  idTurno: number;
  usada: boolean;
  canjeada: boolean;
  jugadorTienePais: boolean;
}

export interface TarjetaDto {
  idtarjeta: number;
  pais: Pais;
  simbolo: string;
}

export interface Continente {
  idContinente: number;
  nombre: string;
}

export interface AtaqueResponseDto {
  ataqueExitoso: boolean;
  conquista: boolean;
  dadosAtaque: number[];
  dadosDefensor: number[];
}

export interface AtaqueDto {
  idJugador: number;
  idPaisOrigen: number;
  idPaisDestino: number;
}

export interface MoverFichas {
  idJugador: number;
  idPaisOrigen: number;
  idPaisDestino: number;
  cantidadFichas: number;
}

export interface VerificacionObjetivo {
  gano: boolean;
  objetivoCumplido: string;
}

export interface UsarTarjetaEnPaisDto {
  idTarjeta: number;
  idJugador: number;
}

export interface CanjeTarjetasDto {
  idTarjetas: number[];
  idJugador: number;
}