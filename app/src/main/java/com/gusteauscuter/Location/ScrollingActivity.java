package com.gusteauscuter.Location;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.BoringLayout;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Iterator;

public class ScrollingActivity extends AppCompatActivity {

    private static final int CHECK_INTERVAL = 1000 * 20;
    private static final int NETWORK_LISTENER_INTERVAL = 1000 * 1;
    private static final int GPS_LISTENER_INTERVAL = 1000 * 2;
    private static final float MIN_DISTANCE = 0;

    private int timesOfLocationUpdate = 0;
    private int timesOfGpsUpdate = 0;
    private int timesOfNetworkUpdate = 0;
    private int timesSatelliteStatus = 0;
    private int countSatellites = 0;

    private boolean isColletStarted = false;
    //代码中慎用此项判断条件
    private boolean locationExist = true;

    private TextView currentLocationInfoTV;
    private TextView controlInfoTV;
    private TextView satellitesInfoTV;
    private FloatingActionButton fab;

    private static final String TAG = "LocationTagInfo";

    private LocationManager locationManager;
    private LocationListener gpsListener = null;
    private LocationListener networkListner = null;
    private Location currentLocation;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopLocationListener();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scrolling);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        controlInfoTV = (TextView) findViewById(R.id.controlInfoTV);
        currentLocationInfoTV = (TextView) findViewById(R.id.currentLocationInfoTV);
        satellitesInfoTV = (TextView) findViewById(R.id.satellitesInfoTV);
        fab = (FloatingActionButton) findViewById(R.id.fab);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //获取定位服务
                locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

                //开启GPS信息获取后台服务
                if (!isColletStarted) {
                    isColletStarted = true;
                    //注册监听事件
                    registerLocationListener();

                    Snackbar.make(view, "Start GPS Information Collect Service", Snackbar.LENGTH_SHORT)
                            .setAction("Action", null).show();

                } else {
                    isColletStarted = false;

                    //更新控制信息
                    updateControlInfo();
                    //注销监听
                    stopLocationListener();

                    Snackbar.make(view, "Stop GPS Information Collect Service", Snackbar.LENGTH_SHORT)
                            .setAction("Action", null).show();
                }
            }
        });
    }

    private void registerLocationListener() {

        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(getApplicationContext(), "应用未获取权限，请开启应用权限后重试...", Toast.LENGTH_SHORT).show();
            return;
        }
        //监听gps状态
        locationManager.addGpsStatusListener(listenerGpsStatus);
        networkListner = new MyLocationListner();
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, NETWORK_LISTENER_INTERVAL, MIN_DISTANCE, networkListner);
        gpsListener = new MyLocationListner();
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, GPS_LISTENER_INTERVAL, MIN_DISTANCE, gpsListener);
    }

    private void stopLocationListener() {

        fab.setVisibility(View.VISIBLE);

        timesOfLocationUpdate = 0;
        timesOfGpsUpdate = 0;
        timesOfNetworkUpdate = 0;
        timesSatelliteStatus = 0;
        //关闭服务
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "关闭监听服务,没有LocationManager权限");
            return;
        }
        locationManager.removeUpdates(networkListner);
        locationManager.removeUpdates(gpsListener);
        locationManager.removeGpsStatusListener(listenerGpsStatus);
        Log.e(TAG, "===成功关闭监听服务");
    }


    //位置监听
    private class MyLocationListner implements LocationListener {

        /**
         * 位置信息变化时触发
         */
        public void onLocationChanged(Location location) {

            if (LocationManager.GPS_PROVIDER.equals(location.getProvider()))
                timesOfGpsUpdate++;
            else if (LocationManager.NETWORK_PROVIDER.equals(location.getProvider()))
                timesOfNetworkUpdate++;

            // Called when a new location is found by the location provider.
            Log.i(TAG, "Got New Location of provider:" + location.getProvider());
            if (currentLocation != null) {
                if (isBetterLocation(location, currentLocation)) {
                    Log.i(TAG, "It's a better location");
                    currentLocation = location;
                    updateLocationInfo(location);
                } else {
                    Log.i(TAG, "Not very good!");
                }
            } else {
                Log.i(TAG, "It's first location");
                currentLocation = location;
                updateLocationInfo(location);
            }

            fabBlin();

        }

        /**
         * GPS状态变化时触发
         */
        public void onStatusChanged(String provider, int status, Bundle extras) {
            switch (status) {
                //GPS状态为可见时
                case LocationProvider.AVAILABLE:
                    Log.i(TAG, "当前GPS状态为可见状态");
                    break;
                //GPS状态为服务区外时
                case LocationProvider.OUT_OF_SERVICE:
                    Log.i(TAG, "当前GPS状态为服务区外状态");
                    break;
                //GPS状态为暂停服务时
                case LocationProvider.TEMPORARILY_UNAVAILABLE:
                    Log.i(TAG, "当前GPS状态为暂停服务状态");
                    break;
            }
        }

        /**
         * GPS开启时触发
         */
        public void onProviderEnabled(String provider) {
            satellitesInfoTV.setText("GPS开启，定位中...");
        }

        /**
         * GPS禁用时触发
         */
        public void onProviderDisabled(String provider) {
            //updateLocationInfo(null);
            satellitesInfoTV.setText("GPS关闭，无GPS信息...");

        }

    }

    ;

    //状态监听
    GpsStatus.Listener listenerGpsStatus = new GpsStatus.Listener() {

        public void onGpsStatusChanged(int event) {
            timesSatelliteStatus++;
            switch (event) {
                //第一次定位
                case GpsStatus.GPS_EVENT_FIRST_FIX:
                    Log.i(TAG, "第一次定位");
                    break;
                //卫星状态改变
                case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
                    Log.i(TAG, "卫星状态改变");
                    updateSatellitesInfo();
                    break;
                //定位启动
                case GpsStatus.GPS_EVENT_STARTED:
                    Log.i(TAG, "定位启动");
                    break;
                //定位结束
                case GpsStatus.GPS_EVENT_STOPPED:
                    Log.i(TAG, "定位结束");
                    break;
            }
            updateControlInfo();

            fabBlin();
        }

        ;
    };

    private void fabBlin(){
        if(fab.getVisibility()==View.VISIBLE) {
            fab.setVisibility(View.INVISIBLE);
        }else{
            fab.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 实时更新文本内容
     *
     * @param location
     */
    private void updateLocationInfo(Location location) {
        if (location != null) {
            locationExist = true;
            timesOfLocationUpdate++;

            currentLocationInfoTV.setText("====位置信息===");
            currentLocationInfoTV.append("\n@来源 ：" + location.getProvider());
            currentLocationInfoTV.append("\t\t精度 ：" + location.getAccuracy() + "m");

            currentLocationInfoTV.append("\n@经度 ：" + String.valueOf(location.getLongitude()));
            currentLocationInfoTV.append("\n@纬度 ：" + String.valueOf(location.getLatitude()));
            currentLocationInfoTV.append("\n@海拔 ：" + location.getAltitude() + "m");
            currentLocationInfoTV.append("\n@速度 ：" + location.getSpeed() + "m/s");
            currentLocationInfoTV.append("\n@方向 ：" + location.getBearing());
            //currentLocationInfoTV.append("\n格林威治时间：" + 1000*location.getTime());
            //currentLocationInfoTV.append("\n系统上电时间：     " +location.getElapsedRealtimeNanos());
            //currentLocationInfoTV.append("\n额外来源：" +location.getExtras());

            Log.i(TAG, "时间：" + location.getTime());
            Log.i(TAG, "经度：" + location.getLongitude());
            Log.i(TAG, "纬度：" + location.getLatitude());
            Log.i(TAG, "海拔：" + location.getAltitude());

            updateControlInfo();
        } else {
            //定位失败提示信息
            //locationExist=false;
            currentLocationInfoTV.setText("信噪比过低，移步开阔地段，重试...");
            Log.i(TAG, "定位失败！");
        }
    }

    private void updateSatellitesInfo() {
        GpsStatus gpsStatus = locationManager.getGpsStatus(null);
        //获取卫星颗数的默认最大值
        int maxSatellites = gpsStatus.getMaxSatellites();
        //创建一个迭代器保存所有卫星
        Iterator<GpsSatellite> iters = gpsStatus.getSatellites().iterator();
        if (iters.hasNext()) {
            satellitesInfoTV.setText("===卫星数据===");
        }
        countSatellites = 0;
        while (iters.hasNext() && countSatellites <= maxSatellites) {
            countSatellites++;
            GpsSatellite s = iters.next();
            if (countSatellites < 10)
                satellitesInfoTV.append("\n#卫星0" + countSatellites);
            else
                satellitesInfoTV.append("\n#卫星" + countSatellites);
            satellitesInfoTV.append("\t\t\t方向角" + s.getAzimuth());
            satellitesInfoTV.append("\t\t高度角" + s.getElevation());
            satellitesInfoTV.append("\t\t信噪比" + s.getSnr());
            //satellitesInfoTV.append("\t\t伪随机数" +s.getPrn());
        }
        updateControlInfo();
    }

    private void updateControlInfo() {
        controlInfoTV.setText("===当前状态===");
        if (!locationExist) {
            controlInfoTV.setText("获取定位信息失败！请检查...");
        } else {
            if (isColletStarted) {
                controlInfoTV.append("实时更新中...");
                //controlInfoTV.append("卫星监听次数：" + timesSatelliteStatus);

            }else
                controlInfoTV.append("更新停止.");
        }
        controlInfoTV.append("\n有效位置次数：" + timesOfLocationUpdate );
        controlInfoTV.append("\t\t\t最小距离：" + MIN_DISTANCE + "m");
        controlInfoTV.append("\n网络位置更新：" + timesOfNetworkUpdate);
        controlInfoTV.append("\t\t\t监听周期：" + NETWORK_LISTENER_INTERVAL / 1000 + "s");
        //controlInfoTV.append("\t\t\t网络监听：" + timesSatelliteStatus*GPS_LISTENER_INTERVAL/NETWORK_LISTENER_INTERVAL);
        controlInfoTV.append("\n卫星位置更新：" + timesOfGpsUpdate );
        controlInfoTV.append("\t\t\t监听周期：" + GPS_LISTENER_INTERVAL / 1000 + "s");
        //controlInfoTV.append("\t\t\t卫星监听：" + timesSatelliteStatus);
        controlInfoTV.append("\n搜索卫星数量：" + countSatellites);
        controlInfoTV.append("\n卫星监听：" + timesSatelliteStatus);

    }

    /**
     * 返回查询条件
     *
     * @return
     */
    private Criteria getCriteria() {
        Criteria criteria = new Criteria();
        //设置定位精确度 Criteria.ACCURACY_COARSE比较粗略，Criteria.ACCURACY_FINE则比较精细
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        //设置是否要求速度
        criteria.setSpeedRequired(false);
        //设置是否需要方位信息
        criteria.setBearingRequired(false);
        //设置是否需要海拔信息
        criteria.setAltitudeRequired(false);
        // 设置对电源的需求
        criteria.setPowerRequirement(Criteria.POWER_LOW);
        // 设置是否允许运营商收费
        criteria.setCostAllowed(false);
        return criteria;
    }


    protected boolean isBetterLocation(Location location, Location currentBestLocation) {
        if (currentBestLocation == null) {
            // A new location is always better than no location
            return true;
        }

        // Check whether the new location fix is newer or older
        long timeDelta = location.getTime() - currentBestLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > CHECK_INTERVAL;
        boolean isSignificantlyOlder = timeDelta < -CHECK_INTERVAL;
        boolean isNewer = timeDelta > 0;

        // If it's been more than two minutes since the current location,
        // use the new location
        // because the user has likely moved
        if (isSignificantlyNewer) {
            return true;
            // If the new location is more than two minutes older, it must
            // be worse
        } else if (isSignificantlyOlder) {
            return false;
        }

        // Check whether the new location fix is more or less accurate
        int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation
                .getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 200;

        // Check if the old and new location are from the same provider
        boolean isFromSameProvider = isSameProvider(location.getProvider(),
                currentBestLocation.getProvider());

        // Determine location quality using a combination of timeliness and
        // accuracy
        if (isMoreAccurate) {
            return true;
        } else if (isNewer && !isLessAccurate) {
            return true;
        } else if (isNewer && !isSignificantlyLessAccurate
                && isFromSameProvider) {
            return true;
        }
        return false;
    }

    /**
     * Checks whether two providers are the same
     */
    private boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null) {
            return provider2 == null;
        }
        return provider1.equals(provider2);
    }

}
