FROM amazoncorretto:17.0.7-alpine AS build_image

# Аргумент сборки с токеном для чтения пакетов в github
ARG GH_TOKEN

# Прокидываем токен в gradle.properties
RUN mkdir -p /home/gradle/.gradle && \
    echo "githubPackagesReadToken=${GH_TOKEN}" >> /home/gradle/.gradle/gradle.properties

COPY . ./

RUN ./gradlew build

# ---

FROM amazoncorretto:17.0.7-alpine

COPY --from=build_image ./build/libs/content-conveyor.jar /content-conveyor.jar

EXPOSE 8008
ENV TZ=Europe/Moscow

ENTRYPOINT ["java", "-jar", "/content-conveyor.jar"]