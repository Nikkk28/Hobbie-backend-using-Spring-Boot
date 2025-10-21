FROM openjdk:17-jdk-alpine

WORKDIR /app

COPY target/hobbiebackend-0.0.1-SNAPSHOT.jar hobbiebackend.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "hobbiebackend.jar"]
