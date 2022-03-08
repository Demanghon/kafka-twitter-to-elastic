package com.elasticsearch.consumer;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.Arrays;

import javax.net.ssl.SSLContext;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.SSLContexts;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder.HttpClientConfigCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import co.elastic.clients.elasticsearch.core.IndexRequest;
import co.elastic.clients.elasticsearch.indices.Alias;
import co.elastic.clients.elasticsearch.indices.CreateIndexRequest;
import co.elastic.clients.elasticsearch.indices.CreateIndexResponse;
import co.elastic.clients.elasticsearch.indices.ExistsRequest;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;

public class ElasticSearchTwitterClient {

    private ElasticsearchClient client;
    private final static String INDEX = "twitter";

    Logger logger = LoggerFactory.getLogger(ElasticSearchTwitterClient.class);

    public ElasticSearchTwitterClient() throws ElasticsearchException, IOException, CertificateException,
            NoSuchAlgorithmException, KeyStoreException, KeyManagementException {

        String elasticHost = System.getenv("ES_HOST");
        if (elasticHost == null)
            throw new IllegalArgumentException("the ES_HOST env variable is not set");
        Integer elasticPort = Integer.valueOf(System.getenv("ES_PORT"));
        if (elasticPort == null)
            throw new IllegalArgumentException("the ES_PORT env variable is not set");
        String password = System.getenv("ES_PASSWORD");
        if (password == null)
            throw new IllegalArgumentException("the ES_PASSWORD env variable is not set");
        String caCert = System.getenv("ES_CA_CRT");
        if (caCert == null)
            throw new IllegalArgumentException("the ES_CA_CRT env variable is not set");

        final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials("elastic", password));

        CertificateFactory factory = CertificateFactory.getInstance("X.509");
        Certificate trustedCa = factory.generateCertificate(new ByteArrayInputStream(caCert.getBytes()));
        KeyStore trustStore = KeyStore.getInstance("pkcs12");
        trustStore.load(null, null);
        trustStore.setCertificateEntry("ca", trustedCa);
        SSLContextBuilder sslContextBuilder = SSLContexts.custom()
                .loadTrustMaterial(trustStore, null);
        final SSLContext sslContext = sslContextBuilder.build();

        // Create the low-level client
        RestClient restClient = RestClient.builder(
                new HttpHost(elasticHost, elasticPort, "https"))
                .setHttpClientConfigCallback(new HttpClientConfigCallback() {
                    @Override
                    public HttpAsyncClientBuilder customizeHttpClient(
                            HttpAsyncClientBuilder httpClientBuilder) {
                        return httpClientBuilder
                                .setDefaultCredentialsProvider(credentialsProvider)
                                .setSSLContext(sslContext)
                                .setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE);
                    }
                })
                .build();

        // Create the transport with a Jackson mapper
        ElasticsearchTransport transport = new RestClientTransport(
                restClient, new JacksonJsonpMapper());

        // And create the API client
        client = new ElasticsearchClient(transport);

        createIndex();

    }

    private void createIndex() throws ElasticsearchException, IOException {
        boolean indiceExsit = client.indices().exists(new ExistsRequest.Builder().index(Arrays.asList(INDEX)).build())
                .value();
        if (!indiceExsit) {
            CreateIndexResponse createResponse = client.indices().create(
                    new CreateIndexRequest.Builder()
                            .index(INDEX)
                            .aliases(INDEX+"_alias",
                                    new Alias.Builder().isWriteIndex(true).build())
                            .build());
            if (!createResponse.acknowledged()) {
                throw new IllegalStateException("impossible to create index " + INDEX);
            }
        }
    }

    public void pushTweet(Long id, String text) throws ElasticsearchException, IOException {
        client.index(new IndexRequest.Builder<Tweet>()
            .id(Long.toString(id))
            .index(INDEX)
            .document(new Tweet(id, text))
            .build());
    }

}
