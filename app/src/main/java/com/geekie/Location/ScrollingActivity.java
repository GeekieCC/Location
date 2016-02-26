package com.geekie.Location;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;


public class ScrollingActivity extends AppCompatActivity implements
        RadioGroup.OnCheckedChangeListener, View.OnClickListener, AMapLocationListener {

    private static final int CHECK_INTERVAL = 1000 * 10;
    private static final String TAG = "LocationTagInfo";

    private RadioGroup rgLocation;
    private RadioButton rbLocationContinue;
    private RadioButton rbLocationOnce;
    private View layoutInterval;
    private EditText etInterval;
    private CheckBox cbAddress;
    private CheckBox cbGpsFirst;
    private TextView tvReult;
    private TextView tvStatus;
    private boolean isLocationStarted=false;

    private FloatingActionButton fab;

    private AMapLocationClient locationClient = null;
    private AMapLocationClientOption locationOption = null;

    private AMapLocation currentLocation=null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scrolling);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        fab = (FloatingActionButton) findViewById(R.id.fab);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //获取定位服务
                LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                //判断GPS是否正常启动
                if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    Toast.makeText(getApplicationContext(), "请开启精确定位模式...", Toast.LENGTH_SHORT).show();
                    //返回开启GPS导航设置界面
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivityForResult(intent, 0);
                    //return;
                }


            }
        });


        rgLocation = (RadioGroup) findViewById(R.id.rg_location);
        rbLocationContinue = (RadioButton)findViewById(R.id.rb_continueLocation);
        rbLocationOnce = (RadioButton)findViewById(R.id.rb_onceLocation);
        layoutInterval = findViewById(R.id.layout_interval);
        etInterval = (EditText) findViewById(R.id.et_interval);
        cbAddress = (CheckBox) findViewById(R.id.cb_needAddress);
        cbGpsFirst = (CheckBox) findViewById(R.id.cb_gpsFirst);
        tvReult = (TextView) findViewById(R.id.tv_result);
        tvStatus=(TextView) findViewById(R.id.tv_status);

        rgLocation.setOnCheckedChangeListener(this);
        fab.setOnClickListener(this);

        locationClient = new AMapLocationClient(this.getApplicationContext());
        locationOption = new AMapLocationClientOption();
        // 设置定位模式为高精度模式
        locationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        // 设置定位监听
        locationClient.setLocationListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (null != locationClient) {
            /**
             * 如果AMapLocationClient是在当前Activity实例化的，
             * 在Activity的onDestroy中一定要执行AMapLocationClient的onDestroy
             */
            locationClient.onDestroy();
            locationClient = null;
            locationOption = null;
        }
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        switch (checkedId) {
            case R.id.rb_continueLocation:
                //只有持续定位设置定位间隔才有效，单次定位无效
                layoutInterval.setVisibility(View.VISIBLE);
                //只有在高精度模式单次定位的情况下，GPS优先才有效
                cbGpsFirst.setVisibility(View.GONE);
                locationOption.setOnceLocation(false);
                break;
            case R.id.rb_onceLocation:
                //只有持续定位设置定位间隔才有效，单次定位无效
                layoutInterval.setVisibility(View.GONE);
                //只有在高精度模式单次定位的情况下，GPS优先才有效
                cbGpsFirst.setVisibility(View.VISIBLE);
                locationOption.setOnceLocation(true);
                break;
        }
    }

    /**
     * 设置控件的可用状态
     */
    private void setViewEnable(boolean isEnable) {
        rbLocationContinue.setEnabled(isEnable);
        rbLocationOnce.setEnabled(isEnable);
        etInterval.setEnabled(isEnable);
        cbAddress.setEnabled(isEnable);
        cbGpsFirst.setEnabled(isEnable);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.fab) {
            if (!isLocationStarted) {
                setViewEnable(false);
                initOption();
                // 设置定位参数
                locationClient.setLocationOption(locationOption);
                // 启动定位
                locationClient.startLocation();
                tvReult.setText("");
                mHandler.sendEmptyMessage(Utils.MSG_LOCATION_START);
            } else {
                setViewEnable(true);
                // 停止定位
                locationClient.stopLocation();
                mHandler.sendEmptyMessage(Utils.MSG_LOCATION_STOP);
            }
            isLocationStarted=!isLocationStarted;
        }
    }

    // 根据控件的选择，重新设置定位参数
    private void initOption() {
        // 设置是否需要显示地址信息
        locationOption.setNeedAddress(cbAddress.isChecked());
        /**
         * 设置是否优先返回GPS定位结果，如果30秒内GPS没有返回定位结果则进行网络定位
         * 注意：只有在高精度模式下的单次定位有效，其他方式无效
         */
        locationOption.setGpsFirst(cbGpsFirst.isChecked());
        String strInterval = etInterval.getText().toString();
        if (!TextUtils.isEmpty(strInterval)) {
            // 设置发送定位请求的时间间隔,最小值为1000，如果小于1000，按照1000算
            locationOption.setInterval(Long.valueOf(strInterval));
        }

    }

    Handler mHandler = new Handler() {
        public void dispatchMessage(android.os.Message msg) {
            switch (msg.what) {
                //开始定位
                case Utils.MSG_LOCATION_START:
                    tvStatus.setText("...正在定位...");
                    //tvReult.setText("");
                    break;
                // 定位完成
                case Utils.MSG_LOCATION_FINISH:
                    AMapLocation location = (AMapLocation) msg.obj;

                    if (currentLocation != null) {
                        String result = Utils.getLocationStr(location);

                        if (isBetterLocation(location, currentLocation)) {
                            Log.i(TAG, "It's a better location");
                            tvReult.setText("+++定位更新+++");
                            tvReult.append(result);
                            currentLocation = location;
                        } else {
                            Log.i(TAG, "Not very good!");
                            tvReult.append("\n***无效定位***");
                            tvReult.append(result);
                        }
                        tvStatus.setText("...location...");

                    } else {
                        Log.i(TAG, "It's first location");
                        currentLocation = location;
                        tvStatus.setText("...首次定位中...");
                    }

                    fabBlin();
                    break;
                //停止定位
                case Utils.MSG_LOCATION_STOP:
                    tvStatus.setText("===定位停止===");
                    break;
                default:
                    break;
            }
        };
    };

    // 定位监听
    @Override
    public void onLocationChanged(AMapLocation loc) {
        if (null != loc) {
            Message msg = mHandler.obtainMessage();
            msg.obj = loc;
            msg.what = Utils.MSG_LOCATION_FINISH;
            mHandler.sendMessage(msg);
        }
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
        boolean isSignificantlyLessAccurate = (location.getAccuracy()/currentBestLocation.getAccuracy()) > 1.5;

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

    private void fabBlin() {
        if (fab.getVisibility() == View.VISIBLE) {

            fab.setVisibility(View.INVISIBLE);
            new Handler().postDelayed(new Runnable() {

                @Override
                public void run() {
                    fab.setVisibility(View.VISIBLE);
                }
            }, 200);
        }
    }
}
