package com.example.myapplicationweather.ui.adapter;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class CityDataLoader {
    private Context context;
    private JSONObject cityData;

    public CityDataLoader(Context context) {
        this.context = context;
        try (InputStream is = context.getAssets().open("cities.json")) {
            byte[] buffer = new byte[is.available()];
            is.read(buffer);
            cityData = new JSONObject(new String(buffer, "UTF-8"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<String> getProvinces() {
        List<String> provinces = new ArrayList<>();
        try {
            JSONArray provinceArray = cityData.getJSONArray("provinces");
            for (int i = 0; i < provinceArray.length(); i++) {
                provinces.add(provinceArray.getJSONObject(i).getString("name"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return provinces;
    }

    public List<String> getCities(String provinceName) {
        List<String> cities = new ArrayList<>();
        try {
            JSONArray provinceArray = cityData.getJSONArray("provinces");
            for (int i = 0; i < provinceArray.length(); i++) {
                JSONObject province = provinceArray.getJSONObject(i);
                if (province.getString("name").equals(provinceName)) {
                    JSONArray cityArray = province.getJSONArray("cities");
                    for (int j = 0; j < cityArray.length(); j++) {
                        cities.add(cityArray.getJSONObject(j).getString("name"));
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return cities;
    }

    public List<String> getDistricts(String provinceName, String cityName) {
        List<String> districts = new ArrayList<>();
        try {
            JSONArray provinceArray = cityData.getJSONArray("provinces");
            for (int i = 0; i < provinceArray.length(); i++) {
                JSONObject province = provinceArray.getJSONObject(i);
                if (province.getString("name").equals(provinceName)) {
                    JSONArray cityArray = province.getJSONArray("cities");
                    for (int j = 0; j < cityArray.length(); j++) {
                        JSONObject city = cityArray.getJSONObject(j);
                        if (city.getString("name").equals(cityName)) {
                            JSONArray districtArray = city.getJSONArray("districts");
                            for (int k = 0; k < districtArray.length(); k++) {
                                districts.add(districtArray.getString(k));
                            }
                        }
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return districts;
    }
}

