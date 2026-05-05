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

## Historial de desarrollo

El historial completo de sesiones de desarrollo (decisiones de diseño, refactors, bug-fixes, sistema de pactos, fin de partida, etc.) se mantiene en [CHANGELOG.md](./CHANGELOG.md).

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
