-- ============================================================
-- V5 — Fechas de alta y de última actualización
--
-- El par created_at / updated_at es práctica estándar en cualquier
-- tabla que guarde registros con vida propia: permite ordenar por
-- antigüedad real, auditar cambios y responder "¿cuándo pasó esto?"
-- sin depender del orden de los IDs.
--
-- Lo reciben las cuatro tablas donde falta esa información y hace
-- falta:
--   usuarios  — el alta de la cuenta, que hasta ahora no se guardaba
--               en ningún lado.
--   partidas  — fecha_inicio es DATE (sin hora), así que dos partidas
--               del mismo día no se podían ordenar entre sí. Además
--               fecha_actualizacion marca el último movimiento, que
--               en una partida terminada es su final.
--   salas     — no tenía ninguna marca temporal.
--   mensajes  — el chat se ordenaba por id_mensaje. Funciona por
--               accidente del autoincremental, pero no es una fecha:
--               no se puede mostrar la hora de un mensaje.
--
-- NO lo reciben, y es deliberado:
--   pactos             — ya llevan fecha_propuesta, fecha_aceptacion y
--                        fecha_ruptura, que dicen más que un updated_at.
--   turnos             — ya llevan `inicio`.
--   jugadores,         — son estado de juego: nacen y mueren con la
--   estado_paises,       partida que los contiene, y su antigüedad es
--   estado_tarjetas      la de esa partida.
--   países, objetivos, — datos de referencia inmutables, cargados por
--   tarjetas, límites…   la propia migración V2.
--
-- Las columnas quedan con DEFAULT now() para que el ALTER complete
-- las filas existentes y para que las migraciones que insertan datos
-- semilla (V3) no tengan que enumerarlas. En marcha las escribe
-- Spring Data JPA Auditing, no el DEFAULT.
-- ============================================================

ALTER TABLE usuarios
    ADD COLUMN fecha_alta          TIMESTAMP(6) NOT NULL DEFAULT now(),
    ADD COLUMN fecha_actualizacion TIMESTAMP(6) NOT NULL DEFAULT now();

ALTER TABLE salas
    ADD COLUMN fecha_alta          TIMESTAMP(6) NOT NULL DEFAULT now(),
    ADD COLUMN fecha_actualizacion TIMESTAMP(6) NOT NULL DEFAULT now();

ALTER TABLE partidas
    ADD COLUMN fecha_alta          TIMESTAMP(6) NOT NULL DEFAULT now(),
    ADD COLUMN fecha_actualizacion TIMESTAMP(6) NOT NULL DEFAULT now();

ALTER TABLE mensajes
    ADD COLUMN fecha_alta          TIMESTAMP(6) NOT NULL DEFAULT now(),
    ADD COLUMN fecha_actualizacion TIMESTAMP(6) NOT NULL DEFAULT now();

-- Las partidas ya jugadas sí saben su día: se respeta esa fecha en lugar
-- del now() del ALTER, para que el historial del comandante no muestre
-- todas las campañas viejas apiladas en el día de la migración.
UPDATE partidas
   SET fecha_alta          = fecha_inicio,
       fecha_actualizacion = fecha_inicio
 WHERE fecha_inicio IS NOT NULL;
