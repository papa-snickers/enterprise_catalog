FROM eclipse-temurin:11-jre
WORKDIR /app
COPY build/libs/enterprise-catalog-server.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
