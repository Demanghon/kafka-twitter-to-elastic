package com.twitter.producer;

/***
 * This class represents a Tweet
 */
public class Tweet {

    private Long id;
    private String text;

    public Tweet(Long id, String text){
        this.id = id;
        this.text = text;
    }

    public Long getId(){
        return this.id;
    }
    
    public String getText(){
        return this.text;
    }
}
