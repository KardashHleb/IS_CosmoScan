# syntax=docker/dockerfile:1

FROM eclipse-temurin:17-jdk AS jacoco
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
COPY web-ui/reports/index.html web-ui/reports/index.html

RUN chmod +x gradlew \
    && ./gradlew check copyJacocoReportsForWeb --no-daemon

FROM nginx:1.27-alpine

COPY web-ui/nginx.conf /etc/nginx/conf.d/default.conf
COPY web-ui/index.html /usr/share/nginx/html/
COPY web-ui/assets /usr/share/nginx/html/assets/
COPY --from=jacoco /app/web-ui/reports /usr/share/nginx/html/reports/

EXPOSE 80
