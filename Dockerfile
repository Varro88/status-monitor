FROM gradle:8.5-jdk17 AS build
WORKDIR /app

# Copy only the gradle wrapper and build files first to leverage Docker caching
COPY build.gradle settings.gradle gradlew /app/
COPY gradle /app/gradle

# Run gradle wrapper to download dependencies (layer caching optimization)
RUN ./gradlew build -x test || return 0

# Now copy the rest of the project
COPY . /app

# Build the app
RUN ./gradlew clean build -x test

# Use a minimal JDK image for the actual app
FROM azul/zulu-openjdk:17-latest
VOLUME /tmp

# Copy the built jar from the previous stage
COPY --from=build /app/build/libs/*.jar app.jar

# Run the jar
ENTRYPOINT ["java", "-jar", "/app.jar"]
