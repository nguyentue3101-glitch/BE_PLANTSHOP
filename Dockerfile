FROM eclipse-temurin:17-jdk

WORKDIR /app

COPY . .

RUN chmod +x gradlew
RUN ./gradlew clean bootJar -x test

EXPOSE 8080

CMD ["java", "-jar", "build/libs/backend-plantshop-0.0.1-SNAPSHOT.jar"]
