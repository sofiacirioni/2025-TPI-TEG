// src/app/features/socketService/socket.service.ts
import { Injectable } from '@angular/core';
import { Client, IMessage, StompSubscription } from '@stomp/stompjs';
import * as SockJS from 'sockjs-client';
import { environment } from '../../../environments/environment';
import { PartidaEventWs } from '../models/interfaces/game-event.interface';

@Injectable({ providedIn: 'root' })
export class WebSocketService {
  private stompClient: Client;
  private conectado: boolean = false;
  private salaId: number | null = null;
  private partidaSub: StompSubscription | null = null;

  constructor() {
    this.stompClient = new Client({
      webSocketFactory: () => new SockJS(environment.wsUrl),
      reconnectDelay: 5000,
      debug: (str) => console.log('[STOMP]', str)
    });
  }

  conectar(salaId: number, callbackJugador?: (jugador: any) => void, token?: string) {
    if (this.conectado && this.salaId === salaId) return;

    this.salaId = salaId;

    if (token) {
      this.stompClient.connectHeaders = { Authorization: `Bearer ${token}` };
    }

    this.stompClient.onConnect = () => {
      console.log('✅ Conectado al WebSocket');
      this.conectado = true;

      if (callbackJugador) {
        this.stompClient.subscribe(`/topic/sala.${salaId}`, (message: IMessage) => {
          const jugador = JSON.parse(message.body);
          callbackJugador(jugador);
        });
      }

      this.stompClient.subscribe(`/topic/sala.${salaId}.inicio`, (message: IMessage) => {
        const data = JSON.parse(message.body);
        if (this.callbackInicioPartida) {
          this.callbackInicioPartida(data);
        }
      });
    };

    this.stompClient.activate();
  }

  private callbackInicioPartida: ((data: any) => void) | null = null;

  suscribirseInicioPartida(salaId: number, callback: (data: any) => void) {
    if (!this.conectado) {
      this.callbackInicioPartida = callback;
      return; // la conexión se encargará de la subscripción al conectarse
    }

    this.stompClient.subscribe(`/topic/sala.${salaId}.inicio`, (message: IMessage) => {
      const data = JSON.parse(message.body);
      callback(data);
    });
  }

  emitirNuevoJugador(salaId: number, jugador: any) {
    if (!this.stompClient || !this.conectado) {
      console.warn('❌ No conectado aún. No se puede emitir jugador.');
      return;
    }

    this.stompClient.publish({
      destination: `/app/sala.${salaId}`,
      body: JSON.stringify(jugador)
    });
  }

  emitirInicioPartida(salaId: number, data: { url: string }) {
    if (!this.stompClient || !this.conectado) {
      console.warn('❌ No conectado aún. No se puede emitir inicio de partida.');
      return;
    }

    this.stompClient.publish({
      destination: `/app/sala.${salaId}.inicio`,
      body: JSON.stringify(data)
    });
  }

  /**
   * Suscribe al topic de eventos de la partida.
   * Llama a `callback` cada vez que el backend emite un evento de juego.
   * Si ya hay una suscripción activa para esta partida, no duplica.
   */
  suscribirseEventosPartida(idPartida: number, callback: (evento: PartidaEventWs) => void): void {
    const topic = `/topic/partida.${idPartida}.evento`;

    if (!this.conectado) {
      // Registrar para cuando se conecte (solo para reconexiones tardías)
      const origOnConnect = this.stompClient.onConnect;
      this.stompClient.onConnect = (frame) => {
        if (origOnConnect) origOnConnect.call(this.stompClient, frame);
        this.doSuscribirPartida(topic, callback);
      };
      if (this.stompClient.active) {
        // ya activado pero aún no conectado — esperar el callback
      } else {
        this.stompClient.activate();
      }
      return;
    }

    this.doSuscribirPartida(topic, callback);
  }

  private doSuscribirPartida(topic: string, callback: (e: PartidaEventWs) => void): void {
    // Desuscribir la anterior si cambia la partida
    this.partidaSub?.unsubscribe();
    this.partidaSub = this.stompClient.subscribe(topic, (message: IMessage) => {
      try {
        const evento: PartidaEventWs = JSON.parse(message.body);
        callback(evento);
      } catch (e) {
        console.error('[WS] Error parseando evento partida:', e);
      }
    });
  }

  desuscribirsePartida(): void {
    this.partidaSub?.unsubscribe();
    this.partidaSub = null;
  }

  /** Asegura que la conexión esté activa (sin sala). Útil para el tablero. */
  asegurarConexion(): void {
    if (!this.stompClient.active) {
      this.stompClient.activate();
    }
  }

  desconectar() {
    if (this.stompClient && this.stompClient.active) {
      this.stompClient.deactivate();
      this.conectado = false;
      this.salaId = null;
      this.callbackInicioPartida = null;
      this.partidaSub = null;
    }
  }
}
