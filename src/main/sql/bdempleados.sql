CREATE DATABASE bdempleados;

-- 1. Eliminar la tabla anterior (si existe).
--    CASCADE no se usa para evitar borrar objetos relacionados inadvertidamente.
DROP TABLE IF EXISTS empleado;

-- 2. Crear la tabla y proteger la integridad de los datos en PostgreSQL.
CREATE TABLE empleado (
    id_empleado  INTEGER GENERATED ALWAYS AS IDENTITY,
    codigo       VARCHAR(20)  NOT NULL,
    nombre       VARCHAR(100) NOT NULL,
    correo       VARCHAR(150) NOT NULL,
    cargo        VARCHAR(80)  NOT NULL,
    salario      NUMERIC(12, 2) NOT NULL,
    fecha_registro TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    activo       BOOLEAN NOT NULL DEFAULT TRUE,

    CONSTRAINT pk_empleado PRIMARY KEY (id_empleado),
    CONSTRAINT uq_empleado_codigo UNIQUE (codigo),
    CONSTRAINT uq_empleado_correo UNIQUE (correo),
    CONSTRAINT ck_empleado_salario_positivo CHECK (salario > 0),
    CONSTRAINT ck_empleado_nombre_no_vacio CHECK (BTRIM(nombre) <> ''),
    CONSTRAINT ck_empleado_correo_no_vacio CHECK (BTRIM(correo) <> ''),
    CONSTRAINT ck_empleado_cargo_no_vacio CHECK (BTRIM(cargo) <> '')
);

-- 3. Insertar diez empleados.
--    No incluimos id_empleado, fecha_registro ni activo:
--    PostgreSQL genera el ID y aplica los valores DEFAULT a fecha_registro y activo.
INSERT INTO empleado (codigo, nombre, correo, cargo, salario)
VALUES
    ('EMP001', 'Ana Martínez',    'ana.martinez@example.com',    'Desarrolladora', 1250.00),
    ('EMP002', 'Carlos López',    'carlos.lopez@example.com',    'Analista',       1100.00),
    ('EMP003', 'María González',  'maria.gonzalez@example.com',  'Contadora',      1350.00),
    ('EMP004', 'José Ramírez',    'jose.ramirez@example.com',    'Soporte técnico', 900.00),
    ('EMP005', 'Lucía Herrera',   'lucia.herrera@example.com',   'Diseñadora',     1050.00),
    ('EMP006', 'Diego Castillo',  'diego.castillo@example.com',  'Administrador',  1450.00),
    ('EMP007', 'Sofía Morales',   'sofia.morales@example.com',   'Recursos Humanos', 1150.00),
    ('EMP008', 'Andrés Torres',   'andres.torres@example.com',   'Tester QA',      1000.00),
    ('EMP009', 'Valeria Ruiz',    'valeria.ruiz@example.com',    'Gerente',        2200.00),
    ('EMP010', 'Miguel Flores',   'miguel.flores@example.com',   'Técnico de redes', 980.00);

-- 4. Verificar los diez registros y los valores automáticos.
SELECT
    id_empleado,
    codigo,
    nombre,
    correo,
    cargo,
    salario,
    fecha_registro,
    activo
FROM empleado
ORDER BY id_empleado;

-- 5. Comprobar el total de empleados y el estado de actividad.
SELECT
    COUNT(*) AS total_empleados,
    COUNT(*) FILTER (WHERE activo = TRUE) AS empleados_activos,
    COUNT(*) FILTER (WHERE activo = FALSE) AS empleados_inactivos
FROM empleado;

-- 6. PRUEBAS OPCIONALES DE RESTRICCIONES (NO EJECUTAR en el flujo normal).
--    Descomenta y ejecuta UNA prueba a la vez. Cada INSERT debe fallar.
--
-- Código duplicado: viola uq_empleado_codigo.
-- INSERT INTO empleado (codigo, nombre, correo, cargo, salario)
-- VALUES ('EMP001', 'Empleado Duplicado', 'duplicado1@example.com', 'Prueba', 100.00);
--
-- Correo duplicado: viola uq_empleado_correo.
-- INSERT INTO empleado (codigo, nombre, correo, cargo, salario)
-- VALUES ('EMP011', 'Empleado Duplicado', 'ana.martinez@example.com', 'Prueba', 100.00);
--
-- Salario cero: viola ck_empleado_salario_positivo.
-- INSERT INTO empleado (codigo, nombre, correo, cargo, salario)
-- VALUES ('EMP012', 'Salario Cero', 'salario.cero@example.com', 'Prueba', 0.00);
--
-- Nombre vacío: viola ck_empleado_nombre_no_vacio.
-- INSERT INTO empleado (codigo, nombre, correo, cargo, salario)
-- VALUES ('EMP013', '   ', 'nombre.vacio@example.com', 'Prueba', 100.00);
--
-- Campo obligatorio NULL: viola NOT NULL.
-- INSERT INTO empleado (codigo, nombre, correo, cargo, salario)
-- VALUES ('EMP014', NULL, 'nombre.null@example.com', 'Prueba', 100.00);
