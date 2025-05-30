package com.example.myapplicationweather.ui.fragment;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.text.InputType;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.preference.EditTextPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreferenceCompat;

import com.example.myapplicationweather.ui.activities.LoginActivity;
import com.example.myapplicationweather.R;
import com.example.myapplicationweather.controller.DBHelper;
import java.util.List;

public class SettingsFragment extends PreferenceFragmentCompat {

    private DBHelper dbHelper;
    private String currentUsername;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);

        // 初始化数据库帮助类
        dbHelper = new DBHelper(getContext());

        // 获取当前用户名
        currentUsername = LoginActivity.getCurrentUsername(getContext());
        if (currentUsername == null) {
            Toast.makeText(getContext(), "未登录，请重新登录", Toast.LENGTH_SHORT).show();
            return;
        }

        //获取用户选择的默认城市
        Preference cityPreference = findPreference("default_city");
        if (cityPreference != null) {
            // 从数据库获取当前用户已选城市（省-市-区格式）
            String selectedCity = dbHelper.getSelectedCity(currentUsername);
            // 显示当前选择的城市在 Summary
            cityPreference.setSummary(selectedCity != null ? "默认城市：" + selectedCity : "请选择默认城市");
            // 设置点击事件，弹出城市选择器
            cityPreference.setOnPreferenceClickListener(preference -> {
                // 打开城市选择对话框
                CityPickerDialogFragment dialog = CityPickerDialogFragment.newInstance(selectedCity);
                dialog.setCitySelectedListener((province, city, district) -> {
                    // 组合完整的城市名称
                    String newCity = province + "-" + city + "-" + district;
                    // 保存用户选择的城市到数据库
                    boolean success = dbHelper.updateSelectedCity(currentUsername, newCity);
                    if (success) {
                        Toast.makeText(getContext(), "默认城市已更新为：" + newCity, Toast.LENGTH_SHORT).show();
                        cityPreference.setSummary("默认城市：" + newCity); // 更新显示
                    } else {
                        Toast.makeText(getContext(), "更新城市失败", Toast.LENGTH_SHORT).show();
                    }
                });
                dialog.show(getParentFragmentManager(), "city_picker");
                return true;
            });
        }


        // 获取选择感兴趣城市的 Preference
        Preference interestedCitiesPreference = findPreference("interested_cities");
        if (interestedCitiesPreference != null) {
            // 从数据库获取当前用户感兴趣的城市列表
            List<String> interestedCities = dbHelper.getInterestedCities(currentUsername);

            // 更新 Summary 显示当前感兴趣的城市
            updateInterestedCitiesSummary(interestedCitiesPreference, interestedCities);

            interestedCitiesPreference.setOnPreferenceClickListener(preference -> {
                // 打开城市选择器对话框
                InterestedCitiesDialogFragment dialog = InterestedCitiesDialogFragment.newInstance(interestedCities);
                dialog.setCitySelectedListener(newCities -> {
                    // 更新用户感兴趣的城市到数据库
                    boolean success = dbHelper.updateInterestedCities(currentUsername, newCities);
                    if (success) {
                        Toast.makeText(getContext(), "感兴趣的城市已更新", Toast.LENGTH_SHORT).show();
                        updateInterestedCitiesSummary(interestedCitiesPreference, newCities); // 更新 Summary
                        // 将每个感兴趣的城市添加到 History 表
                        for (String city : newCities) {
                            dbHelper.addCityToHistory(currentUsername, city);
                        }
                    } else {
                        Toast.makeText(getContext(), "更新感兴趣的城市失败", Toast.LENGTH_SHORT).show();
                    }
                });
                dialog.show(getParentFragmentManager(), "interested_cities_picker");
                return true;
            });
        }

        // 修改用户名
        Preference changeUsernamePreference = findPreference("change_username");
        if (changeUsernamePreference != null) {
            changeUsernamePreference.setOnPreferenceClickListener(preference -> {
                showChangeUsernameDialog(getContext());
                return true;
            });
        }

        // 修改密码
        Preference changePasswordPreference = findPreference("change_password");
        if (changePasswordPreference != null) {
            changePasswordPreference.setOnPreferenceClickListener(preference -> {
                showChangePasswordDialog(getContext());
                return true;
            });
        }

        // 自动登录开关逻辑
        SwitchPreferenceCompat autoLoginPreference = findPreference("auto_login");
        if (autoLoginPreference != null) {
            autoLoginPreference.setOnPreferenceChangeListener((preference, newValue) -> {
                boolean isAutoLoginEnabled = (boolean) newValue;
                dbHelper.updateAutoLoginStatus(currentUsername, isAutoLoginEnabled);
                String message = isAutoLoginEnabled ? "自动登录已启用" : "自动登录已禁用";
                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                return true;
            });
        }

        // 注销
        Preference logoutPreference = findPreference("logout");
        if (logoutPreference != null) {
            logoutPreference.setOnPreferenceClickListener(preference -> {
                LoginActivity.logoutCurrentUser(getContext());
                Toast.makeText(getContext(), "注销成功", Toast.LENGTH_SHORT).show();
                // 返回登录页面
                getActivity().finish();
                return true;
            });
        }

        // 设置历史记录城市数量限制
        EditTextPreference historyCityLimit = findPreference("history_city_limit");
        if (historyCityLimit != null) {
            historyCityLimit.setOnPreferenceChangeListener((preference, newValue) -> {
                try {
                    int limit = Integer.parseInt((String) newValue);
                    if (limit < 1 || limit > 10) {
                        Toast.makeText(getContext(), "请输入1到10之间的数字", Toast.LENGTH_SHORT).show();
                        return false;
                    }
                    // 更新历史记录限制到数据库或应用配置
                    dbHelper.updateHistoryLimit(currentUsername,limit);
                    Toast.makeText(getContext(), "历史城市记录限制已更新为：" + limit, Toast.LENGTH_SHORT).show();
                    return true;
                } catch (NumberFormatException e) {
                    Toast.makeText(getContext(), "请输入有效的数字", Toast.LENGTH_SHORT).show();
                    return false;
                }
            });
        }
    }

    private void showChangeUsernameDialog(Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("修改用户名");

        // 输入框
        final EditText input = new EditText(context);
        input.setHint("请输入新用户名");
        builder.setView(input);

        builder.setPositiveButton("确认", (dialog, which) -> {
            String newUsername = input.getText().toString().trim();
            if (newUsername.isEmpty()) {
                Toast.makeText(context, "用户名不能为空", Toast.LENGTH_SHORT).show();
                return;
            }

            // 调用数据库更新方法
            boolean success = dbHelper.updateUsername(currentUsername, newUsername);
            if (success) {
                Toast.makeText(context, "用户名修改成功", Toast.LENGTH_SHORT).show();
                currentUsername = newUsername; // 更新当前用户名
                LoginActivity.logoutCurrentUser(context); // 更新缓存中的用户名
            } else {
                Toast.makeText(context, "用户名修改失败", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("取消", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void showChangePasswordDialog(Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("修改密码");

        // 创建一个垂直方向的 LinearLayout 作为容器
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);

        // 创建旧密码输入框
        EditText oldPasswordInput = new EditText(context);
        oldPasswordInput.setHint("请输入旧密码");
        oldPasswordInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        layout.addView(oldPasswordInput);

        // 创建新密码输入框
        EditText newPasswordInput = new EditText(context);
        newPasswordInput.setHint("请输入新密码");
        newPasswordInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        layout.addView(newPasswordInput);

        // 将 LinearLayout 设置为对话框的 View
        builder.setView(layout);

        // 设置确认按钮
        builder.setPositiveButton("确认", (dialog, which) -> {
            String oldPassword = oldPasswordInput.getText().toString().trim();
            String newPassword = newPasswordInput.getText().toString().trim();

            if (oldPassword.isEmpty() || newPassword.isEmpty()) {
                Toast.makeText(context, "密码不能为空", Toast.LENGTH_SHORT).show();
                return;
            }

            // 验证旧密码并更新
            if (dbHelper.checkUser(currentUsername, oldPassword)) {
                boolean success = dbHelper.updatePassword(currentUsername, newPassword);
                if (success) {
                    Toast.makeText(context, "密码修改成功", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, "密码修改失败", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(context, "旧密码错误", Toast.LENGTH_SHORT).show();
            }
        });

        // 设置取消按钮
        builder.setNegativeButton("取消", (dialog, which) -> dialog.dismiss());

        builder.show();
    }

    private void updateInterestedCitiesSummary(Preference preference, List<String> cities) {
        if (cities != null && !cities.isEmpty()) {
            preference.setSummary("感兴趣的城市：" + String.join(", ", cities));
        } else {
            preference.setSummary("请选择您感兴趣的城市");
        }
    }

}
