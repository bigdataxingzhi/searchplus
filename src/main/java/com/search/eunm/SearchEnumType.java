package com.search.eunm;

public enum SearchEnumType {
	
	SOLR("solr"),//使用进行搜索
	ELASTICSEARCH("elasticsearch");  //使用elasticsearch进行搜索

	private String searchType;
	
	SearchEnumType(String searchType){
		this.searchType = searchType;
	}
	
	public String getSearchType() {
		return searchType;
	}
	
	public static SearchEnumType of(String value) {
        for (SearchEnumType type : SearchEnumType.values()) {
            if (type.getSearchType().equals(value)) {
                return type;
            }
        }
      return null;
    }
}
