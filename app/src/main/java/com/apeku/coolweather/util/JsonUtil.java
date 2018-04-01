package com.apeku.coolweather.util;

import android.text.TextUtils;

import com.apeku.coolweather.db.City;
import com.apeku.coolweather.db.County;
import com.apeku.coolweather.db.Province;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by apeku on 2018/3/29.
 */

public class JsonUtil {

    public static List<Province> handleProvinceResponse(String response){

        List<Province> provinceList=new ArrayList<Province>();
        if(!TextUtils.isEmpty(response)){

            try {
                JSONArray jsonArray=new JSONArray(response);
                for(int i=0;i<jsonArray.length();i++){
                    JSONObject jsonObject=jsonArray.getJSONObject(i);
                    Province province=new Province();
                    province.setProvinceCode(jsonObject.getInt("id"));
                    province.setProvinceName(jsonObject.getString("name"));
                    provinceList.add(province);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return provinceList;
    }

    public static List<City> handleCityResponse(String response,int provinceId){

        List<City> cityList=new ArrayList<City>();
        if(!TextUtils.isEmpty(response)){

            try {
                JSONArray jsonArray=new JSONArray(response);
                for(int i=0;i<jsonArray.length();i++){
                    JSONObject jsonObject=jsonArray.getJSONObject(i);
                    City city=new City();
                    city.setCityCode(jsonObject.getInt("id"));
                    city.setCityName(jsonObject.getString("name"));
                    city.setProvinceId(provinceId);
                    cityList.add(city);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return cityList;
    }
    public static List<County> handleCountyResponse(String response, int cityId){

        List<County> countyList=new ArrayList<County>();
        if(!TextUtils.isEmpty(response)){

            try {
                JSONArray jsonArray=new JSONArray(response);
                for(int i=0;i<jsonArray.length();i++){
                    JSONObject jsonObject=jsonArray.getJSONObject(i);
                    County county=new County();
                    county.setCityId(cityId);
                    county.setCountyCode(jsonObject.getInt("id"));
                    county.setCountyName(jsonObject.getString("name"));
                    county.setWeatherId(jsonObject.getString("weather_id"));
                    countyList.add(county);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return countyList;
    }
}
