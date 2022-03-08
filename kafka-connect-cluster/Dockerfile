FROM quay.io/strimzi/kafka:0.28.0-kafka-3.1.0
USER root:root
#install twitter connector
RUN mkdir -p /opt/kafka/plugins/kafka-connect-twitter
RUN curl -L https://github.com/jcustenborder/kafka-connect-twitter/releases/download/0.2.26/kafka-connect-twitter-0.2.26.tar.gz \
| tar xvz -C  /opt/kafka/plugins/kafka-connect-twitter
#install elasticsearch connector
RUN mkdir -p /opt/kafka/plugins/camel-elasticsearch-rest-kafka-connector
RUN curl https://repo.maven.apache.org/maven2/org/apache/camel/kafkaconnector/camel-elasticsearch-rest-kafka-connector/0.11.5/camel-elasticsearch-rest-kafka-connector-0.11.5-package.tar.gz \
| tar xvz -C  /opt/kafka/plugins/camel-elasticsearch-rest-kafka-connector
USER 1001