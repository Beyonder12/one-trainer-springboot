FROM eclipse-temurin:17-jdk AS builder
WORKDIR /app

# Copy Maven wrapper
COPY mvnw .
COPY mvnw.cmd .
COPY .mvn .mvn

# Copy project files
COPY pom.xml .
COPY src ./src

RUN chmod +x mvnw
RUN ./mvnw clean package -DskipTests

# ===== RUNTIME IMAGE =====
FROM eclipse-temurin:17-jre
WORKDIR /app

COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
