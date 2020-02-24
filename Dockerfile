FROM openjdk:11-jre
COPY target/zeebe-http-worker.jar app.jar
RUN mkdir -p /newrelic
ADD https://download.newrelic.com/newrelic/java-agent/newrelic-agent/current/newrelic.jar /newrelic/newrelic.jar
ADD https://download.newrelic.com/newrelic/java-agent/newrelic-agent/current/newrelic.yml /newrelic/newrelic.yml
RUN chmod 644 /newrelic/newrelic.jar
EXPOSE 8080
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar"]
