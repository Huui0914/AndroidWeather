package com.example.myapplicationweather.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplicationweather.R;
import com.example.myapplicationweather.model.dataClass.CityWeatherData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.view.LineChartView;

public class CityWeatherAdapter extends RecyclerView.Adapter<CityWeatherAdapter.ViewHolder> {

    private List<CityWeatherData> cityWeatherList;

    public CityWeatherAdapter(List<CityWeatherData> cityWeatherList) {
        this.cityWeatherList = cityWeatherList;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_city_chart, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CityWeatherData data = cityWeatherList.get(position);

        holder.cityName.setText(data.getCityName());
        drawLineChart(holder.chart, data.getDates(), data.getHighTemps(), data.getLowTemps());
    }

    @Override
    public int getItemCount() {
        return cityWeatherList.size();
    }

    private void drawLineChart(LineChartView chart, List<String> dates, List<Integer> highTemps, List<Integer> lowTemps) {
        List<PointValue> highPoints = new ArrayList<>();
        List<PointValue> lowPoints = new ArrayList<>();

        for (int i = 0; i < dates.size(); i++) {
            highPoints.add(new PointValue(i, highTemps.get(i)));
            lowPoints.add(new PointValue(i, lowTemps.get(i)));
        }

        Line highLine = new Line(highPoints).setColor(0xFF56B7F1).setCubic(true).setHasLabels(true);
        Line lowLine = new Line(lowPoints).setColor(0xFFB2746F).setCubic(true).setHasLabels(true);

        LineChartData lineChartData = new LineChartData();
        lineChartData.setLines(Arrays.asList(highLine, lowLine));

        Axis axisX = new Axis();
        axisX.setName("日期");
        List<AxisValue> axisValues = new ArrayList<>();
        for (int i = 0; i < dates.size(); i++) {
            axisValues.add(new AxisValue(i).setLabel(dates.get(i)));
        }
        axisX.setValues(axisValues);

        Axis axisY = new Axis();
        axisY.setName("温度 (°C)");

        // 设置Y轴的间隔
        int minTemp = Collections.min(lowTemps); // 找到最低温度
        int maxTemp = Collections.max(highTemps); // 找到最高温度
        int interval = 5; // 设置每个间隔为5度
        List<AxisValue> yAxisValues = new ArrayList<>();
        for (int i = minTemp; i <= maxTemp; i += interval) {
            yAxisValues.add(new AxisValue(i).setLabel(String.valueOf(i)));
        }
        axisY.setValues(yAxisValues);

        lineChartData.setAxisXBottom(axisX);
        lineChartData.setAxisYLeft(axisY);

        chart.setLineChartData(lineChartData);
    }


    public void setData(List<CityWeatherData> newData) {
        this.cityWeatherList = newData; // 替换旧数据
        notifyDataSetChanged(); // 通知 RecyclerView 刷新数据
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView cityName;
        LineChartView chart;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cityName = itemView.findViewById(R.id.city_name);
            chart = itemView.findViewById(R.id.chart);
        }
    }
}


