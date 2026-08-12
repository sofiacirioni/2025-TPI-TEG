-- ============================================================
-- V4 — Contadores de combate por jugador
--
-- La pantalla de estadísticas muestra el parte de la campaña recién jugada,
-- pero no había forma de saber cuántos ataques lanzó cada comandante: el
-- único dato de combate era `consquisto`, un booleano que solo indica si
-- corresponde entregar tarjeta al final del turno.
--
-- Los contadores viven acá y no en la tabla `estadisticas` porque cada fila de
-- `jugadores` YA es por partida (un jugador se crea por cada partida), así que
-- son naturalmente por campaña y se pueden incrementar en vivo durante el
-- combate sin tablas ni joins extra.
--
-- Las partidas anteriores a esta migración quedan en cero: no hay registro
-- histórico de ataques del que reconstruirlos.
-- ============================================================

ALTER TABLE jugadores
    ADD COLUMN ataques_lanzados     INTEGER NOT NULL DEFAULT 0,
    ADD COLUMN conquistas           INTEGER NOT NULL DEFAULT 0,
    ADD COLUMN tropas_abatidas      INTEGER NOT NULL DEFAULT 0,
    ADD COLUMN tropas_perdidas      INTEGER NOT NULL DEFAULT 0,
    ADD COLUMN defensas_resistidas  INTEGER NOT NULL DEFAULT 0,
    ADD COLUMN canjes_realizados    INTEGER NOT NULL DEFAULT 0;

COMMENT ON COLUMN jugadores.ataques_lanzados    IS 'Ataques resueltos como atacante en esta partida.';
COMMENT ON COLUMN jugadores.conquistas          IS 'Ataques que terminaron tomando el país. Efectividad = conquistas / ataques_lanzados.';
COMMENT ON COLUMN jugadores.tropas_abatidas     IS 'Tropas enemigas eliminadas, atacando o defendiendo.';
COMMENT ON COLUMN jugadores.tropas_perdidas     IS 'Tropas propias eliminadas, atacando o defendiendo.';
COMMENT ON COLUMN jugadores.defensas_resistidas IS 'Ataques recibidos en los que conservó el país.';
COMMENT ON COLUMN jugadores.canjes_realizados   IS 'Canjes de tarjetas efectuados en esta partida.';
