package com.twitter.producer;

public interface StreamCallback {

    void onTweetReceived(Tweet tweet);
    
}
