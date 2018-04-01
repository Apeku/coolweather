package com.apeku.coolweather.ui;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.apeku.coolweather.R;
import com.apeku.coolweather.constant.Address;
import com.apeku.coolweather.db.City;
import com.apeku.coolweather.db.County;
import com.apeku.coolweather.db.Province;
import com.apeku.coolweather.util.DbUtil;
import com.apeku.coolweather.util.HttpUtil;
import com.apeku.coolweather.util.JsonUtil;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by apeku on 2018/3/29.
 */

public class ChooseAreaFragment extends Fragment {

    private static final String TAG = "ChooseAreaFragment";
   
    private static final int LEVEL_PROVINCE=0;

    private static final int LEVEL_CITY=1;

    private static final int LEVEL_COUNTY=2;


    private TextView titleText;

    private Button btnBack;

    private ListView listView;

    private ArrayAdapter<String> mAdapter;

    private List<String> mDataStringList=new ArrayList<String>();

    private List<Province> provinceList;

    private List<City> cityList;

    private List<County> countyList;

    private Province currentProvince;

    private City currentCity;

    private int mLevel;

    private ProgressDialog mProgressDialog;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_choose_area,container,false);
        titleText= (TextView) view.findViewById(R.id.title_text);
        btnBack= (Button) view.findViewById(R.id.btn_back);
        listView= (ListView) view.findViewById(R.id.list_view);
        mAdapter=new ArrayAdapter<String>(this.getActivity(),android.R.layout.simple_list_item_1,mDataStringList);
        listView.setAdapter(mAdapter);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setLevel(LEVEL_PROVINCE);
        notifyLevelChanged();
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (mLevel){

                    case LEVEL_PROVINCE:
                        currentProvince=provinceList.get(position);
                        setLevel(LEVEL_CITY);
                        break;
                    case LEVEL_CITY:
                        currentCity = cityList.get(position);
                        setLevel(LEVEL_COUNTY);
                        break;
                    default:
                        break;
                }

                notifyLevelChanged();

            }
        });

    }


    private void notifyLevelChanged() {

        switch (mLevel){

            case LEVEL_PROVINCE:
                onLevelProvince();
                break;
            case LEVEL_CITY:
                onLevelCity();
                break;
            case LEVEL_COUNTY:
                onLevelCounty();
                break;

            default:
                break;
        }
    }

    private void onLevelCounty() {

        titleText.setText(currentCity.getCityName());
        btnBack.setVisibility(View.VISIBLE);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setLevel(LEVEL_CITY);
                currentCity=null;
                notifyLevelChanged();
            }
        });
        countyList=DataSupport.where("cityid=?",currentCity.getId()+"").find(County.class);
        if(countyList.size()>0){
            refreshDataString(countyList);
            mAdapter.notifyDataSetChanged();
        }else{

            String address=Address.ADDRESS_MAIN+"/"+currentProvince.getProvinceCode()+"/"+currentCity.getCityCode();
            queryFromServer(address);
        }

        //TODO：可能后续需要处理县或区的条目点击事件
    }

    private void onLevelCity() {

        titleText.setText(currentProvince.getProvinceName());
        btnBack.setVisibility(View.VISIBLE);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setLevel(LEVEL_PROVINCE);
                currentProvince=null;
                notifyLevelChanged();
            }
        });
        cityList=DataSupport.where("provinceid=?",String.valueOf(currentProvince.getId())).find(City.class);
        if(cityList.size()>0){

            refreshDataString(cityList);
            mAdapter.notifyDataSetChanged();
        }else {

            String address=Address.ADDRESS_MAIN+"/"+currentProvince.getProvinceCode();
            queryFromServer(address);
        }


    }


    private void onLevelProvince() {

        titleText.setText("中国");
        btnBack.setVisibility(View.GONE);
        provinceList= DataSupport.findAll(Province.class);
        if(provinceList.size()>0){

            refreshDataString(provinceList);
            mAdapter.notifyDataSetChanged();
        }else{
            String address= Address.ADDRESS_MAIN;
            queryFromServer(address);
        }
        Log.d(TAG, "onLevelProvince: ");

    }

    private void queryFromServer(String address){

        showProgress();
        HttpUtil.sendOkHttpRequest(address, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        cancelProgress();
                        Toast.makeText(getActivity(), "请求服务器发生错误", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                String responseString=response.body().string();
                Log.d(TAG, "onResponse成功:"+responseString);
                switch (mLevel){

                    case LEVEL_PROVINCE:
                        provinceList= JsonUtil.handleProvinceResponse(responseString);
                        DbUtil.saveListData2Db(provinceList);
                        refreshDataString(provinceList);
                        break;
                    case LEVEL_CITY:
                        cityList=JsonUtil.handleCityResponse(responseString,currentProvince.getId());
                        DbUtil.saveListData2Db(cityList);
                        refreshDataString(cityList);
                        break;
                    case LEVEL_COUNTY:
                        countyList=JsonUtil.handleCountyResponse(responseString,currentCity.getId());
                        DbUtil.saveListData2Db(countyList);
                        refreshDataString(countyList);
                        break;
                    default:
                        break;
                }
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        cancelProgress();
                        mAdapter.notifyDataSetChanged();
                    }
                });


            }
        });

    }

    private void showProgress() {

        mProgressDialog=new ProgressDialog(this.getActivity());
        mProgressDialog.setMessage("正在加载中...");
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.show();
    }

    private void cancelProgress(){

        if(mProgressDialog!=null)
            mProgressDialog.dismiss();
    }

    private <T> void refreshDataString(List<T> list){

        if(list==null||list.size()<=0)
            return;
        mDataStringList.clear();
        for(T item:list){
            String itemDataString=null;
            if(item instanceof Province){
                itemDataString=((Province)item).getProvinceName();
                

            }else if(item instanceof City){
                itemDataString=((City)item).getCityName();
            }else if(item instanceof County){
                itemDataString=((County)item).getCountyName();
            }else {
                return;
            }
            mDataStringList.add(itemDataString);
        }

    }

    private void setLevel(int level){

        mLevel=level;
    }
}
