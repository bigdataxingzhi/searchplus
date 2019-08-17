package com.search.operation.solr;

import java.io.Serializable;
import java.util.List;

/**
 * 通用多结果Service返回结构
 * @author 星志
 *
 * @param <T>
 */
public class SearchResult<T> implements Serializable {

	private long recordCount;
	private int totalPages;
	private List<T> searchList;
	public long getRecordCount() {
		return recordCount;
	}
	public void setRecordCount(long recordCount) {
		this.recordCount = recordCount;
	}
	public int getTotalPages() {
		return totalPages;
	}
	public void setTotalPages(int totalPages) {
		this.totalPages = totalPages;
	}
	public List<T> getSearchList() {
		return searchList;
	}
	public void setSearchList(List<T> searchList) {
		this.searchList = searchList;
	}
	
	
}
