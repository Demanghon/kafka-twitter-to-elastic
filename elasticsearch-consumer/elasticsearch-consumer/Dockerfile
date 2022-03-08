FROM maven:openjdk

WORKDIR /app

COPY ./ ./

RUN mvn clean package

CMD ["java", "-jar", "target/elasticsearch-consumer-1.0-SNAPSHOT-jar-with-dependencies.jar"]