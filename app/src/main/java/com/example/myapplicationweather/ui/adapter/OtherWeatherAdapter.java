package com.example.myapplicationweather.ui.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

import com.example.myapplicationweather.R;
import com.example.myapplicationweather.model.dataClass.WeatherData;

import org.json.JSONException;
import org.json.JSONObject;

public class OtherWeatherAdapter extends RecyclerView.Adapter<OtherWeatherAdapter.WeatherViewHolder> {

    private List<WeatherData> weatherDataList = new ArrayList<>();

    // ViewHolder类
    static class WeatherViewHolder extends RecyclerView.ViewHolder {
        TextView cityNameTextView;
        TextView temperatureTextView;
        TextView weatherTextView;

        public WeatherViewHolder(@NonNull View itemView) {
            super(itemView);
            cityNameTextView = itemView.findViewById(R.id.city_name);
            temperatureTextView = itemView.findViewById(R.id.temperature);
            weatherTextView = itemView.findViewById(R.id.weather);
        }
    }

    public void setData(List<WeatherData> data) {
        this.weatherDataList = data;
        Log.d("OtherCityAdapter", "setData收到数据大小：" + data.size());
        notifyDataSetChanged(); // 刷新 RecyclerView
    }

    @NonNull
    @Override
    public WeatherViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.other_weather_item, parent, false);
        return new WeatherViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WeatherViewHolder holder, int position) {
        WeatherData weatherData = weatherDataList.get(position);
        holder.cityNameTextView.setText(weatherData.getCityName());
        holder.temperatureTextView.setText(weatherData.getTemperature());
        holder.weatherTextView.setText(weatherData.getWeather());

        // 长按监听器：显示对应的 LifeData 数据
        holder.itemView.setOnLongClickListener(v -> {
            showSuggestionDialog(v.getContext(), weatherData.getLifeDataJson());
            return true;
        });
    }

    @Override
    public int getItemCount() {
        Log.d("OtherCityAdapter", "getItemCount: " + weatherDataList.size());
        return weatherDataList.size();
    }

    // 显示生活指数弹窗
    private void showSuggestionDialog(Context context, String lifeDataJson) {
        try {
            JSONObject rootObject = new JSONObject(lifeDataJson);
            JSONObject suggestion = rootObject.getJSONArray("results")
                    .getJSONObject(0)
                    .getJSONObject("suggestion");

            StringBuilder suggestionDetails = new StringBuilder();
            appendSuggestion(suggestionDetails, suggestion, "car_washing", "洗车指数");
            appendSuggestion(suggestionDetails, suggestion, "dressing", "穿衣指数");
            appendSuggestion(suggestionDetails, suggestion, "flu", "感冒指数");
            appendSuggestion(suggestionDetails, suggestion, "sport", "运动指数");
            appendSuggestion(suggestionDetails, suggestion, "travel", "旅游指数");
            appendSuggestion(suggestionDetails, suggestion, "uv", "紫外线指数");

            new AlertDialog.Builder(context)
                    .setTitle("生活指数")
                    .setMessage(suggestionDetails.toString())
                    .setPositiveButton("确定", null)
                    .show();

        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(context, "生活指数解析失败", Toast.LENGTH_SHORT).show();
        }
    }

    private void appendSuggestion(StringBuilder builder, JSONObject suggestion, String key, String displayName) {
        try {
            JSONObject detail = suggestion.getJSONObject(key);
            String brief = detail.optString("brief", "无");
            String details = detail.optString("details", "无");

            builder.append(displayName)
                    .append(": ")
                    .append(brief)
                    .append("\n")
                    .append(details)
                    .append("\n\n");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}


