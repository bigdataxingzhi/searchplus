package com.search.operation.ealsticsearch;

import java.util.List;

import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import com.search.elasticsearch.util.BucketResult;
import com.search.elasticsearch.util.MapSearch;
import com.search.elasticsearch.util.RentSearch;
import com.search.elasticsearch.util.SearchSort;
import com.search.elasticsearch.util.ServiceMultiResult;
import com.search.elasticsearch.util.ServiceResult;


/**
 * 检索接口
 */
public interface ISearchService {
    /**
     * 索引目标
     * @param searchId
     */
    void index(Long searchId);

    /**
     * 移除索引
     * @param searchId
     */
    void remove(Long searchId);

    /**
     * 查询接口
     * @param rentSearch
     * @return
     */
    ServiceMultiResult<Long> query(QueryBuilder boolQuery,RentSearch rentSearch,SearchSort searchSort,String searchId);

    /**
     * 获取补全建议关键词
     */
    ServiceResult<List<String>> suggest(String prefix);

    /**
     * 聚合特定小区的房间数
     */
  //  ServiceResult<Long> aggregateDistrictHouse(String cityEnName, String regionEnName, String district);
    public ServiceResult<Long> aggregateOne(String aggregationField,BoolQueryBuilder boolQuery) ;
    /**
     * 聚合城市数据
     * @param cityEnName
     * @return
     */
    ServiceMultiResult<BucketResult> mapAggregate(String aggregationField,BoolQueryBuilder boolQuery);

    /**
     * 城市级别地图查询
     * @return
     */
    ServiceMultiResult<Long> mapQuery(String searchRegionName, String orderBy,
			 String searchRegionValue,
             String orderDirection,
             int start,
             int size,
             SearchSort searchSort);
    
    /**
     * 精确范围数据查询
     * @param mapSearch
     * @return
     */
    ServiceMultiResult<Long> mapQuery(String searchRegionName,SearchSort searchSort,MapSearch mapSearch,String searchId);
}
