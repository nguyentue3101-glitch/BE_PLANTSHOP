# ===== Stage 1: Build =====
FROM gradle:8.5-jdk17-alpine AS build
WORKDIR /app

# Copy toàn bộ source
COPY . .

# Build Spring Boot (không chạy test)
RUN gradle build -x test

# ===== Stage 2: Run =====
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copy file jar từ stage build
COPY --from=build /app/build/libs/*.jar app.jar

# Render dùng port 8080
EXPOSE 8080

# Chạy Spring Boot
ENTRYPOINT ["java","-jar","app.jar"]
