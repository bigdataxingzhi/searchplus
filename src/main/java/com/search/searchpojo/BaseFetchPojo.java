package com.search.searchpojo;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.common.collect.Lists;
import com.search.elasticsearch.util.SearchSuggest;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BaseFetchPojo {
	
     private Long indexId;
	
	private List<SearchSuggest> suggest = null;

    private List<String> noanalyzerValues = Lists.newArrayList();;
    
 
    private List<String> needanalyzerField = Lists.newArrayList();

    private String name;
    
    

	public String getName() {
		return name;
	}


	public void setName(String name) {
		this.name = name;
	}


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


	public List<String> getNoanalyzerValues() {
		return noanalyzerValues;
	}


	public void setNoanalyzerValues(List<String> noanalyzerValues) {
		this.noanalyzerValues = noanalyzerValues;
	}


	public List<String> getNeedanalyzerField() {
		return needanalyzerField;
	}


	public void setNeedanalyzerField(List<String> needanalyzerField) {
		this.needanalyzerField = needanalyzerField;
	}


    
    

}
