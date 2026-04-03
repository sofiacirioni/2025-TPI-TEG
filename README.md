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
- **Colores de trazo corregidos**: strokes del SVG cambiados de `rgba(0,0,0,...)` a `rgba(67,42,30,...)` (`--color-oscuro`). Sombras de fichas mantienen negro puro (son drop-shadows decorativas).

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