package com.example.myapplicationweather.ui.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplicationweather.ui.activities.LoginActivity;
import com.example.myapplicationweather.R;
import com.example.myapplicationweather.ui.adapter.CityWeatherAdapter;
import com.example.myapplicationweather.controller.DBHelper;
import com.example.myapplicationweather.model.dataClass.CityWeatherData;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class HistoryFragment extends Fragment {

    private RecyclerView recyclerView;
    private List<CityWeatherData> weatherDataList = new ArrayList<>();
    private CityWeatherAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_history, container, false);

        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new CityWeatherAdapter(weatherDataList);//配置recycler_view的适配器
        recyclerView.setAdapter(adapter);

        fetchHistoryWeatherData();//更新历史浏览记录
        return view;
    }
    //更新历史浏览记录
    private void fetchHistoryWeatherData() {
        DBHelper dbHelper = new DBHelper(getContext());

        // 获取当前用户感兴趣的城市列表（存储为省-市-区格式）
        List<String> interestedCities = dbHelper.getInterestedCities(LoginActivity.getCurrentUsername(getContext()));
        if (interestedCities.isEmpty()) {
            Log.d("WeatherInfo", "没有用户感兴趣的城市");
            return;
        }

        // 从数据库获取按 last_viewed_time 排序的历史记录
        int historyCityLimit = dbHelper.getHistoryLimit(LoginActivity.getCurrentUsername(getContext())); // 获取用户设置的历史记录限制
        List<String> latestCities = dbHelper.getLatestHistoryCities(LoginActivity.getCurrentUsername(getContext()), historyCityLimit);

        if (latestCities.isEmpty()) {
            Log.d("WeatherInfo", "没有浏览记录");
            return;
        }

        // 用于存储所有城市的天气数据
        List<CityWeatherData> weatherDataList = new ArrayList<>();
        OkHttpClient client = new OkHttpClient();

        // 遍历裁剪后的城市列表并请求天气数据
        for (String fullCityName : latestCities ) {
            // 解析市级名称用于 API 请求
            String cityName = parseCityName(fullCityName);

            String url = "https://api.seniverse.com/v3/weather/daily.json?key=St1laUfb95SdtioT8&location="
                    + cityName + "&language=zh-Hans&unit=c&start=0&days=5";

            Request request = new Request.Builder().url(url).build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    e.printStackTrace();
                    Log.e("WeatherError", "API 请求失败: " + e.getMessage() + " 城市: " + fullCityName);
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    if (response.isSuccessful() && response.body() != null) {
                        try {
                            String responseData = response.body().string();
                            JSONObject root = new JSONObject(responseData);
                            JSONArray dailyArray = root.getJSONArray("results")
                                    .getJSONObject(0)
                                    .getJSONArray("daily");

                            List<String> dates = new ArrayList<>();
                            List<Integer> highTemps = new ArrayList<>();
                            List<Integer> lowTemps = new ArrayList<>();

                            for (int i = 0; i < dailyArray.length(); i++) {
                                JSONObject day = dailyArray.getJSONObject(i);
                                dates.add(day.optString("date", "未知日期"));
                                highTemps.add(day.optInt("high", 0));
                                lowTemps.add(day.optInt("low", 0));
                            }

                            CityWeatherData cityWeatherData = new CityWeatherData(cityName, dates, highTemps, lowTemps);
                            synchronized (weatherDataList) {
                                weatherDataList.add(cityWeatherData);
                            }

                            if (weatherDataList.size() == latestCities.size()) {
                                requireActivity().runOnUiThread(() -> {
                                    adapter.setData(weatherDataList); // 假设适配器有 setData 方法
                                    adapter.notifyDataSetChanged();
                                });
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                            Log.e("WeatherError", "JSON 解析失败: " + e.getMessage() + " 城市: " + fullCityName);
                        }
                    } else {
                        Log.e("WeatherError", "API 请求失败: " + response.message() + " 城市: " + fullCityName);
                    }
                }
            });
        }
    }

    private String parseCityName(String fullCityName) {
        if (fullCityName.contains("-")) {
            String[] parts = fullCityName.split("-");
            if (parts.length > 1) {
                return parts[1]; // 返回市级名称
            }
        }
        return fullCityName; // 如果是单城市名，直接返回
    }
}
