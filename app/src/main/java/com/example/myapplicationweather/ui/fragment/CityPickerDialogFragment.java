package com.example.myapplicationweather.ui.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.myapplicationweather.R;
import com.example.myapplicationweather.ui.adapter.CityDataLoader;

import java.util.List;

public class CityPickerDialogFragment extends DialogFragment {

    private String selectedCity;
    private CitySelectedListener citySelectedListener;

    public interface CitySelectedListener {
        void onCitySelected(String province, String city, String district);
    }

    public static CityPickerDialogFragment newInstance(String currentCity) {
        CityPickerDialogFragment fragment = new CityPickerDialogFragment();
        Bundle args = new Bundle();
        args.putString("selectedCity", currentCity);
        fragment.setArguments(args);
        return fragment;
    }

    public void setCitySelectedListener(CitySelectedListener listener) {
        this.citySelectedListener = listener;
    }

    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_city_picker, null);

        Spinner provinceSpinner = view.findViewById(R.id.spinner_province);
        Spinner citySpinner = view.findViewById(R.id.spinner_city);
        Spinner districtSpinner = view.findViewById(R.id.spinner_district);

        // 初始化省、市、区联动逻辑
        CityDataLoader loader = new CityDataLoader(getContext());
        List<String> provinces = loader.getProvinces();

        ArrayAdapter<String> provinceAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, provinces);
        provinceSpinner.setAdapter(provinceAdapter);

        provinceSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedProvince = provinces.get(position);
                List<String> cities = loader.getCities(selectedProvince);

                ArrayAdapter<String> cityAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, cities);
                citySpinner.setAdapter(cityAdapter);

                citySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        String selectedCity = cities.get(position);
                        List<String> districts = loader.getDistricts(selectedProvince, selectedCity);

                        ArrayAdapter<String> districtAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, districts);
                        districtSpinner.setAdapter(districtAdapter);
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {}
                });
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        return new AlertDialog.Builder(requireContext())
                .setTitle("选择城市")
                .setView(view)
                .setPositiveButton("确定", (dialog, which) -> {
                    String province = (String) provinceSpinner.getSelectedItem();
                    String city = (String) citySpinner.getSelectedItem();
                    String district = (String) districtSpinner.getSelectedItem();

                    // 通过回调返回选择结果
                    if (citySelectedListener != null) {
                        citySelectedListener.onCitySelected(province, city, district);
                    }
                })
                .setNegativeButton("取消", null)
                .create();
    }
}

