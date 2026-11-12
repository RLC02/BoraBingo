# Multi-stage build 100% gratuito e leve para o Render
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY auth-service auth-service
COPY gateway-service gateway-service
COPY bingo-sala-service bingo-sala-service
COPY cartela-service cartela-service

# Compila apenas o auth-service sem rodar testes
RUN mvn -B clean package -pl auth-service -am -DskipTests

# Runtime minimalista JRE 17
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/auth-service/target/auth-service-1.0.0.jar app.jar

ENV PORT=8081
ENV JAVA_TOOL_OPTIONS="-XX:+UseSerialGC -Xms32m -Xmx128m -XX:MaxMetaspaceSize=96m -Xss256k -XX:ReservedCodeCacheSize=32m -XX:TieredStopAtLevel=1 -XX:+ExitOnOutOfMemoryError"

EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
