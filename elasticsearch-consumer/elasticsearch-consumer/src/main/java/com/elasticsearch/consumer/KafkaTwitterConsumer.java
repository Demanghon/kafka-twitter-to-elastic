package com.elasticsearch.consumer;


import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.config.SslConfigs;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KafkaTwitterConsumer {
    
    public static KafkaTwitterConsumer instance;

    private static Logger logger = LoggerFactory.getLogger(KafkaTwitterConsumer.class);

    public static KafkaTwitterConsumer getInstance(){
        if(instance == null)
            instance = new KafkaTwitterConsumer();
        return instance;
    }

    private KafkaConsumer<Long, String> consumer;

    private KafkaTwitterConsumer(){
        String sslTruststoreCertificates = System.getenv("KAFKA_CA_CRT");
        if(sslTruststoreCertificates == null) throw new IllegalArgumentException("the KAFKA_CA_CRT env variable is not set");
        String bootstrapServer = System.getenv("BOOTSTRAP_SERVERS");
        if(bootstrapServer == null) throw new IllegalArgumentException("the BOOTSTRAP_SERVERS env variable is not set");
        String topic = System.getenv("TOPIC");
        if(topic == null) throw new IllegalArgumentException("the TOPIC env variable is not set");

        // create consumers properties
        Properties properties = new Properties();
        properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServer);
        properties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, LongDeserializer.class.getName());
        properties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SSL");
        properties.put(SslConfigs.SSL_TRUSTSTORE_TYPE_CONFIG, "PEM");
        properties.put(SslConfigs.SSL_TRUSTSTORE_CERTIFICATES_CONFIG, sslTruststoreCertificates);
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, "elasticsearch-consumer");
        properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        properties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
        properties.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, "20");


        consumer = new KafkaConsumer<Long, String>(properties);

        consumer.subscribe(Collections.singleton(topic));
    }

    public void consume(ConsumerCallback callback){
        //poll for new data
        while(true){
            ConsumerRecords<Long, String> records = consumer.poll(Duration.ofMillis(100));

            for(ConsumerRecord<Long, String> record:records){
                try{
                    callback.onDataReceived(record.key(), record.value());
                    consumer.commitSync();
                    logger.info("commit data");
                }catch(IllegalStateException e){
                    logger.warn(String.format("failed to process tweet %d", record.key()));
                }

            }

        }
    }

    public void close(){
        consumer.close();
    }
    

}
