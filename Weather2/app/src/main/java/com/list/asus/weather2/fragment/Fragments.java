package com.list.asus.weather2.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.list.asus.weather2.Adapter.HourlyForecastAdapter;
import com.list.asus.weather2.R;
import com.list.asus.weather2.gson.DailyForecast;
import com.list.asus.weather2.gson.Weather;
import com.list.asus.weather2.service.AutoUpdateService;
import com.list.asus.weather2.util.HttpUtil;
import com.list.asus.weather2.util.Utility;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by HP on 2017/2/16.
 */

public class Fragments extends Fragment {

    private String weatherIdCity;
    public  void setweaId(String id){
        weatherIdCity = id;
    }

    private ScrollView weatherLayout;
    private TextView titleUpdateLocTime, titleUpdateUtcTime, cityNameText,
            degreeText, weatherInfoText, sensibleTempText,
            relativeHumidityText, precipitationText, windDirection1Text, windDirection2Text,
    //AQI
    aqiText, no2Text, o3Text, pm10Text, pm25Text, qltyText,so2Text,
    //Suggestion
    airText, comfortText, carWashText, sportText,
            dressSuggestionText, fluText, travelText, ultravioletText;
    private LinearLayout forecastDailyLayout;
    public SwipeRefreshLayout swipeRefreshLayout;

    private View view;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.view_page_item, null);

        initView();
        Log.d("123","onC"+weatherIdCity);
        weatherLayout.setVisibility(View.INVISIBLE);
        requestWeather(weatherIdCity);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                requestWeather(weatherIdCity);
            }
        });
        return view;
    }

    //初始化控件
    private void initView() {
        weatherLayout = (ScrollView) view.findViewById(R.id.weather_layout);
        cityNameText = (TextView) view.findViewById(R.id.city_name_text);
        titleUpdateLocTime = (TextView) view.findViewById(R.id.title_update_loc_time);
        titleUpdateUtcTime = (TextView) view.findViewById(R.id.title_update_utc_time);
        //Now
        degreeText = (TextView) view.findViewById(R.id.degree_text);
        sensibleTempText = (TextView) view.findViewById(R.id.sensible_temp_text);
        relativeHumidityText = (TextView) view.findViewById(R.id.relative_humidity_text);
        precipitationText = (TextView) view.findViewById(R.id.precipitation_text);
        windDirection1Text = (TextView) view.findViewById(R.id.wind_direction_text);
        windDirection2Text = (TextView) view.findViewById(R.id.wind_text);
        weatherInfoText = (TextView) view.findViewById(R.id.weather_into_text);
        //Daily布局
        forecastDailyLayout = (LinearLayout) view.findViewById(R.id.forecast_daily_layout);
        //AQI
        aqiText = (TextView) view.findViewById(R.id.aqi_text);
        no2Text = (TextView) view.findViewById(R.id.no2_text);
        o3Text = (TextView) view.findViewById(R.id.o3_text);
        pm10Text = (TextView) view.findViewById(R.id.pm10_text);
        pm25Text = (TextView) view.findViewById(R.id.pm25_text);
        qltyText = (TextView) view.findViewById(R.id.qlty_text);
        so2Text = (TextView) view.findViewById(R.id.so2_text);
        //Suggestion
        comfortText = (TextView) view.findViewById(R.id.comfort_text);
        carWashText = (TextView) view.findViewById(R.id.car_wash_text);
        sportText = (TextView) view.findViewById(R.id.sport_text);
        dressSuggestionText = (TextView) view.findViewById(R.id.dress_suggestion_text);
        fluText = (TextView) view.findViewById(R.id.flu_text);
        airText = (TextView) view.findViewById(R.id.air_text);
        travelText = (TextView) view.findViewById(R.id.travel_text);
        ultravioletText = (TextView) view.findViewById(R.id.ultraviolet_text);
        //下拉刷新
        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_refresh);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary);

    }

    //根据城市id请求天气信息:先从缓存中寻找，找不到再去网上请求
    public void requestWeather(final String weatherId) {
        SharedPreferences pref = getActivity().getSharedPreferences("weatherData",MODE_PRIVATE);
        String responseText = pref.getString(weatherId,"");
        if ("".equals(responseText)) {             //从网上请求
            Log.d("123456", "req:" + weatherId);
            String weatherURl = "https://free-api.heweather.com/v5/weather?city="
                    + weatherId + "&key=2881ccb3103344c389011b756a3b2120";
            HttpUtil.sendOkHttpRequest(weatherURl, new Callback() {

                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getActivity(), "获取天气信息失败failure",
                                    Toast.LENGTH_SHORT).show();
                            swipeRefreshLayout.setRefreshing(false);
                        }
                    });
                }

                @Override
                public void onResponse(Call call, final Response response) throws IOException {
                    final String responseText = response.body().string();  //被转换成字符串的json数据
                    //存储此地的天气数据
                    SharedPreferences.Editor editor = getActivity().getSharedPreferences("weatherData", MODE_PRIVATE).edit();
                    editor.putString(weatherId, responseText);
                    editor.apply();
                    final Weather weather = Utility.handleWeatherResponse(responseText);
                    Log.d("123456", "12" + weather);
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (weather != null && "ok".equals(weather.status)) {
                                showWeatherInfo(weather);
                            } else {
                                Toast.makeText(getActivity(), "获取天气信息失败",
                                        Toast.LENGTH_SHORT).show();
                            }
                            swipeRefreshLayout.setRefreshing(false);
                        }
                    });
                }
            });
        }else {                                            //从缓存中读取
            final Weather weather = Utility.handleWeatherResponse(responseText);
            Log.d("123456", "12" + weather);
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (weather != null && "ok".equals(weather.status)) {
                        showWeatherInfo(weather);
                    } else {
                        Toast.makeText(getActivity(), "获取天气信息失败",
                                Toast.LENGTH_SHORT).show();
                    }
                    swipeRefreshLayout.setRefreshing(false);
                }
            });
        }
    }

    //处理并展示Weather实体类中的数据
    private void showWeatherInfo(Weather weather) {
        String cityName = weather.basic.cityName;
        String updateLocTime = "loc：" + weather.basic.update.updateLocTime;
        String updateUtcTime = "utc：" + weather.basic.update.updateUtcTime;
        String degree = weather.now.temperature + "℃";
        String sensibleTemp = "体感温度：" + weather.now.sensibleTemp + "℃";
        String relativeHumidity = "相对湿度：" + weather.now.relativeHumidity + "%";
        String precipitation = "降水量：" + weather.now.precipitation + "mm";
        String windDirection1 = "风向：" + weather.now.wind.windDirection1 + "°";
        String windDirection2 = "风向：" + weather.now.wind.windDirection2;
        String weatherInfo = weather.now.more.info;
        cityNameText.setText(cityName);
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
            View view = LayoutInflater.from(getContext()).inflate(R.layout.forecast_daily_item,
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
                view.findViewById(R.id.forecast_hourly_recycler_view_layout);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
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
        //开启后台服务，进行更新
        Intent intent = new Intent(getActivity(), AutoUpdateService.class);
        getActivity().startService(intent);
    }

}
