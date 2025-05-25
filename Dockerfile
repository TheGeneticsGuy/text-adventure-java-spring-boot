
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

# Package the application (skip tests for faster builds in this stage)
# The JAR will be in /app/target/
RUN ./mvnw package -DskipTests

# Stage 2: Create the final, smaller image with only the JRE and the app JAR
# Use a JRE base image as we only need to run the app, not build it
FROM eclipse-temurin:21-jre-jammy

# Set the working directory
WORKDIR /app

# Argument to define the JAR file (can be overridden at build time if needed)
ARG JAR_FILE=target/text-rpg-backend-0.0.1-SNAPSHOT.jar

# Copy the JAR from the builder stage
COPY --from=builder /app/${JAR_FILE} app.jar

# Expose the port the application will run on (Render will map this)
# Spring Boot defaults to 8080, Render will use $PORT
EXPOSE 8080

# Command to run the application
# Render will set the PORT environment variable. Spring Boot will pick it up.
ENTRYPOINT ["java", "-jar", "/app/app.jar"]