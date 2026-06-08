FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app
COPY . .
RUN ./mvnw clean package -DskipTests -Dmaven.test.skip=true
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "target/*.jar"]