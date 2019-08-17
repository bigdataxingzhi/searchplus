package com.search.elasticsearch.util;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 百度位置信息,需要地理位置查询是,可以使用该对象
 */
public class BaiduMapLocation {
    // 经度
    @JsonProperty("lon")//存储在es中的名字,不可更改
    private double longitude;

    // 纬度
    @JsonProperty("lat")//存储在es中的名字,不可更改
    private double latitude;

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }
}
