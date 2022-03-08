FROM maven:openjdk

WORKDIR /app

COPY ./ ./

RUN mvn clean package

CMD ["java", "-jar", "target/twitter-producer-1.0-SNAPSHOT-jar-with-dependencies.jar"]