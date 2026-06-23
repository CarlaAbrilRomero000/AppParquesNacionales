-- ============================================================
--  Sistema de Parques Nacionales - Esquema, tabla y SP
--  Este script lo ejecuta DatabaseInitializer al arrancar.
--  Los lotes se separan con la palabra GO (en su propia línea).
-- ============================================================

-- Esquema "parques"
IF NOT EXISTS (SELECT 1 FROM sys.schemas WHERE name = 'parques')
    EXEC('CREATE SCHEMA parques');
GO

-- Tabla TipoParque
IF NOT EXISTS (
    SELECT 1 FROM sys.tables t
    JOIN sys.schemas s ON t.schema_id = s.schema_id
    WHERE s.name = 'parques' AND t.name = 'TipoParque')
BEGIN
    CREATE TABLE parques.TipoParque (
        id_tipo_parque INT IDENTITY(1,1) NOT NULL,
        descripcion    VARCHAR(100)      NOT NULL,
        CONSTRAINT PK_TipoParque PRIMARY KEY (id_tipo_parque)
    );
END
GO

-- Datos de ejemplo (solo si la tabla está vacía)
IF NOT EXISTS (SELECT 1 FROM parques.TipoParque)
BEGIN
    INSERT INTO parques.TipoParque (descripcion) VALUES
        ('Parque Nacional'),
        ('Reserva Natural'),
        ('Monumento Natural'),
        ('Parque Provincial');
END
GO

-- ------------------------------------------------------------
--  ALTA
-- ------------------------------------------------------------
CREATE OR ALTER PROCEDURE parques.TipoParqueInsertar
    @p_descripcion VARCHAR(100)
AS
BEGIN
    SET NOCOUNT ON;

    IF @p_descripcion IS NULL OR LTRIM(RTRIM(@p_descripcion)) = ''
        THROW 50001, 'La descripción no puede estar vacía.', 1;

    IF EXISTS (SELECT 1 FROM parques.TipoParque WHERE descripcion = @p_descripcion)
        THROW 50002, 'Ya existe un tipo de parque con esa descripción.', 1;

    INSERT INTO parques.TipoParque (descripcion) VALUES (@p_descripcion);
END
GO

-- ------------------------------------------------------------
--  MODIFICACIÓN
-- ------------------------------------------------------------
CREATE OR ALTER PROCEDURE parques.TipoParqueModificar
    @p_id_tipo_parque INT,
    @p_descripcion     VARCHAR(100)
AS
BEGIN
    SET NOCOUNT ON;

    IF @p_descripcion IS NULL OR LTRIM(RTRIM(@p_descripcion)) = ''
        THROW 50001, 'La descripción no puede estar vacía.', 1;

    IF NOT EXISTS (SELECT 1 FROM parques.TipoParque WHERE id_tipo_parque = @p_id_tipo_parque)
        THROW 50003, 'No existe el tipo de parque indicado.', 1;

    IF EXISTS (SELECT 1 FROM parques.TipoParque
               WHERE descripcion = @p_descripcion
                 AND id_tipo_parque <> @p_id_tipo_parque)
        THROW 50002, 'Ya existe otro tipo de parque con esa descripción.', 1;

    UPDATE parques.TipoParque
       SET descripcion = @p_descripcion
     WHERE id_tipo_parque = @p_id_tipo_parque;
END
GO

-- ------------------------------------------------------------
--  BAJA
-- ------------------------------------------------------------
CREATE OR ALTER PROCEDURE parques.TipoParqueEliminar
    @p_id_tipo_parque INT
AS
BEGIN
    SET NOCOUNT ON;

    IF NOT EXISTS (SELECT 1 FROM parques.TipoParque WHERE id_tipo_parque = @p_id_tipo_parque)
        THROW 50003, 'No existe el tipo de parque indicado.', 1;

    DELETE FROM parques.TipoParque WHERE id_tipo_parque = @p_id_tipo_parque;
END
GO

-- ============================================================
--  TABLA: Parque  -  ABM mediante procedimientos almacenados
-- ============================================================

-- ------------------------------------------------------------
--  ALTA
-- ------------------------------------------------------------
CREATE OR ALTER PROCEDURE parques.ParqueInsertar
    @p_codigo_oficial VARCHAR(50),
    @p_nombre         VARCHAR(100),
    @p_ubicacion      VARCHAR(255),
    @p_superficie     DECIMAL(12,2),
    @p_id_tipo_parque INT
AS
BEGIN
    SET NOCOUNT ON;
    DECLARE @errores NVARCHAR(MAX) = N'';

    IF LTRIM(RTRIM(ISNULL(@p_codigo_oficial, ''))) = ''
        SET @errores += N'- El código oficial es obligatorio.' + CHAR(13);
    IF EXISTS (SELECT 1 FROM parques.Parque WHERE codigo_oficial = @p_codigo_oficial)
        SET @errores += N'- Ya existe un parque con ese código oficial.' + CHAR(13);
    IF LTRIM(RTRIM(ISNULL(@p_nombre, ''))) = ''
        SET @errores += N'- El nombre del parque es obligatorio.' + CHAR(13);
    IF LTRIM(RTRIM(ISNULL(@p_ubicacion, ''))) = ''
        SET @errores += N'- La ubicación del parque es obligatoria.' + CHAR(13);
    IF ISNULL(@p_superficie, 0) <= 0
        SET @errores += N'- La superficie debe ser mayor a cero.' + CHAR(13);
    IF NOT EXISTS (SELECT 1 FROM parques.TipoParque WHERE id_tipo_parque = @p_id_tipo_parque)
        SET @errores += N'- El tipo de parque indicado no existe.' + CHAR(13);

    IF @errores != N'' THROW 50000, @errores, 1;

    INSERT INTO parques.Parque (codigo_oficial, nombre, ubicacion, superficie, id_tipo_parque)
    VALUES (@p_codigo_oficial, @p_nombre, @p_ubicacion, @p_superficie, @p_id_tipo_parque);
END
GO

-- ------------------------------------------------------------
--  MODIFICACIÓN
-- ------------------------------------------------------------
CREATE OR ALTER PROCEDURE parques.ParqueModificar
    @p_id_parque      INT,
    @p_codigo_oficial VARCHAR(50),
    @p_nombre         VARCHAR(100),
    @p_ubicacion      VARCHAR(255),
    @p_superficie     DECIMAL(12,2),
    @p_id_tipo_parque INT
AS
BEGIN
    SET NOCOUNT ON;
    DECLARE @errores NVARCHAR(MAX) = N'';

    IF NOT EXISTS (SELECT 1 FROM parques.Parque WHERE id_parque = @p_id_parque)
        SET @errores += N'- No existe un parque con el ID indicado.' + CHAR(13);
    IF LTRIM(RTRIM(ISNULL(@p_codigo_oficial, ''))) = ''
        SET @errores += N'- El código oficial es obligatorio.' + CHAR(13);
    IF EXISTS (SELECT 1 FROM parques.Parque WHERE codigo_oficial = @p_codigo_oficial AND id_parque != @p_id_parque)
        SET @errores += N'- Ya existe otro parque con ese código oficial.' + CHAR(13);
    IF LTRIM(RTRIM(ISNULL(@p_nombre, ''))) = ''
        SET @errores += N'- El nombre del parque es obligatorio.' + CHAR(13);
    IF ISNULL(@p_superficie, 0) <= 0
        SET @errores += N'- La superficie debe ser mayor a cero.' + CHAR(13);
    IF NOT EXISTS (SELECT 1 FROM parques.TipoParque WHERE id_tipo_parque = @p_id_tipo_parque)
        SET @errores += N'- El tipo de parque indicado no existe.' + CHAR(13);

    IF @errores != N'' THROW 50000, @errores, 1;

    UPDATE parques.Parque
    SET codigo_oficial = @p_codigo_oficial, nombre = @p_nombre, ubicacion = @p_ubicacion,
        superficie = @p_superficie, id_tipo_parque = @p_id_tipo_parque
    WHERE id_parque = @p_id_parque;
END
GO

-- ------------------------------------------------------------
--  BAJA
-- ------------------------------------------------------------
CREATE OR ALTER PROCEDURE parques.ParqueEliminar
    @p_id_parque INT
AS
BEGIN
    SET NOCOUNT ON;
    DECLARE @errores NVARCHAR(MAX) = N'';

    IF NOT EXISTS (SELECT 1 FROM parques.Parque WHERE id_parque = @p_id_parque)
        SET @errores += N'- No existe un parque con el ID indicado.' + CHAR(13);
    IF EXISTS (SELECT 1 FROM comercial.Concesion WHERE id_parque = @p_id_parque)
        SET @errores += N'- No se puede eliminar: el parque tiene concesiones registradas.' + CHAR(13);
    IF EXISTS (SELECT 1 FROM personal.HistorialGuardaparque WHERE id_parque = @p_id_parque)
        SET @errores += N'- No se puede eliminar: el parque tiene historial de guardaparques.' + CHAR(13);
    IF EXISTS (SELECT 1 FROM turismo.AtraccionTour WHERE id_parque = @p_id_parque)
        SET @errores += N'- No se puede eliminar: el parque tiene atracciones/tours registrados.' + CHAR(13);
    IF EXISTS (SELECT 1 FROM ventas.HistorialPrecio WHERE id_parque = @p_id_parque)
        SET @errores += N'- No se puede eliminar: el parque tiene historial de precios registrado.' + CHAR(13);

    IF @errores != N'' THROW 50000, @errores, 1;

    DELETE FROM parques.Parque WHERE id_parque = @p_id_parque;
END
GO
