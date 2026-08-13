-- ============================================================
-- V3 — Usuarios de prueba
--
-- Permiten entrar al juego apenas levanta el stack, sin tener que
-- registrarse a mano. Los tres comparten la contraseña  Test123@
-- (hash BCrypt, el mismo algoritmo que usa AuthController al
-- registrar, así que el login normal los valida sin cambios).
--
-- Son credenciales de desarrollo conocidas y públicas: NO dejar
-- esta migración activa en un despliegue real de cara a internet.
-- ============================================================

INSERT INTO usuarios (usuario, correo, contrasenia, imagen) VALUES
    ('testuser',  'test@test.com',  '$2a$10$J/EXvNzFW/UUJy9Ibpk/j.raDKoPKTnobqKgoKzokWatU1HwzUuBK', 'assets/images/avatars/SofiCirioni.png'),
    ('comandante','comandante@test.com', '$2a$10$J/EXvNzFW/UUJy9Ibpk/j.raDKoPKTnobqKgoKzokWatU1HwzUuBK', 'assets/images/avatars/Maxi.png'),
    ('aliado',    'aliado@test.com', '$2a$10$J/EXvNzFW/UUJy9Ibpk/j.raDKoPKTnobqKgoKzokWatU1HwzUuBK', 'assets/images/avatars/AgosCh.png');
