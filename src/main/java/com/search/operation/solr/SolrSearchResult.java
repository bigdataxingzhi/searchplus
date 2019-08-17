package com.search.operation.solr;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.apache.solr.common.SolrDocumentList;

import com.google.common.collect.Maps;

public class SolrSearchResult implements Serializable {

	
	private static final long serialVersionUID = -402822022852459887L;
	
	private long recordCount;
	private int totalPages;
	
	Map<String, Map<String, List<String>>> highlightingValues = Maps.newHashMap();

	public Map<String, Map<String, List<String>>> getHighlightingValues() {
		return highlightingValues;
	}

	public void setHighlightingValues(Map<String, Map<String, List<String>>> highlightingValues) {
		this.highlightingValues = highlightingValues;
	}

	private SolrDocumentList solrDocumentList;

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

	

	public SolrDocumentList getSolrDocumentList() {
		return solrDocumentList;
	}

	public void setSolrDocumentList(SolrDocumentList solrDocumentList) {
		this.solrDocumentList = solrDocumentList;
	}
	
	
}
