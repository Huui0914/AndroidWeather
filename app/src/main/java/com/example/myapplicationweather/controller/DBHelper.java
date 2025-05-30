package com.example.myapplicationweather.controller;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.content.ContentValues;
import android.util.Log;

import com.baidu.mapapi.model.LatLng;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DBHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "user_db.db"; // 数据库名称
    private static final int DATABASE_VERSION = 1; // 数据库版本

    // 用户表的名称和字段
    private static final String TABLE_USER = "user";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_USERNAME = "username";
    private static final String COLUMN_PASSWORD = "password";
    private static final String COLUMN_SELECTED_CITY = "selected_city";
    private static final String COLUMN_INTERESTED_CITIES = "interested_cities";
    private static final String COLUMN_HISTORY_LIMIT = "history_limit";
    private static final String COLUMN_AUTO_LOGIN = "auto_login";

    // 城市表字段
    private static final String TABLE_CITY = "city";
    private static final String COLUMN_CITY_ID = "id"; // 主键
    private static final String COLUMN_PROVINCE_NAME = "province_name"; // 省名称
    private static final String COLUMN_CITY_NAME = "city_name"; // 市名称
    private static final String COLUMN_DISTRICT_NAME = "district_name"; // 区/县名称
    private static final String COLUMN_CITY_IMAGE = "image_path"; // 城市图片路径
    private static final String COLUMN_CITY_LONGITUDE = "city_longitude"; // 经度
    private static final String COLUMN_CITY_LATITUDE = "city_latitude"; // 纬度

    //历史表字段
    private static final String TABLE_HISTORY = "history";
    private static final String COLUMN_HISTORY_ID = "id";
    private static final String COLUMN_HISTORY_USERNAME = "username";
    private static final String COLUMN_HISTORY_CITY_NAME = "city_name";
    private static final String COLUMN_LAST_VIEWED_TIME = "last_viewed_time";

    // 创建用户表的SQL语句
    private static final String CREATE_TABLE_USER =
            "CREATE TABLE " + TABLE_USER + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_USERNAME + " TEXT NOT NULL, " +
                    COLUMN_PASSWORD + " TEXT NOT NULL, " +
                    COLUMN_SELECTED_CITY + " TEXT, " +
                    COLUMN_INTERESTED_CITIES + " TEXT, " +
                    COLUMN_HISTORY_LIMIT + " INTEGER DEFAULT 6, " +
                    COLUMN_AUTO_LOGIN + " INTEGER DEFAULT 0);";

    // 创建城市表 SQL
    private static final String CREATE_TABLE_CITY =
            "CREATE TABLE " + TABLE_CITY + " (" +
                    COLUMN_CITY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_PROVINCE_NAME + " TEXT NOT NULL, " +
                    COLUMN_CITY_NAME + " TEXT NOT NULL, " +
                    COLUMN_DISTRICT_NAME + " TEXT, " +
                    COLUMN_CITY_IMAGE + " TEXT, " +
                    COLUMN_CITY_LONGITUDE + " REAL, " +
                    COLUMN_CITY_LATITUDE + " REAL" +
                    ");";

    //创建历史表
    private static final String CREATE_TABLE_HISTORY =
            "CREATE TABLE " + TABLE_HISTORY + " (" +
                    COLUMN_HISTORY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_HISTORY_USERNAME + " TEXT NOT NULL, " +
                    COLUMN_HISTORY_CITY_NAME + " TEXT NOT NULL, " +
                    COLUMN_LAST_VIEWED_TIME + " TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "FOREIGN KEY(" + COLUMN_HISTORY_USERNAME + ") REFERENCES " + TABLE_USER + "(" + COLUMN_USERNAME + "), " +
                    "FOREIGN KEY(" + COLUMN_HISTORY_CITY_NAME + ") REFERENCES " + TABLE_CITY + "(" + COLUMN_CITY_NAME + "));";

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // 创建用户表
        db.execSQL(CREATE_TABLE_USER);
        // 创建城市表
        db.execSQL(CREATE_TABLE_CITY);
        initializeCityData(db);
        db.execSQL(CREATE_TABLE_HISTORY);
    }
    private void initializeCityData(SQLiteDatabase db) {
        // 北京
        insertCity(db, "北京市", "北京市", "东城区", "drawable/beijing_img", "116.407526", "39.904030");
        insertCity(db, "北京市", "北京市", "西城区", "drawable/beijing_img", "116.366794", "39.915309");
        insertCity(db, "北京市", "北京市", "朝阳区", "drawable/beijing_img", "116.366794", "39.915309");
        insertCity(db, "北京市", "北京市", "海淀区", "drawable/beijing_img", "116.366794", "39.915309");
        // 杭州
        insertCity(db, "浙江省", "杭州市", "西湖区", "drawable/hangzhou_img", "120.130663", "30.259244");
        insertCity(db, "浙江省", "杭州市", "上城区", "drawable/hangzhou_img", "120.171465", "30.257200");
        insertCity(db, "浙江省", "杭州市", "余杭区", "drawable/hangzhou_img", "120.171465", "30.257200");
        // 宁波
        insertCity(db, "浙江省", "宁波市", "海曙区", "drawable/hangzhou_img", "120.130663", "30.259244");
        insertCity(db, "浙江省", "宁波市", "江北区", "drawable/hangzhou_img", "120.171465", "30.257200");
        insertCity(db, "浙江省", "宁波市", "北仑区", "drawable/hangzhou_img", "120.171465", "30.257200");
        // 上海
        insertCity(db, "上海市", "上海市", "虹桥区", "drawable/shanghai_img", "121.567706", "31.245944");
        insertCity(db, "上海市", "上海市", "浦东新区", "drawable/shanghai_img", "121.567706", "31.245944");
        insertCity(db, "上海市", "上海市", "徐汇区", "drawable/shanghai_img", "121.43752", "31.179973");
        // 广州
        insertCity(db, "广东省", "广州市", "天河区", "drawable/guangzhou_img", "113.335367", "23.13559");
        insertCity(db, "广东省", "广州市", "越秀区", "drawable/guangzhou_img", "113.280637", "23.125178");
        insertCity(db, "广东省", "广州市", "白云区", "drawable/guangzhou_img", "113.280637", "23.125178");
        // 深圳
        insertCity(db, "广东省", "深圳市", "南山区", "drawable/shenzhen_img", "113.930713", "22.53332");
        insertCity(db, "广东省", "深圳市", "福田区", "drawable/shenzhen_img", "114.057865", "22.543096");
        insertCity(db, "广东省", "深圳市", "罗田区", "drawable/shenzhen_img", "114.057865", "22.543096");
    }
    private void insertCity(SQLiteDatabase db, String provinceName, String cityName, String districtName,
                            String imagePath, String cityLongitude, String cityLatitude) {
        ContentValues values = new ContentValues();
        values.put("province_name", provinceName); // 省份
        values.put("city_name", cityName); // 城市
        values.put("district_name", districtName); // 区县
        values.put("image_path", imagePath); // 图片路径
        values.put("city_longitude", cityLongitude); // 经度
        values.put("city_latitude", cityLatitude); // 纬度
        db.insert(TABLE_CITY, null, values);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER);
        onCreate(db);
    }

    // 插入新用户
    public boolean addUser(String username, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        //ContentValues:用于存储要插入到数据库表中或者用于更新数据库表中已有记录的数据
        ContentValues values = new ContentValues();
        values.put(COLUMN_USERNAME, username);
        values.put(COLUMN_PASSWORD, password);

        long result = db.insert(TABLE_USER, null, values);
        db.close();
        return result != -1; // 如果返回-1表示插入失败
    }

    // 验证用户登录
    public boolean checkUser(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USER, null,
                COLUMN_USERNAME + "=? AND " + COLUMN_PASSWORD + "=?",
                new String[]{username, password}, null, null, null);

        boolean userExists = cursor != null && cursor.moveToFirst();
        cursor.close();
        db.close();
        return userExists;
    }

    // 更新用户名
    public boolean updateUsername(String currentUsername, String newUsername) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USERNAME, newUsername);

        int rows = db.update(TABLE_USER, values, COLUMN_USERNAME + "=?", new String[]{currentUsername});
        db.close();
        return rows > 0; // 返回是否更新成功
    }

    // 更新密码
    public boolean updatePassword(String username, String newPassword) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_PASSWORD, newPassword);

        int rows = db.update(TABLE_USER, values, COLUMN_USERNAME + "=?", new String[]{username});
        db.close();
        return rows > 0; // 返回是否更新成功
    }

    //更新用户选择的城市
    public boolean updateSelectedCity(String username, String selectedCity) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("selected_city", selectedCity);

        int rows = db.update("user", values, "username = ?", new String[]{username});
        db.close();
        return rows > 0;
    }

    // 获取用户选择的城市
    public String getSelectedCity(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(
                "user",
                new String[]{"selected_city"}, // 查询 selected_city 字段
                "username = ?",
                new String[]{username},
                null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            String selectedCity = cursor.getString(cursor.getColumnIndexOrThrow("selected_city"));
            cursor.close();
            db.close();
            return selectedCity; // 返回完整的省-市-区格式
        }

        db.close();
        return null; // 如果没有记录，返回 null
    }

    // 查询城市图片路径
    public String getCityImage(String cityName) {
        SQLiteDatabase db = this.getReadableDatabase();
        String imagePath = null;

        Cursor cursor = db.query(TABLE_CITY,
                new String[]{COLUMN_CITY_IMAGE},
                COLUMN_CITY_NAME + "=?",
                new String[]{cityName},
                null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            imagePath = cursor.getString(cursor.getColumnIndex(COLUMN_CITY_IMAGE));
            cursor.close();
        }
        db.close();
        return imagePath;
    }

    // 查询城市经纬度
    public LatLng getCityLatLng(String cityName) {
        LatLng cityLatLng = null;
        SQLiteDatabase db = null;
        Cursor cursor = null;

        try {
            db = getReadableDatabase();
            String query = "SELECT " + COLUMN_CITY_LATITUDE + ", " + COLUMN_CITY_LONGITUDE +
                    " FROM " + TABLE_CITY + " WHERE " + COLUMN_CITY_NAME + " = ?";
            cursor = db.rawQuery(query, new String[]{cityName});

            if (cursor.moveToFirst()) {
                double latitude = cursor.getDouble(cursor.getColumnIndex(COLUMN_CITY_LATITUDE));
                double longitude = cursor.getDouble(cursor.getColumnIndex(COLUMN_CITY_LONGITUDE));
                cityLatLng = new LatLng(latitude, longitude);
            }
        } catch (Exception e) {
            Log.e("DBHelper", "查询城市经纬度失败: " + e.getMessage());
        } finally {
            if (cursor != null) cursor.close();
            if (db != null) db.close();
        }

        return cityLatLng;
    }

    // 保存用户选择的感兴趣城市
    public boolean updateInterestedCities(String username, List<String> cities) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("interested_cities", String.join(",", cities)); // 将城市列表存储为逗号分隔的字符串

        int rows = db.update("user", values, "username = ?", new String[]{username});
        db.close();
        return rows > 0;
    }

    // 获取用户选择的感兴趣城市
    public List<String> getInterestedCities(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(
                "user",
                new String[]{"interested_cities"}, // 查询感兴趣的城市字段
                "username = ?",
                new String[]{username},
                null, null, null);

        List<String> cities = new ArrayList<>();
        if (cursor != null && cursor.moveToFirst()) {
            String citiesString = cursor.getString(cursor.getColumnIndexOrThrow("interested_cities"));
            if (citiesString != null && !citiesString.isEmpty()) {
                cities.addAll(Arrays.asList(citiesString.split(","))); // 将逗号分隔的字符串转为列表
            }
            cursor.close();
        }
        db.close();
        return cities;
    }

//    public void addCityToHistory(String username, String cityName) {
//        SQLiteDatabase db = this.getWritableDatabase();
//
//        // 查询是否已存在记录
//        Cursor cursor = db.query(TABLE_HISTORY,
//                new String[]{COLUMN_HISTORY_ID},
//                COLUMN_HISTORY_USERNAME + " = ? AND " + COLUMN_HISTORY_CITY_NAME + " = ?",
//                new String[]{username, cityName},
//                null, null, null);
//
//        if (cursor != null && cursor.getCount() > 0) {
//            // 已存在记录，更新最后查看时间
//            ContentValues values = new ContentValues();
//            values.put(COLUMN_LAST_VIEWED_TIME, System.currentTimeMillis());
//
//            db.update(TABLE_HISTORY, values,
//                    COLUMN_HISTORY_USERNAME + " = ? AND " + COLUMN_HISTORY_CITY_NAME + " = ?",
//                    new String[]{username, cityName});
//            Log.d("DBHelper", "Updated last viewed time for city: " + cityName);
//        } else {
//            // 不存在记录，插入新记录
//            ContentValues values = new ContentValues();
//            values.put(COLUMN_HISTORY_USERNAME, username);
//            values.put(COLUMN_HISTORY_CITY_NAME, cityName);
//            values.put(COLUMN_LAST_VIEWED_TIME, System.currentTimeMillis());
//
//            long result = db.insert(TABLE_HISTORY, null, values);
//            if (result == -1) {
//                Log.e("DBHelper", "Failed to insert city into history for user: " + username);
//            } else {
//                Log.d("DBHelper", "City added to history: " + cityName + " for user: " + username);
//            }
//        }
//
//        if (cursor != null) {
//            cursor.close();
//        }
//    }
    public void addCityToHistory(String username, String cityName) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COLUMN_HISTORY_USERNAME, username);
        values.put(COLUMN_HISTORY_CITY_NAME, cityName);
        values.put(COLUMN_LAST_VIEWED_TIME, System.currentTimeMillis()); // 使用当前时间

        long result = db.insert(TABLE_HISTORY, null, values);
        if (result == -1) {
            Log.e("DBHelper", "Failed to insert city into history for user: " + username);
        } else {
            Log.d("DBHelper", "City added to history: " + cityName + " for user: " + username);
        }
    }



    public List<String> getLatestHistoryCities(String username, int limit) {
        List<String> cities = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        // 查询 `History` 表，按 `last_viewed_time` 降序排列，限制返回条数
        String query = "SELECT " + COLUMN_HISTORY_CITY_NAME +
                " FROM " + TABLE_HISTORY +
                " WHERE " + COLUMN_HISTORY_USERNAME + " = ?" +
                " ORDER BY " + COLUMN_LAST_VIEWED_TIME + " DESC" +
                " LIMIT ?";

        Cursor cursor = db.rawQuery(query, new String[]{username, String.valueOf(limit)});

        if (cursor != null) {
            while (cursor.moveToNext()) {
                cities.add(cursor.getString(cursor.getColumnIndex(COLUMN_HISTORY_CITY_NAME)));
            }
            cursor.close();
        }

        return cities;
    }

    // 更新用户选择的历史设置
    public void updateHistoryLimit(String username, int historyLimit) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_HISTORY_LIMIT, historyLimit);
        db.update(TABLE_USER, values, COLUMN_USERNAME + " = ?", new String[]{username});
        db.close();
    }

    public int getHistoryLimit(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(
                TABLE_USER,
                new String[]{COLUMN_HISTORY_LIMIT},
                COLUMN_USERNAME + " = ?",
                new String[]{username},
                null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            int limit = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_HISTORY_LIMIT));
            cursor.close();
            db.close();
            return limit;
        }
        db.close();
        return 6; // 默认值为 6
    }

    public void updateAutoLoginStatus(String username, boolean isAutoLoginEnabled) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("auto_login", isAutoLoginEnabled ? 1 : 0);
        db.update(TABLE_USER, values, "username = ?", new String[]{username});
        db.close();
    }

    public boolean isAutoLoginEnabled(String username) {
        SQLiteDatabase db = this.getReadableDatabase();//获取数据库
        //Cursor用于遍历数据库查询结果集的一个接口类型
        Cursor cursor = db.query(
                TABLE_USER,
                new String[]{"auto_login"},
                "username = ?",
                new String[]{username},
                null, null, null);
        //判断查询结果是否非空并且至少有一条记录
        if (cursor != null && cursor.moveToFirst()) {
            boolean isEnabled = cursor.getInt(cursor.getColumnIndexOrThrow("auto_login")) == 1;
            cursor.close();
            db.close();
            return isEnabled;
        }
        db.close();
        return false; // 默认返回禁用
    }

}

