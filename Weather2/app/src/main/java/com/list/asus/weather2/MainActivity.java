package com.list.asus.weather2;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.list.asus.weather2.Adapter.HourlyForecastAdapter;
import com.list.asus.weather2.Adapter.SpinnerAdapter;
import com.list.asus.weather2.gson.DailyForecast;
import com.list.asus.weather2.gson.Weather;
import com.list.asus.weather2.util.HttpUtil;
import com.list.asus.weather2.util.Utility;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    public DrawerLayout mDrawerLayout;
    private ScrollView weatherLayout;
    private Spinner titleCity;
    private TextView titleUpdateLocTime, titleUpdateUtcTime,
            degreeText, weatherInfoText, sensibleTempText,
            relativeHumidityText, precipitationText, windDirection1Text, windDirection2Text,
            aqiText, no2Text, o3Text, pm10Text, pm25Text, qltyText,so2Text,
            airText, comfortText, carWashText, sportText,
            dressSuggestionText, fluText, travelText, ultravioletText;
    private LinearLayout forecastDailyLayout;
    private ImageView backgroundPicImg;
    private ImageButton tip_drawerLayout;
    public SwipeRefreshLayout swipeRefreshLayout;

    private NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //对API进行判断
        if (Build.VERSION.SDK_INT >= 21) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        setContentView(R.layout.activity_main);
        initView();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String weatherString = prefs.getString("weather", null);
        final String weatherId;
        if (weatherString != null){
            //有缓存时直接解析数据
            Weather weather = Utility.handleWeatherResponse(weatherString);
            weatherId = weather.basic.weatherId;
        }else {
            //无缓存时去服务器查询天气
            weatherId = getIntent().getStringExtra("weather_id");
            requestWeather(weatherId);
        }
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                requestWeather(weatherId);
            }
        });

    }

    //初始化控件
    private void initView() {
        weatherLayout = (ScrollView) findViewById(R.id.weather_layout);
        titleCity = (Spinner) findViewById(R.id.title_city);
        titleUpdateLocTime = (TextView) findViewById(R.id.title_update_loc_time);
        titleUpdateUtcTime = (TextView) findViewById(R.id.title_update_utc_time);
        //Now
        degreeText = (TextView) findViewById(R.id.degree_text);
        sensibleTempText = (TextView) findViewById(R.id.sensible_temp_text);
        relativeHumidityText = (TextView) findViewById(R.id.relative_humidity_text);
        precipitationText = (TextView) findViewById(R.id.precipitation_text);
        windDirection1Text = (TextView) findViewById(R.id.wind_direction_text);
        windDirection2Text = (TextView) findViewById(R.id.wind_text);
        weatherInfoText = (TextView) findViewById(R.id.weather_into_text);
        //Daily布局
        forecastDailyLayout = (LinearLayout) findViewById(R.id.forecast_daily_layout);
        //AQI
        aqiText = (TextView) findViewById(R.id.aqi_text);
        no2Text = (TextView) findViewById(R.id.no2_text);
        o3Text = (TextView) findViewById(R.id.o3_text);
        pm10Text = (TextView) findViewById(R.id.pm10_text);
        pm25Text = (TextView) findViewById(R.id.pm25_text);
        qltyText = (TextView) findViewById(R.id.qlty_text);
        so2Text = (TextView) findViewById(R.id.so2_text);
        //Suggestion
        comfortText = (TextView) findViewById(R.id.comfort_text);
        carWashText = (TextView) findViewById(R.id.car_wash_text);
        sportText = (TextView) findViewById(R.id.sport_text);
        dressSuggestionText = (TextView) findViewById(R.id.dress_suggestion_text);
        fluText = (TextView) findViewById(R.id.flu_text);
        airText = (TextView) findViewById(R.id.air_text);
        travelText = (TextView) findViewById(R.id.travel_text);
        ultravioletText = (TextView) findViewById(R.id.ultraviolet_text);
        //背景
        backgroundPicImg = (ImageView) findViewById(R.id.background_pic);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        tip_drawerLayout = (ImageButton) findViewById(R.id.tip_drawerLayout);
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary);

        //设置titleCity的文字大小及颜色
        String[] cityId = getResources().getStringArray(R.array.cityId);
        SpinnerAdapter adapter = new SpinnerAdapter(this,
                android.R.layout.simple_spinner_item,cityId);
        titleCity.setAdapter(adapter);

        //spinner列表点击事件的监听
        titleCity.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                String[] cityId = getResources().getStringArray(R.array.cityId);
                requestWeather(cityId[position]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        //策划列表的监听


        //背景图片初始化
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String backgroundPic = prefs.getString("background_pic", null);
        if (backgroundPic != null){
            Glide.with(this).load(backgroundPic).into(backgroundPicImg);
        } else {
            loadBackgroundPic();
        }


        //侧滑提示符点击事件
        tip_drawerLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDrawerLayout.openDrawer(GravityCompat.START);
            }
        });
    }

    //根据城市id请求天气信息
    public void requestWeather(String weatherId) {
        String weatherURl = "https://free-api.heweather.com/v5/weather?city="
                + weatherId + "&key=2881ccb3103344c389011b756a3b2120";
        HttpUtil.sendOkHttpRequest(weatherURl, new Callback() {

            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, "获取天气信息失败",
                                Toast.LENGTH_SHORT).show();
                        swipeRefreshLayout.setRefreshing(false);
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseText = response.body().string();
                final Weather weather = Utility.handleWeatherResponse(responseText);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (weather != null && "ok".equals(weather.status)){
                            SharedPreferences.Editor editor = PreferenceManager
                                    .getDefaultSharedPreferences(MainActivity.this)
                                    .edit();
                            editor.putString("weather", responseText);
                            editor.apply();
                            showWeatherInfo(weather);
                        }else {
                            Toast.makeText(MainActivity.this, "获取天气信息失败",
                                    Toast.LENGTH_SHORT).show();
                        }
                        swipeRefreshLayout.setRefreshing(false);
                    }
                });
            }
        });
        //加载背景图片
        loadBackgroundPic();
    }

    //处理并展示Weather实体类中的数据
    private void showWeatherInfo(Weather weather) {
        //String cityName = weather.basic.cityName;
        String updateLocTime = "loc：" + weather.basic.update.updateLocTime;
        String updateUtcTime = "utc：" + weather.basic.update.updateUtcTime;
        String degree = weather.now.temperature + "℃";
        String sensibleTemp = "体感温度：" + weather.now.sensibleTemp + "℃";
        String relativeHumidity = "相对湿度：" + weather.now.relativeHumidity + "%";
        String precipitation = "降水量：" + weather.now.precipitation + "mm";
        String windDirection1 = "风向：" + weather.now.wind.windDirection1 + "°";
        String windDirection2 = "风向：" + weather.now.wind.windDirection2;
        String weatherInfo = weather.now.more.info;
        //titleCity.setText(cityName);
        titleUpdateLocTime.setText(updateLocTime);
        titleUpdateUtcTime.setText(updateUtcTime);
        degreeText.setText(degree);
        sensibleTempText.setText(sensibleTemp);
        relativeHumidityText.setText(relativeHumidity);
        precipitationText.setText(precipitation);
        windDirection1Text.setText(windDirection1);
        windDirection2Text.setText(windDirection2);
        weatherInfoText.setText(weatherInfo);
        //加载daily布局
        forecastDailyLayout.removeAllViews();
        for (DailyForecast dailyForecast : weather.dailyForecastList){
            View view = LayoutInflater.from(this).inflate(R.layout.forecast_daily_item,
                    forecastDailyLayout, false);
            TextView dateText = (TextView) view.findViewById(R.id.days_text);
            TextView infoText = (TextView) view.findViewById(R.id.info_text);
            TextView maxText = (TextView) view.findViewById(R.id.min_max_text);
            dateText.setText(String.valueOf(dailyForecast.date));
            dateText.setText(dailyForecast.date);
            infoText.setText(dailyForecast.more.info);
            maxText.setText(dailyForecast.temperature.min + "℃"
                    + "~" + dailyForecast.temperature.max + "℃");
            forecastDailyLayout.addView(view);
        }
        //加载Hourly布局
        RecyclerView recyclerView = (RecyclerView)
                findViewById(R.id.forecast_hourly_recycler_view_layout);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);  //实现横向布局
        recyclerView.setLayoutManager(layoutManager);
        HourlyForecastAdapter hourlyForecastAdapter =
                new HourlyForecastAdapter(weather.hourlyForecastList);
        recyclerView.setAdapter(hourlyForecastAdapter);
        if (weather.aqi != null){
            aqiText.setText(weather.aqi.city.aqi);
            no2Text.setText(weather.aqi.city.no2);
            o3Text.setText(weather.aqi.city.o3);
            pm10Text.setText(weather.aqi.city.pm10);
            pm25Text.setText(weather.aqi.city.pm25);
            qltyText.setText(weather.aqi.city.qlty);
            so2Text.setText(weather.aqi.city.so2);
        }
        String comfort = "舒适度：" + weather.suggestion.comfort.info1
                + "\n\n" + weather.suggestion.comfort.info;
        String carWash = "洗车指数：" + weather.suggestion.comfort.info1
                + "\n\n" + weather.suggestion.carWash.info;
        String sport = "运动指数：" + weather.suggestion.sport.info1
                + "\n\n" + weather.suggestion.sport.info;
        String dressSuggestion = "穿衣指数：" + weather.suggestion.dressSuggestion.info1
                + "\n\n" + weather.suggestion.dressSuggestion.info;
        String flu = "易发：" + weather.suggestion.flu.info1
                + "\n\n" + weather.suggestion.flu.info;
        String air = "空气条件：" + weather.suggestion.air.info1
                + "\n\n" + weather.suggestion.air.info;
        String ultraviolet = "紫外线：" + weather.suggestion.ultraviolet.info1
                + "\n\n" + weather.suggestion.ultraviolet.info;
        String travel = "旅行指数：" + weather.suggestion.travel.info1
                + "\n\n" + weather.suggestion.travel.info;
        comfortText.setText(comfort);
        carWashText.setText(carWash);
        sportText.setText(sport);
        dressSuggestionText.setText(dressSuggestion);
        fluText.setText(flu);
        airText.setText(air);
        ultravioletText.setText(ultraviolet);
        travelText.setText(travel);
        weatherLayout.setVisibility(View.VISIBLE);
    }

    //加载背景图片
    public void loadBackgroundPic() {
        String requestBackgroungPic = "http://guolin.tech/api/bing_pic";
        HttpUtil.sendOkHttpRequest(requestBackgroungPic, new Callback() {

            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String backgroundPic = response.body().string();
                SharedPreferences.Editor editor = PreferenceManager
                        .getDefaultSharedPreferences(MainActivity.this).edit();
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

}
