package com.example.myapplicationweather.ui.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.myapplicationweather.R;

import java.util.ArrayList;
import java.util.List;

public class InterestedCitiesDialogFragment extends DialogFragment {

    private List<String> currentCities; // 当前已选择的城市列表
    private CitySelectedListener citySelectedListener; // 城市选择回调接口

    // 定义回调接口
    public interface CitySelectedListener {
        void onCitySelected(List<String> cities); // 用于回调返回已选择的城市列表
    }

    // 实例化对话框
    public static InterestedCitiesDialogFragment newInstance(List<String> currentCities) {
        InterestedCitiesDialogFragment fragment = new InterestedCitiesDialogFragment();
        Bundle args = new Bundle();
        args.putStringArrayList("currentCities", currentCities != null ? new ArrayList<>(currentCities) : new ArrayList<>());
        fragment.setArguments(args);
        return fragment;
    }

    // 设置城市选择回调
    public void setCitySelectedListener(CitySelectedListener listener) {
        this.citySelectedListener = listener;
    }

    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        // 加载自定义布局
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_interested_cities, null);

        // 获取视图组件
        ListView citiesListView = view.findViewById(R.id.cities_list);
        Button addCityButton = view.findViewById(R.id.add_city_button);

        // 获取当前已选择的城市列表
        currentCities = getArguments() != null ? getArguments().getStringArrayList("currentCities") : new ArrayList<>();

        // 初始化 ArrayAdapter
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, currentCities);
        citiesListView.setAdapter(adapter);

        // 设置长按删除城市功能
        citiesListView.setOnItemLongClickListener((parent, view1, position, id) -> {
            new AlertDialog.Builder(getContext())
                    .setTitle("删除城市")
                    .setMessage("确定删除 " + currentCities.get(position) + " 吗？")
                    .setPositiveButton("删除", (dialog, which) -> {
                        currentCities.remove(position); // 删除城市
                        adapter.notifyDataSetChanged(); // 更新适配器
                    })
                    .setNegativeButton("取消", null)
                    .show();
            return true;
        });

        // 添加新城市按钮点击事件
        addCityButton.setOnClickListener(v -> {
            // 每次创建新的 CityPickerDialogFragment，并动态传入最新的城市列表
            CityPickerDialogFragment cityPickerDialog = new CityPickerDialogFragment();
            cityPickerDialog.setCitySelectedListener((province, city, district) -> {
                String fullCity = province + "-" + city + "-" + district;
                // 检查是否已存在该城市
                if (!currentCities.contains(fullCity)) {
                    currentCities.add(fullCity); // 添加到列表
                    adapter.notifyDataSetChanged(); // 更新适配器
                } else {
                    Toast.makeText(getContext(), "城市已存在", Toast.LENGTH_SHORT).show();
                }
            });
            // 显示城市选择器
            cityPickerDialog.show(getParentFragmentManager(), "city_picker");
        });

        // 构建对话框
        return new AlertDialog.Builder(requireContext())
                .setTitle("选择感兴趣的城市")
                .setView(view)
                .setPositiveButton("保存", (dialog, which) -> {
                    if (citySelectedListener != null) {
                        citySelectedListener.onCitySelected(new ArrayList<>(currentCities)); // 回调选择的城市列表
                    }
                })
                .setNegativeButton("取消", null)
                .create();
    }
}
