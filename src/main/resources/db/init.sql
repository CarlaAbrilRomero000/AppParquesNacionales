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

-- ============================================================
--  IMPORTACIÓN: Organizaciones Distinguidas
--  Schemas, tablas y procedimiento de carga desde CSV.
-- ============================================================

-- Esquema "estadisticas" (tablas destino de la importación)
IF NOT EXISTS (SELECT 1 FROM sys.schemas WHERE name = 'estadisticas')
    EXEC('CREATE SCHEMA estadisticas');
GO

-- Esquema "importaciones" (procedimientos de carga)
IF NOT EXISTS (SELECT 1 FROM sys.schemas WHERE name = 'importaciones')
    EXEC('CREATE SCHEMA importaciones');
GO

-- Tabla destino de las organizaciones distinguidas.
-- Clave de unicidad / upsert: organizacion + calle + numero.
IF NOT EXISTS (
    SELECT 1 FROM sys.tables t
    JOIN sys.schemas s ON t.schema_id = s.schema_id
    WHERE s.name = 'estadisticas' AND t.name = 'OrganizacionesDistinguidas')
BEGIN
    CREATE TABLE estadisticas.OrganizacionesDistinguidas (
        id_organizacion     INT IDENTITY(1,1) NOT NULL,
        organizacion        VARCHAR(200)      NOT NULL,
        rubro               VARCHAR(100)      NULL,
        subrubro            VARCHAR(100)      NULL,
        calle               VARCHAR(200)      NULL,
        numero              VARCHAR(50)       NULL,
        pais                VARCHAR(100)      NULL,
        provincia           VARCHAR(100)      NULL,
        ciudad              VARCHAR(100)      NULL,
        telefono            VARCHAR(100)      NULL,
        facebook            VARCHAR(200)      NULL,
        web                 VARCHAR(200)      NULL,
        programa            VARCHAR(200)      NULL,
        fecha_distincion    DATE              NULL,
        fecha_revalidacion  DATE              NULL,
        fecha_creacion      DATETIME2         NOT NULL CONSTRAINT DF_OrgDist_FechaCreacion DEFAULT SYSDATETIME(),
        fecha_actualizacion DATETIME2         NULL,
        CONSTRAINT PK_OrganizacionesDistinguidas PRIMARY KEY (id_organizacion)
    );
END
GO

-- Índice único que soporta el upsert sin MERGE del procedimiento de carga.
IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = 'UX_OrganizacionesDistinguidas_Clave'
      AND object_id = OBJECT_ID('estadisticas.OrganizacionesDistinguidas'))
BEGIN
    CREATE UNIQUE INDEX UX_OrganizacionesDistinguidas_Clave
        ON estadisticas.OrganizacionesDistinguidas (organizacion, calle, numero);
END
GO

-- Tabla de filas rechazadas durante las importaciones de CSV.
IF NOT EXISTS (
    SELECT 1 FROM sys.tables t
    JOIN sys.schemas s ON t.schema_id = s.schema_id
    WHERE s.name = 'estadisticas' AND t.name = 'ErroresImportacion')
BEGIN
    CREATE TABLE estadisticas.ErroresImportacion (
        id_error                INT IDENTITY(1,1) NOT NULL,
        archivo_origen          VARCHAR(1000)     NULL,
        motivo_error            VARCHAR(500)      NULL,
        indice_tiempo_valor     VARCHAR(300)      NULL,
        region_destino_valor    VARCHAR(300)      NULL,
        origen_visitantes_valor VARCHAR(300)      NULL,
        visitas_valor           VARCHAR(300)      NULL,
        observaciones_valor     VARCHAR(300)      NULL,
        fecha_registro          DATETIME2         NOT NULL CONSTRAINT DF_ErroresImportacion_Fecha DEFAULT SYSDATETIME(),
        CONSTRAINT PK_ErroresImportacion PRIMARY KEY (id_error)
    );
END
GO

-- ------------------------------------------------------------
--  importaciones.ImportarOrganizacionesDistinguidas
--  Carga, valida y hace upsert (sin MERGE) del CSV indicado.
--  Devuelve registros_insertados, actualizados y rechazados.
-- ------------------------------------------------------------
CREATE OR ALTER PROCEDURE importaciones.ImportarOrganizacionesDistinguidas
    @p_ruta_archivo VARCHAR(500)   -- nombre del archivo (ej.: 'registro-organizaciones-distinguidas.csv')
AS
BEGIN
    SET NOCOUNT ON;
    SET DATEFORMAT ymd;

    DECLARE @v_ruta_base     VARCHAR(500)  = 'C:\Importaciones\';
    DECLARE @v_ruta_completa VARCHAR(1000) = @v_ruta_base + @p_ruta_archivo;
    DECLARE @v_existe_archivo INT          = 0;
    DECLARE @v_sql            NVARCHAR(MAX);
    DECLARE @v_insertados     INT          = 0;
    DECLARE @v_actualizados   INT          = 0;
    DECLARE @v_rechazados     INT          = 0;

    BEGIN TRY
        ----------------------------------------------------------
        -- 1) Validar existencia del archivo
        ----------------------------------------------------------
        EXEC master.dbo.xp_fileexist @v_ruta_completa, @v_existe_archivo OUTPUT;

        IF @v_existe_archivo IS NULL OR @v_existe_archivo = 0
            THROW 50000, 'El archivo especificado no existe o no es accesible desde el motor de SQL Server.', 1;

        ----------------------------------------------------------
        -- 2) Crear tabla temporal de staging y cargar el CSV.
        ----------------------------------------------------------
        IF OBJECT_ID('tempdb..#StagingOrganizaciones') IS NOT NULL DROP TABLE #StagingOrganizaciones;

        CREATE TABLE #StagingOrganizaciones (
            organizacion_raw       VARCHAR(300) NULL,
            rubro_raw              VARCHAR(200) NULL,
            subrubro_raw           VARCHAR(200) NULL,
            calle_raw              VARCHAR(300) NULL,
            numero_raw             VARCHAR(100) NULL,
            pais_raw               VARCHAR(200) NULL,
            provincia_raw          VARCHAR(200) NULL,
            ciudad_raw             VARCHAR(200) NULL,
            telefono_raw           VARCHAR(200) NULL,
            facebook_raw           VARCHAR(300) NULL,
            web_raw                VARCHAR(300) NULL,
            programa_raw           VARCHAR(300) NULL,
            fecha_distincion_raw   VARCHAR(50)  NULL,
            fecha_revalidacion_raw VARCHAR(50)  NULL
        );

        SET @v_sql = N'
            BULK INSERT #StagingOrganizaciones
            FROM ''' + REPLACE(@v_ruta_completa, '''', '''''') + N'''
            WITH (
                FIELDTERMINATOR = '','',
                ROWTERMINATOR   = ''0x0a'',
                FIRSTROW        = 2,
                CODEPAGE        = ''65001'',
                MAXERRORS       = 2147483647
            );';

        EXEC sp_executesql @v_sql;

        ALTER TABLE #StagingOrganizaciones
            ADD row_num            INT           NULL,
                organizacion       VARCHAR(200)  NULL,
                rubro              VARCHAR(100)  NULL,
                subrubro           VARCHAR(100)  NULL,
                calle              VARCHAR(200)  NULL,
                numero             VARCHAR(50)   NULL,
                pais               VARCHAR(100)  NULL,
                provincia          VARCHAR(100)  NULL,
                ciudad             VARCHAR(100)  NULL,
                telefono           VARCHAR(100)  NULL,
                facebook           VARCHAR(200)  NULL,
                web                VARCHAR(200)  NULL,
                programa           VARCHAR(200)  NULL,
                fecha_distincion   DATE          NULL,
                fecha_revalidacion DATE          NULL,
                motivo_error       VARCHAR(500)  NULL;

        ----------------------------------------------------------
        -- 3) Poblar columnas de validación
        ----------------------------------------------------------

        -- Asignar número de fila
        ;WITH Num AS (
            SELECT row_num, ROW_NUMBER() OVER (ORDER BY (SELECT NULL)) AS rn
            FROM #StagingOrganizaciones
        )
        UPDATE Num SET row_num = rn;

        -- Parsear columnas tipadas (una sola vez cada conversión)
        UPDATE #StagingOrganizaciones
        SET
            organizacion       = NULLIF(LTRIM(RTRIM(organizacion_raw)), ''),
            rubro              = NULLIF(LTRIM(RTRIM(rubro_raw)), ''),
            subrubro           = NULLIF(LTRIM(RTRIM(subrubro_raw)), ''),
            calle              = NULLIF(LTRIM(RTRIM(calle_raw)), ''),
            numero             = NULLIF(LTRIM(RTRIM(numero_raw)), ''),
            pais               = NULLIF(LTRIM(RTRIM(pais_raw)), ''),
            provincia          = NULLIF(LTRIM(RTRIM(provincia_raw)), ''),
            ciudad             = NULLIF(LTRIM(RTRIM(ciudad_raw)), ''),
            telefono           = NULLIF(LTRIM(RTRIM(telefono_raw)), ''),
            facebook           = NULLIF(LTRIM(RTRIM(facebook_raw)), ''),
            web                = NULLIF(LTRIM(RTRIM(web_raw)), ''),
            programa           = NULLIF(LTRIM(RTRIM(programa_raw)), ''),
            fecha_distincion   = TRY_CAST(LTRIM(RTRIM(REPLACE(fecha_distincion_raw,   CHAR(13), ''))) AS DATE),
            fecha_revalidacion = TRY_CAST(LTRIM(RTRIM(REPLACE(fecha_revalidacion_raw, CHAR(13), ''))) AS DATE);

        -- Validar usando las columnas ya computadas (sin repetir conversiones)
        UPDATE #StagingOrganizaciones
        SET motivo_error = CASE
                WHEN organizacion IS NULL
                    THEN N'El nombre de la organización es nulo o vacío.'
                WHEN rubro IS NULL
                    THEN N'El rubro es nulo o vacío.'
                WHEN LTRIM(RTRIM(REPLACE(ISNULL(fecha_distincion_raw, ''), CHAR(13), ''))) <> ''
                 AND fecha_distincion IS NULL
                    THEN N'La fecha de distinción es inválida.'
                WHEN LTRIM(RTRIM(REPLACE(ISNULL(fecha_revalidacion_raw, ''), CHAR(13), ''))) <> ''
                 AND fecha_revalidacion IS NULL
                    THEN N'La fecha de revalidación es inválida.'
                ELSE NULL
            END;

        -- Marcar duplicados dentro del archivo: conservar la última ocurrencia
        ;WITH Duplicados AS (
            SELECT row_num,
                   ROW_NUMBER() OVER (
                       PARTITION BY organizacion, calle, numero
                       ORDER BY row_num DESC
                   ) AS orden
            FROM #StagingOrganizaciones
            WHERE motivo_error IS NULL
        )
        UPDATE s
        SET s.motivo_error = N'Registro duplicado dentro del archivo (se conserva la última ocurrencia con esa clave).'
        FROM #StagingOrganizaciones s
        INNER JOIN Duplicados d ON d.row_num = s.row_num
        WHERE d.orden > 1;

        ----------------------------------------------------------
        -- 4) Registrar filas rechazadas
        ----------------------------------------------------------
        INSERT INTO estadisticas.ErroresImportacion
            (archivo_origen, motivo_error, indice_tiempo_valor, region_destino_valor,
             origen_visitantes_valor, visitas_valor, observaciones_valor)
        SELECT
            @v_ruta_completa,
            s.motivo_error,
            s.organizacion_raw,
            s.rubro_raw,
            s.calle_raw,
            s.numero_raw,
            s.fecha_distincion_raw
        FROM #StagingOrganizaciones s
        WHERE s.motivo_error IS NOT NULL;

        SET @v_rechazados = @@ROWCOUNT;

        ----------------------------------------------------------
        -- 5) Upsert sin MERGE sobre OrganizacionesDistinguidas
        ----------------------------------------------------------
        BEGIN TRANSACTION;

        -- 5a) Actualizar registros existentes cuyos datos cambiaron
        UPDATE dest
        SET dest.rubro               = s.rubro,
            dest.subrubro            = s.subrubro,
            dest.pais                = s.pais,
            dest.provincia           = s.provincia,
            dest.ciudad              = s.ciudad,
            dest.telefono            = s.telefono,
            dest.facebook            = s.facebook,
            dest.web                 = s.web,
            dest.programa            = s.programa,
            dest.fecha_distincion    = s.fecha_distincion,
            dest.fecha_revalidacion  = s.fecha_revalidacion,
            dest.fecha_actualizacion = SYSDATETIME()
        FROM estadisticas.OrganizacionesDistinguidas dest
        INNER JOIN #StagingOrganizaciones s
            ON  s.organizacion       = dest.organizacion
           AND ISNULL(s.calle, '')   = ISNULL(dest.calle, '')
           AND ISNULL(s.numero, '')  = ISNULL(dest.numero, '')
        WHERE s.motivo_error IS NULL
          AND (
                ISNULL(dest.rubro, '')     <> ISNULL(s.rubro, '')
             OR ISNULL(dest.subrubro, '')  <> ISNULL(s.subrubro, '')
             OR ISNULL(dest.pais, '')      <> ISNULL(s.pais, '')
             OR ISNULL(dest.provincia, '') <> ISNULL(s.provincia, '')
             OR ISNULL(dest.ciudad, '')    <> ISNULL(s.ciudad, '')
             OR ISNULL(dest.telefono, '')  <> ISNULL(s.telefono, '')
             OR ISNULL(dest.facebook, '')  <> ISNULL(s.facebook, '')
             OR ISNULL(dest.web, '')       <> ISNULL(s.web, '')
             OR ISNULL(dest.programa, '')  <> ISNULL(s.programa, '')
             OR ISNULL(CAST(dest.fecha_distincion   AS VARCHAR(20)), '')
                <> ISNULL(CAST(s.fecha_distincion   AS VARCHAR(20)), '')
             OR ISNULL(CAST(dest.fecha_revalidacion AS VARCHAR(20)), '')
                <> ISNULL(CAST(s.fecha_revalidacion AS VARCHAR(20)), '')
              );

        SET @v_actualizados = @@ROWCOUNT;

        -- 5b) Insertar organizaciones nuevas
        INSERT INTO estadisticas.OrganizacionesDistinguidas
            (organizacion, rubro, subrubro, calle, numero, pais, provincia, ciudad,
             telefono, facebook, web, programa, fecha_distincion, fecha_revalidacion)
        SELECT
            s.organizacion, s.rubro, s.subrubro, s.calle, s.numero, s.pais,
            s.provincia, s.ciudad, s.telefono, s.facebook, s.web, s.programa,
            s.fecha_distincion, s.fecha_revalidacion
        FROM #StagingOrganizaciones s
        WHERE s.motivo_error IS NULL
          AND NOT EXISTS (
              SELECT 1 FROM estadisticas.OrganizacionesDistinguidas dest
              WHERE dest.organizacion      = s.organizacion
                AND ISNULL(dest.calle, '')  = ISNULL(s.calle, '')
                AND ISNULL(dest.numero, '') = ISNULL(s.numero, '')
          );

        SET @v_insertados = @@ROWCOUNT;

        COMMIT TRANSACTION;

        ----------------------------------------------------------
        -- 6) Limpieza y resultado
        ----------------------------------------------------------
        DROP TABLE #StagingOrganizaciones;

        SELECT
            @v_insertados   AS registros_insertados,
            @v_actualizados AS registros_actualizados,
            @v_rechazados   AS registros_rechazados;
    END TRY
    BEGIN CATCH
        IF XACT_STATE() <> 0 ROLLBACK TRANSACTION;
        IF OBJECT_ID('tempdb..#StagingOrganizaciones') IS NOT NULL DROP TABLE #StagingOrganizaciones;
        THROW;
    END CATCH
END
GO
