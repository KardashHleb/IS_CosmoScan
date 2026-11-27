# syntax=docker/dockerfile:1

FROM eclipse-temurin:17-jdk AS build
WORKDIR /app

COPY gradlew .
COPY gradle gradle
COPY build.gradle settings.gradle ./
COPY file-validation/build.gradle file-validation/build.gradle
COPY gateway/build.gradle gateway/build.gradle
COPY submission-service/build.gradle submission-service/build.gradle
COPY file-storing-service/build.gradle file-storing-service/build.gradle
COPY analysis-service/build.gradle analysis-service/build.gradle
COPY file-validation/src file-validation/src
COPY gateway/src gateway/src
COPY submission-service/src submission-service/src
COPY file-storing-service/src file-storing-service/src
COPY analysis-service/src analysis-service/src

RUN chmod +x gradlew \
    && ./gradlew :gateway:bootJar -x test --no-daemon

FROM eclipse-temurin:17-jre
WORKDIR /app

RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/*

COPY --from=build /app/gateway/build/libs/gateway.jar app.jar

EXPOSE 8090

HEALTHCHECK --interval=10s --timeout=5s --retries=5 --start-period=30s \
  CMD curl -f http://localhost:8090/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
