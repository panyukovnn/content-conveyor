FROM amazoncorretto:17.0.7-alpine AS build_image

COPY . ./

# Аргумент сборки с токеном для чтения пакетов в github
ARG GH_TOKEN

# Прокидываем токен в gradle.properties
RUN mkdir -p ~/.gradle && \
    echo "githubPackagesReadToken=${GH_TOKEN}" >> ~/.gradle/gradle.properties

RUN ./gradlew build

# ---

FROM amazoncorretto:17.0.7-alpine

COPY --from=build_image ./build/libs/content-conveyor.jar /content-conveyor.jar

EXPOSE 8008
ENV TZ=Europe/Moscow

ENTRYPOINT ["java", "-jar", "/content-conveyor.jar"]