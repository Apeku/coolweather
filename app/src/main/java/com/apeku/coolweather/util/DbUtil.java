package com.apeku.coolweather.util;

import org.litepal.crud.DataSupport;

import java.util.List;

/**
 * Created by apeku on 2018/3/31.
 */

public class DbUtil {

    public static <T extends DataSupport> void saveListData2Db(List<T> datas){

        if(datas==null||datas.size()<=0)
            return;
        for(DataSupport data:datas){
            data.save();
        }
    }
}
