INSERT INTO categorias (id, nombre) VALUES (1, 'Combos');

INSERT INTO categorias (id, nombre) VALUES (3, 'Bebidas');



INSERT INTO categorias (id, nombre) VALUES (2, 'Extras') ON CONFLICT (id) DO NOTHING;


INSERT INTO productos (categoria_id, nombre, descripcion, precio_base, disponible, requiere_nota) VALUES
(1, 'Combo #1', 'Leche agria, gallo pinto y tortilla.', 100.00, true, false),
(1, 'Combo #2', 'Cerdo frito, gallo pinto y tortilla.', 162.00, true, false),
(1, 'Combo #3', 'Dos huevos enteros, gallo pinto y tortilla.', 106.00, true, false),
(1, 'Combo #4', 'Dos huevos rancheros, gallo pinto y tortilla.', 136.00, true, false),
(1, 'Combo #5', 'Dos huevos revueltos, gallo pinto y tortilla.', 106.00, true, false),
(1, 'Combo #6', 'Dos huevos revueltos con jamón, gallo pinto y tortilla.', 136.00, true, false),
(1, 'Combo #7', 'Dos huevos revueltos con chorizo criollo, gallo pinto y tortilla.', 168.00, true, false),
(1, 'Combo #8', 'Chorizo criollo, gallo pinto y tortilla.', 162.00, true, false),
(1, 'Combo #9', 'Nacatamal especial masa de papa, pan y café. (Sábados y Domingos)', 118.00, true, false),
(1, 'Combo #10', 'Chorizo parrillero, gallo pinto y tortilla.', 132.00, true, false),
(1, 'Combo #11', 'Dos huevos enteros, dos tiras de tocino, gallo pinto y tortilla.', 143.00, true, false),
(1, 'Combo #12', 'Dos huevos enteros, chorizo parrillero, gallo pinto y tortilla.', 175.00, true, false),
(1, 'Combo #13', 'Dos huevos enteros montados en dos rodajas de jamón, gallo pinto y tortilla.', 136.00, true, false),
(1, 'Combo #14', 'Dos huevos revueltos con tomate y cebolla, gallo pinto y tortilla.', 132.00, true, false),
(1, 'Combo #15', 'Dos pancake, 2 oz. de miel de maple, 2 porciones de mantequilla.', 120.00, true, false),
(1, 'Combo #16', 'Dos huevos revueltos con tocino, gallo pinto y tortilla.', 143.00, true, false),
(1, 'Combo #17', 'Un huevo entero, gallo pinto y tortilla.', 84.00, true, false),
(1, 'Combo #18', 'Dos huevos revueltos con cebolla, gallo pinto y tortilla.', 112.00, true, false),
(1, 'Combo #19', 'Dos pancake, 2oz. de miel de maple, 2 mantequilla, dos huevos revueltos y 2 tiras de tocino.', 193.00, true, false),
(1, 'Combo #20', 'Dos huevos revueltos, chorizo parrillero, gallo pinto, tortilla y cebollita criolla.', 175.00, true, false);


INSERT INTO productos (categoria_id, nombre, descripcion, precio_base, disponible, requiere_nota) VALUES
(2, 'Leche Agria', 'Vaso de 10 oz.', 44.00, true, false),
(2, 'Tortilla', 'Unidad', 10.00, true, false),
(2, 'Nacatamal (Fines de semana)', 'Nacatamal especial de cerdo/masa de papa', 155.00, true, false),
(2, 'Gallo pinto', 'Porción', 45.00, true, false),
(2, 'Cuajada', 'Porción', 30.00, true, false),
(2, 'Queso sin freír', 'Porción', 30.00, true, false),
(2, 'Queso frito', 'Porción', 35.00, true, false),
(2, 'Jamón', '2 rodajas', 35.00, true, false),
(2, 'Tocino', '2 tiras', 38.00, true, false),
(2, 'Cerdo frito', 'Porción', 82.00, true, false),
(2, 'Chorizo criollo', 'Porción', 82.00, true, false),
(2, 'Chorizo parrillero de cerdo', 'Porción', 60.00, true, false),
(2, '1 huevo entero', 'Porción', 20.00, true, false),
(2, '2 huevos enteros', 'Porción', 40.00, true, false),
(2, '2 huevos rancheros', 'Porción', 65.00, true, false),
(2, 'Salsa ranchera', 'Porción', 30.00, true, false),
(2, '2 huevos revueltos', 'Porción', 40.00, true, false),
(2, '2 huevos revueltos con tocino', 'Porción', 60.00, true, false),
(2, '2 huevos revueltos con cebolla', 'Porción', 45.00, true, false),
(2, '2 huevos revueltos con tomate', 'Porción', 60.00, true, false),
(2, '2 huevos revueltos con chorizo', 'Porción', 82.00, true, false),
(2, '2 huevos revueltos con jamón', 'Porción', 60.00, true, false),
(2, 'Maduro frito', 'Porción', 30.00, true, false),
(2, 'Crema', 'Porción', 25.00, true, false),
(2, 'Cebollita Criolla', 'Porción', 11.00, true, false);


INSERT INTO productos (categoria_id, nombre, descripcion, precio_base, disponible, requiere_nota) VALUES
(3, 'Café negro', 'Vaso 8 oz. Café negro NESCAFÉ', 30.00, true, false),
(3, 'Café con leche', 'Vaso 8oz Café NESCAFÉ con leche', 45.00, true, false),
(3, 'Café negro con Cremora', 'Vaso. Café negro NESCAFÉ con Cremora', 45.00, true, false),
(3, 'Coca Cola', 'Gaseosa 500ml', 45.00, true, false),
(3, 'Fanta Roja', 'Gaseosa 500ml', 45.00, true, false),
(3, 'Fresca', 'Gaseosa 500ml', 45.00, true, false),
(3, 'Té de limón (Botella)', 'LIPTON en botella', 45.00, true, false),
(3, 'Té de limón (Vaso)', 'Vaso 14 oz.', 45.00, true, false),
(3, 'Hi-C', 'Té de limón', 44.00, true, false),
(3, 'Refresco Natural', 'Varían diariamente (Consultar)', 0.00, true, true);



CREATE TABLE mesas (
    id SERIAL PRIMARY KEY,
    numero INT NOT NULL UNIQUE,
    estado VARCHAR(20) DEFAULT


INSERT INTO mesas (numero) VALUES 
(1), (2), (3), (4), (5), (6), (7), (8), (9), (10), (11), (12);













UPDATE categorias SET nombre = 'Combos' WHERE id = 1;
UPDATE categorias SET nombre = 'Extras' WHERE id = 2;
UPDATE categorias SET nombre = 'Bebidas' WHERE id = 3;



UPDATE categorias SET nombre = TRIM(nombre);





CREATE TABLE IF NOT EXISTS detalle_ordenes (
    id SERIAL PRIMARY KEY,
    orden_id INT,
    producto_id INT,
    cantidad INT DEFAULT 1,
    precio_unitario DECIMAL(10,2)
);

CREATE TABLE IF NOT EXISTS ordenes (
    id SERIAL PRIMARY KEY,
    mesa_id INT,
    fecha TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    notas TEXT,
    estado VARCHAR(20) DEFAULT 'PENDIENTE',
    total DECIMAL(10,2) DEFAULT 0.0
);		