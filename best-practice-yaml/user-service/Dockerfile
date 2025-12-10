FROM eclipse-temurin:17-jdk AS builder
WORKDIR /app

COPY mvnw .
COPY mvnw.cmd .
COPY .mvn .mvn

COPY pom.xml .
COPY src ./src

RUN chmod +x mvnw
RUN ./mvnw clean package -DskipTests

FROM eclipse-temurin:17-jre
WORKDIR /app

COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.jar"]
