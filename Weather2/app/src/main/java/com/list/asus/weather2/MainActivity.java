package com.list.asus.weather2;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.bumptech.glide.Glide;
import com.list.asus.weather2.Adapter.FragAdapter;
import com.list.asus.weather2.fragment.Fragments;
import com.list.asus.weather2.util.HttpUtil;

import org.json.JSONArray;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static android.preference.PreferenceManager.getDefaultSharedPreferences;


public class MainActivity extends FragmentActivity {


    private ViewPager viewPager;
    private ImageView backgroundPicImg;
    private LocationClient mLocationClient;
    private boolean flag = true;  //标志变量

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getArray();
        //对API进行判断
        if (Build.VERSION.SDK_INT >= 21) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        setContentView(R.layout.activity_main);

        initViewPager();

        //背景图片初始化
        backgroundPicImg = (ImageView) findViewById(R.id.background_pic);
        SharedPreferences prefs = getDefaultSharedPreferences(this);
        String backgroundPic = prefs.getString("background_pic", null);
        if (backgroundPic != null){
            Glide.with(this).load(backgroundPic).into(backgroundPicImg);
        } else {
            loadBackgroundPic();
        }

        //FloatingActionButton的点击监听事件
        FloatingActionButton floatingActionButton = (FloatingActionButton) findViewById(R.id.add_floatingActionButton);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ChooseActivity.actionStart(MainActivity.this);
                finish();
            }
        });

    }

    private void initViewPager() {
        if (flag) {
            initLbs();
            flag = false;
        }

        viewPager = (ViewPager) findViewById(R.id.view_pager);
        List<Fragment> fragList = new ArrayList<Fragment>();
        for (String arrayList : C.cityNameArry) {
            Fragments frag = new Fragments();
            frag.setweaId(arrayList);
            Log.d("123456","view main:" + arrayList);
            fragList.add(frag);
        }
        FragAdapter adapter = new FragAdapter(getSupportFragmentManager(),
                fragList);
        viewPager.setAdapter(adapter);
    }

    //加载背景图片
    public void loadBackgroundPic() {
        String requestBackgroundPic = "http://guolin.tech/api/bing_pic";
        HttpUtil.sendOkHttpRequest(requestBackgroundPic, new Callback() {

            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String backgroundPic = response.body().string();
                SharedPreferences.Editor editor =
                        getDefaultSharedPreferences(MainActivity.this).edit();
                editor.putString("backgroundPic", backgroundPic);
                editor.apply();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Glide.with(MainActivity.this).load(backgroundPic).into(backgroundPicImg);
                    }
                });
            }
        });
    }

   /**
     * --------------------------------------------------------------------------------------------
     **/
    //获取位置，定位
    private void initLbs() {
        mLocationClient = new LocationClient(this.getApplicationContext());
        mLocationClient.registerLocationListener(new MyLocationListener());  //注册定位监听器
        List<String> permissionList = new ArrayList<>();  //将没有授权的添加到List集合中
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.READ_PHONE_STATE);
        }
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (!permissionList.isEmpty()) {
            //将List再转换为数组，并通过ActivityCompat提交
            String [] permissions = permissionList.toArray(new String[permissionList.size()]);
            ActivityCompat.requestPermissions(this, permissions, 1);
        } else {
            requestLocation();
        }
    }
    //开始定位
    private void requestLocation() {
        initLocation();
        mLocationClient.start();
    }

    private void initLocation() {
        LocationClientOption option = new LocationClientOption();
        //option.setScanSpan(5000);  //每5秒更新一下位置,设置更新的时间间隔
        option.setIsNeedAddress(true);  //获取当前位置的详细地址信息
        mLocationClient.setLocOption(option);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0) {
                    for (int result : grantResults) {
                        if (result != PackageManager.PERMISSION_GRANTED) {
                            Toast.makeText(this, "必须同意所有权限才能使用本程序",
                                    Toast.LENGTH_SHORT).show();
                            finish();
                            return;
                        }
                    }
                    requestLocation();
                } else {
                    Toast.makeText(this, "发生未知错误", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            default:
        }
    }
    //监听器
    public class MyLocationListener implements BDLocationListener {

        @Override
        public void onReceiveLocation(BDLocation bdLocation) {
            String weatherLocation = bdLocation.getCity();
            C.add(C.cityNameArry,weatherLocation);
            saveArray(C.cityNameArry);
        }

        @Override
        public void onConnectHotSpotMessage(String s, int i) {
            return;
        }


    }

    @Override
    public void onBackPressed() {
        finish();
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mLocationClient.stop();  //停止定位
    }


    //------------------------------读取选择过的城市---------------------------------------
    //读取数据
    public  void getArray() {
        SharedPreferences prefs = getSharedPreferences("choosedCityArray",MODE_PRIVATE);
        try {
            JSONArray jsonArray = new JSONArray(prefs.getString("choosedCityArray", null));
            if (jsonArray != null){
                for (int i = 0; i < jsonArray.length(); i++) {
                    C.add(C.cityNameArry,jsonArray.getString(i));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public  void saveArray(ArrayList<String> StringArray) {
        JSONArray jsonArray = new JSONArray();
        for (String b : StringArray) {
            jsonArray.put(b);
        }
        SharedPreferences.Editor editor =
                getSharedPreferences("choosedCityArray",MODE_PRIVATE).edit();
        editor.putString("choosedCityArray",jsonArray.toString());
        editor.apply();
    }

}
