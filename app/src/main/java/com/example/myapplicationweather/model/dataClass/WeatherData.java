package com.example.myapplicationweather.model.dataClass;

public class WeatherData {
    private String cityName;       // 城市名称
    private String temperature;    // 温度
    private String weather;        // 天气描述
    private String lifeDataJson;   // 生活指数 JSON 数据

    public WeatherData(String cityName, String temperature, String weather, String lifeDataJson) {
        this.cityName = cityName;
        this.temperature = temperature;
        this.weather = weather;
        this.lifeDataJson = lifeDataJson;
    }

    public String getCityName() { return cityName; }
    public String getTemperature() { return temperature; }
    public String getWeather() { return weather; }
    public String getLifeDataJson() { return lifeDataJson; }
}




