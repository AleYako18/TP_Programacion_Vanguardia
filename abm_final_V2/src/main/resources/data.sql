INSERT INTO usuario (id, username, password, nombre, apellido, fecha_nacimiento, rol) VALUES
(1, 'user@example.com', 'password', 'Usuario', 'Prueba', '1990-01-01', 'ESTANDAR'),
(2, 'admin@example.com', 'adminpass', 'Admin', 'Principal', '1985-05-10', 'ADMINISTRADOR');

INSERT INTO articulos (id, nombre, disponible) VALUES
(1, 'Proyector Epson EB-X05', TRUE),
(2, 'Laptop HP EliteBook', FALSE),
(3, 'Cámara Sony Alpha a6400', TRUE);

INSERT INTO salas (id, nombre, capacidad) VALUES
(1, 'Sala de Reuniones 1A', 8),
(2, 'Sala de Conferencias B2', 20);