# Usa una imagen base oficial con JDK 17 (puede ser Temurin u otra)
FROM eclipse-temurin:17-jdk-jammy

# Directorio de trabajo dentro del contenedor
WORKDIR /app

# Copia el .jar generado al contenedor
COPY backend/target/*.jar app.jar

# Expone el puerto 8080 (igual que en tu Spring Boot)
EXPOSE 8080

# Comando para ejecutar la aplicación
ENTRYPOINT ["java", "-jar", "app.jar"]
