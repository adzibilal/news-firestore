package com.example.newsadzifirestore;

public class NewsItem {

    public String documentId;
    public String title;
    public String desc;
    public String imageUrl;

    public NewsItem(String documentId, String title, String desc, String imageUrl) {
        this.documentId = documentId;
        this.title = title;
        this.desc = desc;
        this.imageUrl = imageUrl;
    }
}
