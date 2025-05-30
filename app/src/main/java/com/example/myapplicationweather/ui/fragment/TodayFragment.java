package com.example.myapplicationweather.ui.fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.example.myapplicationweather.ui.activities.LoginActivity;
import com.example.myapplicationweather.R;
import com.example.myapplicationweather.controller.DBHelper;
import com.example.myapplicationweather.model.dataClass.WeatherData;
import com.example.myapplicationweather.ui.adapter.WeatherAdapter;
import com.example.myapplicationweather.ui.adapter.OtherWeatherAdapter;

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

public class TodayFragment extends Fragment {

    private RecyclerView recyclerView;
    private WeatherAdapter weatherAdapter;
    private ImageView cityPhotoImageView;
    private TextView cityNameTextView;
    private TextView temperatureTextView;
    private OtherWeatherAdapter otherCitiesAdapter;
    private final List<WeatherData> weatherDataList = new ArrayList<>(); // 添加变量声明

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_today, container, false);

        // cardView
        cityPhotoImageView = view.findViewById(R.id.cityPhotoImageView);
        cityNameTextView = view.findViewById(R.id.cityNameTextView);
        temperatureTextView = view.findViewById(R.id.temperatureTextView);

        //RecyclerList：weatherDetailsRecyclerView
        recyclerView = view.findViewById(R.id.weatherDetailsRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        weatherAdapter = new WeatherAdapter();//配置weatherDetailsRecyclerView的适配器
        recyclerView.setAdapter(weatherAdapter);

        // 初始化其它感兴趣城市的标题和 RecyclerView：otherCitiesRecyclerView
        RecyclerView otherCitiesRecyclerView = view.findViewById(R.id.otherCitiesRecyclerView);
        otherCitiesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        otherCitiesAdapter = new OtherWeatherAdapter();//配置otherCitiesRecyclerView的适配器
        otherCitiesRecyclerView.setAdapter(otherCitiesAdapter);

        updateCityPhoto();//更新cardView图片
        fetchWeatherData();//更新默认城市的天气数据
        fetchOtherCityWeatherData(otherCitiesRecyclerView,otherCitiesAdapter);//更新其他感兴趣城市的天气数据
        return view;
    }
    //无法更新UI
    private void runOnUiThreadSafely(Runnable action) {
        if (getActivity() != null && !getActivity().isFinishing()) {
            getActivity().runOnUiThread(action);
        } else {
            Log.w("UIUpdate", "无法更新UI，Activity不可用");
        }
    }
    //更新cardView图片
    private void updateCityPhoto() {
        DBHelper dbHelper = new DBHelper(getContext());
        // 第一步：获取用户选择的完整城市 (省-市-区格式)
        String selectedCity = dbHelper.getSelectedCity(LoginActivity.getCurrentUsername(getContext()));
        if (selectedCity != null) {
            // 第二步：解析出城市名称（市）
            String[] cityParts = selectedCity.split("-"); // 分隔省-市-区
            String cityName = cityParts.length > 1 ? cityParts[1] : null; // 获取市的名称
            if (cityName != null) {
                // 第三步：根据城市名称获取对应的图片路径
                String imagePath = dbHelper.getCityImage(cityName.toLowerCase());
                if (imagePath != null) {
                    // 第四步：使用 Glide 加载图片到 ImageView
                    int resId = getResources().getIdentifier(imagePath, null, getContext().getPackageName());
                    Glide.with(this)
                            .load(resId)
                            .into(cityPhotoImageView);
                } else {
                    // 如果没有找到图片路径，加载默认图片
                    Glide.with(this)
                            .load(R.drawable.hangzhou_img) // 默认图片
                            .into(cityPhotoImageView);
                }
            } else {
                // 如果解析失败，加载默认图片
                Glide.with(this)
                        .load(R.drawable.hangzhou_img)
                        .into(cityPhotoImageView);
            }
        } else {
            // 如果用户没有选择城市，加载默认图片
            Glide.with(this)
                    .load(R.drawable.hangzhou_img)
                    .into(cityPhotoImageView);
        }
    }
    //更新默认城市的天气数据
    private void fetchWeatherData() {
        // 获取用户选择的城市
        DBHelper dbHelper = new DBHelper(getContext());
        // 第一步：获取用户选择的完整城市 (省-市-区格式)
        String selectedCity = dbHelper.getSelectedCity(LoginActivity.getCurrentUsername(getContext()));

        // 如果没有选择城市，使用默认城市
        if (selectedCity == null) {
            selectedCity = "beijing"; // 默认城市
        }
        // 第二步：解析出城市名称（市）
        if (selectedCity != null) {
            String[] cityParts = selectedCity.split("-"); // 分隔省-市-区
            String cityName = cityParts.length > 1 ? cityParts[1] : null; // 获取市的名称

            // 动态拼接 API 请求 URL
            String url = "https://api.seniverse.com/v3/weather/now.json?key=St1laUfb95SdtioT8&location="
                    + cityName + "&language=zh-Hans&unit=c";

            OkHttpClient client = new OkHttpClient();//创建 OkHttpClient 对象
            Request request = new Request.Builder().url(url).build();//创建 Request 对象

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                    Log.e("WeatherError", "API 请求失败: " + e.getMessage());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful() && response.body() != null) {
                        String responseData = response.body().string();
                        Log.d("WeatherData", "API 响应: " + responseData);

                        try {
                            JSONObject jsonObject = new JSONObject(responseData);//创建 Json 对象
                            JSONArray resultsArray = jsonObject.getJSONArray("results");
                            JSONObject resultObject = resultsArray.getJSONObject(0);
                            JSONObject locationObject = resultObject.getJSONObject("location");
                            String city = locationObject.getString("name");
                            JSONObject nowObject = resultObject.getJSONObject("now");
                            String weather = nowObject.getString("text");
                            String temperature = nowObject.getString("temperature");

                            if (getActivity() != null) {
                                runOnUiThreadSafely(() -> {
                                    cityNameTextView.setText(city);
                                    temperatureTextView.setText(temperature + "°C");

                                    List<String> data = new ArrayList<>();
                                    data.add("当前天气现象: " + weather);
                                    data.add("当前气温: " + temperature + "度");
                                    weatherAdapter.setData(data);
                                });
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Log.e("WeatherError", "JSON 解析失败: " + e.getMessage());
                        }
                    } else {
                        Log.e("WeatherError", "API 请求失败: " + response.message());
                    }
                }
            });
        }

    }

    //更新其他感兴趣城市的天气数据
    private void fetchOtherCityWeatherData(RecyclerView otherCitiesRecyclerView, OtherWeatherAdapter otherCitiesAdapter) {
        // 从数据库获取用户感兴趣的城市
        DBHelper dbHelper = new DBHelper(getContext());
        // 从数据库获取当前用户感兴趣的城市列表（存储为省-市-区格式）
        List<String> interestedCities = dbHelper.getInterestedCities(LoginActivity.getCurrentUsername(getContext()));
        if (interestedCities.isEmpty()) {
            Log.d("WeatherInfo", "没有用户感兴趣的城市");
            return;
        }

        OkHttpClient client = new OkHttpClient();

        for (String fullCityName : interestedCities) {
            // 解析市级名称
            String cityName = parseCityName(fullCityName);

            String url = "https://api.seniverse.com/v3/weather/now.json?key=St1laUfb95SdtioT8&location="
                    + cityName + "&language=zh-Hans&unit=c";

            Request request = new Request.Builder().url(url).build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                    Log.e("WeatherError", "API 请求失败: " + e.getMessage() + " 城市: " + fullCityName);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful() && response.body() != null) {
                        String responseData = response.body().string();
                        Log.d("WeatherData", "API 响应 (" + fullCityName + "): " + responseData);

                        try {
                            JSONObject jsonObject = new JSONObject(responseData);
                            JSONArray resultsArray = jsonObject.getJSONArray("results");
                            JSONObject resultObject = resultsArray.getJSONObject(0);
                            JSONObject locationObject = resultObject.getJSONObject("location");
                            String city = locationObject.getString("name");
                            JSONObject nowObject = resultObject.getJSONObject("now");
                            String weather = nowObject.getString("text");
                            String temperature = nowObject.getString("temperature");

                            // 请求生活指数数据
                            String lifeUrl = "https://api.seniverse.com/v3/life/suggestion.json?key=St1laUfb95SdtioT8&location="
                                    + city + "&language=zh-Hans";

                            Request lifeRequest = new Request.Builder().url(lifeUrl).build();
                            client.newCall(lifeRequest).enqueue(new Callback() {
                                @Override
                                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                                    Log.e("LifeInfo", "生活指数请求失败：" + e.getMessage());
                                }

                                @Override
                                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                                    if (response.isSuccessful() && response.body() != null) {
                                        String lifeDataJson = response.body().string();
                                        Log.d("WeatherData", "API 响应 (" + city + "): " + lifeDataJson);
                                        // 更新 UI
                                        synchronized (weatherDataList) {
                                            weatherDataList.add(new WeatherData(city, temperature + "°C", weather,lifeDataJson));
                                            Log.d("WeatherData", "已添加城市数据：" + city + "，当前列表大小：" + weatherDataList.size());
                                        }

                                        if (weatherDataList.size() == interestedCities.size()) {
                                            if (getActivity() != null) {
                                                getActivity().runOnUiThread(() -> {
                                                    otherCitiesAdapter.setData(weatherDataList);
                                                    otherCitiesRecyclerView.setVisibility(View.VISIBLE);
                                                });
                                            }
                                        }
                                    }
                                }
                            });
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
    /**
     * 从完整的三级联动城市名中解析市级名称
     * 如果格式是省-市-区，返回市；如果是单城市名，直接返回。
     */
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

