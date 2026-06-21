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
├── config/DatabaseInitializer.java# verifica/crea esquema, tabla TipoParque y SP al arrancar
├── controller/                    # HomeController, ParqueController, TipoParqueController, ImportacionController
├── service/                       # TipoParqueService, ImportacionService
├── repository/                    # TipoParqueRepository, ParqueRepository (JdbcTemplate)
├── model/                         # TipoParque, Parque
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

Esta es la opción recomendada si ya tenés **SQL Server instalado** en tu máquina con
la base **`ParquesNacionalesDB`** ya cargada. Por defecto la app se conecta con
**Autenticación de Windows** (sin usuario ni contraseña) a esa base existente.

```
Chrome → http://localhost:8080 → Spring Boot (IntelliJ) → localhost:1433 → SQL Server local (Windows Auth)
```

### Paso 1 — DLL de autenticación de Windows (una sola vez)
Para usar autenticación de Windows, el driver JDBC necesita la librería nativa
**`mssql-jdbc_auth-12.6.4.x64.dll`** (ya incluida en la carpeta `lib/` del proyecto).
El JVM la busca en el `java.library.path`, que en Windows incluye las carpetas del
**PATH del usuario**. Por eso la carpeta `lib/` del proyecto se agregó al PATH de
usuario; si clonás el repo en otra máquina, agregá esa carpeta al PATH:

```powershell
[Environment]::SetEnvironmentVariable("Path",
  [Environment]::GetEnvironmentVariable("Path","User") + ";<ruta-al-proyecto>\lib", "User")
```

(luego reiniciá IntelliJ para que tome el PATH nuevo). Alternativa: copiar la DLL
dentro de `<JDK>\bin` o agregar en la *Run Configuration* la opción de VM
`-Djava.library.path=<ruta-al-proyecto>\lib`.

### Paso 2 — Ejecutar la aplicación
1. Abrir el proyecto en IntelliJ (lo detecta como proyecto Maven).
2. Ejecutar la clase `ParquesApplication`.
   - Al arrancar se conecta a la base **`ParquesNacionalesDB`** ya existente. El
     `DatabaseInitializer` solo verifica/crea (si faltan) el esquema, la tabla
     `TipoParque` y los procedimientos almacenados; **no toca** los datos de
     `parques.Parque`.
3. Abrir `http://localhost:8080` → tarjeta **Listado de Parques** (o menú *Parques*).

### Ver los datos en SSMS
Conectate a `localhost` con **autenticación de Windows** y consultá:
```sql
SELECT * FROM ParquesNacionalesDB.parques.Parque;
```

> El modo de autenticación, host y base se pueden cambiar con variables de entorno
> (`DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_INTEGRATED_SECURITY`, `DB_USER`, `DB_PASSWORD`)
> o editando `src/main/resources/application.properties`.
> Para usar autenticación SQL en vez de Windows: `DB_INTEGRATED_SECURITY=false` +
> `DB_USER`/`DB_PASSWORD`.

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

| Parámetro      | Valor (local / IntelliJ)    |
|----------------|-----------------------------|
| Host           | `localhost`                 |
| Puerto         | `1433`                      |
| Base           | `ParquesNacionalesDB`       |
| Autenticación  | Windows (`integratedSecurity=true`) |
| Usuario / Clave| — (no aplica con Windows Auth) |

> En Docker se usa autenticación SQL (`sa` / `Parques2024!`, base `ParqueNacionalesDB`).
