package com.search.elasticsearch.util;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.google.common.collect.Lists;

@JsonIgnoreProperties(ignoreUnknown = true)
public class IndexTemplate {
	
	 private Long indexId;
	
	private List<SearchSuggest> suggest = null;

	@JsonInclude(Include.NON_NULL) 
    private BaiduMapLocation location = null;
    
	@JsonIgnore
    private List<String> noanalyzerValues = Lists.newArrayList();;
    
    @JsonIgnore
    private String[] neeedanalyzerValues ;
    
    

	public Long getIndexId() {
		
		return indexId;
	}

	public void setIndexId(Long indexId) {
		this.indexId = indexId;
	}

	public List<SearchSuggest> getSuggest() {
		return suggest;
	}

	public void setSuggest(List<SearchSuggest> suggest) {
		this.suggest = suggest;
	}

	public BaiduMapLocation getLocation() {
		return location;
	}

	public void setLocation(BaiduMapLocation location) {
		this.location = location;
	}

	public List<String> getNoanalyzerValues() {
		return noanalyzerValues;
	}

	public void setNoanalyzerValues(List<String> noanalyzerValues) {
		this.noanalyzerValues = noanalyzerValues;
	}

	public String[] getNeeedanalyzerValues() {
		return neeedanalyzerValues;
	}

	public void setNeeedanalyzerValues(String[] neeedanalyzerValues) {
		this.neeedanalyzerValues = neeedanalyzerValues;
	}


	
    
}
