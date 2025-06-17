FROM amazoncorretto:17.0.7-alpine AS BUILD_IMAGE

COPY . ./

RUN ./gradlew build

# ---

FROM amazoncorretto:17.0.7-alpine

COPY --from=BUILD_IMAGE ./build/libs/content-conveyor.jar /content-conveyor.jar

EXPOSE 8008
ENV TZ=Europe/Moscow

ENTRYPOINT ["java", "-jar", "/content-conveyor.jar"]