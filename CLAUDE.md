# TEG - Táctica y Estrategia de Guerra (2025 TPI)

Juego de estrategia por turnos implementado como aplicación web fullstack. Monorepo con Angular 21 (frontend) + Spring Boot 3.3.5 (backend).

## Stack

| Capa | Tecnología |
|---|---|
| Frontend | Angular 21 (standalone), TypeScript 5.9, SCSS, GSAP |
| Backend | Java 17, Spring Boot 3.3.5, Maven |
| Base de datos | PostgreSQL 15 (Docker), migraciones con Flyway |
| Tiempo real | WebSocket (STOMP + SockJS) |
| Tests E2E | Playwright (`e2e/`) — el frontend no tiene tests unitarios |
| Tests BE | JUnit + Mockito, 291 tests |
| API docs | Swagger `/swagger-ui.html` |

## URLs de desarrollo

- Frontend: http://localhost:4200
- Backend API: http://localhost:8080/api/v1
- WebSocket: http://localhost:8080/ws
- Swagger: http://localhost:8080/swagger-ui.html
- Con Docker todo se sirve desde http://localhost (Nginx proxya `/api` y `/ws`)

**Usuarios semilla** (cargados por Flyway): `test@test.com`, `comandante@test.com`,
`aliado@test.com` — todos con contraseña `Test123@`.

## Comandos

```bash
# Stack completo en Docker (requiere .env — copiar de .env.example)
docker compose up --build

# Desarrollo local: solo la base en Docker, app nativa con hot-reload
docker compose up -d postgres
npm start

# Solo backend (necesita el postgres arriba)
cd Backend && ./mvnw spring-boot:run

# Solo frontend
cd Frontend && npm start

# Tests backend
cd Backend && ./mvnw test

# Tests end-to-end (Playwright levanta backend y frontend por su cuenta)
npx playwright test

# Build producción
cd Frontend && npm run build
```

## Arquitectura del Backend

**Patrón**: Controller → Service (interface) → ServiceImpl → Repository → Entity

```
Controller/         # 13 controladores REST
Services/           # Interfaces de servicio
ServicesImpl/       # Implementaciones
Repositories/       # JPA repositories (14)
Entities/           # Entidades JPA (18, más AuditableEntity como MappedSuperclass)
Dtos/               # Data Transfer Objects
models/             # Enums y modelos de dominio
configs/            # CORS, WebSocket, Swagger, ModelMapper
```

**Entidades clave**: `UsuarioEntity`, `JugadorEntity`, `PartidaEntity`, `SalaEntity`, `PaisEntity`, `TurnoEntity`, `ObjetivoEntity`, `TarjetaEntity`, `EstadoPaisEntity`

## Arquitectura del Frontend

**Patrón**: Standalone components + Services + RxJS

```
src/app/
  core/
    services/       # 13 servicios HTTP y de negocio
    models/
      class/        # Clases de dominio
      interfaces/   # TypeScript interfaces
      enums/        # Color, Simbolo, TipoObjetivo
  pages/
    intro/          # Animación typewriter de entrada
    inicio-sesion/  # Login
    registrarse/    # Registro
    principal/      # Menú principal
    sala/           # Lobby de sala
    config-partida/ # Configuración de partida
    tablero/        # Tablero de juego principal
      componentes/
        mapa-svg/             # Mapa SVG interactivo
        jugadores-panel/      # Lista de jugadores
        dados-modal/          # Modal de dados
        chat-partida/         # Chat en tiempo real
        pactos-overlay/       # Wizard de tratados
        fin-partida-overlay/  # Diario de guerra
    perfil-usuario/ # Legajo: libreta de enrolamiento + hoja de servicios
    estadistica/    # Parte de campaña (pizarra de tiza)
    creditos/       # Expedientes del equipo
    ayuda/          # Reglamento (carpeta de archivo con pestañas)
  shared/
    components/     # paper-card, stamp, player-token, toast
  routes/
```

## Convenciones de código

### Backend (Java)
- Nombres de clases en PascalCase
- Controladores en `Controller/`, servicios en `Services/` e implementaciones en `ServicesImpl/`
- DTOs en `Dtos/` separados por dominio
- Endpoints REST bajo `/api/v1/`
- Excepciones centralizadas en `GlobalExceptionHandler.java`

### Frontend (Angular/TypeScript)
- Componentes standalone con la sintaxis de control flow nueva (@if / @for)
- Archivos de modelo en `core/models/`
- Servicios en `core/services/`
- Pantallas en `pages/`, cada una con su component + scss
- Carpetas de pantalla en kebab-case dentro de `pages/`

## Websocket / Tiempo real

El juego usa STOMP sobre SockJS para actualizaciones en tiempo real del estado de la partida. Configurado en:
- Backend: `WebSocketConfig.java`
- Frontend: `socket.service.ts`

## Testing con Playwright

Playwright está configurado con `--headed` para verificar cambios visuales. Usar para:
- Flujos de login/registro
- Navegación entre pantallas
- Interacciones con el tablero
- WebSocket en tiempo real

```bash
# Ejecutar tests Playwright
npx playwright test

# Test específico
npx playwright test nombre-test.spec.ts
```

## Notas importantes

- El esquema lo gobierna **Flyway**, no Hibernate: la app corre con `ddl-auto=validate`. Al cambiar una entidad hay que agregar una migración nueva en `Backend/src/main/resources/db/migration/` (nunca editar una ya aplicada — Flyway valida el checksum). Sin la migración, el arranque falla a propósito. Reset limpio: `docker compose down -v`.
- Las fechas `fecha_alta` / `fecha_actualizacion` las escribe Spring Data JPA Auditing sobre las entidades que heredan de `AuditableEntity` (usuarios, salas, partidas, mensajes). `@EnableJpaAuditing` va en `Application` y no en una `@Configuration` aparte, porque `@DataJpaTest` arma su contexto desde esa clase.
- Los tests usan H2 en memoria (Flyway apagado, esquema generado por Hibernate) para no depender de un Postgres levantado.
- El CORS está configurado solo para `localhost:4200`. Cambiar en `CorsConfig.java` si se necesitan otros orígenes.
- Los bots (`BotService`) tienen **una sola dificultad**, deliberadamente básica: refuerzos al azar, ataque al vecino con menos tropas, sin reagrupar y sin jugar el objetivo secreto. Cada fase espera `cooldownMs` (3 s) para que el humano vea lo que hacen. Revisar antes de modificar el flujo de turnos.
- Las filas BOT de `jugadores` se graban con el `id_usuario` de quien creó la partida: toda consulta que parta del usuario tiene que filtrar por `tipoJugador = HUMANO`.
- El mapa del tablero es SVG interactivo (`mapa-svg/`).
- La tabla `estadisticas` y su servicio siguen en el código pero **nadie los escribe**: el histórico sale de agregar `jugadores` contra `partidas`.

## Design System — TEG Online

### Concepto y atmósfera
La interfaz simula un cuarto de operaciones militares del siglo XX 
(circa 1939–1945). El jugador debe sentirse como un comandante 
reunido alrededor de una mesa de planificación estratégica. Toda decisión visual debe 
reforzar esta experiencia de inmersión histórica.

Referencias visuales: Cabinet War Rooms de Churchill, película 
Darkest Hour (2017), documentos clasificados de la OSS/MI6.

### Escenas principales
- **Escena intro** (`scene-intro`): vista en perspectiva del cuarto 
  de guerra, oscuro, con lámpara encendida. Imagen: 
  `initial-escene-war-room.webp`
- **Escena juego** (`scene-game`): plano cenital de la mesa de madera 
  con objetos de época en los bordes. Imagen: `game-escene-table.webp`

### Criterios visuales que DEBEN cumplirse
1. Las fuentes Special Elite (títulos) y Roboto Slab (cuerpo) deben 
   cargarse desde assets locales — nunca desde Google Fonts
2. El cursor debe ser una mano de caballero victoriano en todo momento
3. La viñeta oscura debe ser visible en los cuatro bordes de la pantalla
4. Ningún elemento de UI debe verse "moderno" — sin bordes redondeados 
   grandes, sin sombras de colores, sin gradientes vibrantes
5. Los botones siguen el patrón pill con Special Elite y puntos medios: 
   · ACCIÓN · — y la variante la decide el FONDO sobre el que se apoyan, 
   no la pantalla: `.btn-teg-dark` (borde y texto claros) sobre fondo 
   oscuro —mesa, overlay, escena—, `.btn-teg-light` (borde y texto 
   oscuros) sobre papel claro, y `.btn-teg-primary` (relleno verde) sólo 
   cuando ninguno de los dos contrasta lo suficiente. `.btn-teg-danger` 
   queda para acciones irreversibles. Un botón claro sobre un overlay 
   negro no se lee: fue el bug de la interfaz de tratados.
6. Los colores de jugador sobre fondo #EFE8CE deben tener contraste 
   mínimo 3:1 — no modificar sin verificar accesibilidad
7. El botón de volver usa la clase global `.btn-volver`: fijo arriba a 
   la izquierda, con el texto "Volver" y nada más. No crear variantes 
   por pantalla

### Paleta
- Fondo papel/mapa: #EFE8CE
- Texto principal: #432A1E  
- Fondo mesa: imagen WebP (no CSS)
- Sello/peligro: #A01515
- Jugadores: rojo #A01515, azul #1A4080, naranja #BF6800, 
  púrpura #6B2490, verde #1E7A50, dorado #6B4C00

### Lo que NO debe aparecer
- Fuentes sans-serif modernas (Arial, Inter, sistema)
- Colores saturados fuera de la paleta definida
- Sombras de colores (solo sombras negras/oscuras cálidas)
- Bordes redondeados mayores a 8px (excepto botones pill)
- Cualquier elemento que rompa la ilusión de "documento físico 
  sobre una mesa de madera"