# Stage 1: Build the application using Maven and a full JDK

FROM maven:3.9-eclipse-temurin-21-jammy AS builder

# Set the working directory in the container

WORKDIR /app

# Copy the Maven wrapper and pom.xml to leverage Docker cache

COPY .mvn/ .mvn
COPY mvnw pom.xml ./

# Download dependencies (this layer will be cached if pom.xml doesn't change)

RUN ./mvnw dependency:go-offline -B

# Copy the rest of the application source code

COPY src ./src

# Package the application (skip tests for faster builds)

RUN ./mvnw package -DskipTests

# Stage 2: Create the final, smaller image with only the JRE and the app JAR

FROM eclipse-temurin:21-jre

# Set the working directory

WORKDIR /app

# Copy the JAR from the builder stage

COPY --from=builder /app/target/text-rpg-backend-0.0.1-SNAPSHOT.jar app.jar

# Expose the port the application will run on

EXPOSE 8080

# Command to run the application

ENTRYPOINT ["java", "-jar", "app.jar"]