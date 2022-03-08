package com.twitter.producer;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

/**
 * Hello world!
 */
public final class App {
    private App() {
    }

    /**
     * Get Tweets froms twitter Stream API and push them to Kafka.
     * @param args The arguments of the program.
     * @throws URISyntaxException
     * @throws IOException
     */
    public static void main(String[] args) throws IOException, URISyntaxException {
        String bearerToken = System.getenv("TWITTER_BEARER");
        if(bearerToken == null){
            throw new IllegalArgumentException("the TWITTER_BEARER env variable is not set");
        }
        TwitterStream stream = new TwitterStream(bearerToken);
        Map<String, String> rules = new HashMap<>();
        rules.put("cats has:images", "cat images");
        rules.put("dogs has:images", "dog images");
        stream.setupRules(rules);
        stream.connectStream(new StreamCallback() {

            @Override
            public void onTweetReceived(Tweet tweet) {
                TwitterKafkaProducer.getInstance().sendTweet(tweet);
            }
            
        });
    }
}
