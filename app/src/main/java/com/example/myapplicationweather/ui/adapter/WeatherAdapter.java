package com.example.myapplicationweather.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.example.myapplicationweather.R;

public class WeatherAdapter extends RecyclerView.Adapter<WeatherAdapter.WeatherViewHolder> {

    private List<String> data = new ArrayList<>();
    // 创建两个不同的Map来分别存储天气详情文字与对应图标资源id的映射关系
    private Map<String, Integer> weatherIconMap = new HashMap<>();
    private Map<String, Integer> temperatureIconMap = new HashMap<>();

    public WeatherAdapter() {
        // 初始化天气现象对应的图标映射关系，这里根据实际图标资源id来填写
        weatherIconMap.put("当前天气现象:", R.drawable.ic_weather);
        // 初始化气温对应的图标映射关系，比如可以设置一个通用的温度图标（示例中用ic_temperature表示，你替换成实际的图标资源id）
        temperatureIconMap.put("当前气温:", R.drawable.ic_temperature);
    }

    @NonNull
    @Override
    public WeatherViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.weather_item, parent, false);
        return new WeatherViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WeatherViewHolder holder, int position) {
        String detail = data.get(position);
        holder.weatherDetail.setText(detail);

        // 检查字段是否包含特定关键字
        if (detail.contains("当前天气现象:")) {
            holder.weatherIcon.setImageResource(R.drawable.ic_weather);
        } else {
            holder.weatherIcon.setImageResource(R.drawable.ic_temperature);
        }
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public void setData(List<String> newData) {
        this.data = newData;
        notifyDataSetChanged();
    }
    static class WeatherViewHolder extends RecyclerView.ViewHolder {
        ImageView weatherIcon;
        TextView weatherDetail;

        public WeatherViewHolder(@NonNull View itemView) {
            super(itemView);
            weatherIcon = itemView.findViewById(R.id.icon_weather);
            weatherDetail = itemView.findViewById(R.id.text_weather_detail);
        }
    }
}
