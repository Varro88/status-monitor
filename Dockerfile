# Stage 1: Build the Spring Boot app with Gradle
FROM gradle:8.5-jdk17 AS build
WORKDIR /app

# Copy build config first for layer caching
COPY build.gradle settings.gradle gradlew /app/
COPY gradle /app/gradle

# Copy the rest of the source code
COPY . /app

# Build only the bootJar (Spring Boot executable) and skip tests for faster CI builds
RUN ./gradlew clean bootJar -x test

# Stage 2: Create minimal runtime image
FROM azul/zulu-openjdk:17-latest
VOLUME /tmp

# Copy the Spring Boot jar from the build stage
COPY --from=build /app/build/libs/*.jar app.jar

# Run the jar
ENTRYPOINT ["java", "-jar", "/app.jar"]
