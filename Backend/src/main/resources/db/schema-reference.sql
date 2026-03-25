-- ============================================================
-- Schema de referencia — TEG Online
-- Generado como documentación manual del esquema JPA
-- ============================================================
--
-- Las tablas son creadas automáticamente por Hibernate.
-- En el PRIMER despliegue en producción:
--   1. Usar spring.jpa.hibernate.ddl-auto=update para que
--      Hibernate cree las tablas en PostgreSQL.
--   2. Verificar que todo funcione correctamente.
--   3. Cambiar a ddl-auto=validate para producción estable.
--
-- Si se necesita crear el schema manualmente, las tablas
-- siguientes corresponden a las entidades JPA del proyecto.
-- ============================================================

-- ── usuarios ────────────────────────────────────────────────
-- Entidad: UsuarioEntity
-- Tabla:   usuarios
CREATE TABLE IF NOT EXISTS usuarios (
    id_usuario  BIGSERIAL PRIMARY KEY,
    usuario     VARCHAR(50)  NOT NULL,
    correo      VARCHAR(100),
    contrasenia VARCHAR(100) NOT NULL,
    imagen      VARCHAR(200) NOT NULL
);

-- ── salas ───────────────────────────────────────────────────
-- Entidad: SalaEntity
-- Tabla:   salas
CREATE TABLE IF NOT EXISTS salas (
    id_sala      BIGSERIAL PRIMARY KEY,
    nombre_sala  VARCHAR(255) NOT NULL,
    id_usuario   BIGINT REFERENCES usuarios(id_usuario),
    estado       VARCHAR(50),
    url          VARCHAR(255)
);

-- ── jugadores ───────────────────────────────────────────────
-- Entidad: JugadorEntity
-- Tabla:   jugadores
-- Nota: JugadorRepository tiene una native query compatible
--       con PostgreSQL: SELECT * FROM jugadores WHERE id_sala = :idSala
CREATE TABLE IF NOT EXISTS jugadores (
    id_jugador         BIGSERIAL PRIMARY KEY,
    nombre             VARCHAR(255) NOT NULL,
    consquisto         BOOLEAN DEFAULT FALSE,
    id_usuario         BIGINT REFERENCES usuarios(id_usuario),
    tipo_jugador       VARCHAR(50),
    perdio             BOOLEAN,
    id_turno           BIGINT,
    color              VARCHAR(50),
    id_sala            BIGINT NOT NULL REFERENCES salas(id_sala),
    id_partida         BIGINT,
    id_objetivo        BIGINT,
    acepto_pausa       BOOLEAN,
    acepto_renudar     BOOLEAN,
    finalizar_partida  BOOLEAN,
    estado_jugador     VARCHAR(50),
    ejercito           INTEGER DEFAULT 0
);

-- ── partidas ────────────────────────────────────────────────
-- Entidad: PartidaEntity
-- Tabla:   partidas
CREATE TABLE IF NOT EXISTS partidas (
    id_partida      BIGSERIAL PRIMARY KEY,
    fecha_inicio    DATE,
    id_sala         BIGINT REFERENCES salas(id_sala),
    turno_actual    INTEGER,
    estado_partida  VARCHAR(50),
    fase_actual     VARCHAR(50),
    hostilidad      BOOLEAN DEFAULT FALSE,
    ganador_id      BIGINT REFERENCES jugadores(id_jugador)
);

-- ── turnos ──────────────────────────────────────────────────
-- Entidad: TurnoEntity
-- Tabla:   turnos
CREATE TABLE IF NOT EXISTS turnos (
    id_turno    BIGSERIAL PRIMARY KEY,
    nro_turno   INTEGER,
    id_jugador  BIGINT REFERENCES jugadores(id_jugador),
    id_partida  BIGINT REFERENCES partidas(id_partida),
    fase        VARCHAR(50),
    inicio      TIMESTAMP
);

-- ── continentes ─────────────────────────────────────────────
-- Entidad: ContinenteEntity
CREATE TABLE IF NOT EXISTS continentes (
    id_continente  BIGSERIAL PRIMARY KEY,
    nombre         VARCHAR(255)
);

-- ── paises ──────────────────────────────────────────────────
-- Entidad: PaisEntity
-- Tabla:   paises
CREATE TABLE IF NOT EXISTS paises (
    id_pais        BIGSERIAL PRIMARY KEY,
    nombre         VARCHAR(255),
    id_continente  BIGINT NOT NULL REFERENCES continentes(id_continente)
);

-- ── limites ─────────────────────────────────────────────────
-- Entidad: LimiteEntity
CREATE TABLE IF NOT EXISTS limites (
    id_limite   BIGSERIAL PRIMARY KEY,
    id_pais_a   BIGINT REFERENCES paises(id_pais),
    id_pais_b   BIGINT REFERENCES paises(id_pais)
);

-- ── simbolos ────────────────────────────────────────────────
-- Entidad: SimboloEntity
CREATE TABLE IF NOT EXISTS simbolos (
    id_simbolo  BIGSERIAL PRIMARY KEY,
    tipo        VARCHAR(50)
);

-- ── tarjetas ────────────────────────────────────────────────
-- Entidad: TarjetaEntity
CREATE TABLE IF NOT EXISTS tarjetas (
    id_tarjeta  BIGSERIAL PRIMARY KEY,
    id_pais     BIGINT REFERENCES paises(id_pais),
    id_simbolo  BIGINT REFERENCES simbolos(id_simbolo)
);

-- ── estado_tarjeta ──────────────────────────────────────────
-- Entidad: EstadoTarjetaEntity
CREATE TABLE IF NOT EXISTS estado_tarjeta (
    id_estado_tarjeta  BIGSERIAL PRIMARY KEY,
    id_tarjeta         BIGINT REFERENCES tarjetas(id_tarjeta),
    id_partida         BIGINT REFERENCES partidas(id_partida)
);

-- ── estadoPaises (→ estadopaises en PostgreSQL) ─────────────
-- Entidad: EstadoPaisEntity
-- Tabla:   estadoPaises (@Table name tiene camelCase —
--          PostgreSQL convierte a minúsculas: estadopaises)
CREATE TABLE IF NOT EXISTS estadopaises (
    id_estado_pais  BIGSERIAL PRIMARY KEY,
    id_pais         BIGINT NOT NULL REFERENCES paises(id_pais),
    id_jugador      BIGINT REFERENCES jugadores(id_jugador),
    id_estadistica  BIGINT,
    cantidad_tropas INTEGER,
    id_partida      BIGINT REFERENCES partidas(id_partida)
);

-- ── estadisticas ────────────────────────────────────────────
-- Entidad: EstadisticaEntity
CREATE TABLE IF NOT EXISTS estadisticas (
    id_estadistica  BIGSERIAL PRIMARY KEY
    -- Verificar campos en EstadisticaEntity
);

-- ── objetivos ───────────────────────────────────────────────
-- Entidad: ObjetivoEntity
-- Tabla:   objetivos
CREATE TABLE IF NOT EXISTS objetivos (
    id                       BIGSERIAL PRIMARY KEY,
    descripcion              VARCHAR(255),
    id_tipo_objetivo         VARCHAR(50),
    cantidad_paises_objetivo INTEGER,
    limitrofe                INTEGER,
    africa                   INTEGER,
    asia                     INTEGER,
    europa                   INTEGER,
    america_norte            INTEGER,
    america_sur              INTEGER,
    oceania                  INTEGER,
    color_enemigo            VARCHAR(50)
);

-- ── tipo_mensaje ────────────────────────────────────────────
-- Entidad: TipoMensajeEntity
CREATE TABLE IF NOT EXISTS tipo_mensaje (
    id_tipo_mensaje  BIGSERIAL PRIMARY KEY,
    tipo             VARCHAR(50)
);

-- ── mensajes ────────────────────────────────────────────────
-- Entidad: MensajeEntity
CREATE TABLE IF NOT EXISTS mensajes (
    id_mensaje       BIGSERIAL PRIMARY KEY,
    id_partida       BIGINT REFERENCES partidas(id_partida),
    id_jugador       BIGINT REFERENCES jugadores(id_jugador),
    id_tipo_mensaje  BIGINT REFERENCES tipo_mensaje(id_tipo_mensaje),
    contenido        VARCHAR(255)
);

-- ── jugadores_tarjetas (tabla join ManyToMany) ──────────────
-- Generada por Hibernate para JugadorEntity.tarjetas
CREATE TABLE IF NOT EXISTS jugadores_tarjetas (
    jugadores_id_jugador              BIGINT REFERENCES jugadores(id_jugador),
    tarjetas_id_estado_tarjeta        BIGINT REFERENCES estado_tarjeta(id_estado_tarjeta),
    PRIMARY KEY (jugadores_id_jugador, tarjetas_id_estado_tarjeta)
);
