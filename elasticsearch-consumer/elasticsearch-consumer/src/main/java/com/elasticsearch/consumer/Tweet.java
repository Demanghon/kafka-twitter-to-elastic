package com.elasticsearch.consumer;

import java.util.Date;

/***
 * This class represents a Tweet
 */
public class Tweet {

    private Long id;
    private String text;
    private Date createdAt;

    public Tweet(Long id, String text){
        this.id = id;
        this.text = text;
        this.createdAt = new Date();
    }

    public Long getId(){
        return this.id;
    }
    
    public String getText(){
        return this.text;
    }

    public Date getCreatedAt(){
        return this.createdAt;
    }
}

