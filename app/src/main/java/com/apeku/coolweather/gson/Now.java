package com.apeku.coolweather.gson;

import com.google.gson.annotations.SerializedName;

/**
 * Created by apeku on 2018/4/1.
 */

public class Now {

    @SerializedName("tmp")
    public String temperature;

    @SerializedName("cond")
    public More more;

    public class More{

        @SerializedName("txt")
        public String info;
    }
}
