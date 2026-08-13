-- ============================================================
-- V6 — Estado ABANDONADA y limpieza de partidas zombi
--
-- Hasta acá una partida sólo podía estar EN_JUEGO, PAUSADA o
-- TERMINADA, y no había forma de salir de una: quien cerraba la
-- pestaña la dejaba EN_JUEGO para siempre. El resultado es que el
-- historial del comandante se llenaba de campañas fantasma y
-- ninguna llegaba nunca a contar.
--
-- ABANDONADA distingue "me retiré" de "perdí", que para las
-- estadísticas son cosas distintas: en una partida abandonada no se
-- puede saber quién habría ganado, así que figura en el registro
-- pero no suma. Es el mismo criterio que usa el remake de League of
-- Legends, donde una partida interrumpida no cuenta para nada.
-- ============================================================

-- El CHECK de V1 sólo admitía los tres estados originales.
ALTER TABLE partidas DROP CONSTRAINT IF EXISTS partidas_estado_partida_check;

ALTER TABLE partidas
    ADD CONSTRAINT partidas_estado_partida_check
    CHECK (estado_partida IN ('TERMINADA', 'PAUSADA', 'EN_JUEGO', 'ABANDONADA'));

-- Las partidas que quedaron colgadas antes de que existiera el botón de
-- abandonar. Se cierran como abandonadas en lugar de borrarlas: son el
-- registro de que esas campañas se jugaron, aunque no cuenten.
--
-- El corte es "sin actividad en las últimas 24 horas" según la fecha de
-- actualización de la V5. Una partida que se está jugando ahora mismo no
-- se toca.
UPDATE partidas
   SET estado_partida  = 'ABANDONADA',
       fecha_actualizacion = now()
 WHERE estado_partida IN ('EN_JUEGO', 'PAUSADA')
   AND fecha_actualizacion < now() - INTERVAL '24 hours';
