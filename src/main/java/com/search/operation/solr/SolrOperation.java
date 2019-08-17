package com.search.operation.solr;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrQuery.ORDER;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

@Component
public class SolrOperation {

	@Autowired
	HttpSolrServer solrServer;

	/**
	 * 根据查询条件查询索引库
	 * <p>
	 * @param query
	 * @return
	 */
	public SolrSearchResult search(SolrQuery query, String highlightingField, String solrID) throws Exception {
		// 根据query查询索引库
		QueryResponse queryResponse = solrServer.query(query);
		// 取查询结果。
		SolrDocumentList solrDocumentList = queryResponse.getResults();
		// 取查询结果总记录数
		long numFound = solrDocumentList.getNumFound();
		// 取商品列表，需要取高亮显示
		Map<String, Map<String, List<String>>> highlighting = queryResponse.getHighlighting();
		// 将查询到的结果返回
		SolrSearchResult result = new SolrSearchResult();
		result.setRecordCount(numFound);
		result.setHighlightingValues(highlighting);
		result.setSolrDocumentList(solrDocumentList);
		return result;

	}

	/**
	 * 
	 * SolrQuery 查询条件通用封装
	 * 
	 * @param fullClassName 传入一个封装了solr查询条件的类的全限定名
	 * @param params  对应solr查询条件的类的 key==>value值
	 * @return SolrQuery 返回构造好的查询条件
	 */
	public static SolrQuery buildSolrQuery(String fullClassName, Map<String, String> params) {
		SolrQuery solrQuery = new SolrQuery(); // 构造搜索条件
		try {
			Class clazz = Class.forName(fullClassName);
			// 获取实体类的所有属性信息，返回Field数组
			Field[] fields = clazz.getDeclaredFields();
			StringBuffer buffer = new StringBuffer();
			for (int i = 0; i < fields.length; i++) {
				// 设置属性是可以访问的
				fields[i].setAccessible(true);
				// 获取属性名
				String fieldName = fields[i].getName();
				String value = params.get(fieldName);
				switch (fieldName) {
				case "orderDirection":
					if (params.containsKey("orderDirection") && params.containsKey("orderName")) {
						if (params.get("orderDirection").equals("asc")) {
							solrQuery.setSort(params.get("orderName"), ORDER.asc);
						} else {
							solrQuery.setSort(params.get("orderName"), ORDER.desc);
						}
						params.remove("orderDirection");
						params.remove("orderName");
					}
					break;
				case "orderName":
					if (params.containsKey("orderDirection") && params.containsKey("orderName")) {
						if (params.get("orderDirection").equals("asc")) {
							solrQuery.setSort(params.get("orderName"), ORDER.asc);
						} else {
							solrQuery.setSort(params.get("orderName"), ORDER.desc);
						}
						params.remove("orderDirection");
						params.remove("orderName");
					}
					break;
				case "page" :
					// 设置分页 start=0就是从0开始，，rows=5当前返回5条记录，第二页就是变化start这个值为5就可以了。
					solrQuery.setStart((Math.max(Integer.parseInt(params.get("page")), 1) - 1)
							* (Integer.parseInt(params.get("rows"))));
				break;
				case "rows" :
					// 设置分页 start=0就是从0开始，，rows=5当前返回5条记录，第二页就是变化start这个值为5就可以了。
					solrQuery.setRows(Integer.parseInt(params.get("rows")));
					break;
				case "highlightingField" :
					if (StringUtils.isEmpty(params.get("highlightingField"))) {
						solrQuery.setHighlight(true); // 开启高亮组件
						solrQuery.addHighlightField(fieldName);// 高亮字段
						solrQuery.setHighlightSimplePre("<em>");// 标记，高亮关键字前缀
						solrQuery.setHighlightSimplePost("</em>");// 后缀
					}
					break;
				default:
					buffer.append(fieldName).append(":").append(value);
					if (i != fields.length - 1) {
						buffer.append("  AND  ");
					}
					break;
				}
			}
			String query = buffer.toString();
			query = StringUtils.trim(query);
			String end = query.substring(query.length()-4);
			if(end.equals("AND")) {
				end = StringUtils.trim(end);
				solrQuery.setQuery(end);
			}else {
				solrQuery.setQuery(query);

			}
		} catch (Exception e) {
			return null;
		}
		return solrQuery;
	}
	
	
public static String gethighlightingValue(String solrId,SolrDocument solrDocument,String highlightingField,Map<String, Map<String, List<String>>> highlighting ) {
		String highlightingValue = null;
		if (StringUtils.isEmpty(highlightingField) && StringUtils.isEmpty(solrId)) {
			return highlightingValue;
		}else{
			// 将高亮的标题数据写回到数据对象中
			for (Map.Entry<String, Map<String, List<String>>> highlightingEntry : highlighting.entrySet()) {

				if (!highlightingEntry.getKey().equals(solrId)) {
					continue;
				}
				highlightingValue = StringUtils.join(highlightingEntry.getValue().get(highlightingField), "");
				
				break;
			}
		return highlightingValue;
		}
		
	}
}
