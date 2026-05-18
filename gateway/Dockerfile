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
    && ./gradlew :gateway:bootJar -x test --no-daemon

FROM eclipse-temurin:17-jre
WORKDIR /app

COPY --from=build /app/gateway/build/libs/gateway.jar app.jar

EXPOSE 8090

ENTRYPOINT ["java", "-jar", "app.jar"]
