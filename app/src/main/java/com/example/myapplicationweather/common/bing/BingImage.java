package com.example.myapplicationweather.common.bing;

public class BingImage {
    public String url;
    public String copyright;

    public String getFullUrl() {
        return "https://www.bing.com" + url;
    }
}
