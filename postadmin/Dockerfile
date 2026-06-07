FROM maven:3.9.9-eclipse-temurin-17 AS build

WORKDIR /workspace

COPY pom.xml ./
COPY postapi ./postapi
COPY postcryption ./postcryption
COPY postadmin ./postadmin

RUN mvn -q -DskipTests clean package

FROM bellsoft/liberica-openjdk-debian:17.0.14

WORKDIR /app

ENV SPRING_DATASOURCE_URL=jdbc:h2:file:/app/data/postadmin;MODE=MySQL;DATABASE_TO_UPPER=false
ENV JAVA_OPTS=""

COPY --from=build /workspace/postadmin/target/postadmin-0.0.1-SNAPSHOT.jar /app/app.jar

RUN mkdir -p /app/data

VOLUME ["/app/data"]
EXPOSE 8088

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
