package com.example.myapplicationweather.ui.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.myapplicationweather.ui.fragment.RecommendedFragment;
import com.example.myapplicationweather.ui.fragment.TodayFragment;

public class WeatherTabAdapter extends FragmentStateAdapter {

    public WeatherTabAdapter(@NonNull Fragment fragment) {
        super(fragment);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0: // Today tab
                return new TodayFragment();  // 创建 TodayFragment 实例
            case 1: // Recommended tab
                return new RecommendedFragment();  // 创建 RecommendedFragment 实例
            default:
                return new TodayFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 2; // 2 Tabs: "今日" and "推荐"
    }
}

