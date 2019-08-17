package com.search.operation.ealsticsearch;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.admin.indices.analyze.AnalyzeAction;
import org.elasticsearch.action.admin.indices.analyze.AnalyzeRequestBuilder;
import org.elasticsearch.action.admin.indices.analyze.AnalyzeResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.geo.GeoPoint;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.reindex.BulkByScrollResponse;
import org.elasticsearch.index.reindex.DeleteByQueryAction;
import org.elasticsearch.index.reindex.DeleteByQueryRequestBuilder;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.sort.SortOrder;
import org.elasticsearch.search.suggest.Suggest;
import org.elasticsearch.search.suggest.SuggestBuilder;
import org.elasticsearch.search.suggest.SuggestBuilders;
import org.elasticsearch.search.suggest.completion.CompletionSuggestion;
import org.elasticsearch.search.suggest.completion.CompletionSuggestionBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.primitives.Longs;
import com.search.baudumapservice.BaiduMapService;
import com.search.configuration.RabbitMQConfiguration;
import com.search.configuration.SearchConfig;
import com.search.elasticsearch.util.BaiduMapLocation;
import com.search.elasticsearch.util.BucketResult;
import com.search.elasticsearch.util.IndexMessage;
import com.search.elasticsearch.util.IndexTemplate;
import com.search.elasticsearch.util.MapSearch;
import com.search.elasticsearch.util.RentSearch;
import com.search.elasticsearch.util.SearchSort;
import com.search.elasticsearch.util.SearchSuggest;
import com.search.elasticsearch.util.ServiceMultiResult;
import com.search.elasticsearch.util.ServiceResult;
import com.search.searchpojo.BaseFetchPojo;
import com.search.searchpojo.BaseLbsPojo;

/**
 * 
 * @author 星志
 *
 */

public abstract class SearchServiceImpl implements ISearchService {
    private static final Logger logger = LoggerFactory.getLogger(ISearchService.class);

    @Autowired
   private SearchConfig searchConfig;
    
    @Autowired
    private static RestTemplate  restTemplate ;
    
    @Autowired
    private RabbitMQConfiguration rabbitConfig;
    
    @Autowired
    BaiduMapService baiduMapService;
    
    @Value("${rearchDataFromUrl}")
   private static String rearchDataFromUrl;
    
    @Value("${rearchDataFromUrl}")
    private static String rearchDataFromControllerMapping;
    
    @Value("${rearchDataFromHandlerMapping}")
    private static String rearchDataFromHandlerMapping;
    
    @Value("${Index_Key_AGG_DISTRICT}")
    private String searchAgg;
    
    @Value("${SEARCH_RABBIT_QUEUENAME}")
    private String queueName="search"; 
    
    @Value("${SEARCH_EXCHANGE_NAME}")
    private String exchangeName="search";
    
    @Value("${SEARCH_KRYROUTE_NAME}")
    private String keyRoute="search";
    
    private static String indexName ="search";

    private static String indexType = "search" ;
    


    public static String getIndexName() {
		return indexName;
	}


	public static void setIndexName(String indexName) {
		SearchServiceImpl.indexName = indexName;
	}


	public static String getIndexType() {
		return indexType;
	}


	public static void setIndexType(String indexType) {
		SearchServiceImpl.indexType = indexType;
	}

	@Autowired
    private TransportClient esClient;


    private static final ObjectMapper objectMapper = new ObjectMapper();

    
    public SearchServiceImpl() {
    	super();
    	this.setIndexName(searchConfig.getElasticsearchIndexName());
    	this.setIndexType(searchConfig.getElasticsearchIndexType());
    }
    
    
    public static String RetireUrl() {
    	StringBuffer buffer = new StringBuffer();
    	buffer.append(rearchDataFromUrl).append(rearchDataFromControllerMapping)
    	.append("{").append(rearchDataFromHandlerMapping).append("}");
    	return StringUtils.trim(buffer.toString());
    }

    /**使用示例:其他服务只需要添加以下代码,即可做到索引的自动化构建*/
 /**   @RabbitListener(queues= "queue")
   * void bulidIndex(Message message) {
   * 	handleMessage("".getBytes(), "id", 0L, Object.class);
   *   
   * }
   */
    
    /**
     * 
     * @param content                 从mq中读取到的json字符串
     * @param searchIdField           es中id名
     * @param searchId                es中的id值
     * @param clazz                   需要远程获取到的查询结果类型
     */
	private void handleMessage(byte[] content,String searchIdField,Class clazz) {
        try {
        	//将mq中的消息转化为IndexMessage.
            IndexMessage message = objectMapper.readValue(content,IndexMessage.class);

            switch (message.getOperation()) {
            //创建索引
                case IndexMessage.INDEX:
                    this.createOrUpdateIndex(searchIdField,message,clazz);
                    break;
                    //删除索引
                case IndexMessage.REMOVE:
                    this.removeIndex(message,searchIdField);
                    break;
                default:
                    logger.warn("Not support message content " + content);
                    break;
            }
        } catch (IOException e) {
            logger.error("Cannot parse json for " + content, e);
        }
    }

	
	@SuppressWarnings("unchecked")
	public static <T extends BaseFetchPojo> T parseJsonFromMessage(String message,Class<T> clazz) throws JsonParseException, JsonMappingException, IOException {
		BaseFetchPojo baseFetchPojo = objectMapper.readValue(message, clazz);
		return (T) baseFetchPojo;
	}
	
	
	/**
	 * 在此方法中需要做:
	 * 1.调用 String url = RetireUrl(),生成url;
	 * 2.  加载远程数据
	 * String indexData = restTemplate.getForObject(url, String.class,searchId);
	 *3.解析数据
	 *@SuppressWarnings("unchecked")
	 *BaseFetchPojo baseFetchPojo =  parseJsonFromMessage(indexData,clazz);
	 * @param searchId
	 * @param clazz
	 * @return
	 * @throws IOException 
	 * @throws JsonMappingException 
	 * @throws JsonParseException 
	 */
public static <T extends BaseFetchPojo> T parseData(String searchId,Class<T> clazz) throws JsonParseException, JsonMappingException, IOException { 
   String url = RetireUrl();
   String indexData = restTemplate.getForObject(url, String.class,searchId);
	 @SuppressWarnings("unchecked")
	 BaseFetchPojo baseFetchPojo =  parseJsonFromMessage(indexData,clazz);
  return (T) baseFetchPojo;
   
}

/**
 * 该方法需要用户手动实现,实现说明:
 * 1.将传入的baseFetchPojo转为其子类
 * 2.构造出IndexTemplate的子类对象
 * 3.进行属性拷贝
 * 4.将IndexTemplate对象返回
 * @param baseFetchPojo
 * @return
 */
public abstract <T extends BaseFetchPojo> IndexTemplate bulidIndexTemplate(T baseFetchPojo) ;


/**
 * list与数组间的转换
 * @param baseFetchPojo
 * @param indexTemplate
 */
public void converterListToArray(BaseFetchPojo baseFetchPojo,IndexTemplate indexTemplate) {
	String[] needanalyzerValues = new String[baseFetchPojo.getNeedanalyzerField().size()];
	for(int i= 0;i<baseFetchPojo.getNeedanalyzerField().size();i++) {
		needanalyzerValues[i] = baseFetchPojo.getNeedanalyzerField().get(i);
	}
	
	indexTemplate.setNeeedanalyzerValues(needanalyzerValues);
}


/**
 * 若用户有与麻点图绘制相关业务逻辑时,必须重写此方法
 * @param searchId
 * @return
 */
public  ServiceResult lbsUpload(Long searchId) {
	
	return null;
}

/**
 * 若用户有与麻点图绘制相关业务逻辑时,必须重写此方法
 * @param searchId
 * @return
 */
public ServiceResult lbsRemove(Long searchId) {
	
	return null;
}


/**
 * 
 * @param searchIdField           es中id名
 * @param message                 从mq中转化的消息对象
 * @param clazz                   需要远程获取到的查询结果类型
 * @throws JsonParseException
 * @throws JsonMappingException
 * @throws IOException
 */
    //创建或者更新索引
    private void createOrUpdateIndex(String searchIdField,IndexMessage message,Class clazz) throws JsonParseException, JsonMappingException, IOException {
        Long indexId = message.getIndexId();
        //判断查询结果
        boolean success;
        //调用restTemplate 远程加载数据.
        BaseFetchPojo baseFetchPojo = parseData(indexId.toString(),clazz);
        IndexTemplate indexTemplate = bulidIndexTemplate(baseFetchPojo);
        
/**
 *         根据城市的具体地址,获取经纬度
*/
        if(!StringUtils.isEmpty(message.getCityName()) && !StringUtils.isEmpty(message.getAddress())) {
        	//获取经纬度
            ServiceResult<BaiduMapLocation> location = baiduMapService.getBaiduMapLocation(message.getCityName(), message.getAddress());
            if (!location.isSuccess()) {
                this.index(message.getIndexId(), message.getRetry() + 1);
                return;
            }
            indexTemplate.setLocation(location.getResult());
        }
      


        /**按照searchId进行查询*/
        SearchRequestBuilder requestBuilder = this.esClient.prepareSearch(indexName).setTypes(indexType)
                .setQuery(QueryBuilders.termQuery(searchIdField, indexId));
        logger.debug(requestBuilder.toString());
        /**得到查询返回的SearchResponse接口*/
        SearchResponse searchResponse = requestBuilder.get();
        /**得到查询总数*/
        long totalHit = searchResponse.getHits().getTotalHits();
        /**es中查到的总数为0,进入添加数据分支*/
        if (totalHit == 0) {
            success = create(indexTemplate);
        }
        /**es中查到的总数为1,进入更新分支*/
        else if (totalHit == 1) {
            String esId = searchResponse.getHits().getAt(0).getId();
            success = update(esId, indexTemplate);
        }
        /**es中查到的总数大于0,则发生了异常,则先删除所有,在添加.*/
        else {
            success = deleteAndCreate(searchIdField,totalHit, indexTemplate);
        }

        //更新地图的LBS麻点图
        /**
         * 判断该业务逻辑是否有麻点图,若方法返回null,则没有与麻点图
         * 相关业务,不需要考虑
         */
        ServiceResult serviceResult = lbsUpload(Long.parseLong(searchIdField));
  
        if(null!=serviceResult) {
        	if(!serviceResult.isSuccess()) {
        		success = false;
        	}
        }
        
        //如果失败,则重新尝试.
        if (!success) {
            this.index(message.getIndexId(), message.getRetry() + 1);
        } else {
            logger.debug("Index success with index " + indexId);

        }
    }

    private void removeIndex(IndexMessage message,String searchIdField) {
        Long indexId = message.getIndexId();
        DeleteByQueryRequestBuilder builder = DeleteByQueryAction.INSTANCE
                .newRequestBuilder(esClient)
                .filter(QueryBuilders.termQuery(searchIdField, indexId))
                .source(indexName);

        logger.debug("Delete by query for house: " + builder);

        BulkByScrollResponse response = builder.get();
        long deleted = response.getDeleted();
        logger.debug("Delete total " + deleted);
        
        
// 删除地图麻点图
    ServiceResult serviceResult = lbsRemove(Long.parseLong(searchIdField));
    
     //用户无关于麻点图相关逻辑
     if(null==serviceResult) {
    	 if(deleted <= 0) {
    		 logger.warn("Did not remove data from es for response: " + response);
             /** 重新加入消息队列*/
             this.remove(indexId, message.getRetry() + 1);
    	 }
     }else {
    	 //用户有关于麻点图相关逻辑
    	 if (!serviceResult.isSuccess() || deleted <= 0) {
             logger.warn("Did not remove data from es for response: " + response);
             /** 重新加入消息队列*/
             this.remove(indexId, message.getRetry() + 1);
         }
     }
     
    }


    @Override
    public void index(Long houseId) {
        this.index(houseId, 0);
    }

    private void index(Long searchId, int retry) {
        //如果上一步消息处理时发生异常,则应该重试几次
        //重试次数未打上线时,可重试
        if (retry > IndexMessage.MAX_RETRY) {
            logger.error("Retry index times over 3 for index: " + searchId + " Please check it!");
            return;
        }
//通过消息中间件发送消息,时接收端重试.
       IndexMessage message = new IndexMessage(searchId, IndexMessage.INDEX, retry);
//  通过rabbitmq发送消息 
       rabbitConfig.convertAndSend(exchangeName, keyRoute, message);

    }

    private boolean create(IndexTemplate indexTemplate) {
        if (!updateSuggest( indexTemplate)) {
            return false;
        }

        try {
            /**
             * 向es中添加索引
             */
            IndexResponse response = this.esClient.prepareIndex(indexName, indexType)
                    .setSource(objectMapper.writeValueAsBytes(indexTemplate), XContentType.JSON).get();

            logger.debug("Create index with index: " + indexTemplate.getIndexId());
            /**创建成功*/
            if (response.status() == RestStatus.CREATED) {
                return true;
            } else {
                return false;
            }
        } catch (JsonProcessingException e) {
            logger.error("Error to index :" + indexTemplate.getIndexId(), e);
            return false;
        }
    }

    /**
     * 向es中更新文档
     * @param esId
     * @param indexTemplate
     * @return
     */
    private boolean update(String esId,IndexTemplate indexTemplate) {
        if (!updateSuggest(indexTemplate)) {
            return false;
        }

        try {
            /**
             * 向es中更新索引
             */
            UpdateResponse response = this.esClient.prepareUpdate(indexName, indexType, esId).setDoc(objectMapper.writeValueAsBytes(indexTemplate), XContentType.JSON).get();

            logger.debug("Update index {}: ",indexTemplate.getIndexId());
            if (response.status() == RestStatus.OK) {
                return true;
            } else {
                return false;
            }
        } catch (JsonProcessingException e) {
            logger.error("Error to index :{}",indexTemplate.getIndexId(), e);
            return false;
        }
    }

    /**
     * totalHit表示应该被删除几条文档
     * DeleteResponse  result = this.esClient.prepareDelete(indexName, indexType
     * , String.valueOf(indexTemplate.getHouseId().intValue())).get();
     * @param totalHit
     * @param indexTemplate
     * @return
     */
    private boolean deleteAndCreate(String searchIdField,long totalHit, IndexTemplate indexTemplate) {
      /**按实例文档进行删除.*/
        DeleteByQueryRequestBuilder builder = DeleteByQueryAction.INSTANCE
                .newRequestBuilder(esClient)
                .filter(QueryBuilders.termQuery(searchIdField, indexTemplate.getIndexId()))
                .source(indexName);

        //打印查询语句
        logger.debug("Delete by query for house: " + builder);

        /**注意此处写法*/
        BulkByScrollResponse response = builder.get();
        long deleted = response.getDeleted();
        //如果在ES中删除的总数不等于传入的总数,打印警告
        if (deleted != totalHit) {
            logger.warn("Need delete {}, but {} was deleted!", totalHit, deleted);
            return false;
        } else {
            return create( indexTemplate);
        }
    }

    @Override
    public void remove(Long houseId) {
        this.remove(houseId, 0);
    }


    private void remove(Long searchId, int retry) {
        if (retry > IndexMessage.MAX_RETRY) {
            logger.error("Retry remove times over 3 for index: " + searchId + " Please check it!");
            return;
        }

     IndexMessage message = new IndexMessage(searchId, IndexMessage.REMOVE, retry);
     rabbitConfig.convertAndSend(exchangeName, keyRoute, message);
    }

    /**
     * es 按照传入的RentSearch所包装的条件进行查询.
     * @param rentSearch
     * @return
     */
    @Override
    public ServiceMultiResult<Long> query(QueryBuilder boolQuery,RentSearch rentSearch,SearchSort searchSort,String searchId) {
       

        SearchRequestBuilder requestBuilder = this.esClient.prepareSearch(indexName)
                .setTypes(indexType)
                .setQuery(boolQuery)
                .addSort(
                		searchSort.getSortKey(rentSearch.getOrderBy()),//默认排序字段
                        SortOrder.fromString(rentSearch.getOrderDirection())//排序方式
                )
                .setFrom(rentSearch.getStart())//开始数
                .setSize(rentSearch.getSize())//查询几条
                .setFetchSource(searchId, null);//查询结果只返回searchId

        logger.debug(requestBuilder.toString());

        List<Long> searchIds = new ArrayList<>();
        SearchResponse response = requestBuilder.get();
        if (response.status() != RestStatus.OK) {
            logger.warn("Search status is no ok for " + requestBuilder);
            return new ServiceMultiResult<>(0, searchIds);
        }
     //SearchHit为返回结果
        for (SearchHit hit : response.getHits()) {
            System.out.println(hit.getSource());
            searchIds.add(Longs.tryParse(String.valueOf(hit.getSource().get(searchId))));
        }
        return new ServiceMultiResult<>(response.getHits().totalHits, searchIds);
    }

    /**
     * 使用分词器对输入的信息进行分词,然后将分析结果放入indexTemplate.suggest
     * @param indexTemplate
     * @return
     */
    private boolean updateSuggest(IndexTemplate indexTemplate) {
        //将需要分词的字段填入分词器
        AnalyzeRequestBuilder requestBuilder = new AnalyzeRequestBuilder(
                this.esClient, AnalyzeAction.INSTANCE, indexName, indexTemplate.getNeeedanalyzerValues()) ;
        //使用ik分词器的ik_smart进行分词.
        requestBuilder.setAnalyzer("ik_smart");
        //开始分词
        AnalyzeResponse response = requestBuilder.get();
        //得到分词结果
        List<AnalyzeResponse.AnalyzeToken> tokens = response.getTokens();
        //没有分词信息
        if (tokens == null) {
            logger.warn("Can not analyze token for field: {}", indexTemplate.getNeeedanalyzerValues());
            return false;
        }
        //遍历分词结果,依次设置值
        List<SearchSuggest> suggests = new ArrayList<>();
        for (AnalyzeResponse.AnalyzeToken token : tokens) {
            ///排除数字类型 & 小于2个字符的分词结果
            if ("<NUM>".equals(token.getType()) || token.getTerm().length() < 2) {
                continue;
            }

            SearchSuggest suggest = new SearchSuggest();
            suggest.setInput(token.getTerm());
            suggests.add(suggest);
        }

        /** 定制化不需要分词字段的补全*/
        if(indexTemplate.getNoanalyzerValues().size()>0) {
        	for (String noanalyzerFieldValue : indexTemplate.getNoanalyzerValues()) {
        		SearchSuggest suggest = new SearchSuggest();
        		suggest.setInput(noanalyzerFieldValue);
        		suggests.add(suggest);
			}
        }
        //完善indexTemplate
        indexTemplate.setSuggest(suggests);
        return true;
    }

    /**
     * 自动补全输入的关键字
     * @param prefix
     * @return
     */
    @Override
    public ServiceResult<List<String>> suggest(String prefix) {
        CompletionSuggestionBuilder suggestion = SuggestBuilders.completionSuggestion("suggest").prefix(prefix).size(5);//搜索prefix,搜索5个

        SuggestBuilder suggestBuilder = new SuggestBuilder();
        //autocomplete 随意起名
        suggestBuilder.addSuggestion("autocomplete", suggestion);

        SearchRequestBuilder requestBuilder = this.esClient.prepareSearch(indexName)
                .setTypes(indexType)
                .suggest(suggestBuilder);

        logger.debug(requestBuilder.toString());

        SearchResponse response = requestBuilder.get();
        //得到suggest
        Suggest suggest = response.getSuggest();
        if (suggest == null) {
            return ServiceResult.of(new ArrayList<>());
        }
        //得到结果
        Suggest.Suggestion result = suggest.getSuggestion("autocomplete");

        int maxSuggest = 0;
        //set过滤,方便去重
        Set<String> suggestSet = new HashSet<>();
        //对结果进行过滤
        for (Object term : result.getEntries()) {
            if (term instanceof CompletionSuggestion.Entry) {
                CompletionSuggestion.Entry item = (CompletionSuggestion.Entry) term;

                if (item.getOptions().isEmpty()) {
                    continue;
                }

                for (CompletionSuggestion.Entry.Option option : item.getOptions()) {
                    //查看set集合中是否已经含有该字段
                    String tip = option.getText().string();
                    if (suggestSet.contains(tip)) {
                        continue;
                    }
                    suggestSet.add(tip);
                    maxSuggest++;
                }
            }
            //如果set中元素大于5,跳出循环
            if (maxSuggest > 5) {
                break;
            }
        }
        List<String> suggests = Lists.newArrayList(suggestSet.toArray(new String[]{}));
        return ServiceResult.of(suggests);
    }

 /**
  *  根据字段名聚合数据
  *  查询结果为单条记录时使用
  */
    @Override
        public ServiceResult<Long> aggregateOne(String aggregationField,BoolQueryBuilder boolQuery) {
        SearchRequestBuilder requestBuilder = this.esClient.prepareSearch(indexName)
                .setTypes(indexType)
                .setQuery(boolQuery)
                .addAggregation(
                        AggregationBuilders.terms(searchAgg)//起一个聚合名
                        .field(aggregationField)//对哪个字段进行聚合
                ).setSize(0);//不需要原始数据,只需要聚合的数据

        logger.debug(requestBuilder.toString());

        SearchResponse response = requestBuilder.get();
        if (response.status() == RestStatus.OK) {
            //传入聚合名进行查询
            Terms terms = response.getAggregations().get(searchAgg);
            //判断是否查出了聚合数据
            if (terms.getBuckets() != null && !terms.getBuckets().isEmpty()) {
                //注意:把聚合字段传入
                return ServiceResult.of(terms.getBucketByKey(aggregationField).getDocCount());
            }
        } else {
            logger.warn("Failed to Aggregate for:{} " + aggregationField);

        }
        return ServiceResult.of(0L);
    }

  
    /**
     *  根据字段名聚合数据
     *  查询结果为多条记录时使用
     */
    @Override
    public ServiceMultiResult<BucketResult> mapAggregate(String aggregationField,BoolQueryBuilder boolQuery) {
        AggregationBuilder aggBuilder = AggregationBuilders.terms(searchAgg)
                .field(aggregationField);
        SearchRequestBuilder requestBuilder = this.esClient.prepareSearch(indexName)
                .setTypes(indexType)
                .setQuery(boolQuery)
                .addAggregation(aggBuilder);

        logger.debug(requestBuilder.toString());

        SearchResponse response = requestBuilder.get();
        List<BucketResult> buckets = new ArrayList<>();
        if (response.status() != RestStatus.OK) {
            logger.warn("Aggregate status is not ok for " + requestBuilder);
            return new ServiceMultiResult<>(0, buckets);
        }

        Terms terms = response.getAggregations().get(searchAgg);
        for (Terms.Bucket bucket : terms.getBuckets()) {
            buckets.add(new BucketResult(bucket.getKeyAsString(), bucket.getDocCount()));
        }

        return new ServiceMultiResult<>(response.getHits().getTotalHits(), buckets);
    }

/**
 * 全地图查询
 * searchRegionName                 在地图中搜索的区域名
 * orderBy                          排序字段
 * searchRegionValue                在地图中搜索的区域值
 * orderDirection                   排序方向
 * start                            起始值
 * size                             查找个数
 * searchSort                       排序工具类
 * return ServiceMultiResult<Long>  返回有es中id组成的多值结果
 * 
 */
    @Override
    public ServiceMultiResult<Long> mapQuery(String searchRegionName, String orderBy,
    										 String searchRegionValue,
                                             String orderDirection,
                                             int start,
                                             int size,
                                             SearchSort searchSort) {
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        boolQuery.filter(QueryBuilders.termQuery(searchRegionName, searchRegionValue));

        SearchRequestBuilder searchRequestBuilder = this.esClient.prepareSearch(indexName)
                .setTypes(indexType)
                .setQuery(boolQuery)
                .addSort(searchSort.getSortKey(orderBy), SortOrder.fromString(orderDirection))
                .setFrom(start)
                .setSize(size);

        List<Long> searchIds = new ArrayList<>();
        SearchResponse response = searchRequestBuilder.get();
        if (response.status() != RestStatus.OK) {
            logger.warn("Search status is not ok for " + searchRequestBuilder);
            return new ServiceMultiResult<>(0, searchIds);
        }

        for (SearchHit hit : response.getHits()) {
        	searchIds.add(Longs.tryParse(String.valueOf(hit.getSource().get(searchAgg))));
        }
        return new ServiceMultiResult<>(response.getHits().getTotalHits(), searchIds);
    }

    /**
     * 根据左上角和右下角的经纬度精确查找
     * @param mapSearch
     * @return
     */
    @Override
    public ServiceMultiResult<Long> mapQuery(String searchRegionName,SearchSort searchSort,MapSearch mapSearch,String searchId) {
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        boolQuery.filter(QueryBuilders.termQuery(searchRegionName, mapSearch.getCityEnName()));

        boolQuery.filter(
                //es专用的地图查询
            QueryBuilders.geoBoundingBoxQuery("location")
                .setCorners(
                		//左上角经纬度
                        new GeoPoint(mapSearch.getLeftLatitude(), mapSearch.getLeftLongitude()),
                       //右下角经纬度
                        new GeoPoint(mapSearch.getRightLatitude(), mapSearch.getRightLongitude())
                ));

        SearchRequestBuilder builder = this.esClient.prepareSearch(indexName)
                .setTypes(indexType)
                .setQuery(boolQuery)
                .addSort(searchSort.getSortKey(mapSearch.getOrderBy()),
                        SortOrder.fromString(mapSearch.getOrderDirection()))
                .setFrom(mapSearch.getStart())
                .setSize(mapSearch.getSize());

        List<Long> searchIds = new ArrayList<>();
        SearchResponse response = builder.get();
        if (RestStatus.OK != response.status()) {
            logger.warn("Search status is not ok for " + builder);
            return new ServiceMultiResult<>(0, searchIds);
        }

        for (SearchHit hit : response.getHits()) {
        	searchIds.add(Longs.tryParse(String.valueOf(hit.getSource().get(searchId))));
        }
        return new ServiceMultiResult<>(response.getHits().getTotalHits(), searchIds);
    }
}
