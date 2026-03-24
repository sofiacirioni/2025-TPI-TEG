# TEG - Táctica y Estrategia de Guerra (2025 TPI)

Juego de estrategia por turnos implementado como aplicación web fullstack. Monorepo con Angular 19 (frontend) + Spring Boot 3.3.5 (backend).

## Stack

| Capa | Tecnología |
|---|---|
| Frontend | Angular 19, TypeScript 5.8, Bootstrap 5, ng-bootstrap |
| Backend | Java 17, Spring Boot 3.3.5, Maven |
| Base de datos | H2 file-based (`Backend/data/tegdb`) |
| Tiempo real | WebSocket (STOMP + SockJS) |
| Tests FE | Jasmine + Karma |
| Tests BE | JUnit + Mockito |
| API docs | Swagger `/swagger-ui.html` |

## URLs de desarrollo

- Frontend: http://localhost:4200
- Backend API: http://localhost:8080/api/v1
- WebSocket: http://localhost:8080/ws
- Swagger: http://localhost:8080/swagger-ui.html
- H2 Console: http://localhost:8080/h2-console (user: `sa`, pass: vacío)

## Comandos

```bash
# Levantar ambos simultáneamente (desde raíz)
npm start

# Solo backend
cd Backend && ./mvnw spring-boot:run

# Solo frontend
cd Frontend && npm start

# Tests backend
cd Backend && ./mvnw test

# Tests frontend (Karma, headed)
cd Frontend && npm test

# Build producción
cd Frontend && npm run build
```

## Arquitectura del Backend

**Patrón**: Controller → Service (interface) → ServiceImpl → Repository → Entity

```
Controller/         # 11 controladores REST
Services/           # Interfaces de servicio
ServicesImpl/       # Implementaciones
Repositories/       # JPA repositories (13)
Entities/           # Entidades JPA (16)
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
    services/       # 9+ servicios HTTP y de negocio
    models/
      class/        # 18+ clases de dominio
      interfaces/   # TypeScript interfaces
      enums/        # Color, Simbolo, TipoObjetivo
  features/
    InicioSesion/   # Login
    Registrarse/    # Registro
    Principal/      # Menú principal
    Sala/           # Lobby de sala
    ConfigPartida/  # Configuración de partida
    tablero/        # Tablero de juego principal
      componentes/
        mapa-svg/           # Mapa SVG interactivo
        acciones-pais/      # Panel de acciones por país
        jugadores-panel/    # Lista de jugadores
        dados-modal/        # Modal de dados
    PerfilUsuario/  # Perfil de usuario
    Estadistica/    # Estadísticas
    Ayuda/          # Reglas del juego
```

## Convenciones de código

### Backend (Java)
- Nombres de clases en PascalCase
- Controladores en `Controller/`, servicios en `Services/` e implementaciones en `ServicesImpl/`
- DTOs en `Dtos/` separados por dominio
- Endpoints REST bajo `/api/v1/`
- Excepciones centralizadas en `GlobalExceptionHandler.java`

### Frontend (Angular/TypeScript)
- Componentes standalone (Angular 19)
- Archivos de modelo en `core/models/`
- Servicios en `core/services/`
- Features en `features/` con estructura propia (component, service, styles)
- Nombres de carpetas de features en PascalCase

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

- La DB H2 es persistente (archivo `Backend/data/tegdb`). Al cambiar entidades con `ddl-auto=update`, puede haber conflictos. Para reset limpio, eliminar el archivo `.mv.db`.
- El CORS está configurado solo para `localhost:4200`. Cambiar en `CorsConfig.java` si se necesitan otros orígenes.
- Los bots (`BotService`) tienen lógica de juego automática — revisar antes de modificar el flujo de turnos.
- El mapa del tablero es SVG interactivo (`mapa-svg/`).

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
   · ACCIÓN ·
6. Los colores de jugador sobre fondo #EFE8CE deben tener contraste 
   mínimo 3:1 — no modificar sin verificar accesibilidad

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