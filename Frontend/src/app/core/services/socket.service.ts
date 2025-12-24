// src/app/features/socketService/socket.service.ts
import { Injectable } from '@angular/core';
import { Client, IMessage } from '@stomp/stompjs';
import * as SockJS from 'sockjs-client';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class WebSocketService {
  private stompClient: Client;
  private conectado: boolean = false;
  private salaId: number | null = null;

  constructor() {
    this.stompClient = new Client({
      webSocketFactory: () => new SockJS(environment.wsUrl),
      reconnectDelay: 5000,
      debug: (str) => console.log('[STOMP]', str)
    });
  }

  conectar(salaId: number, callbackJugador?: (jugador: any) => void) {
    if (this.conectado && this.salaId === salaId) return;

    this.salaId = salaId;

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

  desconectar() {
    if (this.stompClient && this.stompClient.active) {
      this.stompClient.deactivate();
      this.conectado = false;
      this.salaId = null;
      this.callbackInicioPartida = null;
    }
  }
}
