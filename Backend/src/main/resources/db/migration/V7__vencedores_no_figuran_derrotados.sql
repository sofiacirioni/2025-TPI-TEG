-- Repara los datos que dejó un bug de cierre de partida.
--
-- Las tres ramas de victoria de ObjetivoServiceImpl marcaban `perdio = true`
-- para TODOS los jugadores de la partida, incluido el vencedor. Como la pizarra
-- de estadísticas tacha las filas a partir de ese campo, el ganador aparecía
-- tachado junto a los eliminados.
--
-- El código ya no lo hace; acá se corrigen las partidas ya jugadas.

UPDATE jugadores j
SET perdio = false
FROM partidas p
WHERE p.ganador_id = j.id_jugador
  AND j.perdio = true;
