FROM bellsoft/liberica-openjdk-debian:17.0.14
WORKDIR /app

ENV SPRING_DATASOURCE_URL=jdbc:h2:file:/app/data/postadmin;MODE=MySQL;DATABASE_TO_UPPER=false
ENV JAVA_OPTS=""

ARG JAR_FILE=postadmin/target/postadmin-0.0.1-SNAPSHOT.jar
COPY ${JAR_FILE} /app/app.jar

RUN mkdir -p /app/data

VOLUME ["/app/data"]
EXPOSE 8088

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
