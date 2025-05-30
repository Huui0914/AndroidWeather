package com.example.myapplicationweather.ui.activities;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

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
import com.example.myapplicationweather.R;

import java.util.ArrayList;
import java.util.List;

public class TestActivity extends AppCompatActivity {
    private MapView mMapView = null;
    private LocationClient mLocationClient = null;
    private BaiduMap mBaiduMap;
    private boolean isFirstLoc = true;  //是否首次定位
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_test);
        //获取地图控件引用
        mMapView = (MapView) findViewById(R.id.bmapView);
        //地图对象
        mBaiduMap = mMapView.getMap();

        //定位初始化
        try {
            mLocationClient = new LocationClient(getApplicationContext());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        //通过LocationClientOption设置LocationClient相关参数
        LocationClientOption option = new LocationClientOption();
        option.setOpenGps(true); // 打开gps
        option.setCoorType("bd09ll"); // 设置坐标类型
        option.setScanSpan(1000);//快速回到定位间隔200ms
        option.setIsNeedAddress( true );
        option.setWifiCacheTimeOut( 5*60*1000 );
        option.setLocationMode( LocationClientOption.LocationMode.Hight_Accuracy );
////可选，设置是否需要设备方向结果
//        option.setNeedDeviceDirect(true);
//        //可选，默认false，设置定位时是否需要海拔信息，默认不需要，除基础定位版本都可用
//        option.setIsNeedAltitude(true);
        option.setIsNeedLocationPoiList(true);// 可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到


        //设置locationClientOption
        mLocationClient.setLocOption(option);

        List<String> permissionList = new ArrayList<>();
        if (ContextCompat.checkSelfPermission(TestActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(android.Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (ContextCompat.checkSelfPermission(TestActivity.this, android.Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(android.Manifest.permission.READ_PHONE_STATE);
        }
        if (ContextCompat.checkSelfPermission(TestActivity.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (!permissionList.isEmpty()) {
            String[] permissions = permissionList.toArray(new String[permissionList.size()]);
            ActivityCompat.requestPermissions(TestActivity.this, permissions, 1);
        }
        //注册LocationListener监听器
        MyLocationListener myLocationListener = new MyLocationListener();
        mLocationClient.registerLocationListener(myLocationListener);
        mLocationClient.start();
    }

    @Override
    protected void onResume() {
        mMapView.onResume();
        super.onResume();
    }

    @Override
    protected void onPause() {
        mMapView.onPause();
        super.onPause();
    }
    @Override
    protected void onDestroy() {
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
            if (location == null || mMapView == null){
                return;
            }

            mBaiduMap.setMyLocationEnabled(true);

//            MyLocationData locData = new MyLocationData.Builder()
//                    .accuracy(location.getRadius())
//                    // 此处设置开发者获取到的方向信息，顺时针0-360
//                    .direction(location.getDirection()).latitude(location.getLatitude())
//                    .longitude(location.getLongitude()).build();
//            mBaiduMap.setMyLocationData(locData);
            MyLocationData locData = new MyLocationData.Builder()
                    .accuracy(location.getRadius())
                    // 此处设置开发者获取到的方向信息，顺时针0-360
                    .direction(location.getDirection()).latitude(location.getLatitude())
                    .longitude(location.getLongitude()).build();
            mBaiduMap.setMyLocationData(locData);
            //判断是否是第一次定位
            if(isFirstLoc){
                //isFirstLoc = false;
                //将地图移动到定位的位置
                float f = mBaiduMap.getMaxZoomLevel();
                LatLng ll = new LatLng( 30.2489634, 120.2052342);
//                LatLng ll = new LatLng( location.getLatitude(),location.getLongitude() );
                MapStatusUpdate u = MapStatusUpdateFactory.newLatLngZoom( ll,f-2 );
                mBaiduMap.animateMapStatus( u );
                //一次定位结束，当再次点击定位时候即可回到自己所在位置
                mLocationClient.stop();
            }

            StringBuffer sb = new StringBuffer(256);
            sb.append( "time:" );
            sb.append(location.getTime());
            sb.append("\nerror code : ");
            sb.append(location.getLocType());
            sb.append("\nlatitude : ");
            sb.append(location.getLatitude());
            sb.append("\nlontitude : ");
            sb.append(location.getLongitude());
            sb.append("\nradius : ");
            sb.append(location.getRadius());
            if (location.getLocType() == BDLocation.TypeGpsLocation){
                //GPS定位
                sb.append("\nspeed : ");
                sb.append(location.getSpeed());
                sb.append("\nsatellite : ");
                sb.append(location.getSatelliteNumber());
                sb.append( "\nheight:" );
                sb.append( location.getAltitude() );
                sb.append( "\ndirection:" );
                sb.append( location.getDirection() );
                sb.append( "\naddr:" );
                sb.append( location.getAddrStr() );
                sb.append( "\ndescribe:" );
                sb.append( "GPS定位成功！" );
            } else if (location.getLocType() == BDLocation.TypeNetWorkLocation){
                sb.append("\naddr : ");
                sb.append(location.getAddrStr());
                sb.append( "\noperationer:" );
                sb.append( location.getOperators() );
                sb.append( "\ndescribe:" );
                sb.append( "网络定位成功" );
            }else if(location.getLocType() == BDLocation.TypeOffLineLocation){
                //离线定位
                sb.append( "\ndescribe:" );
                sb.append( "离线定位成功" );
            }else if(location.getLocType() == BDLocation.TypeServerError){
                sb.append( "\ndescribe:" );
                sb.append( "server定位失败，没有对应的位置信息" );
            }else if(location.getLocType() == BDLocation.TypeNetWorkException){
                sb.append( "\ndescribe:" );
                sb.append( "网络连接失败" );
            }
            Log.e("TEST",sb.toString());
        }
    }

}