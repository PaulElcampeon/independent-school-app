# Stage 1: Build
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Runtime
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Install wget for Docker health checks
RUN apk add --no-cache wget

# Copy JAR
COPY --from=build /app/target/*.jar app.jar

# Create non-root user
RUN addgroup -g 1001 -S appuser && adduser -u 1001 -S appuser -G appuser
USER appuser

EXPOSE 8080

ENTRYPOINT ["java","-Xms128m","-Xmx256m","-XX:+UseContainerSupport","-XX:+UseStringDeduplication","-jar","app.jar"]
