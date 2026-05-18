# syntax=docker/dockerfile:1

FROM eclipse-temurin:17-jdk AS build
WORKDIR /app

COPY gradlew .
COPY gradle gradle
COPY build.gradle settings.gradle ./
COPY gateway/build.gradle gateway/build.gradle
COPY submission-service/build.gradle submission-service/build.gradle
COPY file-storing-service/build.gradle file-storing-service/build.gradle
COPY analysis-service/build.gradle analysis-service/build.gradle
COPY gateway/src gateway/src
COPY submission-service/src submission-service/src
COPY file-storing-service/src file-storing-service/src
COPY analysis-service/src analysis-service/src

RUN chmod +x gradlew \
    && ./gradlew :file-storing-service:bootJar -x test --no-daemon

FROM eclipse-temurin:17-jre
WORKDIR /app

RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/*

COPY --from=build /app/file-storing-service/build/libs/file-storing-service.jar app.jar

EXPOSE 8092

HEALTHCHECK --interval=10s --timeout=5s --retries=5 --start-period=40s \
  CMD curl -f http://localhost:8092/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
