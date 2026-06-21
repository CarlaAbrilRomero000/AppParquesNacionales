package org.example.parques;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Punto de entrada de la aplicación "Sistema de Parques Nacionales".
 *
 * <p>Al ejecutarse levanta un servidor web embebido (Tomcat) accesible en
 * http://localhost:8080</p>
 */
@SpringBootApplication
public class ParquesApplication {

    public static void main(String[] args) {
        SpringApplication.run(ParquesApplication.class, args);
    }
}
