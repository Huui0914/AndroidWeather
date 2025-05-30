package com.example.myapplicationweather;

import android.app.Application;

import com.baidu.mapapi.CoordType;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.common.BaiduMapSDKException;

public class App extends Application {

        @Override
        public void onCreate() {
            super.onCreate();
            try {
                // 在使用 SDK 各组间之前初始化 context 信息，传入 ApplicationContext
                SDKInitializer.initialize(this);
            } catch (BaiduMapSDKException e) {
                e.printStackTrace();
            }
            SDKInitializer.setCoordType(CoordType.BD09LL);
        }

}
