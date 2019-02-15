package com.example.wangx.chatonly;


import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;
import com.example.wangx.chatonly.server.DataBaseManager;
import com.example.wangx.chatonly.server.SecuityManager;
import com.example.wangx.chatonly.server.ServerManager;
import com.example.wangx.chatonly.util.LogUtil;

import org.litepal.LitePal;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LBSActivity extends AppCompatActivity {
    public LocationClient mLocationClient;
    private BDLocation location;
    private MapView mapView;
    private BaiduMap baiduMap;
    private BitmapDescriptor mMarker;
    private boolean isFirstLocate = true;
    private boolean isLogin = false;
    private String chatobj_address,chatobj,label;
    private ServerManager serverManager = ServerManager.getServerManager();
    private LogUtil Log = new LogUtil();
    private boolean inserted = true;
    private String add1,add2;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mLocationClient = new LocationClient(getApplicationContext());
        mLocationClient.registerLocationListener(new MyLocationListener());
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_lbs);
        mapView = (MapView) findViewById(R.id.bmapView);
        mMarker = BitmapDescriptorFactory.fromResource(R.drawable.location);
        baiduMap = mapView.getMap();
        //普通地图
        baiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
        baiduMap.setMyLocationEnabled(true);
        Toolbar toolbar = (Toolbar)findViewById(R.id.toobar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar!=null){
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        baiduMap.setOnMarkerClickListener(new BaiduMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                Intent intent = new Intent(LBSActivity.this,ChatActivity.class);
                intent.putExtra("chatobj",chatobj);
                intent.putExtra("contextname","fromlbsactivity");
                startActivity(intent);
                finish();
                return true;
            }
        });
        baiduMap.setOnMapClickListener(new BaiduMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                double la = latLng.latitude;
                double lo = latLng.longitude;
                if (Math.abs(la - location.getLatitude()) < 0.001 && Math.abs(lo - location.getLongitude()) < 0.001) {
                    Toast.makeText(LBSActivity.this, "You clicked latLng", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(LBSActivity.this, "You clicked Baidumap", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public boolean onMapPoiClick(MapPoi mapPoi) {
                return false;
            }
        });
        List<String> permissionList = new ArrayList<>();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.READ_PHONE_STATE);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (!permissionList.isEmpty()) {
            String[] permissions = permissionList.toArray(new String[permissionList.size()]);
            ActivityCompat.requestPermissions(this, permissions, 1);
        } else {
            requestLocation();
        }
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case android.R.id.home:
                finish();
                break;
        }
        return true;
    }
    private String Encryption(String code,String index)
    {
        String new_str = "";
        if(code == null||code ==""){
            return "";
        }
        //加密
        for(int i = 0;i<code.length();i++){
            char a = code.toCharArray()[i];
            char a1 = (char)((int)a+Integer.valueOf(index));
            new_str+=a1;
        }
        return new_str;
    }
    private String Deciphering(String code,String index){
        if(code == null||code ==""){
            return "";
        }
        //解密
        String a_back = "";
        for(int i = 0;i<code.length();i++){
            char a = code.toCharArray()[i];
            char a1 = (char)((int)a-Integer.valueOf(index));
            a_back+=a1;
        }
        return a_back;
    }
    private boolean login() {
        if (isLogin){
            return true;
        }
        else {
            // send msg to servers
            String username_send = getIntent().getStringExtra("username_send");
            String address="";
            Address address1;
            try{
                add1 = String.valueOf(location.getLatitude());
                add2 = String.valueOf(location.getLongitude());
                address = add1+","+add2;
                if (inserted){
                    address1= new Address();
                    address1.setId(1);
                    address1.setAddress1(add1);
                    address1.setAddress2(add2);
                    address1.save();
                    inserted = false;
                }
                else {
                    //更新数据库
                    address1 = LitePal.find(Address.class,1);
                    address1.setId(1);
                    address1.setAddress1(add1);
                    address1.setAddress2(add2);
                    address1.save();
                }
            }
            catch (Exception e){
                address = "null,null";
            }
            String msg = "[LOGIN]:[" + username_send +","+ address + "]";
            //加密
            String index = String.valueOf(username_send.length());
            msg = Encryption(msg,index);
            serverManager.setMessage(null,msg,null);
            serverManager.sendMessage();
            // get msg from servers return
            String ack = serverManager.getMessage();
            //解密
            ack = Deciphering(ack,index);
            Log.d("username","ack : "+String.valueOf(ack));
            // deal msg
            if (ack == null||ack=="") {
                return false;
            }
            serverManager.setMessage(null,null,null);
            String p = "\\[ACKLOGIN\\]:\\[(.*),(.*),(.*),(.*)\\]";
            Pattern pattern = Pattern.compile(p);
            Matcher matcher = pattern.matcher(ack);
            if (matcher.find()){
                label = matcher.group(1);
                chatobj = matcher.group(2);
                chatobj_address = matcher.group(3)+","+matcher.group(4);
                Log.d("username","label : "+label);
                Log.d("username","chatobj : "+chatobj);
                Log.d("username","chatobj_address : "+chatobj_address);
            }
            if (chatobj.equals("nobody")){
                return false;
            }
            isLogin = true;
            return label.equals("1");
        }
    }

    private void navigateTo(final String chatobj_address) {
        String p = "(.*),(.*)";
        Pattern pattern = Pattern.compile(p);
        Matcher matcher = pattern.matcher(chatobj_address);
        String la="",lo="";
        if (matcher.find()) {
            la = matcher.group(1);
            lo = matcher.group(2);
        }
        Log.d("username","la : "+la);
        Log.d("username","lo : "+lo);
        if (isFirstLocate) {
            OverlayOptions options;
            // 经纬度
            LatLng ll = new LatLng(Double.parseDouble(la), Double.parseDouble(lo));
            // 图标
            options = new MarkerOptions().position(ll).icon(mMarker).zIndex(5);
            baiduMap.addOverlay(options);
            MapStatusUpdate update = MapStatusUpdateFactory.newLatLng(ll);
            baiduMap.animateMapStatus(update);
            update = MapStatusUpdateFactory.zoomTo(16f);
            baiduMap.animateMapStatus(update);
            isFirstLocate = false;
        }

    }
    private void navigateTo1(final BDLocation location) {
        if (isFirstLocate) {
            LatLng ll;
            // 经纬度
            ll = new LatLng((location.getLatitude()), (location.getLongitude() ));
            MapStatusUpdate update = MapStatusUpdateFactory.newLatLng(ll);
            baiduMap.animateMapStatus(update);
            update = MapStatusUpdateFactory.zoomTo(16f);
            baiduMap.animateMapStatus(update);
            isFirstLocate = false;
        }
        MyLocationData.Builder locationBuilder = new MyLocationData.Builder();
        locationBuilder.latitude(location.getLatitude());
        locationBuilder.longitude(location.getLongitude());
        MyLocationData locationData = locationBuilder.build();
        baiduMap.setMyLocationData(locationData);
    }
    private void requestLocation() {
        initLocation();
        mLocationClient.start();
    }

    private void initLocation() {
        LocationClientOption option = new LocationClientOption();
        option.setScanSpan(5000);
        option.setIsNeedAddress(true);
        mLocationClient.setLocOption(option);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mLocationClient.stop();
        mapView.onDestroy();
        baiduMap.setBuildingsEnabled(false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0) {
                    for (int result : grantResults) {
                        if (result != PackageManager.PERMISSION_DENIED) {
                            Toast.makeText(this, "必须同意权限才能使用本程序", Toast.LENGTH_SHORT).show();
                            finish();
                            return;
                        }
                    }
                    requestLocation();
                } else {
                    Toast.makeText(this, "未知错误", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            default:
        }
    }

    public class MyLocationListener implements BDLocationListener {
        @Override
        public void onReceiveLocation(final BDLocation bdLocation) {
            if (bdLocation.getLocType() == BDLocation.TypeGpsLocation || bdLocation.getLocType() == BDLocation.TypeNetWorkLocation) {
                location = bdLocation;
                if (login()){
                    navigateTo(chatobj_address);
                }
                else {
                    navigateTo1(bdLocation);
                }
            }
        }
    }

}
