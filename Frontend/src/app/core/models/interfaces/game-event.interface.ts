export type GameEventTipo =
  | 'INCORPORACION'
  | 'ATAQUE_INICIADO'
  | 'RESULTADO_DADOS'
  | 'CONQUISTA'
  | 'TARJETA_OBTENIDA'
  | 'TARJETA_CANJEADA'
  | 'REAGRUPAMIENTO'
  | 'FIN_TURNO';

export interface DiceSelectionParams {
  paisOrigen: string;
  paisDestino: string;
  maxDados: number;
  timerSegundos: number;
}

export interface GameEvent {
  tipo: GameEventTipo;
  titulo: string;
  descripcion?: string;
  jugadorActivo?: string;
  colorJugador?: string;
  paisOrigen?: string;
  paisDestino?: string;
  /** ms de duración. 0 = bloqueante (espera dismissCurrent). Default: 3500 */
  duracionMs?: number;
  // Datos de combate (RESULTADO_DADOS / CONQUISTA)
  dadosAtaque?: number[];
  dadosDefensor?: number[];
  conquista?: boolean;
  perdidasAtacante?: number;
  perdidasDefensor?: number;
  // Parámetros de selección de dados (ATAQUE_INICIADO)
  diceSelection?: DiceSelectionParams;
}

/** DTO que llega via WebSocket desde el backend */
export interface PartidaEventWs {
  tipo: string;
  jugadorNombre: string;
  jugadorColor: string;
  descripcion: string;
  paisOrigen: string;
  paisDestino: string;
  dadosAtaque: number[];
  dadosDefensor: number[];
  conquista: boolean;
  perdidasAtacante: number;
  perdidasDefensor: number;
  idPartida: number;
}
