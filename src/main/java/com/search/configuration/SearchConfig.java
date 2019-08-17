package com.search.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

@PropertySource(value = {"classpath:properties"})
@Component
@ConfigurationProperties(prefix = "search")
public class SearchConfig {


	private String searchType;
	private String elasticsearchClusterName;
	private String elasticsearchHost;
	private String elasticsearchPort;
	private int elasticsearchMaxRetry;  //es可以重建索引的次数
	private String elasticsearchIndexName="search";
	private String elasticsearchIndexType="search";

	private String soleCoreURL;
	private int solrMaxRetries=1;
	private int solrConnectiontimeout=1000;
	
	public String getSearchType() {
		return searchType;
	}
	public void setSearchType(String searchType) {
		this.searchType = searchType;
	}
	public String getElasticsearchClusterName() {
		return elasticsearchClusterName;
	}
	public void setElasticsearchClusterName(String elasticsearchClusterName) {
		this.elasticsearchClusterName = elasticsearchClusterName;
	}
	public String getElasticsearchHost() {
		return elasticsearchHost;
	}
	public void setElasticsearchHost(String elasticsearchHost) {
		this.elasticsearchHost = elasticsearchHost;
	}
	public String getElasticsearchPort() {
		return elasticsearchPort;
	}
	public void setElasticsearchPort(String elasticsearchPort) {
		this.elasticsearchPort = elasticsearchPort;
	}
	
	public int getElasticsearchMaxRetry() {
		return elasticsearchMaxRetry;
	}
	public void setElasticsearchMaxRetry(int elasticsearchMaxRetry) {
		this.elasticsearchMaxRetry = elasticsearchMaxRetry;
	}
	
	public String getElasticsearchIndexName() {
		return elasticsearchIndexName;
	}
	public void setElasticsearchIndexName(String elasticsearchIndexName) {
		this.elasticsearchIndexName = elasticsearchIndexName;
	}
	public String getElasticsearchIndexType() {
		return elasticsearchIndexType;
	}
	public void setElasticsearchIndexType(String elasticsearchIndexType) {
		this.elasticsearchIndexType = elasticsearchIndexType;
	}
	public String getSoleCoreURL() {
		return soleCoreURL;
	}
	public void setSoleCoreURL(String soleCoreURL) {
		this.soleCoreURL = soleCoreURL;
	}
	public int getSolrMaxRetries() {
		return solrMaxRetries;
	}
	public void setSolrMaxRetries(int solrMaxRetries) {
		this.solrMaxRetries = solrMaxRetries;
	}
	public int getSolrConnectiontimeout() {
		return solrConnectiontimeout;
	}
	public void setSolrConnectiontimeout(int solrConnectiontimeout) {
		this.solrConnectiontimeout = solrConnectiontimeout;
	}
	
	
}
