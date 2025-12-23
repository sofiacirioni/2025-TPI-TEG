INSERT INTO continentes (id_continente, nombre) VALUES
                                                    (1, 'America del Norte'),
                                                    (2, 'America del Sur'),
                                                    (3, 'Europa'),
                                                    (4, 'Africa'),
                                                    (5, 'Asia'),
                                                    (6, 'Oceania');

/*2. Inserciones en paises*/

/*América del Norte
*/
INSERT INTO paises (id_pais, nombre, id_continente) VALUES
                                                        (1, 'Alaska', 1),
                                                        (2, 'Yukon', 1),
                                                        (3, 'Oregon', 1),
                                                        (4, 'California', 1),
                                                        (5, 'Mexico', 1),
                                                        (6, 'Nueva York', 1),
                                                        (7, 'Terranova', 1),
                                                        (8, 'Labrador', 1),
                                                        (9, 'Groenlandia', 1),
                                                        (10, 'Canada', 1);

/*América del Sur
*/
INSERT INTO paises (id_pais, nombre, id_continente) VALUES
                                                        (11, 'Colombia', 2),
                                                        (12, 'Peru', 2),
                                                        (13, 'Brasil', 2),
                                                        (14, 'Argentina', 2),
                                                        (15, 'Chile', 2),
                                                        (16, 'Uruguay', 2);

/*Europa*/

INSERT INTO paises (id_pais, nombre, id_continente) VALUES
                                                        (17, 'Islandia', 3),
                                                        (18, 'Gran Bretaña', 3),
                                                        (19, 'Francia', 3),
                                                        (20, 'España', 3),
                                                        (21, 'Alemania', 3),
                                                        (22, 'Polonia', 3),
                                                        (23, 'Rusia', 3),
                                                        (24, 'Suecia', 3),
                                                        (25, 'Italia', 3);

/*África*/

INSERT INTO paises (id_pais, nombre, id_continente) VALUES
                                                        (26, 'Sahara', 4),
                                                        (27, 'Egipto', 4),
                                                        (28, 'Etiopia', 4),
                                                        (29, 'Zaire', 4),
                                                        (30, 'Sudafrica', 4),
                                                        (31, 'Madagascar', 4);

/*Asia*/

INSERT INTO paises (id_pais, nombre, id_continente) VALUES
                                                        (32, 'Tartaria', 5),
                                                        (33, 'Taymir', 5),
                                                        (34, 'Kamchatka', 5),
                                                        (35, 'Aral', 5),
                                                        (36, 'Siberia', 5),
                                                        (37, 'Mongolia', 5),
                                                        (38, 'Gobi', 5),
                                                        (39, 'China', 5),
                                                        (40, 'Turquia', 5),
                                                        (41, 'Israel', 5),
                                                        (42, 'Arabia', 5),
                                                        (43, 'India', 5),
                                                        (44, 'Malasia', 5),
                                                        (45, 'Japon', 5),
                                                        (46, 'Iran', 5);

/*Oceanía*/

INSERT INTO paises (id_pais, nombre, id_continente) VALUES
                                                        (47, 'Sumatra', 6),
                                                        (48, 'Borneo', 6),
                                                        (49, 'Java', 6),
                                                        (50, 'Australia', 6);


/*⸻

3. Inserciones en limites
*/

INSERT INTO limites (id_pais1, id_pais2) VALUES
-- América del Norte
(1, 2), (1, 3),
(2, 3), (2, 10),
(3, 10), (3, 4), (3, 6),
(4, 5), (4, 6),
(5, 11), -- intercontinental
(6, 7), (6, 9), (6, 10),
(7, 8), (7, 10),
(8, 9),

-- América del Sur
(11, 12), (11, 13),
(12, 13), (12, 15), (12, 14),
(13, 14), (13, 16),
(14, 15), (14, 16),

-- Europa
(17, 18), (17, 24),
(18, 20), (18, 21),
(19, 20), (19, 21), (19, 25),
(21, 22), (21, 25),
(22, 23), (23, 24),

-- África
(26, 27), (26, 28), (26, 29),
(27, 28), (27, 31),
(28, 29), (28, 30),
(29, 30), (29, 31),

-- Asia
(32, 33), (32, 35), (32, 36),
(33, 36),
(34, 36), (34, 39), (34, 45),
(35, 36), (35, 46),
(36, 37), (36, 39),
(37, 38), (37, 39), (37, 46),
(38, 39), (38, 46),
(39, 43), (39, 44), (39, 45), (39, 46),
(40, 41), (40, 42), (40, 46),
(41, 42),
(42, 43),
(43, 44),
(44, 46),

-- Oceanía
(47, 50), (48, 50), (49, 50),

-- Conexiones Intercontinentales
(1, 34),   -- Alaska - Kamchatka
(5, 11),   -- México - Colombia
(9, 17),   -- Groenlandia - Islandia
(13, 26),  -- Brasil - Sahara
(15, 50),  -- Chile - Australia
(20, 26),  -- España - Sahara
(22, 27),  -- Polonia - Egipto
(22, 40),  -- Polonia - Turquía
(23, 35),  -- Rusia - Aral
(23, 40),  -- Rusia - Turquía
(23, 46),  -- Rusia - Irán
(27, 40),  -- Egipto - Turquía
(27, 41),  -- Egipto - Israel
(43, 47),  -- India - Sumatra
(44, 48);  -- Malasia - Borneo


INSERT INTO objetivos (
    descripcion, id_tipo_objetivo, cantidad_paises_objetivo, limitrofe,
    africa, asia, europa, america_norte, america_sur, oceania, color_enemigo
) VALUES
    -- Ocupación total y parcial de continentes
    ('Ocupar África (6 países), 5 países de América del Norte y 4 países de Europa', 'SECRETO', 15, 0, 6, 0, 4, 5, 0, 0, NULL),
    ('Ocupar América del Sur (6 países), 7 países de Europa y 3 países limítrofes entre sí en cualquier lugar del mapa', 'SECRETO', 16, 3, 0, 0, 7, 0, 6, 0, NULL),
    ('Ocupar Asia (15 países) y 2 países de América del Sur', 'SECRETO', 17, 0, 0, 15, 0, 0, 2, 0, NULL),
    ('Ocupar Europa (9 países), 4 países de Asia y 2 países de América del Sur', 'SECRETO', 15, 0, 0, 4, 9, 0, 2, 0, NULL),
    ('Ocupar América del Norte (10 países), 2 países de Oceanía y 4 de Asia', 'SECRETO', 16, 0, 0, 4, 0, 10, 0, 2, NULL),
    ('Ocupar 2 países de Oceanía, 2 países de África, 2 países de América del Sur, 3 países de Europa, 4 de América del Norte y 3 de Asia', 'SECRETO', 16, 0, 2, 3, 3, 4, 2, 2, NULL),
    ('Ocupar Oceanía (4 países), América del Norte (10 países) y 2 países de Europa', 'SECRETO', 16, 0, 0, 0, 2, 10, 0, 4, NULL),
    ('Ocupar América del Sur (6 países), África (6 países) y 5 países de América del Norte', 'SECRETO', 17, 0, 6, 0, 0, 5, 6, 0, NULL),

    -- Eliminación por color
    ('Destruir al ejército azul', 'SECRETO', 0, 0, 0, 0, 0, 0, 0, 0, 'AZUL'),
    ('Destruir al ejército rojo', 'SECRETO', 0, 0, 0, 0, 0, 0, 0, 0, 'ROJO'),
    ('Destruir al ejército negro', 'SECRETO', 0, 0, 0, 0, 0, 0, 0, 0, 'NARANJA'),
    ('Destruir al ejército amarillo', 'SECRETO', 0, 0, 0, 0, 0, 0, 0, 0, 'AMARILLO'),
    ('Destruir al ejército verde', 'SECRETO', 0, 0, 0, 0, 0, 0, 0, 0, 'VERDE'),
    ('Destruir al ejército magenta', 'SECRETO', 0, 0, 0, 0, 0, 0, 0, 0, 'VIOLETA'),

    -- Objetivo general
    ('Ocupar 30 países', 'GENERAL', 30, 0, 0, 0, 0, 0, 0, 0, NULL);

INSERT INTO TARJETAS (ID_TARJETA, ID_PAIS, SIMBOLO) VALUES
                                                        -- América del Norte
                                                        (1, 1, 'CANION'),
                                                        (2, 2, 'GALEON'),
                                                        (3, 3, 'GLOBO'),
                                                        (4, 4, 'GLOBO'),
                                                        (5, 5, 'GLOBO'),
                                                        (6, 6, 'CANION'),
                                                        (7, 7, 'GLOBO'),
                                                        (8, 8, 'GLOBO'),
                                                        (9, 9, 'GALEON'),
                                                        (10, 10, 'GLOBO'),

                                                        -- América del Sur
                                                        (11, 11, 'GALEON'),
                                                        (12, 12, 'CANION'),
                                                        (13, 13, 'CANION'),
                                                        (14, 14, 'COMODIN'),
                                                        (15, 15, 'GALEON'),
                                                        (16, 16, 'GALEON'),

                                                        -- Europa
                                                        (17, 17, 'CANION'),
                                                        (18, 18, 'CANION'),
                                                        (19, 19, 'GALEON'),
                                                        (20, 20, 'GALEON'),
                                                        (21, 21, 'CANION'),
                                                        (22, 22, 'GLOBO'),
                                                        (23, 23, 'GALEON'),
                                                        (24, 24, 'CANION'),
                                                        (25, 25, 'GALEON'),

                                                        -- África
                                                        (26, 26, 'GLOBO'),
                                                        (27, 27, 'GALEON'),
                                                        (28, 28, 'GALEON'),
                                                        (29, 29, 'CANION'),
                                                        (30, 30, 'GLOBO'),
                                                        (31, 31, 'CANION'),

                                                        -- Asia
                                                        (32, 32, 'GLOBO'),
                                                        (33, 33, 'GLOBO'),
                                                        (34, 34, 'GALEON'),
                                                        (35, 35, 'GLOBO'),
                                                        (36, 36, 'CANION'),
                                                        (37, 37, 'CANION'),
                                                        (38, 38, 'GALEON'),
                                                        (39, 39, 'CANION'),
                                                        (40, 40, 'CANION'),
                                                        (41, 41, 'CANION'),
                                                        (42, 42, 'GLOBO'),
                                                        (43, 43, 'GALEON'),
                                                        (44, 44, 'GLOBO'),
                                                        (45, 45, 'GLOBO'),
                                                        (46, 46, 'GALEON'),

                                                        -- Oceanía
                                                        (47, 47, 'GALEON'),
                                                        (48, 48, 'CANION'),
                                                        (49, 49, 'GLOBO'),
                                                        (50, 50, 'GLOBO');



