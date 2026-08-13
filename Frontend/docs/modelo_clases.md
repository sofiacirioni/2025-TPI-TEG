# CLASES

---

## Continentes
**Atributos:**
- `id_continente`
- `nombre`

**Métodos:**
- `obtenerPaises()`
- `calcularDominio()`

---

## Limites
**Atributos:**
- `id_pais1`
- `id_pais2`

**Métodos:**
- `esLimite()`

---

## Paises
**Atributos:**
- `id_pais`
- `nombre`
- `id_continente`

**Métodos:**
- `agregarPais()`
- `obtenerVecinos()`

---

## Tarjetas
**Atributos:**
- `id_tarjeta`
- `id_pais`
- `id_simbolo`

**Métodos:**
- `obtenerSimbolo()`

---

## Usuarios
**Atributos:**
- `nombre`
- `apellido`
- `contraseña`
- `imagen`

**Métodos:**
- `crearUsuario()`
- `cargarUsuario()`
- `editarUsuario()`
- `eliminarUsuario()`

---

## Objetivos
**Atributos:**
- `descripcion`
- `tipoObjetivo`

**Métodos:**
- `repartirObjetivos()`
- `resetearObjetivos()`

---

## Símbolos
**Enum:**
- `Símbolos de Tarjetas`

---

## Estadísticas
**Atributos:**
- `id_estadistica`
- `configuracion`
- `estado`
- `url`

**Métodos:**
- `registrarEvento()`
- `exportarJSON()`

---

## Estado_paises
**Atributos:**
- `id_estado_pais`
- `id_pais`
- `id_jugador`
- `id_estadísticas`

**Métodos:**
- `actualizarTropas()`

---

## Turnos
**Atributos:**
- `id_turno`
- `nro_turno`
- `id_fase`

**Métodos:**
- `obtenerTurno()`

---

## Tableros
**Atributos:**
- `id_tablero`
- `id_estado_pais`

**Métodos:**
- *(ninguno especificado)*

---

## Tipo_jugador
**Atributos:**
- `id_tipo_jugador`
- `descripcion`

**Métodos:**
- `esBot()`

---

## Partida
**Atributos:**
- `id_partida`
- `fecha_inicio`
- `id_turno`
- `estado`

**Métodos:**
- `crearPartida()`
- `cargarPartida()`

---

## Fase_turnos
**Atributos:**
- `id_fase`
- `descripcion`

**Métodos:**
- `obtenerFaseTurno()`

---

## Estado_tarjetas
**Atributos:**
- `id_estado_tarjeta`
- `id_tarjeta`
- `id_jugador`
- `id_turno`

**Métodos:**
- `asignarTarjeta()`
- `obtenerEstadoTarjeta()`

---

## Jugadores
**Atributos:**
- `id_jugador`
- `nombre`
- `id_objetivo`
- `id_usuario`
- `id_tipo_jugador`
- `perdio`
- `id_color`

**Métodos:**
- `moverTropa()`
- `crearPais()`
- `asegurarTropas()`
- `asignarTropa()`
- `revisarObjetivo()`
- `canjear()`
- `tienePacto()`
- `asignarColor()`
- `haPerdido()`
- `esInactivo()`

---

## Colores
**ENUM:**
- `colores`

---

## Tipo_objetivo
**ENUM:**
- `tipos de objetivos`