FROM maven:3.6.0-jdk-11-slim AS build
VOLUME /tmp
COPY src /home/app/src
COPY target /home/app/target
COPY pom.xml /home/app
RUN mvn -f /home/app/pom.xml clean package

ENTRYPOINT ["java","-jar","/home/app/target/pad-0.0.1-SNAPSHOT.jar"]