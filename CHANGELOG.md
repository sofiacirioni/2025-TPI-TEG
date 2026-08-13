# CHANGELOG — TEG

Historial de sesiones de desarrollo del proyecto TEG (TPI 2025).
Las sesiones más recientes están al final del archivo.

---

## Sesiones

### Migración frontend (rama `feature/front-migration`)
- Migración completa de components a Angular standalone
- Integración del mapa SVG interactivo con sistema de turnos
- Corrección de flujo de ataque y lógica de juego

### Design system (sesión 2025-03-19/20)
- Implementación de SCSS parcial con Bootstrap 5.3
- Tokens de color, tipografía, botones, superficies y cursores custom
- Pantalla de créditos con layout de expedientes/carpetas

### Pantalla principal y animación de intro (sesión 2026-03-21)
- **IntroComponent**: animación typewriter estilo terminal clasificada, con fecha/hora real del sistema y año ficticio `194█`
- **PrincipalComponent**: layout con panel inferior (T·E·G / subtítulo / JUGAR), botones de navegación en esquinas, canvas de 130 partículas doradas
- Transición JUGAR: zoom-in del fondo → oscurecimiento → crossfade a mesa de juego (3.2s total)
- Fade suave intro→principal: overlay negro + fade-in del host
- Iconos de nav con `z-index: 10000` para quedar encima de la viñeta global
- Cursores custom aplicados correctamente (sin override en componentes)

### Auth, seguridad y sistema de notificaciones (sesión 2026-03-25)

#### Backend — limpieza y hardening de auth
- **Endpoints duplicados eliminados**: removidos `POST /api/v1/usuario` (registro antiguo) y `POST /api/v1/usuario/login` de `UsuarioController`; la autenticación queda centralizada exclusivamente en `AuthController`
- **BCrypt corregido en `actualizarUsuario`**: se reemplazó comparación en texto plano (`.equals()`) por `passwordEncoder.matches()` y `passwordEncoder.encode()`
- **DTOs eliminados**: `Credencial.java` y `UsuarioDeleteDto.java` (sin uso)
- **`findByCorreoAndContrasenia` eliminado** de `UsuarioRepository`
- **`/auth/refresh` devuelve 401 correctamente**: se extrajo helper `extractRefreshCookie()` y se lanza `ResponseStatusException(HttpStatus.UNAUTHORIZED)` en lugar de `IllegalStateException` (que mapeaba como 500)
- **`AuthEntryPoint.java` (nuevo)**: implementa `AuthenticationEntryPoint`, responde JSON `{error, status: 401}` ante requests no autenticados — eliminando los 403 incorrectos
- **`cookie.secure` configurable por entorno**: via `@Value("${cookie.secure:false}")`, `application-prod.properties` y `.env.example`; `ResponseCookie` con `SameSite: Strict` en prod y `Lax` en dev

#### Frontend — limpieza de auth
- **`registrarse.service.ts` eliminado** (ApiService legacy sin uso)
- **`principal.component.ts`**: reemplazado `localStorage.getItem('usuario')` por `authService.isAuthenticated()`
- **`perfil-usuario.component.ts`**: removido `localStorage.setItem('usuario', ...)`

#### Sistema de notificaciones tipo teletipo
- **`NotificationService`** (`core/services/`): `BehaviorSubject` con tipos `success | error | warning | info`, auto-dismiss configurable, `dismiss(id)` manual
- **`ToastComponent`** (`shared/components/toast/`): standalone, efecto typewriter (26ms/char), animación slide-in desde la derecha, `z-index: calc(var(--z-vignette) + 1)` para quedar sobre la viñeta global
- **Estética de teletipo**: tiras de perforaciones laterales (pseudo-elementos `::before`/`::after`) con círculos oscuros sobre fondo de papel, textura diagonal sutil, tinte de color por tipo via CSS custom property `--paper-bg`
- **Integrado en `app.component.html`** sobre `<router-outlet>`
- **Login y registro**: reemplazados `alert()` por `notificationService.error()` / `success()` con mensajes en tono militar
- **Barrel files**: `shared/components/index.ts` y `core/services/index.ts`

### SalaComponent — refactor a telegrama de época (sesión 2026-03-26)

- **`pages/sala/sala.component.html`** reescrito: layout de telegrama militar con `.telegrama-wrap` / `.tele-paper`, perforaciones CSS, `SlideInDirective` (`appSlideIn`), grilla de metadatos (PARA / DE / FECHA / REF.), 3 opciones radio-style (A/B/C) con inputs colapsables inline, sello CLASSIFIED (blend-mode multiply), footer de estado mayor
- **`pages/sala/sala.component.scss`** (migrado de `.css`): papel crema `#EDE5B0`, perforaciones via `::before`/`::after`, transición `max-height: 0 → 60px` para campos colapsables, `.tele-radio` con animación fill al seleccionar, `.sala-actions` fuera del telegrama con botón confirm y link volver
- **`pages/sala/sala.component.ts`** reescrito: `FormsModule` reemplaza `ReactiveFormsModule`; estado con `opcionActual`, `nombreSala`, `codigoSala`; `seleccionar()`, `confirmar()`, `generarNombreDefault()`, `textoBoton` getter; `alert()` reemplazado por `notificationService.error()`; polyfill `(window as any).global = window` restaurado para compatibilidad SockJS
- **`shared/components/stamp/stamp.component.ts`**: corregida ruta `'clasificado'` → `/assets/vectors/classified-stamp.svg`
- **`shared/components/index.ts`**: agregado `export { StampComponent }` explícito para resolver problema de resolución webpack con `export *`
- **`pages/registrarse/registrarse.component.ts`**: import de `StampComponent` movido a ruta directa (evita ambigüedad del barrel)
- Revisión Playwright confirmada: todos los checks DOM pasan, 0 errores de consola, 0 requests 404

### ConfigPartida + TableroComponent — refactor UI completo (sesión 2026-03-31)

#### ConfigPartida — correcciones
- **JWT en WebSocket STOMP**: `socket.service.ts` ahora acepta `token?: string` en `conectar()` y lo pasa como `connectHeaders: { Authorization: 'Bearer <token>' }` antes de `activate()`. `config-partida.component.ts` obtiene el token vía `authService.getAccessToken()` y lo propaga.
- **`.orden-firma` separación**: `margin-top` aumentado de `0.25rem` a `1rem` para garantizar separación visible de la firma respecto a los jugadores.

#### TableroComponent — reescritura completa
- **Layout fullscreen**: fondo de madera oscura, `mapa-zona` central al 100%, paneles flotantes absolutos — sin grid Bootstrap.
- **Mapa SVG**: fills RGBA con opacidad 0.45 (efecto papel), fichas mejoradas (círculo sólido + texto Special Elite con sombra de refuerzo), fondo oceánico `#8AABBC`.
- **Zoom y pan**: rueda del ratón (escala 0.5–3×), arrastre con mouse, botones `+ − R`, indicador `{{ scale * 100 | number:'1.0-0' }}%`.
- **Modal de país inline** (reemplaza CDK Dialog): `.modal-pais` con tachuela decorativa, posicionado relativo al click sobre el mapa, acciones condicionales por fase (COLOCACION / ATACAR / MOVER_TROPAS).
- **Info-barra superior**: fase + dots de progreso, turno + N°, ejércitos disponibles, timer countdown con parpadeo urgente al ≤30s.
- **Sobre del objetivo**: componente envelope animado top-left, toggle con `.objetivo-modal`.
- **Reloj SVG decorativo**: agujas de horas/minutos/segundos sincronizadas con la hora real del sistema; 12 marcas horarias precomputadas como `clockTicks` (evita `Math` en template Angular).
- **Panel derecho — fichas de jugadores**: avatar SVG por color, nombre, países, ejércitos, indicador de turno activo.
- **Panel izquierdo**: historial de operaciones tipificado (ataque/ok), tarjetas mini con símbolo, chat grupal local con input (UI preparada para futura integración WebSocket).
- **Polling reducido**: `interval(1000)` → `interval(3000)` en `tablero.service.ts`.
- **Sin `alert()`**: todos los avisos migrados a `NotificationService` (success/error/warning/info).
- **Resultado de ataque**: overlay modal con dados en emoji, nombres de países origen/destino, estado CONQUISTA/REPELIDO.
- **Modal de canje de tarjetas**: selección de combinación posible con confirmación.
- **Limpieza de imports**: removido import corrupto `@angular-devkit/build-angular` de `tablero.service.ts`.
- Revisión Playwright: 14/14 elementos presentes, timer countdown verificado, reloj 15 líneas, modal de país con tachuela, objetivo legible, zoom funcional, chat operativo, 0 errores 404, 1 solo error de consola esperado (`/auth/refresh` 401 en sesión nueva).

### SalaComponent — ajustes UX y perforaciones reales (sesión 2026-03-27)

- **Perforaciones reales**: técnica migrada de `.tele-paper::before` a `.telegrama-wrap::before/after` — tiras con `background-color: transparent` + `radial-gradient(transparent 5px, #EDE5B0 5.5px)`; los agujeros muestran la imagen de mesa real a través del papel
- **`.tele-paper` reestructurado**: `margin: 0 18px` para no cubrir las tiras; `padding: 1rem 0` para más espacio vertical; `border-left/right` como línea divisoria
- **Input código → 6 cajas OTP** (`.codigo-box`): auto-foco al tipear, backspace retrocede, paste desde portapapeles distribuye en las 6 cajas; `codigoChars: string[]` + getter `codigoSala`; botón deshabilitado hasta completar los 6 caracteres
- **Color inputs**: `var(--player-azul)` en `.campo-inp` y `.codigo-box`, consistente con login/registro
- **Fecha con redacción**: `"DD MMM 194█"` y `"DD/MMM/4█"` — último dígito del año reemplazado por █ (U+2588), coherente con la intro
- **Opacidad encabezado/metadatos**: `.tele-titulo`, `.tele-subtitulo`, `.tele-header-right`, `.meta-k`, `.meta-v` → `rgba(40, 25, 5, 0.3)` (igual que footer derecho)
- **Código de sala 6 chars** (`SalaServiceImpl`): reemplaza `UUID.randomUUID()` por `generarCodigoSala()` con charset sin I/O/0/1
- **Opción C eliminada**: tipo `opcionActual: 'crear' | 'unirse'`; `GET /partida/disponibles` y `POST /partida/{id}/unirse/{id}` marcados con TODO en `PartidaController`
- **Metadatos en 2 columnas**: grid `1fr 1fr`, PARA+FECHA izq., DE+REF. der.
- **Opción activa en rojo**: `var(--color-sello)` en texto, código y radio button (inner dot vía `::after`)
- **Placeholder**: font-size 10px (igual al label), `text-transform: none` para no heredar uppercase del input

### Tablero — refactor visual del mapa, zoom/pan y modales (sesión 2026-04-03)

#### Mapa SVG — correcciones visuales
- **SVG letterboxing eliminado**: añadido `preserveAspectRatio="xMidYMid slice"` al `<svg>` del mapa — equivalente a `background-size: cover`, elimina las barras laterales cuando la relación de aspecto del contenedor no coincide con el viewBox 1920×1080.
- **Colores de trazo corregidos**: strokes del SVG cambiados de `rgba(0,0,0,...)` a `rgba(67,42,30,...)` (`--color-oscuro`).

#### Controles de zoom — reubicación fuera del mapa
- **`.mapa-controles` fuera de `.mapa-zona`**: posicionados absolutamente (`right: 172px, bottom: 48px`) en el hueco entre el mapa y el panel derecho, sin interferir con el área interactiva.
- **Estilo `btn-teg-dark`** con override local `.ctrl-btn` para tamaño reducido (padding 5px 10px).

#### Zoom/pan — bounds y restricciones
- **`clampTranslate()`**: limita `translateX/Y` al rango `[-(scale-1)*containerSize, 0]` — impide espacios vacíos al arrastrar.
- **Pan solo activo cuando `scale > 1`**: arrastar con zoom = 1 no hace nada.
- **`transform-origin: top left`** explícito para cálculos correctos de bounds.

#### Layout — ajustes de espacio
- **`.mapa-zona`** reducido: `top: 80px`, `bottom: 48px`, `right: 218px` — más margen para paneles y barra superior.
- **`.panel-izquierdo` bajado a `top: 120px`**: elimina solapamiento del historial sobre el sobre del objetivo.
- **`.sobre-objetivo` subido a `z: calc(var(--z-vignette) + 3)`**: resuelve conflicto de z-index con el panel izquierdo.

#### Modal de país — rediseño tipo ficha de campo
Estructura de 3 capas:
1. **Frame exterior** (`modal-pais`): padding 8px con `background-color` del color sólido del dueño.
2. **Papel interior** (`modal-papel`): `var(--color-claro)` con textura diagonal sutil, `overflow: hidden` para clippear el strip.
3. **Strip de título** (`modal-nombre-strip`): `darken($color-claro, 22%)` (~#BFB07A) con chinchetas rojas en los extremos, texto `var(--color-oscuro)` en `var(--font-heading)` uppercase.
- Botones: `btn-teg-primary` (colocar/mover) y `btn-teg-danger` (atacar) con override `.btn-modal-accion`.
- Tests Playwright: 20/20 pasando.

### Tablero — avatares, pipes de display y mapa (sesión 2026-04-07)

#### Avatares de jugadores
- **`JugadorDto.url`** reutilizado para transportar `usuario.imagen` desde el backend — mapeado manualmente en `PartidaServiceImpl.cargarPartida()` tras el modelMapper.
- **`JugadorDto` interface** (frontend): añadido campo `url?: string`.
- **Panel derecho**: el círculo de jugador muestra `background-color` sólido para todos, y renderiza `<img class="ficha-avatar-img">` con `object-fit: cover` cuando `!esBot && jugador.url` existe. Fallback via `(error)` que oculta la imagen rota.
- **Bots**: solo muestran el color plano del jugador, sin intento de carga de imagen.
- **Insignias**: rutas cambiadas de SVG a PNG (`assets/images/insignias/*.png`) para preservar efectos de textura/sombra exportados.

#### Pipes de visualización
- **`NombrePaisPipe`** (`core/pipes/nombre-pais.pipe.ts`): corrige ortografía de países y continentes al mostrarse en UI sin tocar la BD. Correcciones: `Yukon→Yukón`, `Oregon→Oregón`, `Mexico→México`, `Canada→Canadá`, `Peru→Perú`, `Gran Bretana→Gran Bretaña`, `Espana→España`, `Etiopia→Etiopía`, `Sudafrica→Sudáfrica`, `Turquia→Turquía`, `Japon→Japón`, `Iran→Irán`, continentes: `Africa→África`, `Oceania→Oceanía`, `America del Norte/Sur`.
- **`FaseDisplayPipe`** (`core/pipes/fase-display.pipe.ts`): convierte fases de BD a display legible en mayúsculas con tildes: `COLOCACION→COLOCACIÓN`, `MOVER_TROPAS→MOVER TROPAS`. Fallback: reemplaza `_` por espacio.
- Ambos pipes aplicados en `tablero.component.html` y `acciones-pais.component.html`.

#### Mapa — fichas
- **Sombra de ficha eliminada**: removida la `<ellipse>` decorativa (`fill: rgba(0,0,0,0.4)`) que proyectaba sombra oval bajo cada token en `mapa-svg.component.html` — mejora legibilidad sobre países con colores similares.

### Tablero — sistema de notificaciones de juego en tiempo real (sesión 2026-04-08)

Sistema de overlay visual que muestra eventos del juego sobre el mapa, con dos modos: panel de combate interactivo y notificación simple.

#### Backend — broadcasts WebSocket

- **`PartidaEventDto`** (`Dtos/PartidaEventDto.java`): nuevo DTO para broadcasts WS con campos `tipo`, `jugadorNombre`, `jugadorColor`, `paisOrigen`, `paisDestino`, `dadosAtaque`, `dadosDefensor`, `conquista`, `perdidasAtacante`, `perdidasDefensor`, `idPartida`.
- **`Ataque.java`**: campo opcional `cantDadosAtacante` (`Integer`, nullable) — permite al atacante elegir cuántos dados usar; `null` = máximo automático.
- **`AtaqueResponseDto.java`**: campos `perdidasAtacante` y `perdidasDefensor` añadidos a la respuesta.
- **`TurnoServiceImpl`**: inyectado `SimpMessagingTemplate`; broadcasts a `/topic/partida.{id}.evento` en:
  - `ataque()`: respeta `cantDadosAtacante`, calcula pérdidas, emite evento `ATAQUE` o `CONQUISTA`.
  - `cambiarFaseTurno()`: emite `FIN_TURNO` al avanzar de jugador.
  - `agregarFichas()`: emite `INCORPORACION` al colocar ejércitos.
  - `moverFichas()`: emite `REAGRUPAMIENTO` al mover tropas.
  - `entregarTarjetaSiCorresponde()`: emite `TARJETA_OBTENIDA`.
  - `validarCanjeTarjetas()`: emite `TARJETA_CANJEADA`.

#### Frontend — `TableroEventService`

Servicio cola (`core/services/tablero-event.service.ts`) que procesa eventos de juego uno a la vez:

- `enqueue(event)`: encola un `GameEvent`; si la cola estaba vacía, lo procesa inmediatamente.
- `enqueueFromWs(ws, esJugadorLocal)`: convierte `PartidaEventWs` en `GameEvent`; omite ATAQUE/CONQUISTA para el atacante local (ya los encoló desde la respuesta HTTP).
- `resolveDiceSelection(n)`: emite el número de dados elegido vía `diceResult$`.
- `advanceAfterDice()`: avanza la cola manualmente después de la respuesta HTTP del ataque.
- `dismissCurrent()`: descarta el evento actual y avanza.
- Eventos bloqueantes (`ATAQUE_INICIADO`): no tienen timer automático; esperan `advanceAfterDice()`.
- Eventos con duración: `RESULTADO_DADOS` 4 s, `CONQUISTA` 5 s, resto 2.5–3.5 s.

#### Frontend — `TableroEventDisplayComponent`

Componente overlay (`tablero-event-display/`) con cuatro modos visuales:

1. **Selección de dados** (`ATAQUE_INICIADO`): panel de combate con nombres de países, botones 1–N dados, countdown 5 s, botón `· ATACAR ·`. El timer auto-confirma con el máximo de dados al expirar.
2. **Resultado de combate** (`RESULTADO_DADOS` / `CONQUISTA`): dados animados con CSS (aparecen escalonados, los perdedores quedan marcados en rojo), pérdidas de cada bando, etiqueta CONQUISTA / ATAQUE EXITOSO / REPELIDO.
3. **Pantalla de conquista** (transición post-resultado): overlay sello ★ con nombre del conquistador y país capturado.
4. **Notificación simple** (resto de eventos): tarjeta con franja lateral del color del jugador, ícono y texto descriptivo.

Posicionado como overlay absoluto sobre la zona del mapa (`z-index: calc(var(--z-vignette) + 6)`). Usa `pointer-events: none` en `:host`; los paneles interactivos tienen `pointer-events: all`.

#### Frontend — integración en `TableroComponent`

- WS subscripción a `/topic/partida.{id}.evento` en el primer emit de `partidaObservable`; desuscripción en `ngOnDestroy`.
- `atacarDesdeModal()` reescrito: cierra el modal, suscribe `diceResult$.pipe(take(1))` → realiza HTTP con `cantDadosAtacante`, encola resultado y llama `advanceAfterDice()`; encola `ATAQUE_INICIADO` con `diceSelection`.
- `reagruparDesdeModal()` y `confirmarCanje()`: encolan sus respectivos eventos al completarse.
- `getColorVarJugador()`: mapea color de jugador a `var(--player-*)`.
- `partida.interface.ts`: `AtaqueDto` con `cantDadosAtacante?`, `AtaqueResponseDto` con `perdidasAtacante`/`perdidasDefensor`.
- `socket.service.ts`: método `suscribirseEventosPartida()`, `desuscribirsePartida()`, `asegurarConexion()`.

#### Modelo de datos

```typescript
interface GameEvent {
  tipo: 'INCORPORACION' | 'ATAQUE_INICIADO' | 'RESULTADO_DADOS' | 'CONQUISTA'
      | 'TARJETA_OBTENIDA' | 'TARJETA_CANJEADA' | 'REAGRUPAMIENTO' | 'FIN_TURNO';
  titulo: string; descripcion?: string;
  jugadorActivo?: string; colorJugador?: string;
  paisOrigen?: string; paisDestino?: string;
  dadosAtaque?: number[]; dadosDefensor?: number[];
  conquista?: boolean; perdidasAtacante?: number; perdidasDefensor?: number;
  duracionMs?: number; diceSelection?: DiceSelectionParams;
}
```

### Tablero — corrección de bugs de flujo de juego y tests e2e (sesión 2026-04-09)

Corrección de cinco bugs que impedían jugar correctamente, mejora de validaciones y suite de tests de flujo completo.

#### Backend

- **`TurnoServiceImpl.turnoBot()`**: bug corregido — cuando `hostilidad=true` y el bot no conquista ningún país (`faseAtaque()` retorna `false`), `faseReagrupar()` nunca se llamaba y el bot quedaba bloqueado en fase `MOVER_TROPAS` indefinidamente. Ahora `faseReagrupar` siempre se ejecuta independientemente del resultado del ataque.
- **Nuevo endpoint `GET /api/v1/turno/reagrupar/destinos`** (`TurnoController`): corre el BFS real de `esConectadoPorTerritorioPropio()` contra todos los países propios del jugador y devuelve solo los alcanzables. Evita mostrar destinos inválidos en el frontend.
- **`EstadoPaisRepository`**: nuevo método `findAllByJugador_IdJugadorAndPartida_IdPartida()` (Spring Data derivado).
- **`TurnoService`** (interface): declaración del método `getDestinosReagrupamiento(idPaisOrigen, idJugador, idPartida)`.

#### Frontend — bugs corregidos

| # | Bug | Causa raíz | Fix |
|---|---|---|---|
| 1 | **Crash al atacar** — el ataque nunca se ejecutaba | `cerrarModalPais()` ponía `paisModalSeleccionado` en `null` antes de que el callback de dados lo leyera en línea 503 | Guardar `idOrigen` en variable local antes de llamar `cerrarModalPais()` |
| 2 | **Colocación acepta negativos / cero / más del disponible** | `min="0"` sin validación en el componente | `min="1"`, botón `COLOCAR` deshabilitado si `input < 1 \|\| > ejercitosDisponibles`, validación espejo en `defenderDesdeModal()` |
| 3 | **Indicador de turno no se actualiza** | Hay que esperar el próximo tick del polling (3 s) después de avanzar fase | `avanzarFaseTurno()` llama `tableroServicio.forceRefresh()` en la respuesta HTTP; `forceRefresh()` fue añadido a `TableroServicio` |
| 4 | **Reagrupamiento muestra todos los países propios** | `calcularLimitrofes()` listaba todos los propios; el BFS solo existía en el backend | Ahora llama al nuevo endpoint `/turno/reagrupar/destinos`; fallback a limítrofes propios directos si el endpoint falla |
| 5 | **Historial no registra eventos WS** | Los eventos de otros jugadores (FIN_TURNO, ataques, etc.) no llegaban al historial | Suscripción a `currentEvent$` en `ngOnInit` que agrega automáticamente cada evento al historial, evitando duplicar los que ya registra el propio jugador |

#### Frontend — `TableroServicio`

- `currentUrl`: guarda la URL activa para poder hacer `forceRefresh()`.
- `forceRefresh()`: hace una consulta inmediata a `obtenerPartidaByUrl()` sin esperar el intervalo del polling.
- `getDestinosReagrupamiento(idEstadoPaisOrigen, idJugador, idPartida)`: llama al nuevo endpoint BFS.
- `startPolling()` y `stopPolling()` limpiados (sin duplicado de lógica).

#### Tests e2e — `flujo-completo.spec.ts`

Suite nueva (`e2e/flujo-completo.spec.ts`, 16 tests) que cubre el flujo de principio a fin:

| Bloque | Tests |
|---|---|
| Estado inicial | tablero carga completo, número de turno positivo, países tienen dueño |
| Fase COLOCACIÓN | colocar ejércitos, botón deshabilitado con valores inválidos (neg/0/exceso) |
| Fase ATACAR | panel de dados aparece, historial registra el evento, destinos son solo limítrofes |
| Fase MOVER TROPAS | reagrupamiento funciona, destinos vienen del BFS backend |
| Ciclo turno + bots | número de turno avanza, jugador activo cambia, dos rondas completas |
| Historial y eventos | entradas acumulan, notificaciones tienen estructura correcta |

Los tests que dependen del turno del usuario usan `esperarMiTurno()` — si el turno no llega en el timeout hacen `test.skip()` en lugar de fallar. Los tests de validación de UI son determinísticos y siempre corren.

También corregido en `tablero-dev.spec.ts`: el test "el turno pasa a los bots automáticamente" ahora espera a que sea el turno del usuario antes de leer el número base (evitaba `N° 1 → N° 1` por estado heredado de tests anteriores). Timeout aumentado a 90 s.

### Tablero — sistema de objetivos secretos con revelación animada (sesión 2026-04-12)

Refactorización completa del sistema de objetivos secretos: seguimiento dinámico del progreso, regla alternativa del reglamento TEG, revelación con sobre animado y panel de progreso en tiempo real.

#### Backend — modelo y lógica de objetivos

- **`JugadorEntity`**: campo `eliminadoPorColor: Color` (`@Enumerated(STRING)`, nullable) — registra qué jugador eliminó a cada bando. Necesario para implementar la regla alternativa.
- **`TurnoServiceImpl.ataque()`**: detecta eliminación del defensor (0 países restantes), setea `defensor.perdio = true` y `defensor.eliminadoPorColor = atacante.color`, persiste. Emite progreso del objetivo del atacante vía WebSocket al topic personal `/topic/partida.{id}.objetivo.{jugadorId}`.
- **`ObjetivoServiceImpl.verificarObjetivos()`**: corrección de la regla alternativa — si el enemigo del objetivo fue eliminado por otro jugador, el objetivo **no** se cumple sino que se convierte a "conquistar 30 países".
- **`ObjetivoServiceImpl.calcularProgreso()`** (nuevo): genera un `ObjetivoProgresoDto` con ítems de progreso concretos:
  - Objetivo convertido (regla alternativa): un ítem `X/30 países`.
  - Objetivo de eliminación: muestra cuántos países le quedan al enemigo (`N países restantes`).
  - Objetivo territorial: un ítem por continente requerido (`X/N países`).

#### Backend — nuevos DTOs y endpoint

| DTO | Campos |
|---|---|
| `ObjetivoItemDto` | `descripcion`, `valorActual`, `valorObjetivo`, `completado` |
| `ObjetivoProgresoDto` | `descripcion`, `items: List<ObjetivoItemDto>`, `completado`, `objetivoConvertido` |
| `ObjetivoDto` (extendido) | `colorEnemigo`, `cantidadPaisesObjetivo`, `africa`, `asia`, `europa`, `americaNorte`, `americaSur`, `oceania` |

- **`GET /api/v1/objetivos/progreso/{idJugador}`** → `ObjetivoProgresoDto`: snapshot de progreso actual del objetivo del jugador.

#### Backend — WebSocket de progreso

`TurnoServiceImpl.ataque()` emite a `/topic/partida.{idPartida}.objetivo.{idJugador}` (topic personal) cada vez que el atacante conquista un país, enviando el `ObjetivoProgresoDto` actualizado. Solo recibe el jugador dueño del objetivo.

#### Frontend — interfaces y servicios

- **`partida.interface.ts`**: `ObjetivoDto` extendido con campos estructurales; nuevas interfaces `ObjetivoItem` y `ObjetivoProgreso`.
- **`tablero.service.ts`**: método `obtenerProgresoObjetivo(idJugador)` — `GET /api/v1/objetivos/progreso/{idJugador}`.
- **`socket.service.ts`**: método `suscribirseProgresoObjetivo(idPartida, idJugador, callback)` — suscripción al topic personal de progreso; retorna `StompSubscription` para permitir desuscripción por componente.

#### Frontend — `ObjetivoRevelacionComponent` (nuevo)

Componente standalone (`tablero/componentes/objetivo-revelacion/`) que muestra el objetivo secreto al inicio de la partida mediante una animación de sobre de época.

**Máquina de estados**: `'cerrado' → 'abriendo' → 'abierto' → 'cerrando'`

**Animación del sobre** (CSS puro):
- Solapa triangular (`border-left/right/bottom`) que se abre con `rotateX(170deg)` en 0.55 s.
- Hoja de órdenes que emerge del sobre con `translateY(-160px)` cuando `estado === 'abierto'`.
- Lacre rojo central (`$color-sello`) con texto "TEG".
- Overlay de pantalla completa con `backdrop-filter: blur(6px)`.
- Auto-dismiss en 15 s si el usuario no interactúa.

**Contenido de la hoja** (diseño militar de época):
- Número de orden generado aleatoriamente, sello "MISIÓN ASIGNADA".
- Descripción textual del objetivo.
- Lista de ítems con checkbox `□/■` y contador `X/N` (para objetivos territoriales) o texto "en curso" (eliminación).
- Footer con firma y "COMANDO SUPREMO".
- Botón `· ENTENDIDO ·` visible solo cuando el sobre está abierto.

#### Frontend — integración en `TableroComponent`

- `showRevelacion`: boolean, se activa en el primer load cuando `jugadorUsuario?.objetivo` existe y la revelación no fue mostrada aún (`revelacionMostrada` flag).
- `progresoObjetivo: ObjetivoProgreso | null`: cargado en init vía REST, actualizado en tiempo real vía WS.
- `objetivoWsSub: StompSubscription | null`: desuscripto en `ngOnDestroy`.
- `onRevelacionConfirmada()`: oculta el overlay y carga el progreso inicial.
- Panel ORDEN SECRETA (sidebar): reemplaza el texto estático por lista de ítems dinámica de `progresoObjetivo` con `□/■` checkboxes y contadores `X/N`. Fallback al texto plano si el progreso aún no llegó.

#### Tests e2e — actualizaciones

- **`tablero-dev.spec.ts` `beforeEach`**: descarta el overlay de revelación si está visible (`.btn-entendido` click + 800 ms).
- **`flujo-completo.spec.ts` `beforeEach`**: ídem — misma lógica de dismiss para que los tests existentes no queden bloqueados por el overlay de pantalla completa.
- **Nuevo bloque `'Revelación de objetivo — sobre animado'`** en `tablero-dev.spec.ts` (6 tests): overlay aparece al cargar, solapa se abre (`sobre-flap.abierta`), ítems tienen formato `X/N`, botón ENTENDIDO descarta el overlay, design system (`$font-heading`, color `$color-sello`), panel ORDEN SECRETA muestra ítems post-cierre.
- Test existente `'el sobre del objetivo abre el modal al hacer click'` actualizado para manejar ambos formatos de panel (`.objetivo-texto` antiguo y `.objetivo-desc`/`.objetivo-items` nuevo).

### Tablero — corrección de bugs de UX y sistema de eventos (sesión 2026-04-10)

Cinco correcciones de bugs encontrados en pruebas manuales del tablero de juego.

#### 1. Orden conquista/tarjeta

- **Causa**: `TARJETA_OBTENIDA` llega por WebSocket antes de que la respuesta HTTP del ataque sea procesada, por lo que se encolaba primero y aparecía antes del resultado de combate.
- **Fix**: nuevo método `enqueueAtFront(event)` en `TableroEventService` que inserta al principio de la cola con prioridad. El resultado del ataque (CONQUISTA/RESULTADO_DADOS) lo usa en `atacarDesdeModal()` para garantizar que siempre se muestre antes de la notificación de tarjeta.

#### 2. Defensor no mostrado en panel de ataque

- **Causa raíz doble**: (a) type mismatch entre `EstadoPaisDto.idJugador` (string JSON) y `JugadorDto.idJugador` (number TypeScript) hacía que `find()` nunca coincidiera; (b) el template usaba `@if (jugadorDefensor)` que es falsy con string vacío.
- **Fix**: comparación con `Number()` en ambos lados al buscar el jugador defensor; template actualizado para mostrar `'—'` como fallback cuando el nombre es vacío/undefined.

#### 3. Timer — inactividad no arranca en el primer turno

- **Causa**: `iniciarInactividad()` corre desde `iniciarTimer()` en `ngOnInit()`, antes de que `esMiTurno` esté determinado — retorna inmediatamente por la guarda `if (!this.esMiTurno) return`.
- **Fix**: cuando el suscriptor de `partidaObservable` detecta que `esMiTurno` pasa de `false` a `true` por primera vez y no hay intervalo de inactividad activo, llama `iniciarInactividad()`.

#### 4. Timer — al expirar, solo avanzaba una fase

- **Causa**: al llegar a 0, se llamaba `avanzarFaseTurno()` una sola vez, pasando solo la fase actual en lugar del turno completo.
- **Fix**: nuevo método privado `saltarTurnoCompleto(intentosRestantes=3)` que encadena hasta 3 llamadas secuenciales a `cambiarTurno`, verificando después de cada respuesta si `esMiTurno` sigue siendo `true`; si es `false`, el turno ya pasó al siguiente jugador y se detiene.

#### 5. Posición y opacidad de overlays

- **Notificaciones simples más arriba**: `padding-top` del wrapper de eventos reducido de `14%` a `6%` — aparecen en el cuarto superior del mapa en lugar del tercio.
- **Viñeta menos oscura**: gradiente radial de `tablero.component.scss` expandido — zona transparente de `35%` a `50%`, elipse más amplia (`90%×80%` → `95%×88%`), opacidades reducidas de `0.25/0.55/1.0` a `0.18/0.42/0.7`, el mapa central queda notoriamente más visible.

### Tablero — pulido de animación del sobre, notificaciones y modal de país (sesión 2026-04-16)

Seis mejoras de UX/UI al tablero: corrección de la animación del sobre de objetivo, fluidez de notificaciones con bots, posicionamiento inteligente del modal de país, y nuevos tests e2e.

#### 1. Objetivos — actualización tras conquista (verificación)

El progreso ya se refrescaba por WS y por polling al cambiar de turno. Se confirmó que `tablero.component.ts` también llama a `obtenerProgresoObjetivo()` en cada evento `CONQUISTA` recibido — la actualización es inmediata, no solo al final del turno.

#### 2. Animación del sobre de objetivo — solapa triangular real

**Problema**: `.sobre-flap` usaba el truco CSS border con `width: 460px` en lugar de `width: 0`, produciendo un trapezoide de 920 px de ancho desconectado del cuerpo del sobre.

**Fix** (`objetivo-revelacion.component.scss`):
- Reemplazado por `height: 110px; clip-path: polygon(0 0, 100% 0, 50% 100%)` — triángulo real apuntando hacia abajo, unido al cuerpo en el layout flexbox.
- `flex-shrink: 0` para que el flap no colapse.
- Mismo patrón de líneas diagonales que el cuerpo (via `::before`) para coherencia visual.
- Lacre reposicionado a `top: 22px` (dentro del área visible del triángulo).
- Sombra `drop-shadow(0 3px 6px)` proyectada hacia el cuerpo (profundidad correcta).

**Animación de cierre** corregida para que el vuelo hacia el sobre-objetivo sea visible:
- `scale(0.08)` → `scale(0.43)` — 460 px × 0.43 ≈ 198 px, coincide con el ancho real del sobre-objetivo (200 px).
- `opacity` ahora hace fade-out solo en los últimos 250 ms (delay `0.5s`) — el sobre es visible durante el vuelo completo.

#### 3. Panel izquierdo — margen sobre el modal de objetivo

`top: 155px` → `top: 230px` en `.panel-izquierdo` (`tablero.component.scss`). Deja espacio libre para que el `objetivo-modal` desplegado (progreso + ítems) no tape el historial ni las tarjetas.

#### 4. Notificaciones — velocidad adaptativa con bots

**Problema**: los bots ejecutan sus turnos sincrónicamente y generan muchos eventos en ráfaga; las notificaciones se encolaban y bloqueaban al jugador durante decenas de segundos esperando que pasaran una por una.

**Fix** (`tablero-event.service.ts`, método `processNext()`):
- Si `queue.length > 2`, las notificaciones simples (no combate) se muestran máximo **900 ms** en lugar de sus 2500–3500 ms habituales.
- Los eventos de combate (`RESULTADO_DADOS`, `CONQUISTA`) siempre usan su duración completa para no cortar la animación de dados.
- El threshold `> 2` garantiza que las primeras 2 notificaciones del jugador activo se muestren a velocidad normal y solo el backlog de bots se acelera.

#### 5. Modal de país — posicionamiento inteligente

**Problema**: el modal siempre abría a la derecha y abajo del click, por lo que países del borde derecho del mapa mostraban el modal fuera del área de juego.

**Fix** (`tablero.component.ts`, método `paisClickeado()`):
- Si el click está a menos de `MODAL_W + 12 px` del borde derecho del mapa → el modal abre a la **izquierda** del cursor.
- Si el click está en el tercio inferior del mapa → el modal abre **hacia arriba**.
- Fallback `Math.max/min` garantiza que nunca salga del rectángulo del mapa.

#### 6. Tests e2e — nuevos tests para los cambios

Agregados en `e2e/tablero-dev.spec.ts`:

| Test | Qué verifica |
|---|---|
| `la solapa del sobre tiene forma triangular (clip-path correcto)` | `.sobre-flap` tiene altura > 50 px, `clip-path: polygon(...)`, lacre visible |
| `animación de cierre vuela hacia el sobre-objetivo (top-left)` | El sobre es visible durante el vuelo, el overlay desaparece en < 800 ms, el `sobre-objetivo` no se mueve de posición |
| `el modal no sale del área del mapa al clickear países del borde derecho` | `modal.x + modal.width ≤ mapa.x + mapa.width + 4 px` |
| `el modal no sale del área del mapa al clickear países del borde inferior` | `modal.y + modal.height ≤ mapa.y + mapa.height + 4 px` |

Suite completa: **44/44 passing, 16 skipped** (todos condicionales por estado de sesión/turno).

### Tablero — mejoras a la revelación de objetivos y coherencia visual (sesión 2026-04-14)

Refinamiento del sistema de objetivos: animación más rápida, sobre más grande, corrección de la solapa 3D, animación de posición final, coherencia visual entre el sobre de revelación y el del tablero, y actualización en tiempo real del progreso.

#### `ObjetivoRevelacionComponent` — ajustes de animación

- **Delay eliminado**: el componente iniciaba la animación con 400 ms de espera — reducido a 60 ms para que el sobre aparezca inmediatamente al entrar a la partida.
- **Sobre agrandado**: ancho 320 px → 460 px. Bordes de la solapa triangular (border-left/right) ajustados de 160 px a 230 px cada uno. Cuerpo actualizado. La hoja de órdenes emerge -195 px (antes -160 px) para mostrar más contenido.
- **Solapa 3D corregida**: `rotateX(170deg)` plano reemplazado por `perspective(600px) rotateX(175deg)` — la perspectiva inline hace visible el doblez tridimensional.
- **Animación de posición final**: al hacer click en `· ENTENDIDO ·`, la hoja se retrae y la solapa se cierra primero (bindings cambiados de `estado === 'abierto' || estado === 'cerrando'` a solo `estado === 'abierto'`); luego el sobre vuela hacia la esquina superior izquierda del tablero con `transform: translate(-44vw, -43vh) scale(0.08)`.

#### `tablero.component` — sobre coherente con la animación

El sobre pequeño del tablero (`sobre-objetivo`, top-left) rediseñado para coincidir visualmente con el sobre de revelación:

| Elemento | Antes | Ahora |
|---|---|---|
| Cuerpo (`.sobre-paper`) | `#EDE5B0` beige claro | `#C8A96E` dorado igual que el sobre animado |
| Solapa (`.sobre-solapa`) | `#D4C878` amarillo-verde | `#B8934E` dorado oscuro |
| Hover solapa | `rotateX(160deg)` sin perspectiva | `perspective(500px) rotateX(165deg)` 3D visible |
| Lacre | ausente | Círculo rojo `#A01515` con texto "TEG" (`.sobre-lacre-mini`) |
| Textura | sin textura | Líneas diagonales `135deg` igual que cuerpo del sobre grande |

#### Progreso del objetivo — actualización en tiempo real

El `progresoObjetivo` solo se cargaba una vez al inicio (REST) y actualizaba via WebSocket, pero el topic no emitía en todos los casos. Ahora se refresca también:

- **En cada evento `CONQUISTA`**: cuando cualquier jugador conquista un país, el tablero solicita el progreso actualizado (`GET /api/v1/objetivos/progreso/{idJugador}`).
- **Al inicio de cada turno**: cuando el número de turno cambia en el polling, se recarga el progreso. Garantiza que los contadores reflejen el estado real aunque el WebSocket se haya perdido.

#### Corrección ortográfica

- `'Africa'` → `'África'` en `ObjetivoRevelacionComponent.buildItems()` — corregida la tilde faltante en la cadena de texto mostrada en la hoja de órdenes.
- Import `ObjetivoItem` sin uso eliminado del componente.

### Tablero — notificaciones personalizadas, scroll en historial y pulido de revelación (sesión 2026-04-18)

Ronda de refinamientos sobre notificaciones del juego, panel lateral y animación del sobre de objetivo, con coherencia visual total entre el sobre animado y el del tablero.

#### 1. Notificaciones de combate — personalizadas por rol

- **`TableroEventService.enqueueFromWs()`** refactorizado: firma cambia de `(ws, esJugadorLocal: boolean)` a `(ws, miNombre: string | undefined)`.
- **Filtrado global del autor**: si `ws.jugadorNombre === miNombre` el evento se descarta (ya fue encolado por la respuesta HTTP del propio jugador), evitando notificaciones duplicadas.
- **Texto adaptado para ATAQUE/CONQUISTA** según el rol del receptor:
  - Defensor: `¡TE CONQUISTARON!` / `ATAQUE CONTRA TI`, con descripción `"X atacó Y desde Z"`.
  - Observador: `¡CONQUISTA!` / `COMBATE` genérico.
- `TableroComponent` pasa `this.jugadorUsuario?.nombre` al callback WS.

#### 2. Registro de operaciones — más espacio y scroll vertical

- **`.historial-panel`**: `flex: 0 0 auto` → `flex: 1 1 0` con `min-height: 180px` para que ocupe todo el espacio disponible en el panel izquierdo.
- **`.historial-lista`**: `overflow-y: auto` + scrollbar custom (`width: 4px`, `thumb: rgba(198,156,109,0.25)`, fallback `scrollbar-width: thin` para Firefox).
- **`.slice(0, 3)`** eliminado del `@for` — ahora se muestran todas las entradas y el usuario scrollea para consultar historial.
- `.panel-izquierdo top: 230px → 190px` — el panel aprovecha el espacio extra que antes se dejaba para el sobre-objetivo grande.

#### 3. Revelación de objetivo — sobre más chico y hoja que regresa

**Dimensiones más proporcionadas** (`objetivo-revelacion.component.scss`):
- `$sobre-width: 460px → 360px`, `$sobre-flap-height: 100px → 80px`, `$sobre-body-min: 210px → 180px`.
- Lacre PNG `70px → 50px` (coherente con el tamaño del sobre).

**Animación de hoja en dos fases** (nueva keyframe `hoja-emerge-asentarse`):
- `0%`: `translateY(20px)`, `opacity: 0` (hoja escondida dentro del sobre).
- `55%`: `translateY(-220px)`, `opacity: 1` (emerge completamente hacia arriba).
- `100%`: `translateY(-170px)`, `opacity: 1` (desciende 50 px y se asienta sobre el sobre).
- Resultado: el título y los ítems quedan centrados sobre el sobre en lugar de flotar muy arriba fuera del eje visual.

**Animación de cierre refinada**: `translate(-46vw, -44vh) scale(0.55)` — 360 × 0.55 ≈ 198 px coincide con el ancho real del sobre-objetivo (200 px). Delay `0.18s` para dar tiempo a que la solapa se cierre primero, luego el sobre vuela durante 600 ms.

#### 4. Textura de papel sutil + lacre PNG

**Antes**: patrón diagonal áspero `rgba(0,0,0,0.15)` visible incluso a tamaño normal. **Ahora**: `@mixin papel-textura` con mismos valores que `surface-paper` del login (`44deg, rgba(88,58,24,0.045), 1px líneas, 9px gap`). Aplicado por `::before` en:
- `.sobre-flap` (solapa del sobre grande)
- `.sobre-cuerpo` (cuerpo del sobre grande)
- `.hoja` (hoja de órdenes)
- `.sobre-paper` (sobre pequeño del tablero)
- `.sobre-solapa` (solapa del sobre pequeño)

**Lacre PNG**: reemplazado el círculo rojo con texto "TEG" dibujado en CSS por `<img src="assets/images/tablero/teg-wax-seal.png">` en ambos componentes. El elemento se posiciona fuera del `clip-path` del flap (como absolute sobre el sobre-wrap) para evitar que la rotación 3D de apertura lo recorte. Fade-out con `opacity: 0; transform: translateX(-50%) scale(0.85)` cuando la solapa se abre.

#### 5. Insignias — ruta actualizada

Carpeta movida: `assets/images/insignias/` → `assets/images/tablero/insignias/`. Imports actualizados en `tablero.component.ts` (replace_all sobre todas las ocurrencias del mapa de rangos por color).

#### 6. Sobre-objetivo unificado con el sobre de revelación

El sobre pequeño del tablero (`.sobre-objetivo`) reutiliza las mismas variables y proporciones que el grande, con escala 200/360 ≈ 55%:

| Elemento | Grande (revelación) | Chico (tablero) |
|---|---|---|
| Ancho total | 360 px | 200 px |
| Flap (altura) | 80 px | 44 px |
| Lacre PNG | 50 px | 28 px |
| Fondo | `#C8A96E` | `#C8A96E` |
| Gradiente flap | `#D0B173 → #A88340` | `#D0B173 → #A88340` |
| Textura | Mixin `papel-textura` | Mismas líneas inlined |
| Hover rotación | 172° (animación) | 165° (hover) |

#### 7. Sobre-objetivo oculto durante revelación

**Problema**: al cargar el tablero el sobre-objetivo aparecía de golpe, se ocultaba cuando arrancaba la revelación, y reaparecía con pop al final → flasheo desagradable.

**Fix**: 
- `sobreObjetivoVisible = false` por defecto en `TableroComponent`.
- `ObjetivoRevelacionComponent` emite `@Output cerrandoStart` al iniciar el cierre (antes de que el sobre grande empiece a volar).
- `TableroComponent.onRevelacionCerrandoStart()` pone `sobreObjetivoVisible = true` → fade-in de 350 ms (`sobre-obj-appear` keyframe) mientras el sobre grande aterriza.
- Partidas en curso (sin revelación pendiente): else-if en el polling activa `sobreObjetivoVisible = true` inmediatamente.

#### 8. Fix Playwright — `sobre-objetivo` permanece en DOM

**Problema**: el test `'el sobre del objetivo secreto es visible (top-left)'` fallaba cuando el `beforeEach` no lograba descartar el overlay de revelación en 3 s — con `@if (sobreObjetivoVisible)` el elemento no estaba en el DOM y `toBeVisible()` fallaba.

**Fix**:
- `tablero.component.html`: `@if (sobreObjetivoVisible)` → `[class.invisible]="!sobreObjetivoVisible"` — el elemento siempre está renderizado.
- `tablero.component.scss`: `.sobre-objetivo.invisible { opacity: 0; pointer-events: none; animation: none; }` — invisible visualmente pero con bounding-box (Playwright `toBeVisible()` pasa con `opacity:0`).
- `e2e/tablero-dev.spec.ts` `beforeEach`: `waitForTimeout(800)` → `locator('.rev-overlay').waitFor({ state: 'detached', timeout: 3000 })` — elimina flakiness de races con la animación de cierre en el siguiente click.

#### Resultado

Suite completa Playwright: **39 passed, 6 skipped (preexistentes), 0 failed**.

### Tablero — flujo de ataque dual y pulido de UI de combate (sesión 2026-04-19/20)

Continuación del sistema de combate: se migró de un único endpoint sincrónico a un flujo de dos fases (iniciar + defender) para que atacante y defensor humanos colaboren vía WebSocket, y se pulió por completo la UI del modal de combate (`TableroEventDisplayComponent`).

#### 1. Backend — ataque dual y scheduler de timeout

Dos endpoints nuevos en `TurnoController`:
- `POST /turno/ataque/iniciar` — crea un `AtaquePendiente` en memoria (`ConcurrentHashMap` keyed por `idPartida`), broadcast WS `ATAQUE_INICIADO` con `idAtacante`, `idDefensor`, `cantDadosAtacante`, `maxDadosDefensor`, `timerSegundos: 7`. Short-circuit cuando el defensor es `TipoJugador.BOT`: resuelve inline sin guardar pendiente.
- `POST /turno/ataque/defender` — el defensor envía `cantDadosDefensor`, busca el pendiente y ejecuta `resolverInterno(ctx, cantDadosDefensor)` (lógica compartida con el fast-path de bots).

Nuevo componente `AtaquePendienteStore` (bean Spring). Nuevo método `@Scheduled(fixedRate = 1000)` `resolverAtaquesExpirados()` que barre pendientes con >7s de antigüedad y los resuelve con defensa máxima (safety net para desconexiones).

El endpoint viejo `/turno/ataque` sigue vivo exclusivamente para el `BotServiceImpl` (fast-path cuando atacante es bot), evitando el round-trip WS.

#### 2. Frontend — `TableroEventDisplayComponent` con roles de combate

Cuatro roles en el modal: `'local'` (lanzado desde botón atacar, antes del WS), `'atacante'` (miId === idAtacante → "ESPERANDO DEFENSOR"), `'defensor'` (miId === idDefensor → selector de dados + LANZAR), `'espectador'` (ni atacante ni defensor → "COMBATE EN CURSO").

Método `computeRol(event)` resuelve el rol comparando `AuthService.getJugadorId()` contra `event.idAtacante` / `event.idDefensor`. `esCombate` excepción en `enqueueFromWs`: los eventos de combate NO se saltan para el autor (se necesita que el atacante vea su propio `ATAQUE_INICIADO` para esperar al defensor).

Sticky queue fix: si llega WS `ATAQUE`/`CONQUISTA` mientras `currentTipo === 'ATAQUE_INICIADO'`, el service llama `processNext()` antes de encolar el resultado, desbloqueando el modal.

Race HTTP vs WS (short-circuit bot-defensor): el WS `ATAQUE` broadcast llega ANTES que la respuesta HTTP del `iniciarAtaque`. `advanceAfterDice()` solo llama `processNext()` si `currentTipo === 'ATAQUE_INICIADO'`, para no dismissear el resultado recién encolado.

Animación slot-machine: 3s de valores random (setInterval 80ms) en `RESULTADO_DADOS` y `CONQUISTA` antes de revelar los dados reales — shake sobre el panel entero con la clase `.slot-shake`.

#### 3. UI de combate — refactor visual completo

**Layout a 3 columnas simétrico:** `.ted-battle-grid` con `grid-template-columns: 1fr auto 1fr`. Columna izquierda contiene info del atacante (Desde: + país + ficha + nombre) + sus dados centrados. Columna derecha lo mismo para el defensor. Columna central tiene la flecha de dirección. Las columnas laterales (`1fr`) se centran por sí mismas, así la asimetría en cantidad de dados no rompe el balance visual.

**Decoración de fondo:** `.ted-battle-bg-icon` — `<span>` con `mask-image: attack-icon.svg` + `background-color: rgba(67, 42, 30, 1)` + `opacity: 0.07`, posicionado `absolute` en el centro del grid. Sirve de decoración de fondo (z-index 0), puede ser cubierto por cualquier elemento.

**Header con iconos en los extremos:** `.ted-combat-header` usa `grid-template-columns: auto 1fr auto`, con título centrado vía `justify-self: center`. Los attack-icons quedan pegados a los bordes del modal. `margin-bottom: -10px` pega el divider al título.

**Etiquetas semánticas:** `ATACA` → `Desde:`, `DEFIENDE` → `Hacia:`. Tipografía Roboto Slab italic (misma estética que `Resultado:`). Se eliminaron las repeticiones de labels sobre los dados (ya no hacen falta).

**Ficha en lugar de chip de color:** `.ted-player-ficha` — SVG 18px desde `/assets/vectors/tablero/fichas/{color}-player.svg`. Mapa `FICHA_MAP: Color → filename` en el componente. Nombre del jugador coloreado con el hex del design system vía `[style.color]="getColorHex(colorKey)"` (mapa `COLOR_HEX_MAP`).

**Flecha sobria:** color `rgba(67, 42, 30, 0.65)` (era rojo sello translúcido); ahora acompaña al resto de la paleta oscura.

**Banderines extendidos al modal entero:** `.ted-result-banner` con `width: calc(100% + 48px); margin: 0 -24px; overflow: hidden` — se sale del padding del panel y recorta el sobrante. `.ted-banderin` es un `<span>` con `mask-image` de los SVG `military-simbol-inline-{l,r}.svg` + `background-color: currentColor`, `flex: 1 1 0`, `height: 0.9em`. El padre setea `--banner-color` inline con `ganadorColor` getter (hex atacante vs defensor según conquista o pérdidas), y cascadea vía `currentColor`.

**Eliminado:** botón X de cierre en el panel grande (ya se cierra clickeando afuera), separador `⚡` emoji entre grupos de dados (reemplazado por el bg-icon), labels redundantes arriba de los dados. Clases obsoletas eliminadas: `.ted-combatants`, `.ted-combatant`, `.ted-dice-locked`, `.ted-dice-side`, `.ted-dice-result`, `.ted-dice-group`, `.ted-dice-sep-icon`.

#### 4. Fix UTF-8 — datos de objetivos corruptos en DB

Las descripciones de `ObjetivoEntity` mostraban mojibake (`OceanÃ­a`, `Ã�frica`, `espaÃ±a`) porque H2 cargaba `data.sql` con el encoding de la plataforma en vez de UTF-8. Fix doble:

- `application.properties`: `spring.sql.init.encoding=UTF-8` bajo la sección JPA → los nuevos inserts respetan UTF-8.
- `configs/MojibakeFixer.java` (nuevo, `CommandLineRunner` idempotente): al arrancar detecta filas corruptas vía chars `Ã`, `Â`, `\uFFFD` y las reinterpreta con `new String(desc.getBytes(ISO_8859_1), UTF_8)`. Migra la DB ya instalada sin requerir reset del `.mv.db`.

#### 5. Fix modal de objetivos — botón ENTENDIDO no se superpone

`.btn-entendido` en `ObjetivoRevelacionComponent` migrado de layout flex a `position: fixed; bottom: 6vh; left: 50%; transform: translateX(-50%)`. Antes, cuando la hoja tenía texto largo (objetivos de conquista de 6+ países), el botón quedaba pisado por el texto porque `.hoja-wrap` está `position: absolute` dentro de `.sobre-wrap` (no contribuye al alto del contenedor). Anclarlo al viewport lo mantiene siempre visible y separado.

El keyframe `slide-up` se actualizó para incluir el `translateX(-50%)` en ambos estados.

#### 6. Propagación de `colorKey` en `GameEvent`

Nuevos campos en `GameEvent`: `colorJugadorKey?: string`, `colorDefensorKey?: string` (uppercase, ej: `'ROJO'`). Propagados desde `TableroEventService.enqueueFromWs()` y desde `TableroComponent` al encolar eventos locales (ATAQUE_INICIADO). Permiten que `getFichaPath()` y `getColorHex()` en el componente de display resuelvan la ficha SVG y el hex exacto del design system sin duplicar lógica.

### Tablero — reorganización del layout izquierdo y pulido visual (sesión 2026-04-21)

Sesión enfocada en limpiar la columna izquierda del tablero (el sobre del objetivo tapaba el registro de operaciones), reforzar el indicador de turno con un marco visual sobre el avatar del jugador activo, y completar la vista por continentes del mapa.

#### 1. Columna izquierda unificada — sobre + objetivo + panel

Se introdujo un contenedor `.columna-izquierda` (position: absolute; top: 12px; left: 8px; bottom: 8px; width: 200px; flex column) que agrupa `sobre-objetivo`, `objetivo-modal` y `panel-izquierdo` (historial + tarjetas + chat). Así el historial queda siempre debajo del sobre sin solapes, y el ancho pasa a ser consistente (200px).

#### 2. Objetivo se despliega encima del sobre, no debajo

`.objetivo-modal` cambió a `position: absolute; top: 0; left: 0; z-index: 2` dentro de la columna, superponiéndose al sobre cuando se abre. El click en el sobre sigue abriendo el modal; el click en el modal lo cierra. El panel inferior (historial/tarjetas/chat) queda intacto en ambos estados — antes, al abrir el objetivo, empujaba todo hacia abajo.

#### 3. Marco de turno sobre el avatar del jugador activo

Nuevo asset `assets/images/tablero/player-turn-indicator-frame.png` (838×708 — corona de laureles con sol encima). Se renderiza con `.ficha-turn-frame` dentro de `.ficha-avatar-wrapper`, oculto por defecto (`opacity: 0`) y visible solo cuando `.ficha-jugador.activo` (`opacity: 1` con transición de 300ms).

Dimensiones calculadas para que el hueco de la corona contenga al avatar de 44px: marco `90×76px` con offsets `left: -23px; top: -20px`. El sol sobresale ~20px por encima del avatar; el corte inferior del sol coincide con el borde superior del avatar.

Se reemplazó el outline dorado previo sobre `.ficha-insignia` por este indicador mucho más visible.

#### 4. Pipe de nombres — fallback para tildes en descripciones de objetivos

`NombrePaisPipe` ahora corrige también palabras comunes que aparecen en descripciones de objetivos y llegan sin tilde desde la DB (encoding Windows/H2 residual tras el `MojibakeFixer`):

```
paises → países, limitrofes → limítrofes, ejercito → ejército,
destruccion → destrucción, ocupacion → ocupación, eliminacion → eliminación
```

El pipe ya manejaba nombres de continentes; estas son entradas adicionales al mismo `CORRECCIONES` record.

#### 5. Vista por continentes — modo overlay del mapa

Nuevo modo `modoContinente` en `MapaSvgComponent` (`@Input`, default `false`). Cuando está activo, el mapa pinta cada país con el color de su continente (`CONTINENT_FILL_COLORS` export — rgba 0.35 por continente) en lugar del color del dueño. El botón toggle (`.ctrl-btn--activo`) vive en `.mapa-controles` junto a los de zoom; cuando el modo está on, aparece `.leyenda-continentes` en el borde inferior del mapa con los 6 continentes.

`ngOnChanges` ahora escucha `modoContinente` además de `paises` para reconstruir `mapaPais` cuando se toggle. Se eliminó `CONTINENT_BORDER_COLORS` (los bordes por continente se reemplazaron por el relleno).

### Tablero — chat bloc de notas, objetivo flexible y cableado de features pendientes (sesión 2026-04-25)

Sesión enfocada en (a) rediseñar las conversaciones como bloc de notas militar, (b) terminar de cablear las features que en la sesión 2026-04-21 se habían diseñado pero no instanciado en el HTML (marco de turno, toggle de continentes), y (c) reorganizar el objetivo secreto para que se vea completo sin recortes y empuje al panel inferior según su largo.

#### 1. Chat rediseñado como bloc de notas militar

`.chat-panel` pasó de panel transparente a papel físico con espiral metálica superior, barra "CONVERSACIONES" en beige claro, hoja con renglones azules tenues vía `repeating-linear-gradient`, y margen rojo vertical a 52px. Cada mensaje muestra:

- Hora `HH:mm` en monospace dentro del margen izquierdo (color `#9A7850`).
- Nombre del remitente en color del jugador (`var(--player-X)`, asignado dinámicamente).
- Texto del mensaje en italic Roboto Slab (`var(--color-oscuro)`); mensajes propios en `var(--player-azul)`.
- Auto-scroll al último mensaje **solo si** el usuario ya estaba al fondo — `chatPegadoAlFondo` se calcula en `onChatMensajesScroll()` con tolerancia de 6px y se respeta en `scrollChatAlFondoSiCorresponde()`.

Input con underline dashed `var(--color-oscuro)`, placeholder "escribir...", `aria-label` para accesibilidad.

#### 2. Objetivo flexible — se ve completo y empuja al panel

`.objetivo-modal` ya no usa `inset: 0` (que lo recortaba al alto del sobre). Ahora es `position: relative; width: 100%; box-sizing: border-box; max-height: calc(100vh - 280px); display: flex column`. El sobre se quita del flujo cuando aparece el modal vía:

```scss
.sobre-wrapper:has(.objetivo-modal) .sobre-objetivo { display: none; }
```

Resultado: el modal toma el alto natural de su contenido y empuja al `.panel-izquierdo` hacia abajo. El panel se compacta — `.historial-panel` (flex 1 con scroll), `.tarjetas-panel` (grid fijo 3×2 = 136px para 6 tarjetas máximo), `.chat-panel` (height: 160px fijo para 2-3 mensajes + scroll). Si el objetivo excede 280px de alto, `.objetivo-cuerpo` (flex 1 + overflow-y: auto) hace scroll interno como fallback.

#### 3. Marco de turno cableado en HTML

El asset `player-turn-indicator-frame.png` y las reglas SCSS `.ficha-avatar-wrapper` / `.ficha-turn-frame` existían desde la sesión 2026-04-21, pero el HTML solo renderizaba `.ficha-avatar-circle` directo — el wrapper y el `<img>` del marco nunca se instanciaron. Esta sesión envuelve `.ficha-avatar-circle` en `.ficha-avatar-wrapper` (con `overflow: visible` porque el marco 90×76 desborda al wrapper 44×44) y agrega `<img class="ficha-turn-frame" src="...player-turn-indicator-frame.png">` como hermano. Marco visible solo en `.ficha-jugador.activo`.

El glow previo sobre `.ficha-insignia` se mantiene pero con opacidades muy bajas (0.35 / 0.20 / 0.10 en lugar de 0.65 / 0.40 / 0.20) — el marco es ahora el indicador principal y el glow solo aporta calidez sin competir.

#### 4. Toggle de continentes y leyenda cableados

`MapaSvgComponent` aceptaba `@Input modoContinente` y el SCSS definía `.ctrl-btn--activo` y `.leyenda-continentes` desde 2026-04-21, pero no había botón toggle, ni paso del input al `<app-mapa-svg>`, ni render de la leyenda. Esta sesión agrega:

- Método `toggleContinentes()` en `TableroComponent`.
- Propiedad `leyendaContinentes: ReadonlyArray<{nombre, color}>` con los 6 continentes y sus colores rgba 0.55 (más saturados que el relleno del mapa para legibilidad).
- Botón en `.mapa-controles` con `[class.ctrl-btn--activo]` y `(click)="toggleContinentes()"`.
- `[modoContinente]="modoContinente"` pasado al `<app-mapa-svg>`.
- Render condicional `@if (modoContinente) { .leyenda-continentes }` con `@for` sobre el array.

#### 5. Línea roja del chat persistente al scrollear

El pseudo-elemento del margen rojo estaba en `.chat-mensajes::before` con `position: absolute; top: 0; bottom: 0`. Como `.chat-mensajes` es el scroll container, el sistema de coordenadas absolutas se resolvía contra el top del **contenido** (no del viewport visible) y la línea se desplazaba al scrollear.

Solución: mover el pseudo-elemento a `.chat-panel::after` (parent que NO scrollea) con `top: 40px; bottom: 30px` calculados a partir del alto de `.chat-spiral + .chat-title-bar` arriba y `.chat-input-row` abajo. La línea ahora persiste sobre la zona de mensajes mientras el contenido se desplaza por debajo.

#### 6. Columna izquierda — flex column real

`.columna-izquierda` ahora envuelve **ambos** `.sobre-wrapper` y `.panel-izquierdo` como hermanos en flujo flex column (antes el panel quedaba como sibling absoluto del wrapper). Esto es lo que permite que el modal del objetivo empuje al panel — `flex: 1 1 0; min-height: 0` en el panel hace que se compacte cuando el modal crece.

**Trampa encontrada y corregida**: el primer intento envolvió todo el resto del tablero (mapa, controles, panel-derecho) dentro de `.columna-izquierda` por error en la posición del `</div>` de cierre. Resultado: `.mapa-controles` con `position: absolute; right: 225px` resolvía respecto a la columna de 200px y aparecía en `x = -45` (off-screen). Fix: cerrar la columna inmediatamente después de `.panel-izquierdo`, no al final del `.tablero-container`.

También se removió el `pointer-events: none` que se había puesto en `.columna-izquierda` con la idea de dejar pasar clicks al mapa en dead-space — rompía el hit-test del sobre. Como la columna solo cubre 200px del lado izquierdo, no hay necesidad de pasar clicks a través.

### Tablero — ajustes de iconografía y refactor visual de tarjetas (sesión 2026-04-26)

Sesión enfocada en (a) tres ajustes finos de la UI del tablero (icono de continentes, distribución de la leyenda, alineación de la columna izquierda con el borde del mapa) y (b) refactor visual completo de las tarjetas: chips compactos en el panel + modal de detalle con selección manual conectado al canje existente.

#### 1. Icono de continentes — globo terráqueo

`tablero.component.html` reemplaza el SVG inline del botón "Ver continentes" en `.mapa-controles`: la grilla 2×2 anterior pasa a un mundito (círculo + meridiano elíptico + ecuador + dos paralelos sutiles, todos con `currentColor`).

#### 2. Leyenda de continentes — distribución a lo ancho del mapa

`.leyenda-continentes` cambia `justify-content: center; gap: 18px` por `justify-content: space-between; gap: $space-2`, manteniendo `left: 280px / right: 267px` (mismos límites que `.mapa-zona`). Los 6 items quedan repartidos uniformemente borde a borde del mapa en lugar de aglomerados al centro.

#### 3. Conversaciones alineadas con el borde inferior del mapa

`.columna-izquierda` (ambas declaraciones) cambia `bottom: 8px` por `bottom: 48px` para coincidir con el `.mapa-zona { bottom: 48px }`. Como `.historial-panel` es `flex: 1`, absorbe los 40px de diferencia y empuja `.tarjetas-panel` y `.chat-panel` hacia arriba sin alterar sus alturas fijas.

#### 4. Refactor visual de tarjetas — vista mini

`tablero.component.{ts,html,scss}` reemplaza el chip placeholder anterior (44×64, una letra del símbolo) por un chip de papel 60×72 en grilla 2 columnas, hasta 3 filas con scroll. Estructura: zona superior 60% con SVG del símbolo coloreado (32×32) sobre fondo `rgba(67,42,30,0.05)` y línea separadora; zona inferior 40% con nombre del país en Special Elite 8px truncado.

Los SVGs disponibles (`Frontend/src/assets/vectors/tablero/warship-symbol.svg`, `warplane-symbol.svg`, `war-tank-symbol.svg`) usan `fill: #432a1e` fijo; como no usan `currentColor`, se aplican vía `mask-image` + `background-color` mediante un mixin local `simbolo-mask($url, $color)`. Variables locales del componente:

```scss
$card-canion : #2A6B4A;  // verde militar — tanque/cañón
$card-galeon : #2A5F8F;  // azul marino  — barco
$card-globo  : #8F6B1A;  // dorado oscuro — globo aerostático
```

Los símbolos del backend son `CANION | GALEON | GLOBO | COMODIN` (helper `getSimboloKey()` normaliza). Para `COMODIN` se renderizan los 3 SVGs en miniatura (14×14) en el chip y (32×32) en el modal.

#### 5. Modal de detalle "CARTAS EN MANO"

Reemplaza el `canje-modal` anterior (lista de combinaciones precomputadas) por un overlay de pantalla completa (`backdrop-filter: blur(4px)`, `rgba(4,2,1,0.75)`) con un contenedor de papel diagonal de `min(90vw, 700px)`. Borde rojo decorativo via `inset` shadows (`inset 0 0 0 12px var(--color-claro), inset 0 0 0 13.5px rgba(160,21,21,0.4)`) y sello rotado (`<app-stamp type="clasificado" [rotation]="-12">`) en la esquina superior derecha.

Las tarjetas se muestran en grilla 130×180 con rotación leve alterna (`±1°/0.8°`) para sugerir cartas apoyadas sobre la mesa. Si es turno del jugador y fase = `INCORPORACION`, las tarjetas son seleccionables (hasta 3); selección con borde verde (`var(--color-secondary)`) + overlay `rgba(55,94,65,0.15)`. Cuando los 3 ítems forman combinación válida (3 iguales o 3 distintas — replica la regla de `calcularCombinacionesCanje()`), aparece el botón `· CANJEAR ·`. Si no es turno o la fase es otra, las tarjetas son no seleccionables y se muestra el texto "El canje sólo está disponible durante tu turno, en la fase de Incorporar."

Animaciones por keyframes: `tarjetas-overlay-in` (fade-in 250ms ease-out) y `tarjetas-modal-in` (scale 0.92→1 + fade 250ms ease-out). Accesibilidad: `role="dialog"`, `aria-modal="true"`, `aria-label`, cierre con click en backdrop, click en `✕`, o `Escape` (`HostListener('document:keydown.escape')`).

#### 6. Conexión al canje existente — sin reimplementar lógica

Estado refactorizado en `TableroComponent`: `combinacionSeleccionada: number | null` reemplazado por `tarjetasSeleccionadasIds: Set<number>`. Nuevos miembros: `toggleTarjetaCanje(t)`, `estaSeleccionada(t)`, getter `combinacionValida` (3 ids con `set.size === 1 || set.size === 3`), getter `canjeHabilitado` (`esMiTurno && faseActual === INCORPORACION`), helper `getSimboloKey()`. El método `confirmarCanje()` sigue llamando a `tableroServicio.realizarCanje(dto)` y a `tableroEventService.enqueue({tipo: 'TARJETA_CANJEADA', ...})` igual que antes — sólo cambia que `idTarjetas` se construye desde `tarjetasSeleccionadasIds` en lugar de `combinacionesPosibles[combinacionSeleccionada]`. La auto-apertura del modal en INCORPORACION + ≥5 cartas se preserva.

El click en cualquier chip mini (o sobre el botón "TARJETAS") dispara `abrirModalCanje()` (que ahora resetea el `Set`).

#### 7. Renombre de assets de banderines militares

Los archivos `assets/vectors/tablero/military-simbol-inline-{l,r}.svg` se renombraron a `military-symbol-inline-{l,r}.svg` (corrección ortográfica). Imports en `tablero-event-display.component.scss` actualizados a las 4 ocurrencias.

#### 8. Verificación con Playwright

`e2e/tablero-dev.spec.ts` extiende el bloque "Tarjetas — vista mini y modal de detalle" con 5 tests: existencia del panel, screenshot del panel sin cartas, dimensiones 60×72 de los chips, apertura del modal con Escape, y un test que **inyecta tarjetas mock vía `ng.getComponent(host)`** sobre la instancia real de `TableroComponent` — con `setInterval(250ms)` para resistir el polling de la partida que de otra forma sobrescribiría `cartasJugador`. Captura `tarjetas-{panel-mini, panel-con-cartas, modal-detalle, modal-con-cartas}.png` para inspección visual. 6 tests pasan; build de Angular sin errores (sólo deprecation warnings preexistentes de Sass `darken()`).

### Tablero — fixes funcionales y refinamientos visuales (sesión 2026-04-28)

Sesión larga con dos bloques: (a) ajustes de UX del registro/tarjetas/chat, (b) bug-fixes de combate y backlog de bots, (c) iteración fina sobre marco de turno, botón AVANZAR FASE y tipografía de chips.

#### Bug — registro de operaciones no mostraba nada

`historial-panel` usaba `flex: 0 1 auto` con `max-height: clamp(...)`, pero `historial-lista` interno tiene `flex: 1 1 0` que pide al padre todo el espacio remanente; sin alto resuelto en el padre el contenedor colapsaba a la altura del título y los items quedaban renderizados pero invisibles. **El historial sí se poblaba** (verificado con instrumentación: 25 entradas en el array, 20 en el DOM). Fix: `flex: 0 0 clamp(80px, 12vh, 110px)` (alto fijo, suficiente para 2-3 entradas), lo que además libera espacio para que `chat-panel` (flex: 1) crezca hasta el borde inferior del mapa.

#### Bug — tarjeta entregada antes del resultado de combate

En `TurnoServiceImpl.atacar()` el evento WS `TARJETA_OBTENIDA` se emitía dentro del bloque `if (conquista)`, **antes** del evento `CONQUISTA/ATAQUE`. Refactor: la asignación de la carta en DB queda en su lugar (atomicidad transaccional), pero el `convertAndSend("TARJETA_OBTENIDA")` se mueve fuera del bloque, **después** del envío de `CONQUISTA`. El frontend ahora muestra primero el resultado del combate y recién entonces la nueva carta.

#### Canal de historial separado del de notificaciones

`TableroEventService` agrega un segundo Subject `historialEvent$: Observable<HistorialEvento>` que se emite **antes** del filtro `if (esAutor && !esCombate) return` de `enqueueFromWs`. El componente subscribe a este canal para poblar el registro y deja `currentEvent$` exclusivamente para pausar el timer y refrescar el progreso de objetivo. Resultado: las acciones del propio jugador (incorporar, reagrupar, conquistar, obtener tarjeta, canjear) entran al registro sin alterar la política de notificaciones efímeras (que sigue ocultando el banner al autor de la acción). El método privado `emitirHistorialDesdeWs(ws)` mapea cada `PartidaEventWs` a `{ texto, tipo: 'ataque' | 'ok' | 'normal' }`.

#### Combate — duración recortada + modo rápido en backlog

- `duracionMs` por evento WS: `5000 / 4000` → `3000 / 2700` (CONQUISTA / RESULTADO_DADOS).
- Slot machine de dados: `3000ms` → `1600ms` en flujo normal, `600ms` cuando el componente recibe `event.fastMode === true`.
- Nuevo flag `fastMode?: boolean` en `GameEvent`: el servicio lo inyecta en `processNext()` cuando `queue.length > 2`. Para combates aplica reducción adicional (`duracion = 1500ms` en lugar de 3000) y para notificaciones simples ya existía la regla análoga (900ms tope). Click en backdrop sigue funcionando como skip-to-next.

#### Eliminación del botón LANZAR

El botón `· LANZAR ·` en `TableroEventDisplayComponent` se eliminó: ahora `seleccionarDados(n)` setea la cantidad **y dispara** el ataque vía `lanzarSegunRol()` (refactor del antiguo `lanzar()`). El timeout del countdown sigue auto-disparando con la selección vigente. Removidos del componente: método público `lanzar()`, método `puedeLanzar()`, regla SCSS `.ted-attack-btn`. HTML simplificado a 3 labels condicionales (`COMBATE EN CURSO` / `ESPERANDO DEFENSOR` / `ESPERANDO RESULTADO`). Tests E2E (`tablero-dev.spec.ts`, `flujo-completo.spec.ts`) actualizados al nuevo flujo (click en cantidad lanza directo).

#### Tarjetas — textura grunge + marco lineal + tipografía

Tanto `.tarjeta-mini` (58×87, proporción 2:3) como `.tarjeta-detalle` (140×210) ganan dos pseudo-elementos:

- `::before` con `background-image: url('grunge-vintage-old-paper-texture.jpg')`, `mix-blend-mode: multiply`, `opacity: 0.7`, `z-index: -1`. Aplica el papel envejecido del design system (definido en `_surfaces.scss` como `paper-grunge`).
- `::after` con `inset: 3px` (mini) o `6px` (detalle), `border: 1px solid rgba(67,42,30,0.45/0.5)`. Es el marco lineal interior estilo carta de juego clásica.

El nombre del país se renderiza como `· {{ nombre | nombrePais | uppercase }} ·` (uppercase con puntos medios). En `.tarjeta-mini-pais` la tipografía baja a `font-size: 7px` con `letter-spacing: 0.02em` y `padding: 0 6px` para no invadir el marco interior.

#### Distribución de chips en el panel

`tarjetas-contenido` cambia de `display: grid; grid-template-columns: repeat(2, 60px)` a `display: flex; flex-wrap: wrap; justify-content: flex-start; column-gap: $space-2`. Las cartas fluyen de izquierda a derecha en orden de obtención, sin separarse a los bordes (versión inicial usaba `space-between` que dejaba huecos extraños con 2 cartas). El padding del `tarjetas-panel` queda solo vertical (`$space-2 0`) para que entren los 3 chips de 58px (3 × 58 + 2 × 8 = 190 < 200).

#### Modal "CARTAS EN MANO" — fondo oscuro y combinaciones posibles

Refactor completo del modal: se elimina el contenedor de papel + sello rotado + borde rojo decorativo. El overlay (`rgba(4,2,1,0.82)` + `backdrop-filter: blur(5px)`) es el único fondo y las tarjetas flotan directamente sobre él. Título `CARTAS EN MANO` y subtítulos en `var(--color-claro)` con `text-shadow`. Botón cerrar circular en la esquina superior derecha del overlay. Botón `· CANJEAR ·` con clase `btn-teg-dark` (variante apropiada para fondo oscuro).

Indicador de combinaciones posibles agregado **debajo de la grilla**, siempre visible con 3+ cartas: `· N combinaciones posibles ·` o `· Sin combinaciones posibles ·`. Lee de `combinacionesPosibles.length` que ya se calculaba en el componente.

#### Chat — margen, límite, anti-spam, color uniforme

- Línea roja `::after`: `left: 52 → 28px`. `chat-msg padding-left: 60 → 34px`. `chat-time width: 48 → 26px` con `font-size: 9 → 8px` (5 chars `HH:mm` × ~4.8px monospace = 24px, entra en 26px).
- Input acepta máximo 120 caracteres (`maxlength="120"` + slice defensivo en `enviarChat`).
- Anti-spam: tras 4 mensajes en 10s, cooldown de 15s con countdown visible (`.chat-cooldown` 9px sans-serif rojo sello), input y botón `disabled` durante el bloqueo.
- Color de mensajes uniforme: `.chat-text` y `.chat-input` cambiados de `var(--player-azul)` a `var(--color-oscuro)` — el azul colisionaba con el jugador azul; la identificación del autor ya está en `.chat-sender` (coloreado).

#### Chat — alto extendido al borde inferior del mapa

`chat-panel` cambia de `height: 160px` fijo a `flex: 1 1 0; min-height: 200px; max-height: 380px`. Toma todo el espacio sobrante de `panel-izquierdo` hasta el `bottom: 48px` de la columna izquierda, que ya coincide con el `bottom: 48px` de `mapa-zona`. Verificado: `chat-bottom: 852px === map-bottom: 852px`.

#### Marco de turno del avatar — bajar y evitar recorte izquierdo

El marco PNG (90×76 con corona/sol que sobresale del wrapper 44×44 del avatar) usaba `top: -20px; left: -23px`. Dos problemas:

1. El sol quedaba demasiado arriba — la usuaria pidió que el corte inferior coincida con la curva superior del marco circular del avatar. Ajuste fino: `top: -20 → -17px` (3px más abajo).
2. El borde izquierdo del marco se recortaba. Causa: `panel-derecho` tenía `overflow: hidden` y `panel-jugadores` tenía `overflow-y: auto` — la spec **CSS Overflow Module 3** computa `overflow-x: visible` a `auto` cuando el otro eje no es visible, recortando el contenido horizontal. Fix triple: `panel-derecho { overflow: visible }`, `panel-jugadores { overflow-x: visible; padding-left: $space-6 }` (24px de padding-left para alojar los -23 del marco).

#### Botón AVANZAR FASE — visibilidad y centrado vertical

El botón existía y estaba renderizado pero pasaba desapercibido por contraste insuficiente (`background: rgba(160,21,21,0.35)`, `font-size: 8px`, `border: 0.5px translúcido`). Tras feedback de la usuaria (que prefería el rojo discreto), se mantiene la paleta original pero se aplica el patrón de centrado vertical de `%btn-teg-base`:

```scss
padding: 7px 12px 4px;  // top > bottom para compensar baseline alto de Special Elite
line-height: 1;
```

El truco — visible en todo el design system (`%btn-teg-base { padding: 14px 28px 10px; line-height: 1 }`) — corrige la sensación de texto "subido" propia de la fuente Special Elite, que asienta su baseline más arriba del centro óptico del bbox.

#### Notas finas

- Cada `EstadoTarjetaDto` se renderiza como un chip individual (`@for (t of cartasJugador; track t.idEstadoTarjeta)`). No hay agrupador por símbolo + badge contador — verificado por grep, no hay código residual de versiones previas.
- Comentario explícito agregado en `agregarHistorial`: `unshift` inserta al índice 0 → el evento más reciente queda arriba, sin necesidad de scrollear.
- Verificación visual con scripts Playwright temporales en `C:/Users/sofia/AppData/Local/Temp/teg-verify/`: `verify2.js` (vista general + modal + chat), `verify-frame.js` (marco de turno), `verify-avanzar.js` (botón AVANZAR FASE en distintos viewports), `diagnose-historial.js` (instrumenta `enqueueFromWs` y `agregarHistorial` con spies para diagnosticar el bug del registro).

### Fin de partida — detección inmediata + animación de papel quemado + overlay de resultado (sesión 2026-04-28)

Antes de esta sesión la partida solo terminaba al cerrar la fase de REAGRUPACIÓN (vía `verificarGanador` en `cambiarFaseTurno`) y el frontend saltaba directo a `/estadisticas` sin anunciar al ganador. Esta sesión implementa la detección inmediata, una animación cinematográfica solo para el ganador y un overlay sincronizado entre todos los jugadores con el resultado final.

#### Backend — detección inmediata y broadcast de fin de partida

- **`Dtos/JugadorResultadoDto.java`** y **`Dtos/FinPartidaDto.java`** (nuevos): payload con ganador (`JugadorDto`), `objetivoCumplido` (`ObjetivoProgresoDto`), clasificación ordenada por `cantidadPaises` desc y `momentoFin`.
- **`PartidaEventDto`** extendido: nuevo tipo `FIN_PARTIDA` y campo opcional `finPartida: FinPartidaDto`. Decisión clave: **reusamos el topic existente** `/topic/partida.{idPartida}.evento` en lugar de crear `/topic/partida/{id}/fin` — el frontend ya estaba suscripto, así que evitamos duplicar canales y desuscripciones.
- **`ObjetivoServiceImpl.construirFinPartida(Long idJugadorGanador)`**: arma el DTO completo iterando los jugadores de la partida, recolectando países desde `EstadoPaisRepository`, y derivando `eliminado` cuando `cantidadPaises === 0 && perdio`.
- **`TurnoServiceImpl.resolverInterno()`** — punto crítico: tras una conquista (línea ~983, dentro del bloque `cantidadDefensorTropas <= 0`), llama a `objetivoService.verificarObjetivos(jugador.getIdJugador())` **antes** de emitir el WS `CONQUISTA`. Si gana, fija `victoriaInmediata = true`. Después del `CONQUISTA` se emite `FIN_PARTIDA` (orden visual: combate → fin). Si hubo victoria inmediata, se omite el `TARJETA_OBTENIDA` (la partida ya terminó, no tiene sentido).
- **`TurnoServiceImpl.cambiarFaseTurno()`**: chequeo temprano `if (partida.estadoPartida != EN_JUEGO) return false;` — frena al bot ganador y al siguiente bot del loop, que de otra forma seguían con sus `Thread.sleep` entre fases ignorando el fin.
- **`ObjetivoService` interface**: agregado `construirFinPartida(Long)`.

#### Frontend — interfaces TypeScript y servicios

- **`partida.interface.ts`**: nuevas interfaces `JugadorResultado` y `FinPartida` espejando los DTOs Java.
- **`game-event.interface.ts`**: `FIN_PARTIDA` agregado a `GameEventTipo` y campo `finPartida?: FinPartida` en `PartidaEventWs`.
- **`tablero-event.service.ts`**: `enqueueFromWs` ignora explícitamente `FIN_PARTIDA` (no se encola como notificación efímera) — el `TableroComponent` lo captura directo desde el callback WS para evitar latencia adicional.

#### Frontend — `ObjetivoQuemadoComponent` (animación de papel quemándose)

Componente standalone montado dentro de `.sobre-wrapper` con `position: absolute; inset: 0`. Solo se monta para el jugador ganador. **Estrategia A** (preferida en el plan): GSAP + `mask-image: radial-gradient` + filtro SVG `feTurbulence`/`feDisplacementMap` + canvas con partículas — todo sin librerías nuevas (GSAP 3.14.2 ya estaba en `package.json`).

5 fases temporales (3000ms total), encadenadas con `gsap.timeline`:

| Tiempo | Fase | Cambios |
|---|---|---|
| 0–200ms | Encendido | glow naranja sube a 0.2 |
| 200–800ms | Propagación inicial | mask `transparente: 0→30%`, glow 0.2→0.6 |
| 800–1800ms | Combustión activa | mask 30→80%, glow al máximo (1.0), `displacementMap.scale: 8→14→8` con `yoyo` para que el borde "respire" |
| 1800–2500ms | Consumición | mask 80→110%, glow decrece a 0.4 |
| 2500–3000ms | Desvanecimiento | host `opacity: 1→0`, glow a 0 |

Detalles técnicos:
- La **máscara radial** se reescribe en cada frame de GSAP vía `onUpdate` — es la forma confiable de animar mask-image en Chromium (no usa CSS variables porque `mask-image` no las consume).
- El **filtro SVG** se inyecta inline (no en `:host` SCSS) con `id` único por instancia (`burnEdge_<random>`), para evitar colisiones si llegara a haber dos componentes montados.
- El **canvas de brasas** corre con `requestAnimationFrame` propio (no GSAP — es más simple para un bucle de partículas). 25 partículas máximo, spawn rate variable según fase (pico durante combustión activa: ~1.2/frame), spawn desde el borde del círculo de combustión (`radioActual` derivado de `maskState.transparente`), gravedad leve (+0.05 vy/frame), vida -0.015/frame, render con `fillRect` (más rápido que `arc()/fill()` para 25 partículas).
- El canvas se sincroniza con `devicePixelRatio` (clamp a 2x) para que se vea nítido en HiDPI sin reventar performance.
- `ngOnDestroy` limpia el timeline GSAP, `cancelAnimationFrame` y el `setTimeout` de fallback (3100ms) por si GSAP fuera matado externamente.

#### Frontend — `FinPartidaOverlayComponent` (overlay de resultado)

5 zonas verticales sobre fondo `rgba(4,2,1,0.92)` con `backdrop-filter: blur(6px)`, z-index `calc(var(--z-vignette) + 2)`:

1. **Insignia + medalla**: insignia del ganador (110px, color del player) + medalla TEG (60px, encajada al pie inferior derecho). Reusa los assets existentes `assets/images/tablero/insignias/{color}-insignia.png` y `assets/images/tablero/teg-wax-seal.png`. TODO comments para los SVGs dedicados (`insignia-ganador.svg`, `medalla-teg-oro.svg`) cuando estén listos. Halo dorado vía `drop-shadow(0 0 24px rgba(244,204,64,0.4))`.
2. **Identidad**: avatar circular 80px, nombre del ganador en Special Elite 32px en color del player, subtítulo "COMANDANTE VICTORIOSO" 11px letter-spacing 0.3em.
3. **Objetivo cumplido**: separadores 1px arriba/abajo, "MISIÓN CUMPLIDA", descripción del objetivo en italic, lista de items con check verde `■` y "actual/objetivo ✓".
4. **Bajas en combate**: cards horizontales por jugador no-ganador con avatar 40px, nombre coloreado, "X países", `· ELIMINADO ·` en `var(--color-sello)` con opacidad 0.5 si fue eliminado.
5. **Botón** `· VER ESTADÍSTICAS ·` (clase `btn-teg-dark`): aparece a los 3500ms desde el inicio del overlay con `pointer-events: none` antes de eso.

Animación de entrada escalonada con `gsap.timeline` (consistente con el quemado, en lugar de `animation-delay` CSS): overlay 0–800ms, insignia 400ms (back.out, scale 0.4→1), identidad 900ms (slide-up + fade), subtítulo 1200ms, objetivo 1600ms, ítems 1800+N×120ms con stagger, bajas 2300ms, botón 3500ms.

#### Frontend — integración en `TableroComponent`

- **Suscripción WS**: en el callback de `suscribirseEventosPartida`, si `evento.tipo === 'FIN_PARTIDA' && evento.finPartida`, se invoca `alRecibirFinPartida(evento.finPartida)` directamente y se hace `return` antes de pasar el evento al `TableroEventService`.
- **`alRecibirFinPartida(dto)`**: idempotente (ignora si ya hubo fin), congela la UI (`partidaFinalizada = true`), corta `tableroServicio.stopPolling()`, todos los timers (`timerInterval`, `inactividadInterval`) y pausa el `tableroEventService` (`timerPausado = true`). Si el jugador local **es** el ganador → `mostrarAnimacionQuemado = true` (el overlay esperará a `animacionCompleta`). Si **no** es → `setTimeout(() => mostrarFinPartida = true, 3000)` para que la pantalla quede congelada el mismo tiempo que dura la animación del ganador. **Esto sincroniza la pantalla de resultado entre todos los jugadores** — sin la espera, los espectadores verían el overlay ~3s antes que el ganador, spoileando el resultado durante su animación.
- **Lógica anterior reemplazada**: el `if (result.estado === EstadoPartida.TERMINADA)` que saltaba a `/estadisticas` se simplificó a "detener polling silenciosamente y esperar el WS" — el overlay ya no depende del polling REST.
- **Template**: `<app-objetivo-quemado>` se monta dentro de `.sobre-wrapper` con `@if (mostrarAnimacionQuemado)`, y `<app-fin-partida-overlay>` al final del template fuera del `.tablero-container` con `@if (mostrarFinPartida && datosFinPartida)`.
- `irAEstadisticas()` navega a `/estadisticas` (ruta ya existente).
- `ngOnDestroy` agregó cleanup del `finPartidaTimeout`.

#### Verificación visual con Playwright

Script en `C:/Users/sofia/AppData/Local/Temp/fin-partida-visual.js` (siguiendo memoria: scripts ad-hoc en `/tmp/`, no archivos en `e2e/`). Usa el `storageState` guardado por `tablero-dev.setup.ts`, accede al componente Angular vía `window.ng.getComponent(document.querySelector('app-tablero'))` y llama directamente `alRecibirFinPartida(dto)` con datos sintéticos. Para que esto funcione fuera del flujo WS, el método se cambió de `private` a public (sin riesgo: ya era idempotente y un punto de entrada de evento).

Capturas resultantes en `e2e/screenshots/`:
- `fin-00-baseline.png`: tablero normal antes del evento
- `fin-quemado-zoom-{A-E}.png`: zoom sobre `.sobre-wrapper` a 400/900/1500/2200/2800ms — muestran encendido → núcleo amarillo con halo radial → pico con anillo dorado intenso → consumición → fade out
- `fin-03-overlay-ganador.png`: overlay del ganador (insignia roja, avatar, nombre del player en color rojo, "COMANDANTE VICTORIOSO", objetivo cumplido)
- `fin-04-perdedor-congelado.png`: a 1.5s del evento del perdedor — el overlay aún NO aparece, valida el delay de 3s
- `fin-05-overlay-perdedor.png`: overlay del bot ganador (insignia azul) con las 3 cards de "BAJAS EN COMBATE", el bot eliminado en opacidad 0.5 con `· ELIMINADO ·` en rojo, y el botón "VER ESTADÍSTICAS" habilitado

#### Notas finas

- El error de presupuesto en `tablero.component.scss` (29.40 kB > 24 kB) es **preexistente** a esta sesión (modificación previa al commit base). La compilación TypeScript del frontend pasa limpio; el backend compila con `./mvnw compile` sin warnings nuevos.
- La carta no se otorga si la conquista fue la victoria — `cartaOtorgada && !victoriaInmediata`. Si se otorgara igual, el evento `TARJETA_OBTENIDA` llegaría después del `FIN_PARTIDA` y rompería el orden visual.
- El `ataquePendienteStore` no se purga explícitamente cuando termina la partida; si hubiera un ataque pendiente, el scheduler `resolverAtaquesExpirados` (cada 1s) intentará resolverlo, y `validarYCalcularAtaque` fallará con `IllegalStateException` que se atrapa en el catch del scheduler. Es aceptable — no genera efectos visibles.
- El pre-existing `partida.estado === EstadoPartida.TERMINADA` flow del polling se dejó como **fallback silencioso**: detiene polling y timers sin navegar — para casos de recarga después del fin sin haber recibido el WS (el overlay no se muestra porque tampoco hay datos del DTO; esos casos quedan en un estado neutro hasta que el usuario navegue).

### Sistema de pactos entre jugadores + iteraciones de UX del tablero (sesión 2026-04-30)

Sesión amplia: implementación completa del sistema de pactos del reglamento TEG (3 tipos, ciclo de vida completo, ruptura voluntaria con período de gracia, ruptura automática por conquista) y dos rondas de feedback visual sobre el tablero (botón de pactos, modal de tarjetas, indicador de canjes, fix del bug de reagrupar).

#### Backend — sistema de pactos

- **`models/TipoPacto.java`** y **`models/EstadoPacto.java`** (nuevos): enums con los 3 tipos del reglamento (`PACTO_PAISES`, `PACTO_MUNDIAL`, `PACTO_ZONA_INTERNACIONAL`) y los 6 estados del ciclo de vida (`PROPUESTO`, `ACTIVO`, `ROTO_VOLUNTARIO`, `ROTO_AUTOMATICO`, `RECHAZADO`, `EXPIRADO`).
- **`Entities/PactoEntity.java`**: tabla `pactos` con FKs a `JugadorEntity` (jugadorA proponente, jugadorB receptor), `PaisEntity` opcional para país protegido por cada parte (`PACTO_PAISES`) o país aislado del continente (`PACTO_ZONA_INTERNACIONAL`), `PartidaEntity`, timestamps de propuesta/aceptación/ruptura, y campos `turnoRupturaJugador` + `turnoNumeroAlRomper` para el período de gracia. Creada automáticamente por JPA con `ddl-auto=update`.
- **`Repositories/PactoRepository.java`**: queries `findByPartida_IdPartidaAndEstado`, `findByPartida_IdPartida`, `findActivosEntreJugadores(idA, idB, idPartida)` y `findActivosDelJugador(idJugador, idPartida)` — todas filtran a estados `ACTIVO` o `ROTO_VOLUNTARIO` para reflejar pactos vigentes (los rotos en gracia siguen siendo respetados por la validación de ataques).
- **`Dtos/PactoDto.java`** y **`Dtos/ProponerPactoDto.java`** (nuevos): payload para serialización REST y WS.
- **`PartidaEventDto`** extendido: nuevo campo `pacto: PactoDto` para los nuevos tipos de evento `PACTO_PROPUESTO`, `PACTO_ACEPTADO`, `PACTO_RECHAZADO`, `PACTO_ROTO`. Reusa el topic existente `/topic/partida.{idPartida}.evento` (el frontend ya estaba suscripto).
- **`Services/PactoService.java`** + **`Services/ServicesImpl/PactoServiceImpl.java`** (nuevos): operaciones `proponer`, `aceptar`, `rechazar`, `romperVoluntariamente`, `listarActivos`, `ataqueViolaPactoActivo`, `verificarRupturaAutomaticaPorConquista`, `expirarPactosDeJugadorEliminado`, `procesarPactosEnGracia`. Validaciones de reglamento implementadas:
  - Solo se puede proponer durante el propio turno; no consigo mismo; no si ya hay pacto activo del mismo tipo.
  - **`PACTO_PAISES`**: ambos países deben pertenecer a las partes que los protegen.
  - **`PACTO_ZONA_INTERNACIONAL`**: el proponente debe poseer todos los países del continente menos uno; el restante debe ser del receptor; si hay un tercero en el continente, no califica.
  - **Ataque vs pacto**: si el atacante tiene `PACTO_MUNDIAL` con el dueño del país atacado → bloquea; si `PACTO_PAISES` y el país está en la lista protegida → bloquea; si `PACTO_ZONA_INTERNACIONAL` y el país atacado coincide con `paisZona` → bloquea. Lanza `ResponseStatusException(CONFLICT)`.
  - **Ruptura automática por conquista de tercero**: cuando un jugador NO parte del pacto conquista un país protegido, el pacto pasa a `ROTO_AUTOMATICO` y se emite WS.
  - **Ruptura por eliminación**: cuando un jugador queda eliminado, todos sus pactos pasan a `EXPIRADO`.
  - **Período de gracia voluntario**: el jugador que rompe debe seguir respetando el pacto durante `cantJugadores * 2` turnos (su próximo turno + un turno completo del otro). El conteo es `(nroTurnoActual − turnoNumeroAlRomper)`.
- **`Controller/PactoController.java`**: 5 endpoints REST bajo `/api/v1/pacto`: `POST /proponer`, `POST /{id}/aceptar`, `POST /{id}/rechazar`, `POST /{id}/romper`, `GET /partida/{partidaId}/activos`.
- **`TurnoServiceImpl.validarYCalcularAtaque()`**: integra `pactoService.ataqueViolaPactoActivo()` antes de procesar el ataque — devuelve HTTP 409 si viola un pacto.
- **`TurnoServiceImpl.resolverInterno()`**: tras la conquista, llama `verificarRupturaAutomaticaPorConquista`. Si el defensor queda eliminado, llama `expirarPactosDeJugadorEliminado`.
- **`TurnoServiceImpl.pasarTurno()`**: al inicio de cada turno llama `procesarPactosEnGracia` con el nuevo `nroTurno` para liberar pactos que cumplieron el período de gracia.
- **Bug funcional corregido — reagrupar múltiples veces**: removido el bloqueo `if (turnoActual.isReagrupado())` en `TurnoServiceImpl.moverFichas` (línea ~409). El reglamento oficial del TEG permite mover ejércitos libremente entre países conectados durante toda la fase de Reagrupamiento, sin límite de movimientos. La validación previa era una simplificación interna no documentada. El campo `reagrupado` en la entidad se mantiene por compatibilidad pero ya no actúa como freno.

#### Frontend — interfaces, servicio y eventos

- **`core/models/interfaces/pacto.interface.ts`** (nuevo): tipos `TipoPacto`, `EstadoPacto`, `PactoDto`, `ProponerPactoDto` espejando el backend.
- **`core/services/pacto.service.ts`** (nuevo): cliente REST con `proponer`, `aceptar`, `rechazar`, `romper`, `listarActivos`.
- **`game-event.interface.ts`**: agregados los 4 tipos `PACTO_*` a `GameEventTipo` y campo opcional `pacto: PactoDto` en `GameEvent` y `PartidaEventWs`.
- **`tablero-event.service.ts`**: extensión del `enqueueFromWs` para los 4 nuevos eventos. `PACTO_PROPUESTO` no se encola como notificación efímera (el overlay de respuesta es el feedback principal); los otros 3 generan notificaciones simples (3000/2500ms). Tipo `'pacto'` agregado al `HistorialEvento` para colorear las entradas del registro de operaciones en azul (`rgba(120,160,210,0.90) italic`).

#### Frontend — `BotonPactosComponent`

Componente standalone (`tablero/componentes/boton-pactos/`) con composición visual de dos imágenes superpuestas: hojas (`assets/images/tablero/players-pacts-pages.png`) ocupando el botón completo y pluma (`assets/images/tablero/players-pacts-pen.png`) cruzada en diagonal. **Sin contenedor decorativo** — solo las imágenes son visibles. 110×110px. Animación de hover: la pluma rota -25° y se eleva -7px en una sola transición sobre `transform` (movimientos simultáneos), las hojas escalan x1.02 con leve translateY(-2px). `pointer-events: none` en ambas imgs para que los clicks lleguen siempre al `<button>` aunque la pluma sobresalga del bbox. Badge numérico opcional (esquina superior derecha) con la cantidad de pactos activos del jugador local; estado deshabilitado con opacidad 0.45 cuando no es el turno.

#### Frontend — `PactosOverlayComponent`

Componente standalone con overlay completo (`backdrop-filter: blur(4px)` + `rgba(4,2,1,0.82)`, `min(90vw, 720px)` de ancho). Sin caja de papel — los elementos flotan sobre el fondo oscuro siguiendo la decisión del modal de tarjetas. Dos secciones cuando `vista === 'lista'`:

1. **TRATADOS VIGENTES**: lista de cards horizontales por pacto activo, con avatares de las dos partes, tipo de pacto, descripción detallada (`Brasil ⟷ Sahara`, `Sin restricción geográfica`, `Madagascar — Continente África`). Si el jugador local es parte y el pacto está `ACTIVO`, botón `· ROMPER ·`; si está `ROTO_VOLUNTARIO`, badge `EN RUPTURA` en `var(--color-sello)` con tooltip explicando el período de gracia.
2. **Acción**: botón `· PROPONER NUEVO PACTO ·` (deshabilitado fuera de turno con texto "Solo podés proponer pactos durante tu turno.").

Wizard de 3 pasos cuando se hace click en proponer (reemplaza el contenido del overlay sin cerrarlo):

- **Paso 1 — TIPO**: 3 cards seleccionables (`PACTO_PAISES`, `PACTO_MUNDIAL`, `PACTO_ZONA_INTERNACIONAL`). La card de zona internacional se deshabilita si el jugador no tiene un continente con todos los países menos uno.
- **Paso 2 — RECEPTOR**: cards de los demás jugadores con avatar coloreado, nombre, países y ejércitos. Si ya hay un pacto activo entre ambos del mismo tipo, la card se muestra deshabilitada con "Ya hay un pacto activo entre ustedes". Para zona internacional, solo se muestran los jugadores dueños del país aislado.
- **Paso 3 — DETALLES**: dropdowns específicos del tipo (dos selects para PACTO_PAISES, ninguno para MUNDIAL, dropdown de continente para ZONA_INTERNACIONAL). Vista previa textual del compromiso. Botón `· ENVIAR PROPUESTA ·` que llama al endpoint y muestra notificación de éxito.

Cierre con `Escape` o click en backdrop.

#### Frontend — `RespuestaPactoOverlayComponent`

Overlay para responder a una propuesta entrante. **Bloqueante para el receptor**, **no bloqueante para el resto** (`pointer-events: none` en el backdrop, solo el documento del tratado captura clicks). Diseño tipo "tratado diplomático" en pergamino: encabezado "PROPUESTA DE TRATADO", avatares + nombres de las dos partes con flecha entre ellos, tipo de pacto en grande, descripción detallada, botones `· ACEPTAR ·` / `· RECHAZAR ·` (solo para receptor), countdown de 30 segundos visible que dispara auto-rechazo si no se responde a tiempo. Para los demás muestra "Esperando respuesta de [nombre receptor]...".

#### Frontend — integración en `TableroComponent`

- Estado nuevo: `pactosActivos: PactoDto[]`, `showPactosOverlay: boolean`, `propuestaPendiente: PactoDto | null`.
- Carga inicial vía `pactoService.listarActivos()` cuando se establece la suscripción WS.
- `alRecibirEventoPacto(ws)` maneja los 4 tipos: PROPUESTO abre el overlay de respuesta a todos (modo bloqueante o informativo según rol), ACEPTADO/RECHAZADO/ROTO cierran el overlay si refiere al mismo pacto y refrescan la lista. Notificación `success` al jugador local cuando un pacto en el que participa es firmado.
- Botón cableado en el panel-derecho como último hijo flex-column con `margin-top: auto` + `padding-bottom: 40px` para alinear su base con `bottom: 48px` del mapa.
- Nuevo getter `cantidadPactosLocales: number` para el badge.

#### Iteraciones de UX (rondas de feedback visual)

**Ronda 1** — sobre el tablero base:

- **Modal de tarjetas**: cards 140×210 → 180×270, símbolos 80px → 110px, tipografía país 11px → 14px. Título `CARTAS EN MANO` → `TARJETAS EN MANO`. Margen superior título → grid: 18px (era 0).
- **Indicador de canje en panel TARJETAS**: confirmado que NO existía nada que avisara desde el panel — el conteo de combinaciones solo se mostraba dentro del modal. Agregado `.tarjetas-canje-light`: LED de 10px, gris/oscuro apagado, verde radial con halo + animación pulse de 2.4s cuando `combinacionesPosibles.length > 0`. Alineado al extremo derecho del título "TARJETAS" (botón con `display: flex; justify-content: space-between`).
- **Botón pactos — bug del click**: la pluma con `right: -10%` se renderizaba fuera del bounding box del button. Cualquier click sobre la parte visible de la pluma fallaba. Solución: `pointer-events: none` en ambas imgs (decorativas) y reposicionar la pluma en `right: 0`.
- **Chat conversaciones**: `max-height: 380px → 300px`.

**Ronda 2** — feedback adicional:

- **Chat 200px exactos + posición fija**: `flex: 1 1 0` → `flex: 0 0 200px` con `margin-top: auto` para que SIEMPRE quede pegado al borde inferior de la columna izquierda (alineada con `bottom: 48px` del mapa). No crece ni se mueve aunque el modal del objetivo o las tarjetas cambien de alto.
- **Tarjeta seleccionada conserva el marco lineal interior**: el `--seleccionada` antes sobrescribía el `::after` (cambiando `border: none; background: green` y rompiendo el marco). Ahora el tinte verde se aplica vía `inset 0 0 0 9999px rgba(55, 94, 65, 0.20)` box-shadow (pinta sobre background pero bajo el `::after`), y el `::after` solo cambia el color del border a `rgba(40, 90, 55, 0.7)` manteniendo `inset: 6px` y `border: 1px solid`.
- **Botón pactos — pluma más pequeña + animación simultánea**: pluma `width: 70% → 52%` (~57px en botón de 110px). Rotación más sutil `-30° → -25°`. Desplazamiento aumentado `-4px → -7px`. Una sola declaración `transition: transform` para que rotación y traslación animen exactamente en simultáneo.
- **Panel tarjetas: 3 por fila + altura limitada**: `display: flex; flex-wrap: wrap` (que solo dejaba 2 chips de 58px en 200px de ancho) → `display: grid; grid-template-columns: repeat(3, 1fr); gap: 6px`. Las cartas usan `width: 100%; aspect-ratio: 2/3` (≈60×90px). `tarjetas-panel { max-height: 200px; overflow-y: auto }` para que no domine la columna izquierda cuando el jugador acumula muchas cartas.

#### Verificación con Playwright

Scripts ad-hoc en `C:/Users/sofia/AppData/Local/Temp/`:
- `verify-pactos.cjs`: end-to-end completo (login → sala → 3 bots → tablero → botón pactos → wizard 3 pasos → POST /pacto/proponer → 200 OK).
- `verify-respuesta-overlay-2.cjs`: inyección de `propuestaPendiente` mock vía `ng.getComponent` para capturar el overlay de respuesta en sus dos modos (receptor bloqueante, proponente no-bloqueante).
- `verify-ajustes.cjs` y `verify-ajustes-2.cjs`: verificación numérica de las dos rondas de UX (posición y tamaño del botón, animación de hover, alturas del chat y panel tarjetas, marco lineal preservado en tarjetas seleccionadas, grid 3-col).

Capturas en `e2e/screenshots/pactos-*.png`, `ajustes-*.png` y `r2-*.png`.

#### Notas finas

- WebSocket config tiene `enableSimpleBroker("/topic")` solo, sin `/queue`. El reglamento dice "los pactos son públicos" así que todos los eventos van al topic compartido — el frontend filtra por `idJugadorReceptor` para decidir si abre el overlay en modo bloqueante.
- Antes de levantar el backend hay que recordar que `ddl-auto=update` crea automáticamente la tabla `pactos` sin necesidad de borrar el `.mv.db`.
- Hay un solapamiento conocido de z-index entre `respuesta-pacto-overlay` (`var(--z-vignette) + 5`) y `rev-overlay` del componente `objetivo-revelacion` (`var(--z-vignette) + 10`). En flujo real no afecta porque el rev-overlay se descarta antes de cualquier interacción de pacto, pero los screenshots de verificación tuvieron que eliminar el rev-overlay del DOM manualmente para capturar el de respuesta.

### Símbolos visuales en wizard de pactos (sesión 2026-05-03)

- **Tres insignias PNG nuevas** en `Frontend/src/assets/images/tablero/`: `countries-pact-symbol.png` (escudo con apretón de manos y dos blasones), `global-pact-symbol.png` (medallón circular con manos sobre el globo), `continental-pact-symbol.png` (escudo hexagonal con torre y mapa de Europa). Diseñadas como medallas de bronce/cobre con relieve, alpha transparente.
- **`pactos-overlay.component.html` (paso 1 del wizard)**: agregado `<img class="tipo-card-symbol">` antes del `tipo-card-titulo` en cada una de las 3 cards (`PACTO_PAISES` → countries, `PACTO_MUNDIAL` → global, `PACTO_ZONA_INTERNACIONAL` → continental). `alt=""` + `aria-hidden="true"` por ser decorativos (la card tiene su título textual y descripción).
- **`pactos-overlay.component.scss`**: `.tipo-card` migrada de `text-align: left` a `display: flex; flex-direction: column; align-items: center; text-align: center` para alojar el símbolo encima centrado. Nueva regla `.tipo-card-symbol` con `width/height: 96px`, `object-fit: contain`, `margin-bottom: $space-2` y stack de `drop-shadow` cálidos oscuros (`0 1px 2px rgba(20,10,4,0.45)` + `0 0 6px rgba(20,10,4,0.20)`) — siguiendo el patrón "drop-shadow apilado para PNG con alpha" del design system, pero con tonos oscuros (no dorados, que son del estado activo del indicador de turno).
- **Verificación con Playwright**: script ad-hoc en `C:/Users/sofia/AppData/Local/Temp/verificar-pactos.js`. Capturas guardadas en `.claude/scr-pactos-tipos.png` (overlay completo) y `.claude/scr-pactos-tipos-zoom.png` (recorte de las 3 cards). Símbolos legibles a 96px, sin colisiones con título ni descripción, alineados al ritmo visual existente del wizard.

#### Notas operativas
- Backend Spring Boot requiere `JAVA_HOME` exportado para arrancar via `mvnw`; `npm start` desde la raíz no lo hereda automáticamente. Workaround usado: `export JAVA_HOME="/c/Users/sofia/.sdkman/candidates/java/current"` antes del comando.

### Refinamientos UI tablero — selects, símbolos y respuesta de pactos (sesión 2026-05-04)

Continuación de la sesión anterior. Tres iteraciones de feedback visual con Playwright.

#### Iteración 1 — selects fuera del DS

- **Selects del wizard de pactos** (`detalle-col select`) usaban estilos default del browser con chevron gris fluent del SO. Reemplazado con `appearance: none` + chevron SVG inline (data URI) en `#432A1E` (`var(--color-oscuro)`), fondo doble (textura diagonal repetida 44deg + `var(--color-claro)`), padding `7px 28px 5px 10px` (asimétrico para Special Elite, conservado por consistencia), focus en `var(--color-sello)`. `<option>` también herena tipografía/colores del DS.
- **`.modal-select` del modal-pais** (ATACAR/REAGRUPAR destino): mismo patrón pero adaptado al modal pequeño (210px ancho) — chevron 8×5px, padding `4px 18px 3px 5px`, textura escalada al ritmo del `.modal-papel` (cada 7px), overlay sutil `rgba(67,42,30,0.06)` para diferenciarlo del fondo papel.
- **Investigación**: el componente legacy `acciones-pais` (en `componentes/acciones-pais/`) tiene 2 selects nativos sin estilo, pero **no se usa en ningún lugar activo** (solo auto-referencias) — dead code, no se tocó.

#### Iteración 2 — modal-pais, sobre, animación pluma, símbolo respuesta

**Modal-país — botón cerrar y validación de ataque**:
- Botón "✕ cerrar" textual al final del cuerpo reemplazado por `.modal-close-x` minimalista posicionado `position: absolute; top: 4px; right: 6px` sobre el borde de color del jugador. Mismo patrón visual que `.pactos-cerrar` (sin recuadro, hover `transform: scale(1.18)` + cambio de color).
- **Reglamento**: para iniciar un ataque hace falta más de 1 ejército en el país (uno se queda defendiendo). Antes el botón ATACAR estaba siempre habilitado y el backend respondía error → notificación. Ahora `tablero.component.ts` expone `tropasInsuficientesParaAtacar` (getter `cantidadTropas <= 1`); el botón aplica `[disabled]` + `[attr.title]` y muestra debajo `<p class="modal-aviso">` con texto explicativo en `var(--color-sello)` italic. La sección sigue visible (no se oculta) para que el usuario entienda *por qué* no puede atacar.

**Sello del sobre objetivo**:
- "MI OBJETIVO" → "MISIÓN URGENTE" en el `.sobre-label`.
- `.sobre-sello` (badge CSS rojo "SECRETO" con border y rotación) reemplazado por `<img class="sobre-stamp" src="assets/vectors/top-secret-stamp.svg">`. SVG con viewBox 185.24×67.87 (ratio ~2.73:1), width 100px, `mix-blend-mode: multiply` para que se asiente en el papel sin parecer pegado, `transform: rotate(-5deg)` y drop-shadow apilado (oscuro + tinte rojo sutil).

**Pluma del botón pactos — animación más visible**:
- Iteración previa quedó con `translateY(-7px)` después del `rotate(-25deg)` — el translateY actuaba en el frame rotado, generando ~6.3px verticales reales. Cambio: orden invertido `translateY(-X) rotate(-25deg)` para tener traslación pura en frame de pantalla. Esta sesión `-X` subió de `-12px` a `-20px` y duración de 360ms a 420ms con `cubic-bezier(0.34, 1.5, 0.5, 1)` (overshoot ease-out). Resultado: el peak queda incluso más alto que -20px y el desplazamiento es claramente visible.

**Selects del wizard a 2× tamaño**:
- `.tipo-card-symbol` width/height 96px → 192px. Los relieves (escudo de manos, globo, escudo continental) son ahora plenamente legibles.

#### Iteración 3 — overlay de respuesta de pactos rediseñado

`RespuestaPactoOverlayComponent` extendido con 2 nuevos `@Input`:
- `jugadores: JugadorDto[]` — para distinguir bots de humanos por `tipoJugador === 'BOT'`.
- `resultado: 'ACEPTADO' | 'RECHAZADO' | null` — el padre lo setea cuando llega el WS PACTO_ACEPTADO/RECHAZADO en lugar de limpiar `propuestaPendiente` directamente. El overlay queda visible 3000ms (`TIEMPO_RESULTADO_MS`) mostrando el resultado antes de auto-emitir `respondido`.

**Símbolo del tipo de pacto** centrado encima del documento como medalla colgante: `position: absolute; top: -70px; width/height: 96px` con drop-shadow apilado, ~26px de overlap dentro del documento (compensado con `padding-top: 60px` para no invadir el título).

**Avatares condicionales** (helper `avatarJugadorA/B()` en el componente):
- Humano → `<img class="resp-avatar resp-avatar--img" [src]="avatarFor(id)" [style.border-color]="colorVar(...)">` (la foto del avatar con el color del jugador como borde).
- Bot → mantiene el `<div class="resp-avatar" [style.background-color]>` original (círculo de color).

**Resultado APROBADO/RECHAZADO** como pill rectangular con tipografía `var(--font-heading)`, font-size 22px, letter-spacing 0.28em, border 2px solid currentColor, animación `respPactoStamp` (zoom + leve rotación) `cubic-bezier(0.34, 1.4, 0.64, 1)`. Color verde `#1E7A50` para APROBADO, `var(--color-sello)` para RECHAZADO. Ambos con fondo translúcido del mismo color (8% opacidad).

#### Bug encontrado de paso

`<h2 class="resp-pacto-titulo">PROPUESTA DE TRATADO</h2>` heredaba `color: rgb(239, 232, 206)` (`var(--color-claro)`) desde el typography global aplicado a `h2`. Sobre el fondo papel cream del documento eso lo hacía **invisible**. Override explícito agregado en `.resp-pacto-titulo { color: var(--color-oscuro); }`. Sin esto el wizard mostraba solo el subtítulo italic "Documento diplomático".

#### Verificación con Playwright

Script `verificar-iter3.js` cubre 6 escenarios: sobre con TOP SECRET visible, hover de pluma con elevación, modal-pais en INCORPORACIÓN (close X + input sin spinner), modal-pais en ATAQUE con tropas=1 (botón disabled + aviso), close button del overlay de pactos sin recuadro, y respuesta-pacto-overlay en sus 3 estados (esperando + APROBADO + RECHAZADO) inyectando el `propuestaPendiente` mock vía `ng.getComponent`. Capturas en `.claude/scr-iter3-*.png`.
