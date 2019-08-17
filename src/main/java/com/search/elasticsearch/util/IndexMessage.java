package com.search.elasticsearch.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.search.configuration.SearchConfig;

/**
 * 
 * es 构造索引信息类
 */
@Component
public class IndexMessage {

	@Autowired
	SearchConfig searchConfig;
	
    private Long indexId; //es 索引id
    private String operation;
    private String cityName="";
    private String address="";
    public static final String INDEX = "index";  //创建索引
    public static final String REMOVE = "remove";  //删除索引

	public static final int MAX_RETRY = 3;
    /**防止异常情况下的构建索引失败,可重试*/
    private int retry = 0;

    /**
     * 默认构造器 防止jackson序列化失败
     */
    public IndexMessage() {
    }

    public IndexMessage(Long indexId, String operation, int retry) {
        this.indexId = indexId;
        this.operation = operation;
        this.retry = retry;
    }


    public Long getIndexId() {
		return indexId;
	}

	public void setIndexId(Long indexId) {
		this.indexId = indexId;
	}

	public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public int getRetry() {
        return retry;
    }

    public void setRetry(int retry) {
        this.retry = retry;
    }

	public String getCityName() {
		return cityName;
	}

	public void setCityName(String cityName) {
		this.cityName = cityName;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}
    
    
}
