# Start with a base image containing Java runtime
FROM adoptopenjdk/openjdk11:alpine-jre

# Add a volume pointing to /tmp
VOLUME /tmp

# Make port available to the world outside this container
EXPOSE 8080

# The jar file
ARG JAR_FILE=target/api-gateway-0.0.1-SNAPSHOT.jar

# Add the jar to the container
ADD ${JAR_FILE} api-gateway.jar

# Run the jar file
ENTRYPOINT ["java","-jar","/api-gateway.jar"]