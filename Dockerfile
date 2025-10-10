# Build stage
FROM eclipse-temurin:21-jdk-alpine AS build

WORKDIR /app

# Copy Maven wrapper and pom.xml
COPY .mvn/ .mvn
COPY mvnw pom.xml ./

# Download dependencies (cached layer)
RUN ./mvnw dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the application
RUN ./mvnw package -DskipTests

# Runtime stage
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# 🩹 Install shadow (for `su`) and bash (optional, good for debugging)
RUN apk add --no-cache shadow bash

# Create app user and logs dir
RUN addgroup -S spring && adduser -S spring -G spring

# Copy jar from build stage
COPY --from=build /app/target/*.jar app.jar

USER spring

EXPOSE 8080

# Fix permissions and switch user before launching app
ENTRYPOINT ["java", "-jar", "app.jar"]
