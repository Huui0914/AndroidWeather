package com.example.myapplicationweather.ui.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplicationweather.R;
import com.example.myapplicationweather.controller.DBHelper;

public class LoginActivity extends AppCompatActivity {
    private EditText usernameEditText;
    private EditText passwordEditText;
    private Button loginButton;
    private TextView registerTextView;
    private DBHelper dbHelper;

    private static final String PREF_NAME = "login_prefs";
    private static final String KEY_USERNAME = "current_username";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // 检查自动登录状态
        checkAutoLogin();

        // 初始化视图组件
        usernameEditText = findViewById(R.id.editTextUsername);
        passwordEditText = findViewById(R.id.editTextPassword);
        loginButton = findViewById(R.id.buttonLogin);
        registerTextView = findViewById(R.id.textViewRegister);

        // 获取数据库实例
        dbHelper = new DBHelper(this);

        // 登录按钮点击事件
        loginButton.setOnClickListener(v -> {
            String username = usernameEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(LoginActivity.this, "请输入用户名和密码", Toast.LENGTH_SHORT).show();
            } else {
                if (dbHelper.checkUser(username, password)) {
                    Toast.makeText(LoginActivity.this, "登录成功", Toast.LENGTH_SHORT).show();

                    // 缓存用户名
                    saveCurrentUsername(username);

                    // 跳转到主界面
                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    finish();
                } else {
                    Toast.makeText(LoginActivity.this, "用户名或密码错误", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // 注册文本视图点击事件
        registerTextView.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }

    /**
     * 检查自动登录状态
     */
    private void checkAutoLogin() {
        String currentUsername = getCurrentUsername(this);
        if (currentUsername != null) {
            DBHelper dbHelper = new DBHelper(this);
            boolean isAutoLoginEnabled = dbHelper.isAutoLoginEnabled(currentUsername);

            if (isAutoLoginEnabled) {
                // 如果自动登录开启，直接跳转到主界面
                Toast.makeText(this, "自动登录中...", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                intent.putExtra("username", currentUsername);
                startActivity(intent);
                finish(); // 关闭登录页面
            }
        }
    }

    /**
     * 缓存当前登录的用户名
     */
    private void saveCurrentUsername(String username) {
        SharedPreferences sharedPreferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_USERNAME, username);
        editor.apply();
    }

    /**
     * 获取当前登录的用户名（供其他页面使用）
     */
    public static String getCurrentUsername(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(KEY_USERNAME, null);
    }

    /**
     * 注销当前用户（清除缓存信息）
     */
    public static void logoutCurrentUser(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(KEY_USERNAME);
        editor.apply();
    }
}
