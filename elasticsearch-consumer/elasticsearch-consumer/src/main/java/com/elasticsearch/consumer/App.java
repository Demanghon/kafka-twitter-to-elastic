package com.elasticsearch.consumer;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import co.elastic.clients.elasticsearch._types.ElasticsearchException;

/**
 * Consume data from twitter topic and send them to ElasticSearch
 */
public final class App {
    private App() {
    }

    /**
     * Configure consumer and ElasticSearch output
     * 
     * @param args The arguments of the program.
     * @throws IOException
     * @throws ElasticsearchException
     * @throws KeyStoreException
     * @throws NoSuchAlgorithmException
     * @throws CertificateException
     * @throws KeyManagementException
     */
    public static void main(String[] args) throws ElasticsearchException, IOException, KeyManagementException, CertificateException, NoSuchAlgorithmException, KeyStoreException {
        ElasticSearchTwitterClient esClient = new ElasticSearchTwitterClient();
        KafkaTwitterConsumer.getInstance().consume(new ConsumerCallback() {

            @Override
            public void onDataReceived(Long id, String text) throws IllegalStateException {
                try {
                    esClient.pushTweet(id, text);
                } catch (ElasticsearchException | IOException e) {
                    throw new IllegalStateException(e);
                }
            }

        });
    }
}
