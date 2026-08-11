# TEG — Táctica y Estrategia de Guerra

Juego de estrategia por turnos implementado como aplicación web fullstack. Monorepo con Angular 21 (frontend) + Spring Boot 3.3.5 (backend).

---

## Stack

| Capa | Tecnología |
|---|---|
| Frontend | Angular 21, TypeScript 5.8, Bootstrap 5, SCSS |
| Backend | Java 17, Spring Boot 3.3.5, Maven |
| Base de datos | PostgreSQL 15 |
| Migraciones | Flyway (`Backend/src/main/resources/db/migration`) |
| Tiempo real | WebSocket (STOMP + SockJS) |
| Tests FE | Jasmine + Karma |
| Tests BE | JUnit + Mockito (H2 en memoria) |
| API docs | Swagger `/swagger-ui.html` |

---

## Levantar todo con un comando

Requiere Docker.

```bash
cp .env.example .env
docker compose up --build
```

Listo: http://localhost

Al arrancar, Flyway crea el esquema y carga el tablero (continentes, países,
fronteras, objetivos, tarjetas) más tres usuarios de prueba. No hace falta
ningún paso manual sobre la base.

### Usuarios de prueba

Los tres usan la contraseña `Test123@`:

| Correo | Usuario |
|---|---|
| test@test.com | testuser |
| comandante@test.com | comandante |
| aliado@test.com | aliado |

Son credenciales de desarrollo conocidas — quitar `V3__seed_users.sql` antes de
cualquier despliegue real.

---

## URLs

| Servicio | Con Docker | Desarrollo local |
|---|---|---|
| Frontend | http://localhost | http://localhost:4200 |
| Backend API | http://localhost/api/v1 | http://localhost:8080/api/v1 |
| WebSocket | http://localhost/ws | http://localhost:8080/ws |
| Swagger | http://localhost/swagger-ui.html | http://localhost:8080/swagger-ui.html |

---

## Desarrollo local (sin Docker para la app)

La base sigue siendo PostgreSQL, así que se levanta solo ese contenedor y el
resto corre nativo con hot-reload:

```bash
docker compose up -d postgres
npm start
```

`npm start` levanta backend y frontend a la vez. El backend toma por defecto
`jdbc:postgresql://localhost:5432/tegdb`, que es el puerto que publica el
contenedor, así que ambos modos comparten la misma base.

---

## Migraciones de base de datos

El esquema lo gobierna Flyway y la app corre con `ddl-auto=validate`: Hibernate
verifica que las entidades coincidan con las tablas y **nunca** las modifica.

Para cambiar el modelo hay que agregar un archivo nuevo — no editar los ya
aplicados, porque Flyway valida su checksum:

```
Backend/src/main/resources/db/migration/
├── V1__init_schema.sql       # tablas y claves foráneas
├── V2__reference_data.sql    # tablero del juego
├── V3__seed_users.sql        # usuarios de prueba
└── V4__lo_que_sigue.sql      # ← nuevos cambios acá
```

Para empezar de cero, borrar el volumen:

```bash
docker compose down -v && docker compose up --build
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
│   ├── src/main/resources/
│   │   └── db/migration/       # Migraciones Flyway (esquema + semillas)
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

- **Base de datos**: PostgreSQL en Docker, persistida en el volumen `teg-postgres-data`. Para reset limpio: `docker compose down -v`.
- **Esquema**: lo gobierna Flyway, no Hibernate. Cambiar una entidad sin agregar la migración correspondiente hace fallar el arranque (`ddl-auto=validate`) — eso es intencional.
- **CORS**: sale de `FRONTEND_URL` en el `.env` (coma-separado). Default: `localhost:4200` y `localhost`.
- **Secretos**: viven en `.env`, que está en `.gitignore` y no se commitea. La plantilla es `.env.example`.
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
