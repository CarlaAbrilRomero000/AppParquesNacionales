# ============================================================
#  Etapa 1: compilación con Maven
# ============================================================
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Se copian primero el pom para aprovechar la caché de dependencias
COPY pom.xml .
RUN mvn -B dependency:go-offline

# Se copia el código fuente y se empaqueta el jar
COPY src ./src
RUN mvn -B clean package -DskipTests

# ============================================================
#  Etapa 2: imagen final liviana solo con el JRE
# ============================================================
FROM eclipse-temurin:21-jre
WORKDIR /app

# Carpeta donde se guardarán los XML generados
RUN mkdir -p /app/xml-generados

# Se copia el jar empaquetado en la etapa anterior
COPY --from=build /app/target/parques-app.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
