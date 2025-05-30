package com.example.myapplicationweather.ui.fragment;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;
import com.example.myapplicationweather.ui.activities.LoginActivity;
import com.example.myapplicationweather.R;
import com.example.myapplicationweather.controller.DBHelper;

import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class MapFragment extends Fragment {

    private TextView tv_city_name;
    private MapView mMapView = null;
    private LocationClient mLocationClient = null;
    private BaiduMap mBaiduMap;
    private boolean isFirstLoc = true;  //是否首次定位
    private DBHelper dbHelper;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);
        // 初始化数据库帮助类
        dbHelper = new DBHelper(getContext());
        //获取地图控件引用
        tv_city_name = view.findViewById(R.id.tv_city_name);
        mMapView = view.findViewById(R.id.bmapView);
        //地图对象
        mBaiduMap = mMapView.getMap();
        //定位初始化
        try {
            mLocationClient = new LocationClient(getActivity());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        //通过LocationClientOption设置LocationClient相关参数
        LocationClientOption option = new LocationClientOption();
        option.setOpenGps(true); // 打开gps
        option.setCoorType("bd09ll"); // 设置坐标类型
        option.setScanSpan(1000);//快速回到定位间隔200ms
        option.setIsNeedAddress(true);//是否需要获取位置的详细地址信息
        option.setWifiCacheTimeOut( 5*60*1000 );//Wifi 定位
        option.setLocationMode( LocationClientOption.LocationMode.Hight_Accuracy );//选择高精度的定位模式
        option.setIsNeedLocationPoiList(true);// 可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到
        //设置locationClientOption
        mLocationClient.setLocOption(option);

        //权限的检查与申请操作
        List<String> permissionList = new ArrayList<>();
        if (ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(android.Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(android.Manifest.permission.READ_PHONE_STATE);
        }
        if (ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        //发起权限申请（如果有未授予的权限）
        if (!permissionList.isEmpty()) {
            String[] permissions = permissionList.toArray(new String[permissionList.size()]);
            ActivityCompat.requestPermissions(getActivity(), permissions, 1);
        }
        //注册LocationListener监听器
        MyLocationListener myLocationListener = new MyLocationListener();
        mLocationClient.registerLocationListener(myLocationListener);
        mLocationClient.start();

        return view;
    }

    @Override
    public void onResume() {
        mMapView.onResume();
        super.onResume();
    }

    @Override
    public void onPause() {
        mMapView.onPause();
        super.onPause();
    }
    @Override
    public void onDestroy() {
        mLocationClient.stop();
        mBaiduMap.setMyLocationEnabled(false);
        mMapView.onDestroy();
        mMapView = null;
        super.onDestroy();
    }

    class MyLocationListener extends BDAbstractLocationListener {
        @Override
        public void onReceiveLocation(BDLocation location) {
            //mapView 销毁后不在处理新接收的位置
            if (location == null || mMapView == null) {
                return;
            }
            // 打印完整定位信息
            Log.d("LocationInfo", "LocType: " + location.getLocType());
            Log.d("LocationInfo", "AddrStr: " + location.getAddrStr());
            Log.d("LocationInfo", "City: " + location.getCity());
            Log.d("LocationInfo", "Latitude: " + location.getLatitude());
            Log.d("LocationInfo", "Longitude: " + location.getLongitude());

            mBaiduMap.setMyLocationEnabled(true);//启用地图的我的位置显示功能
            MyLocationData locData = new MyLocationData.Builder()
                    .accuracy(location.getRadius())
                    // 此处设置开发者获取到的方向信息，顺时针0-360
                    .direction(location.getDirection()).latitude(location.getLatitude())
                    .longitude(location.getLongitude()).build();
            mBaiduMap.setMyLocationData(locData);
            //判断是否是第一次定位
            if (isFirstLoc) {
                //isFirstLoc = false;
                //将地图移动到定位的位置
                float f = mBaiduMap.getMaxZoomLevel();
                LatLng ll = new LatLng(30.2489634, 120.2052342);
                MapStatusUpdate u = MapStatusUpdateFactory.newLatLngZoom(ll, f - 2);
                mBaiduMap.animateMapStatus(u);
                //一次定位结束，当再次点击定位时候即可回到自己所在位置
                mLocationClient.stop();
            }

            // 第一步：获取用户选择的完整城市 (省-市-区格式)
            String selectedCity = dbHelper.getSelectedCity(LoginActivity.getCurrentUsername(getContext()));
            if (selectedCity != null) {
                tv_city_name.setText("当前默认城市："+selectedCity);
                // 第二步：解析出城市名称（市）
                String[] cityParts = selectedCity.split("-"); // 分隔省-市-区
                String cityName = cityParts.length > 1 ? cityParts[1] : null; // 获取市的名称
                if (cityName != null) {
                    // 第三步：根据城市名从数据库中查询经纬度
                    LatLng cityLatLng = dbHelper.getCityLatLng(cityName);
                    if (cityLatLng != null) {
                        // 更新地图位置到用户选择的城市
                        updateMapToCity(cityLatLng);
                    } else {
                        Log.e("Database", "未找到城市 " + cityName + " 的经纬度信息");
                    }
                } else {
                    tv_city_name.setText("无法获取城市信息，请检查定位设置");
                }
            }

            StringBuffer sb = new StringBuffer(256);
            sb.append("time:");
            sb.append(location.getTime());
            sb.append("\nerror code : ");
            sb.append(location.getLocType());
            sb.append("\nlatitude : ");
            sb.append(location.getLatitude());
            sb.append("\nlontitude : ");
            sb.append(location.getLongitude());
            sb.append("\nradius : ");
            sb.append(location.getRadius());
            if (location.getLocType() == BDLocation.TypeGpsLocation) {
                //GPS定位
                sb.append("\nspeed : ");
                sb.append(location.getSpeed());
                sb.append("\nsatellite : ");
                sb.append(location.getSatelliteNumber());
                sb.append("\nheight:");
                sb.append(location.getAltitude());
                sb.append("\ndirection:");
                sb.append(location.getDirection());
                sb.append("\naddr:");
                sb.append(location.getAddrStr());
                sb.append("\ndescribe:");
                sb.append("GPS定位成功！");
            } else if (location.getLocType() == BDLocation.TypeNetWorkLocation) {
                sb.append("\naddr : ");
                sb.append(location.getAddrStr());
                sb.append("\noperationer:");
                sb.append(location.getOperators());
                sb.append("\ndescribe:");
                sb.append("网络定位成功");
            } else if (location.getLocType() == BDLocation.TypeOffLineLocation) {
                //离线定位
                sb.append("\ndescribe:");
                sb.append("离线定位成功");
            } else if (location.getLocType() == BDLocation.TypeServerError) {
                sb.append("\ndescribe:");
                sb.append("server定位失败，没有对应的位置信息");
            } else if (location.getLocType() == BDLocation.TypeNetWorkException) {
                sb.append("\ndescribe:");
                sb.append("网络连接失败");
            }
            Log.e("TEST", sb.toString());
        }
    }

    // 更新地图到指定经纬度
    private void updateMapToCity(LatLng latLng) {
        Log.d("LocationInfo","正在更新地图");
        if (mBaiduMap != null) {
            float maxZoomLevel = mBaiduMap.getMaxZoomLevel();
            MapStatusUpdate mapStatusUpdate = MapStatusUpdateFactory.newLatLngZoom(latLng, maxZoomLevel - 2);
            mBaiduMap.animateMapStatus(mapStatusUpdate);
        }
    }

}
