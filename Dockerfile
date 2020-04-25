FROM maven:3.6.3-jdk-11 AS BUILD_IMAGE
COPY src usr/local/src
COPY pom.xml usr/local
RUN mvn -f usr/local/pom.xml clean package -DskipTests

FROM openjdk:11-jre
ARG JAR=usr/local/target/zeebe-http-worker.jar
COPY --from=BUILD_IMAGE ${JAR} app.jar
RUN mkdir -p /newrelic
ADD https://download.newrelic.com/newrelic/java-agent/newrelic-agent/current/newrelic.jar /newrelic/newrelic.jar
ADD https://download.newrelic.com/newrelic/java-agent/newrelic-agent/current/newrelic.yml /newrelic/newrelic.yml
RUN chmod 644 /newrelic/newrelic.jar
EXPOSE 8080
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar"]
