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
