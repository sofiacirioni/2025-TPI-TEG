import { FinPartida } from './partida.interface';
import { PactoDto } from './pacto.interface';

export type GameEventTipo =
  | 'INCORPORACION'
  | 'ATAQUE_INICIADO'
  | 'ATAQUE'
  | 'RESULTADO_DADOS'
  | 'CONQUISTA'
  | 'TARJETA_OBTENIDA'
  | 'TARJETA_CANJEADA'
  | 'REAGRUPAMIENTO'
  | 'FIN_TURNO'
  | 'FIN_PARTIDA'
  | 'PACTO_PROPUESTO'
  | 'PACTO_ACEPTADO'
  | 'PACTO_RECHAZADO'
  | 'PACTO_ROTO';

export interface DiceSelectionParams {
  paisOrigen: string;
  paisDestino: string;
  maxDados: number;
  timerSegundos: number;
  /** Dados ya seleccionados por el atacante (mostrado también al defensor). */
  cantDadosAtacante?: number;
}

export interface GameEvent {
  tipo: GameEventTipo;
  titulo: string;
  descripcion?: string;
  jugadorActivo?: string;
  colorJugador?: string;
  /** Clave de color sin procesar (ROJO, AZUL, ...) — permite al consumidor
   *  derivar el hex o el asset SVG de la ficha del jugador. */
  colorJugadorKey?: string;
  jugadorDefensor?: string;
  colorDefensor?: string;
  colorDefensorKey?: string;
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
  // IDs de atacante/defensor (útil para rol en ATAQUE_INICIADO y resolución)
  idAtacante?: number;
  idDefensor?: number;
  // Identificador de partida (necesario para defenderAtaque desde el modal)
  idPartida?: number;
  // Parámetros de selección de dados (ATAQUE_INICIADO)
  diceSelection?: DiceSelectionParams;
  /** true cuando la cola tiene backlog (>2 eventos pendientes). El componente
   *  acorta el slot-machine y la duración del modal para no acumular delay. */
  fastMode?: boolean;
  /** Payload completo del pacto cuando tipo es PACTO_*. */
  pacto?: PactoDto;
}

/** DTO que llega via WebSocket desde el backend */
export interface PartidaEventWs {
  tipo: string;
  jugadorNombre: string;
  jugadorColor: string;
  jugadorDefensor?: string;
  jugadorColorDefensor?: string;
  descripcion: string;
  paisOrigen: string;
  paisDestino: string;
  dadosAtaque: number[];
  dadosDefensor: number[];
  conquista: boolean;
  perdidasAtacante: number;
  perdidasDefensor: number;
  idPartida: number;
  // Campos específicos de ATAQUE_INICIADO
  cantDadosAtacante?: number;
  maxDadosDefensor?: number;
  timerSegundos?: number;
  idAtacante?: number;
  idDefensor?: number;
  /** Payload cuando tipo === 'FIN_PARTIDA'. */
  finPartida?: FinPartida;
  /** Payload cuando tipo === 'PACTO_*'. */
  pacto?: PactoDto;
}
