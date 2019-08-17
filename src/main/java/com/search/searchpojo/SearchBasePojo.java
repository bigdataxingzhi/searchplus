package com.search.searchpojo;


/**
 * 抽象的搜索封装基类,在开发不同的业务是,继承该类以供传入不同的查询参数
 * @author 星志
 *
 */
public abstract class SearchBasePojo {
	
	//分页参数==>页数
	private Integer page;
	
	//分页参数==>每页几条
	private Integer rows;
	
	//默认的排序方向
	private String orderDirection;
	
	//默认的排序字段
	private String orderName;
	
	private String highlightingField;
	
	

	public String getHighlightingField() {
		return highlightingField;
	}

	public void setHighlightingField(String highlightingField) {
		this.highlightingField = highlightingField;
	}

	public Integer getPage() {
		return page;
	}

	public void setPage(Integer page) {
		this.page = page;
	}

	public Integer getRows() {
		return rows;
	}

	public void setRows(Integer rows) {
		this.rows = rows;
	}

	public String getOrderDirection() {
		return orderDirection;
	}

	public void setOrderDirection(String orderDirection) {
		this.orderDirection = orderDirection;
	}

	public String getOrderName() {
		return orderName;
	}

	public void setOrderName(String orderName) {
		this.orderName = orderName;
	}
	
}
