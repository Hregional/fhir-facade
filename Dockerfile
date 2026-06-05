# Etapa 1: Compilación
FROM maven:3.8.4-openjdk-17 AS build
WORKDIR /app

# Copiar el pom.xml y descargar dependencias para aprovechar el caché de capas de Docker
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copiar el código fuente y compilar
COPY src ./src
RUN mvn clean package -DskipTests

# Etapa 2: Servidor de aplicaciones (Tomcat)
FROM tomcat:10.1-jdk17-temurin

WORKDIR /usr/local/tomcat/webapps/

# Eliminar las aplicaciones por defecto de Tomcat para evitar conflictos
RUN rm -rf ./ROOT ./examples ./docs ./manager ./host-manager

# Copiar el archivo .war generado en la etapa de compilación
# Lo nombramos ROOT.war para que la aplicación sea accesible en la raíz (http://localhost:8080/)
COPY --from=build /app/target/restful-server-example.war ./ROOT.war

# Exponer el puerto por defecto de Tomcat
EXPOSE 8080

# Iniciar Tomcat
CMD ["catalina.sh", "run"]
