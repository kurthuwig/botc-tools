FROM eclipse-temurin:24-jre-alpine

RUN echo 'Europe/Berlin' > /etc/timezone
WORKDIR /opt
CMD java -jar server-all.jar

COPY files/server-all.jar /opt/server-all.jar