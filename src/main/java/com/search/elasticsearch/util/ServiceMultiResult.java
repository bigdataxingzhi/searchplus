package com.search.elasticsearch.util;

import java.util.List;

/**
 * 通用多结果Service返回结构
 */
public class ServiceMultiResult<T> {
    //结果总数
    private long total;
    //当前结果集
    private List<T> result;

    public ServiceMultiResult(long total, List<T> result) {
        this.total = total;
        this.result = result;
    }

    //查询当前结果的总条数
    public int getResultSize() {
        if (this.result == null) {
            return 0;
        }
        return this.result.size();
    }


    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public List<T> getResult() {
        return result;
    }

    public void setResult(List<T> result) {
        this.result = result;
    }

}
