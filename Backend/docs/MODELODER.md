# Modelo DER TEG:

***

## Clase: Pais
- ### Atributos:
  + id_pais
  + nombre
  + id_continente

- ### Metodos:
  + obtenerVecinos()

- ### Responsabilidades:
  + Representar un país en el tablero
  + Determinar adyacencias
  + Asociarse a un continente

***

## Clase: Continente
- ### Atributos:
  + id_continente
  + nombre

- ### Metodos:
  + obtenerPaises()
  + calcularDominio()

- ### Responsabilidades:
  + Agrupar países
  + Permitir bonificaciones por dominio completo

***

## Clase: Limite
- ### Atributos:
  + id_limite
  + id_pais1
  + id_pais2
  + entreContinente

- ### Metodos:
  + esLimiteEntre()

- ### Responsabilidades:
  + Representar adyacencias entre países
  + Indicar si hay límite intercontinental

***

## Clase: Jugador
- ### Atributos:
  + id_jugador
  + nombre
  + id_objetivo
  + id_usuario
  + id_tipo_jugador
  + Perdió
  + id_color

- ### Metodos:
  + moverTropas()
  + atacarPais()
  + reagruparTropas()
  + AsignarTropas()
  + RevisarObjetivo()
  + Canjear()
  + tienePactos()
  + AsignarColor()
  + HaPerdido()
  + EsInactivo()

- ### Responsabilidades:
  + Representa a un jugador

***
## Clase: TipoJugador
- ### Atributos
  + id_tipo_jugador
  + descripcion
- ### Metodos 
  + EsBot()
- ###  Responsabilidades
  + Ver si el jugador va a ser un bot o no

***

## Clase: Usuario
- ### Atributos:
  + id_usuario
  + nombre
  + apellido
  + contraseña
  + imagen

- ### Metodos:
  + autenticar()
  + registrarse()
  + iniciarPartida()
  + CargarPartida()
  + FinalizarPartida()

- ### Responsabilidades:
  + Representar un usuario del sistema
  + Asociarse a un jugador

***

## Clase: Objetivo
- ### Atributos:
  + id_objetivo
  + descripcion
  + id_tipo_objetivo

- ### Metodos:
  + verificarCumplimiento()
  + AsignarObjetivo()

- ### Responsabilidades:
  + Asignar los objetivos al jugador
  + Definir condiciones de victoria para un jugador

***

## Clase: Tarjeta
- ### Atributos:
  + id_tarjeta
  + id_pais
  + id_simbolo

- ### Metodos:
  + obtenerSimbolo()
  + compararSimbolos()
  
- ### Responsabilidades:
  + Servir como recurso para cambio de tropas

***

## Clase: Simbolo
- ### Atributos:
  + id_simbolo
  + tipo

- ### Metodos:

- ### Responsabilidades:

***

## Clase: EstadoTarjeta
- ### Atributos:
  + id_estado_tarjeta
  + id_tarjeta
  + id_jugador
  + id_turno

- ### Metodos:
  + registrarEntrega()

- ### Responsabilidades:
  + Controlar qué jugador tiene qué tarjeta a lo largo de los turnos

***

## Clase: Turnos
- ### Atributos:
  + id_turno
  + nro_turno
  + id_fase
  + id_estadisticas
  + tiempoTurno

- ### Metodos:
  + avanzarTurno()
  + LanzarDados()
  + CambiarFase()
  + CalcularTiempo()

- ### Responsabilidades:
  + Administrar el flujo de la partida

***

## Clase: Fases
- ### Atributos:
  + id_fase
  + descripcion

- ### Metodos:

- ### Responsabilidades:
  + Dividir el turno en etapas lógicas

***

## Clase: Estadistica
- ### Atributos:
  + id_estadistica
  + configuracion
  + estado
  + url

- ### Metodos: 
  + registrarEvento()
  + exportarJSON()

- ### Responsabilidades:
  + Guardar el estado de la partida

***

## Clase: EstadoPais
- ### Atributos:
  + id_estado_pais
  + id_pais
  + id_jugador
  + id_estadistica
  + cantidadTropas

- ### Metodos:
  + actualizarTropas()

- ### Responsabilidades:
  + Representar el control de países por jugador

***

## Clase: Partida
- ### Atributos:
  + id_partida
  + datos
  + id_turno
  + id_comunicacion
  + id_notificacion

- ### Metodos:
  + reanudarPartida()
  + guardarPartida()
  + repartirCartas()
  + FinalizarPartida()
  + RecibirNotificacion()
  + IniciarChat()

- ### Responsabilidades:
  + Representar una sesión de juego

***

## Clase: GuardadoPartida
- ### Atributos:
  + id_partida_guardada
  + codigo
  + id_partida

- ### Metodos:
  + cargarPartida()

- ### Responsabilidades:
  + carga el estado de una partida interrumpida

***
## Clase: Pacto

- ### Metodos:
  + CrearPacto()
  + RomperPacto()

- ### Responsabilidades:
  + Diferenciar tarjetas para combinaciones

***

## Clase: Notificacion

- ### Atributos:
  + id_notificacion
  + notif
- ### Metodos:
  + SeleccionarNotif()
- ### Responsabilidad:
  + Enviar notificaciones sobre el estado de la partida.

***
## Clase: Comunicacion
- ### Atributos:
  + id_comunicacion
  + descripcion
  + id_tipo
- ### Metodos:
  + EnviarMensje()
- ### Responsabilidad:
  + Permite la comunicacion entre jugadores
***
## Clase: TipoComunicacion
- ### Atributos:
  + id_tipo_comunicacion
  + tipo
- ### Metodos:
  + OpcionesMensaje()
- ### Responsabilidad:
  + Opciones de mensajes para enviar


***

![img](/docs/Diagrama_DER.jpeg)