# Build stage
FROM maven:3.9.5-eclipse-temurin-17-alpine AS build
WORKDIR /app

# Copy just the POM file first to leverage Docker cache
COPY pom.xml .

# Download all dependencies
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the application
RUN mvn clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copy the JAR file from the build stage
COPY --from=build /app/target/*.jar app.jar

# Expose the application port
EXPOSE 8080

# Set the entry point to run the application
ENTRYPOINT ["java","-Djava.net.preferIPv4Stack=true","-jar","app.jar"]
