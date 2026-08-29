--Esquema de la BD para poder crear las tablas en Postgres o DBeaver

CREATE TABLE IF NOT EXISTS clientes (
    id     INTEGER PRIMARY KEY,
    nombre VARCHAR(150) NOT NULL,
    email  VARCHAR(150) NOT NULL
);

CREATE TABLE IF NOT EXISTS pedidos (
    id         INTEGER PRIMARY KEY,
    cliente_id INTEGER NOT NULL REFERENCES clientes(id),
    fecha      TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS pedido_servicios (
    id          SERIAL PRIMARY KEY,
    pedido_id   INTEGER NOT NULL REFERENCES pedidos(id) ON DELETE CASCADE,
    tipo        VARCHAR(50) NOT NULL,
    descripcion VARCHAR(100) NOT NULL,
    precio      NUMERIC(10, 2) NOT NULL
);
