package com.example.myapplicationweather.model.dataClass;

import java.util.List;

public class CityWeatherData {
    private String cityName;
    private List<String> dates;
    private List<Integer> highTemps;
    private List<Integer> lowTemps;

    public CityWeatherData(String cityName, List<String> dates, List<Integer> highTemps, List<Integer> lowTemps) {
        this.cityName = cityName;
        this.dates = dates;
        this.highTemps = highTemps;
        this.lowTemps = lowTemps;
    }

    public String getCityName() {
        return cityName;
    }

    public List<String> getDates() {
        return dates;
    }

    public List<Integer> getHighTemps() {
        return highTemps;
    }

    public List<Integer> getLowTemps() {
        return lowTemps;
    }
}

