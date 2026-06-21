package org.example.parques.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.Arrays;

/**
 * Inicializa la base de datos al arrancar la aplicación:
 *
 * <ol>
 *     <li>Espera a que SQL Server esté disponible (reintentos).</li>
 *     <li>Crea la base de datos si no existe (conectándose a {@code master}).</li>
 *     <li>Ejecuta el script {@code db/init.sql} (esquema, tabla y procedimientos
 *         almacenados), dividiéndolo por lotes {@code GO}.</li>
 * </ol>
 *
 * <p>Gracias a esto el proyecto funciona "out of the box" tanto desde IntelliJ
 * como desde Docker, sin pasos manuales de creación de base de datos.</p>
 */
@Component
public class DatabaseInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DatabaseInitializer.class);

    private final JdbcTemplate jdbcTemplate;

    @Value("${app.db.host:localhost}")
    private String host;

    @Value("${app.db.port:1433}")
    private String port;

    @Value("${app.db.name:ParqueNacionalesDB}")
    private String dbName;

    @Value("${spring.datasource.username}")
    private String usuario;

    @Value("${spring.datasource.password}")
    private String password;

    public DatabaseInitializer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) throws Exception {
        crearBaseDeDatosSiNoExiste();
        ejecutarScriptInicial();
        log.info("Inicialización de base de datos completada correctamente.");
    }

    /**
     * Se conecta a la base {@code master} (con reintentos, porque el contenedor
     * de SQL Server tarda en aceptar conexiones) y crea la base de datos.
     */
    private void crearBaseDeDatosSiNoExiste() throws InterruptedException {
        String urlMaster = "jdbc:sqlserver://" + host + ":" + port
                + ";databaseName=master;encrypt=true;trustServerCertificate=true";

        int intentosMaximos = 30;
        for (int intento = 1; intento <= intentosMaximos; intento++) {
            try (Connection con = DriverManager.getConnection(urlMaster, usuario, password);
                 Statement st = con.createStatement()) {
                st.execute("IF DB_ID('" + dbName + "') IS NULL CREATE DATABASE [" + dbName + "]");
                log.info("Base de datos '{}' verificada/creada.", dbName);
                return;
            } catch (Exception ex) {
                log.warn("SQL Server no disponible todavía (intento {}/{}): {}",
                        intento, intentosMaximos, ex.getMessage());
                Thread.sleep(3000);
            }
        }
        throw new IllegalStateException(
                "No fue posible conectarse a SQL Server luego de " + intentosMaximos + " intentos.");
    }

    /**
     * Lee {@code db/init.sql} del classpath y ejecuta cada lote (separado por
     * líneas {@code GO}) contra la base de datos de la aplicación.
     */
    private void ejecutarScriptInicial() throws Exception {
        ClassPathResource recurso = new ClassPathResource("db/init.sql");
        String script = new String(recurso.getInputStream().readAllBytes(), StandardCharsets.UTF_8);

        // Cada lote se separa por una línea que contiene únicamente "GO".
        String[] lotes = script.split("(?im)^\\s*GO\\s*$");

        Arrays.stream(lotes)
                .map(String::trim)
                .filter(lote -> !lote.isEmpty())
                .forEach(jdbcTemplate::execute);

        log.info("Script db/init.sql ejecutado ({} lote(s)).", lotes.length);
    }
}
