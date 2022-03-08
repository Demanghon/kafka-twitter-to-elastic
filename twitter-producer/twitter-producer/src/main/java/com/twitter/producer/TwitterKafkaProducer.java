package com.twitter.producer;

import java.util.Properties;

import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.config.SslConfigs;
import org.apache.kafka.common.serialization.LongSerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TwitterKafkaProducer {

    final Logger logger = LoggerFactory.getLogger(TwitterKafkaProducer.class);

    private static TwitterKafkaProducer instance;

    private KafkaProducer<Long, String> producer;
    private String topic;


    public static TwitterKafkaProducer getInstance(){
        if(instance == null){
            instance = new TwitterKafkaProducer();
        }
        return instance;
    }

    private TwitterKafkaProducer(){
        String sslTruststoreCertificates = System.getenv("CA_CRT");
        if(sslTruststoreCertificates == null) throw new IllegalArgumentException("the CA_CRT env variable is not set");
        String bootstrapServer = System.getenv("BOOTSTRAP_SERVERS");
        if(bootstrapServer == null) throw new IllegalArgumentException("the BOOTSTRAP_SERVERS env variable is not set");
        topic = System.getenv("TOPIC");
        if(topic == null) throw new IllegalArgumentException("the TOPIC env variable is not set");

        Properties properties = new Properties();
        properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,  bootstrapServer);
        properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, LongSerializer.class.getName());
        properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SSL");
        properties.put(SslConfigs.SSL_TRUSTSTORE_TYPE_CONFIG, "PEM");
        properties.put(SslConfigs.SSL_TRUSTSTORE_CERTIFICATES_CONFIG, sslTruststoreCertificates);
        //safe producer
        properties.setProperty(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, "true");
        properties.setProperty(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, "1");
        properties.setProperty(ProducerConfig.RETRIES_CONFIG, Integer.toString(Integer.MAX_VALUE));
        properties.setProperty(ProducerConfig.ACKS_CONFIG, "all");
        //hihgh troughput settings
        //more information on this article: https://blog.cloudflare.com/squeezing-the-firehose/
        properties.setProperty(ProducerConfig.COMPRESSION_TYPE_CONFIG, "snappy");
        properties.setProperty(ProducerConfig.LINGER_MS_CONFIG, "20");
        properties.setProperty(ProducerConfig.BATCH_SIZE_CONFIG, Integer.toString(32*1024));

        // create the producer
        producer = new KafkaProducer<Long, String>(properties);

    }

    public void sendTweet(Tweet tweet){
        ProducerRecord<Long, String> record = new ProducerRecord<Long, String>(topic, tweet.getId(), tweet.getText());
        producer.send(record);
    }

    public void close(){
        producer.flush();
        producer.close();
    }
    
}
