# 🌲 Sistema de Parques Nacionales

Aplicación web sencilla y elegante para un **Trabajo Práctico de Bases de Datos**.
Permite la gestión (ABM) de **Tipos de Parque** mediante procedimientos almacenados
de SQL Server y la **importación de archivos CSV** con generación de XML.

## 🛠️ Tecnologías

- **Java 21 + Spring Boot 3.3** (Spring MVC)
- **Thymeleaf** para las vistas
- **Bootstrap 5 + Bootstrap Icons** para el diseño
- **SQL Server** como base de datos (con *stored procedures*)
- **Spring JDBC** (`JdbcTemplate`) para el acceso a datos
- **Docker / docker compose** para ejecutar todo el entorno

## 🏗️ Arquitectura en capas

```
Controller  →  Service  →  Repository  →  Stored Procedures (SQL Server)
```

```
src/main/java/org/example/parques
├── ParquesApplication.java        # main
├── config/DatabaseInitializer.java# crea BD, tabla y SP al arrancar
├── controller/                    # HomeController, TipoParqueController, ImportacionController
├── service/                       # TipoParqueService, ImportacionService
├── repository/                    # TipoParqueRepository (JdbcTemplate)
├── model/                         # TipoParque
└── exception/                     # ReglaNegocioException
src/main/resources
├── application.properties
├── db/init.sql                    # esquema + tabla + procedimientos almacenados
├── templates/                     # vistas Thymeleaf
└── static/css/styles.css          # estilos temáticos (verdes)
```

## 🚀 Ejecución con Docker (recomendado)

Requisito: tener **Docker Desktop** instalado y abierto.

```bash
docker compose up
```

Esto levanta **SQL Server** y la **aplicación**. La base de datos, la tabla y los
procedimientos almacenados se crean automáticamente al iniciar.

Luego abrir el navegador en:

```
http://localhost:8080
```

Para detener:

```bash
docker compose down
```

## 💻 Ejecución desde IntelliJ IDEA (SQL Server local, sin Docker)

Esta es la opción recomendada si ya tenés **SQL Server instalado** en tu máquina y
querés ver los datos en **SQL Server Management Studio (SSMS)**.

```
Chrome → http://localhost:8080 → Spring Boot (IntelliJ) → localhost:1433 → SQL Server local
```

### Paso 1 — Configurar SQL Server local (una sola vez)
Por defecto SQL Server suele tener el **TCP/IP apagado** y el login **`sa` deshabilitado**,
y el driver JDBC de Java necesita TCP. Para dejar todo listo automáticamente:

> Clic derecho en **`setup-sqlserver-local.ps1`** → **Ejecutar con PowerShell**
> (aceptar permisos de administrador).

El script habilita TCP/IP en el puerto **1433**, activa la autenticación mixta,
reinicia el servicio y habilita el login `sa` con la contraseña `Parques2024!`.

<details>
<summary>Alternativa manual (si preferís no usar el script)</summary>

1. **SQL Server Configuration Manager** → *Protocolos de MSSQLSERVER* → habilitar **TCP/IP**
   → en *IP Addresses → IPAll* poner **TCP Port = 1433** → reiniciar el servicio.
2. En **SSMS** (conectado con autenticación de Windows):
   - *Propiedades del servidor → Seguridad* → **modo de autenticación mixto** → reiniciar servicio.
   - Habilitar `sa`:
     ```sql
     ALTER LOGIN [sa] ENABLE;
     ALTER LOGIN [sa] WITH PASSWORD = N'Parques2024!';
     ```
</details>

### Paso 2 — Ejecutar la aplicación
1. Abrir el proyecto en IntelliJ (lo detecta como proyecto Maven).
2. Ejecutar la clase `ParquesApplication`.
   - Al arrancar, la app **crea sola** la base `ParqueNacionalesDB`, la tabla y los
     procedimientos almacenados (ver `DatabaseInitializer`).
3. Abrir `http://localhost:8080`.

### Ver los datos en SSMS
Conectate a `localhost` (autenticación de Windows, o SQL con `sa` / `Parques2024!`) y consultá:
```sql
SELECT * FROM ParqueNacionalesDB.parques.TipoParque;
```
Cada alta/baja/modificación que hagas desde la web se refleja ahí al instante.

> Las credenciales y el host se pueden cambiar con variables de entorno
> (`DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD`) o editando
> `src/main/resources/application.properties`.

## 📋 Funcionalidades

### Gestión de Tipos de Parque (ABM)
- **Consulta:** `SELECT * FROM parques.TipoParque ORDER BY id_tipo_parque`
- **Alta:** `EXEC parques.TipoParqueInsertar @p_descripcion`
- **Modificación:** `EXEC parques.TipoParqueModificar @p_id_tipo_parque, @p_descripcion`
- **Baja:** `EXEC parques.TipoParqueEliminar @p_id_tipo_parque`

Los mensajes de error de los procedimientos almacenados (`THROW`) se muestran
como alertas de Bootstrap. La eliminación pide confirmación.

### Importación de CSV
- Se selecciona un archivo `.csv` y se presiona **Importar**.
- El sistema genera un **XML** que se guarda en la carpeta `xml-generados/`.
- Se muestra el resultado y el nombre del último XML generado.
- Hay un archivo de prueba en `ejemplos/parques-ejemplo.csv`.

## 🔑 Datos de conexión por defecto

| Parámetro  | Valor          |
|------------|----------------|
| Host       | `localhost`    |
| Puerto     | `1433`         |
| Base       | `ParqueNacionalesDB` |
| Usuario    | `sa`           |
| Contraseña | `Parques2024!` |
