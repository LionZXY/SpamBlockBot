FROM openjdk:8-jdk-alpine3.9 AS builder
WORKDIR /build/
COPY . .
RUN ./gradlew jar

FROM openjdk:8-jre-alpine3.9
WORKDIR /app/
COPY --from=builder  /build/lionbot/build/libs/lionbot.jar .
CMD java -jar lionbot.jar
