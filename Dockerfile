# ---------- Estágio 1: build ----------
ARG JAVA_VERSION=17

FROM maven:3.9-eclipse-temurin-${JAVA_VERSION} AS build
WORKDIR /app

# Copia só o pom primeiro: as dependências ficam em cache
# e só são baixadas de novo se o pom.xml mudar.
COPY pom.xml .
RUN mvn -B -q dependency:go-offline

COPY src ./src
RUN mvn -B -q package -DskipTests

# ---------- Estágio 2: runtime ----------
FROM eclipse-temurin:${JAVA_VERSION}-jre
WORKDIR /app

# curl é usado pelo healthcheck do docker-compose
RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/*

# Usuário sem privilégios (não rodar como root)
RUN useradd --system --create-home appuser
USER appuser

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75"

ENTRYPOINT ["java", "-jar", "app.jar"]