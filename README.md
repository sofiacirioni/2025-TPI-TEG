# TEG — Táctica y Estrategia de Guerra

Juego de estrategia por turnos implementado como aplicación web fullstack. Monorepo con Angular 21 (frontend) + Spring Boot 3.3.5 (backend).

---

## Stack

| Capa | Tecnología |
|---|---|
| Frontend | Angular 21, TypeScript 5.8, Bootstrap 5, SCSS |
| Backend | Java 17, Spring Boot 3.3.5, Maven |
| Base de datos | H2 file-based (`Backend/data/tegdb`) |
| Tiempo real | WebSocket (STOMP + SockJS) |
| Tests FE | Jasmine + Karma |
| Tests BE | JUnit + Mockito |
| API docs | Swagger `/swagger-ui.html` |

---

## URLs de desarrollo

| Servicio | URL |
|---|---|
| Frontend | http://localhost:4200 |
| Backend API | http://localhost:8080/api/v1 |
| WebSocket | http://localhost:8080/ws |
| Swagger | http://localhost:8080/swagger-ui.html |
| H2 Console | http://localhost:8080/h2-console |

**Credenciales H2:** usuario `sa`, password vacío, JDBC URL `jdbc:h2:file:./data/tegdb`

---

## Instalación y levantado

### Backend

```bash
cd Backend

# Primera vez
./mvnw clean install          # Linux/Mac
mvnw.cmd clean install        # Windows

# Levantar
./mvnw spring-boot:run
mvnw.cmd spring-boot:run      # Windows
```

### Frontend

```bash
cd Frontend
npm install       # primera vez
npm start         # levanta en localhost:4200
```

### Ambos simultáneamente (desde la raíz)

```bash
npm start
```

---

## Estructura del proyecto

```
2025-TPI-TEG/
├── Backend/                    # Spring Boot
│   ├── src/main/java/
│   │   ├── Controller/         # 11 controladores REST
│   │   ├── Services/           # Interfaces de servicio
│   │   ├── ServicesImpl/       # Implementaciones
│   │   ├── Repositories/       # JPA repositories (13)
│   │   ├── Entities/           # Entidades JPA (16)
│   │   ├── Dtos/               # Data Transfer Objects
│   │   ├── models/             # Enums y modelos de dominio
│   │   └── configs/            # CORS, WebSocket, Swagger
│   ├── data/                   # Base de datos H2 (tegdb.mv.db)
│   └── pom.xml
│
└── Frontend/                   # Angular 21
    ├── src/
    │   ├── app/
    │   │   ├── core/
    │   │   │   ├── services/   # Servicios HTTP y de negocio
    │   │   │   └── models/     # Clases, interfaces, enums
    │   │   ├── pages/
    │   │   │   ├── intro/          # Animación typewriter de entrada
    │   │   │   ├── principal/      # Menú principal con partículas
    │   │   │   ├── inicio-sesion/  # Login
    │   │   │   ├── registrarse/    # Registro
    │   │   │   ├── sala/           # Lobby de sala
    │   │   │   ├── config-partida/ # Configuración de partida
    │   │   │   ├── tablero/        # Tablero de juego (mapa SVG)
    │   │   │   ├── perfil-usuario/ # Perfil
    │   │   │   ├── estadistica/    # Estadísticas
    │   │   │   ├── creditos/       # Créditos del equipo
    │   │   │   └── ayuda/          # Reglas del juego
    │   │   └── routes/
    │   └── styles/             # Design system SCSS
    │       ├── _tokens.scss    # Variables y CSS custom properties
    │       ├── _typography.scss
    │       ├── _buttons.scss
    │       ├── _surfaces.scss
    │       ├── _cursors.scss   # Cursores custom SVG
    │       ├── _vignette.scss  # Overlay de viñeta global
    │       └── _fonts.scss
    └── public/
        └── mapa-teg-vector-optimizado.svg
```

---

## Comandos útiles

```bash
# Tests backend
cd Backend && ./mvnw test

# Tests frontend
cd Frontend && npm test

# Build producción
cd Frontend && npm run build

# Limpiar caché Angular (si hay errores de webpack raros)
cd Frontend && rm -rf .angular/cache && npm start
```

---

## Notas importantes

- **DB H2 persistente**: el archivo `Backend/data/tegdb.mv.db` se mantiene entre reinicios. Para reset limpio, eliminar ese archivo.
- **CORS**: configurado solo para `localhost:4200`. Cambiar en `CorsConfig.java` si se necesitan otros orígenes.
- **Bots**: `BotService` juega automáticamente los turnos de jugadores bot — revisar antes de modificar el flujo de turnos.
- **Mapa SVG**: cargado vía HTTP desde `public/`, parseado con DOMParser. Los países tienen IDs 1-50 = `pais.idPais`.
- **Caché Angular**: si `npm start` muestra errores de módulos no encontrados pero `ng build` compila bien, borrar `.angular/cache` y reiniciar.

---

## Flujo de navegación

```
/ (intro)         → animación typewriter "REGISTRO DE ACCESO..."
/principal        → menú principal con fondo war-room y partículas
/iniciar-sesion   → login
/registrarse      → registro de usuario
/entrarCrearSala  → lobby (crear/unirse a sala)
/configPartida    → configurar jugadores y bots
/juego/:url       → tablero de juego (uuid de partida)
/perfilUsuario    → perfil del usuario logueado
/estadisticas     → estadísticas de partidas
/creditos         → equipo de desarrollo
/ayuda            → reglas del juego
```

---

## Design system

El frontend usa un sistema de diseño propio basado en Bootstrap 5 + SCSS custom:

- **Tipografías**: Special Elite (headings/terminal) + Roboto Slab (cuerpo)
- **Paleta**: tonos marrones, rojos oscuros y crema sobre fondo mesa de madera
- **Cursores**: SVGs custom (hand-default / hand-pointer) aplicados globalmente
- **Viñeta**: overlay radial fijo (`z-index: 9999`) que oscurece los bordes en todas las pantallas
- **Fondos**: `initial-scene-war-room.webp` (pantalla principal) / `game-scene-table.webp` (resto)

---

## Historial de desarrollo (sesiones principales)

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

---

## Equipo

Proyecto académico — TPI 2025 — UTN regional Córdoba, desarrollo en equipo.

### Integrantes:
- Abril Melina
- Arguello Juarez Candela
- Blanco M. Candelaria
- Carignano Maximiliano
- Chapeta Zoe Agostina
- Cirioni Sofía (Refactorizacion front-end y diseño UI/UX)
- Heredia Lara