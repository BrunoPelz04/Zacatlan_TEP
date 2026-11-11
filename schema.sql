-- =====================================================
-- Script de creación de base de datos (MySQL 8+)
-- =====================================================

CREATE DATABASE IF NOT EXISTS zacatlan_ventas
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE zacatlan_ventas;

-- -----------------------------------------------------
-- Tabla: tipo_user
-- -----------------------------------------------------
CREATE TABLE tipo_user (
  tipo_user_id INT NOT NULL AUTO_INCREMENT,
  nombre       VARCHAR(30) NOT NULL,
  PRIMARY KEY (tipo_user_id),
  UNIQUE KEY uq_tipo_user_nombre (nombre)
) ENGINE=InnoDB;

-- -----------------------------------------------------
-- Tabla: bloque
-- -----------------------------------------------------
CREATE TABLE bloque (
  bloque_id      INT NOT NULL AUTO_INCREMENT,
  nombre         VARCHAR(50) NOT NULL,
  fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  descripcion    VARCHAR(150) NULL,
  PRIMARY KEY (bloque_id),
  UNIQUE KEY uq_bloque_nombre (nombre)
) ENGINE=InnoDB;

-- -----------------------------------------------------
-- Tabla: usuario
-- bloque_id es NULL para admins generales y super admins
-- -----------------------------------------------------
CREATE TABLE usuario (
  usuario_id     INT NOT NULL AUTO_INCREMENT,
  bloque_id      INT NULL,
  tipo_user_id   INT NOT NULL,
  nombres        VARCHAR(50) NOT NULL,
  apelli_ma      VARCHAR(50) NULL,
  apelli_pa      VARCHAR(50) NOT NULL,
  activo         BOOLEAN NOT NULL DEFAULT TRUE,
  fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  fecha_fin      DATETIME NULL,
  telefono       VARCHAR(10) NULL,
  correo         VARCHAR(50) NOT NULL,
  password_hash  VARCHAR(255) NOT NULL,
  PRIMARY KEY (usuario_id),
  UNIQUE KEY uq_usuario_correo (correo),
  KEY idx_usuario_bloque (bloque_id),
  KEY idx_usuario_tipo (tipo_user_id),
  CONSTRAINT fk_usuario_bloque
    FOREIGN KEY (bloque_id) REFERENCES bloque (bloque_id)
    ON UPDATE CASCADE ON DELETE RESTRICT,
  CONSTRAINT fk_usuario_tipo
    FOREIGN KEY (tipo_user_id) REFERENCES tipo_user (tipo_user_id)
    ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE=InnoDB;

-- -----------------------------------------------------
-- Tabla: producto
-- cantidad = presentación base (ej. 1 kg, 1 pza)
-- precio_estimado = precio sugerido, el vendedor puede cambiarlo en la venta
-- -----------------------------------------------------
CREATE TABLE producto (
  producto_id     INT NOT NULL AUTO_INCREMENT,
  nombre          VARCHAR(50) NOT NULL,
  precio_estimado DECIMAL(10,2) NOT NULL,
  cantidad        DECIMAL(10,3) NOT NULL DEFAULT 1,
  unidad_medida   VARCHAR(20) NOT NULL,
  PRIMARY KEY (producto_id),
  CONSTRAINT chk_producto_precio CHECK (precio_estimado >= 0),
  CONSTRAINT chk_producto_cantidad CHECK (cantidad > 0)
) ENGINE=InnoDB;

-- -----------------------------------------------------
-- Tabla: jornada (turno de trabajo de un vendedor)
-- fecha_fin NULL = jornada abierta
-- El vendedor de cada venta se obtiene a través de la jornada
-- -----------------------------------------------------
CREATE TABLE jornada (
  jornada_id   INT NOT NULL AUTO_INCREMENT,
  usuario_id   INT NOT NULL,
  fecha_inicio DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  fecha_fin    DATETIME NULL,
  PRIMARY KEY (jornada_id),
  KEY idx_jornada_usuario_inicio (usuario_id, fecha_inicio),
  CONSTRAINT fk_jornada_usuario
    FOREIGN KEY (usuario_id) REFERENCES usuario (usuario_id)
    ON UPDATE CASCADE ON DELETE RESTRICT,
  CONSTRAINT chk_jornada_fechas CHECK (fecha_fin IS NULL OR fecha_fin >= fecha_inicio)
) ENGINE=InnoDB;

-- -----------------------------------------------------
-- Tabla: ventas
-- precio_final = TOTAL cobrado en la venta
-- cantidad = cantidad vendida (admite decimales, ej. 1.5 kg)
-- -----------------------------------------------------
CREATE TABLE ventas (
  venta_id     INT NOT NULL AUTO_INCREMENT,
  producto_id  INT NOT NULL,
  jornada_id   INT NOT NULL,
  fecha        DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  precio_final DECIMAL(10,2) NOT NULL,
  cantidad     DECIMAL(10,3) NOT NULL,
  PRIMARY KEY (venta_id),
  KEY idx_ventas_fecha (fecha),
  KEY idx_ventas_producto (producto_id),
  KEY idx_ventas_jornada (jornada_id),
  CONSTRAINT fk_ventas_producto
    FOREIGN KEY (producto_id) REFERENCES producto (producto_id)
    ON UPDATE CASCADE ON DELETE RESTRICT,
  CONSTRAINT fk_ventas_jornada
    FOREIGN KEY (jornada_id) REFERENCES jornada (jornada_id)
    ON UPDATE CASCADE ON DELETE RESTRICT,
  CONSTRAINT chk_ventas_precio CHECK (precio_final >= 0),
  CONSTRAINT chk_ventas_cantidad CHECK (cantidad > 0)
) ENGINE=InnoDB;

-- -----------------------------------------------------
-- Tabla: historial (acciones de los administradores)
-- -----------------------------------------------------
CREATE TABLE historial (
  historial_id INT NOT NULL AUTO_INCREMENT,
  usuario_id   INT NOT NULL,
  descripcion  VARCHAR(255) NULL,
  fecha        DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  accion       VARCHAR(20) NOT NULL,
  PRIMARY KEY (historial_id),
  KEY idx_historial_usuario_fecha (usuario_id, fecha),
  CONSTRAINT fk_historial_usuario
    FOREIGN KEY (usuario_id) REFERENCES usuario (usuario_id)
    ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE=InnoDB;

-- -----------------------------------------------------
-- Datos iniciales
-- -----------------------------------------------------
INSERT INTO tipo_user (nombre) VALUES
  ('Vendedor'),
  ('Admin'),
  ('Super admin');

-- -----------------------------------------------------
-- Ejemplo de reporte: ventas de un vendedor en un día
-- -----------------------------------------------------
-- SELECT SUM(v.precio_final) AS total_dia
-- FROM ventas v
-- JOIN jornada j ON j.jornada_id = v.jornada_id
-- WHERE j.usuario_id = 5
--   AND v.fecha >= '2026-11-10'
--   AND v.fecha <  '2026-11-11';
