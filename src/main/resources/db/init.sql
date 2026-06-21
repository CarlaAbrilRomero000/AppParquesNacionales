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
