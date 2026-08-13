# TEG — Táctica y Estrategia de Guerra

Juego de estrategia por turnos implementado como aplicación web fullstack. Monorepo con Angular 21 (frontend) + Spring Boot 3.3.5 (backend).

---

## Stack

| Capa | Tecnología |
|---|---|
| Frontend | Angular 21 (standalone), TypeScript 5.9, SCSS, GSAP |
| Backend | Java 17, Spring Boot 3.3.5, Maven |
| Base de datos | PostgreSQL 15 |
| Migraciones | Flyway (`Backend/src/main/resources/db/migration`) |
| Tiempo real | WebSocket (STOMP + SockJS) |
| Tests BE | JUnit + Mockito, 295 tests (H2 en memoria) |
| Tests E2E | Playwright (`e2e/`) |
| API docs | Swagger `/swagger-ui.html` |

> El frontend **no tiene tests unitarios**. La cobertura automatizada está en el
> backend (JUnit) y en los recorridos end-to-end (Playwright). De Bootstrap se
> importan sólo los parciales de grid y utilidades; el resto de la interfaz es
> un sistema de diseño propio (ver [Design system](#design-system)).

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
├── V1__init_schema.sql                    # tablas y claves foráneas
├── V2__reference_data.sql                 # tablero del juego
├── V3__seed_users.sql                     # usuarios de prueba
├── V4__contadores_de_combate.sql          # parte de combate por jugador
├── V5__auditoria_alta_y_actualizacion.sql # fecha_alta / fecha_actualizacion
└── V6__lo_que_sigue.sql                   # ← nuevos cambios acá
```

Las fechas de alta y actualización las escribe Spring Data JPA Auditing
(`@EnableJpaAuditing` en `Application`) sobre las entidades que heredan de
`AuditableEntity`: usuarios, salas, partidas y mensajes.

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
│   │   ├── Controller/         # 13 controladores REST
│   │   ├── Services/           # Interfaces de servicio
│   │   ├── ServicesImpl/       # Implementaciones
│   │   ├── Repositories/       # JPA repositories (14)
│   │   ├── Entities/           # Entidades JPA (18)
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
# Tests backend (295 tests)
cd Backend && ./mvnw test

# Tests end-to-end — necesitan la app levantada; Playwright la arranca solo
npx playwright test

# Recorrido de demostración, con navegador visible y grabación en video
npx playwright test e2e/demo-pantallas.spec.ts --project=chromium

# Build producción
cd Frontend && npm run build

# Limpiar caché Angular (si hay errores de webpack raros)
cd Frontend && rm -rf .angular/cache && npm start
```

---

## Tests end-to-end (`e2e/`)

Playwright levanta frontend y backend por su cuenta (`webServer` en
`playwright.config.ts`) y corre con navegador visible.

| Archivo | Qué hace |
|---|---|
| `tablero-dev.setup.ts` | Login → crea sala → agrega bots → inicia partida. Guarda la sesión en `e2e/.auth/tablero-state.json` y la URL del tablero en `tablero-url.txt`. Corre solo, antes que el resto. |
| `tablero-dev.spec.ts` | 20+ chequeos visuales y funcionales del tablero: mapa SVG, paneles, viñeta, modal de país, reloj, sobre del objetivo. |
| `flujo-completo.spec.ts` | Partida de punta a punta: colocar ejércitos → atacar con dados → reagrupar → fin de turno → los bots juegan → avanza el número de turno. |
| `demo-pantallas.spec.ts` | Recorrido de demostración por todas las pantallas, pensado para grabar el video. |

```bash
npx playwright test                        # setup + todo
npx playwright test --project=chromium     # sin rehacer el setup
```

Si la sesión guardada expira, borrar `e2e/.auth/tablero-state.json` y volver a
correr.

---

## Notas importantes

- **Base de datos**: PostgreSQL en Docker, persistida en el volumen `teg-postgres-data`. Para reset limpio: `docker compose down -v`.
- **Esquema**: lo gobierna Flyway, no Hibernate. Cambiar una entidad sin agregar la migración correspondiente hace fallar el arranque (`ddl-auto=validate`) — eso es intencional.
- **CORS**: sale de `FRONTEND_URL` en el `.env` (coma-separado). Default: `localhost:4200` y `localhost`.
- **Secretos**: viven en `.env`, que está en `.gitignore` y no se commitea. La plantilla es `.env.example`.
- **Bots**: `BotService` juega automáticamente los turnos de los jugadores bot. Hay **una sola dificultad**, deliberadamente básica: reparte refuerzos al azar, ataca al vecino con menos tropas conservando 3 tropas de guarnición, no reagrupa y no juega su objetivo secreto. El ritmo se controla con dos constantes: `cooldownMs` (1,5 s entre fases) y `pausaEntreAccionesMs` (2 s entre cada colocación y cada ataque, para que se puedan seguir de a una). Un turno de bot dura entre 9 y 30 s según cuántas acciones haga. Revisar antes de modificar el flujo de turnos.
- **Jugadores bot y usuario**: las filas BOT de `jugadores` se graban con el `id_usuario` de quien creó la partida. Toda consulta que parta del usuario tiene que filtrar por `tipoJugador = HUMANO` (ver `JugadorRepository.findParticipacionesHumanas`).
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
/perfilUsuario    → legajo del comandante: libreta de enrolamiento + hoja de servicios
/estadisticas/:id → parte de campaña de una partida (pizarra del cuarto de guerra)
/creditos         → equipo de desarrollo
/ayuda            → reglas del juego
```

---

## Design system

La interfaz simula un cuarto de operaciones militares de 1939–1945: el jugador
mira una mesa de planificación con documentos de época encima. Cada pantalla es
un objeto físico, no un panel:

| Pantalla | Objeto |
|---|---|
| Login / registro | Pase de acceso sellado |
| Créditos | Carpetas de expediente clasificado |
| Reglamento | Carpeta de archivo con pestañas |
| Perfil | Libreta de enrolamiento + hoja de servicios |
| Fin de partida | Diario de guerra con las medallas encima |
| Estadísticas | Pizarra de tiza del cuarto de guerra |

Detalles de implementación:

- **Tipografías**: Special Elite (títulos) + Roboto Slab (cuerpo), servidas
  desde `assets/fonts/` — nunca desde Google Fonts
- **Paleta**: tonos marrones, rojos oscuros y crema sobre fondo mesa de madera
- **Superficies**: mixin `paper-grunge` con seis variantes, para que dos hojas
  contiguas no repitan las mismas manchas
- **Cursores**: SVGs custom (hand-default / hand-pointer) aplicados globalmente
- **Viñeta**: overlay radial fijo (`z-index: 9999`) que oscurece los bordes
- **Botón volver**: clase global `.btn-volver`, fija arriba a la izquierda en
  todas las pantallas
- **Bootstrap**: sólo se importan los parciales de grid, utilidades, modal y
  dropdown; no se usa el tema ni los componentes JS

Las reglas completas están en [CLAUDE.md](./CLAUDE.md).

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
