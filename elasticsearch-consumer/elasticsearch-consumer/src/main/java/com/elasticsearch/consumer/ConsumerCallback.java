package com.elasticsearch.consumer;

public interface ConsumerCallback {
    
    void onDataReceived(Long id, String text) throws IllegalStateException;
}
