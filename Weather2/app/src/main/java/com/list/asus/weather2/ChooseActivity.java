package com.list.asus.weather2;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

public class ChooseActivity extends AppCompatActivity {

    private ArrayList<String> ChoosedList = new ArrayList<>();
    private LocationClient mLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose);

        final DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        TextView addSlipText = (TextView) findViewById(R.id.add_slip_textview);
        TextView backText = (TextView) findViewById(R.id.back_text);
        //侧滑提示
        addSlipText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(GravityCompat.END);
            }
        });
        //返回键
        backText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ChooseActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
        ToggleButton lbsButton = (ToggleButton) findViewById(R.id.lbs_button);
        lbsButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    initLbs();
                    Log.d("ischecked","123");
                }else {
                    Log.d("ischecked","456");
                }

            }
        });

        /*
        *从C中初始化ChoosedList
        */
        ChoosedList = C.cityNameArry;
        saveArray(C.cityNameArry);


        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                ChooseActivity.this, android.R.layout.simple_list_item_1, ChoosedList);
        ListView listView =(ListView) findViewById(R.id.choose_activity_liet_view);
        listView.setAdapter(adapter);

        //listview长按监听
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, final View view,
                                           final int position, long id) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ChooseActivity.this);
                builder.setTitle("提示");
                builder.setMessage("确定删除");
                builder.setNegativeButton("确认",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                C.cityNameArry.remove(position);
                                Log.d("remove","arry"+C.cityNameArry);
                                ChooseActivity.actionStart(ChooseActivity.this);
                            }
                        });
                builder.setPositiveButton("取消", null);
                builder.create().show();
                return true;
            }
        });


    }

    public static void actionStart(Context context) {
        Intent intent = new Intent(context, ChooseActivity.class);
        context.startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(ChooseActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
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
        }

        @Override
        public void onConnectHotSpotMessage(String s, int i) {
            return;
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mLocationClient.stop();  //停止定位
    }
    /**
    * ---------------------------------------------------------------------------------------------
    **/
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
