package com.example.newsapp;

import java.io.Serializable;

public class Card implements Serializable {
    public Card(){

    }
    String section;
    String time;
    String title;
    String articleId;
    String shareUrl;
    String image;
    public Card(String section,String time,String title,String articleId,String shareUrl,String image){
        this.section = section;
        this.time = time;
        this.title = title;
        this.articleId = articleId;
        this.shareUrl = shareUrl;
        this.image = image;
    }
}
