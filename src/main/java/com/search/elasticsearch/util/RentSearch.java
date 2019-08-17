package com.search.elasticsearch.util;

/**
 * 查询条件简单封装,以后的查询条件以该类为基类.
 * @author 星志
 *
 */
public class RentSearch {

	public String orderBy;
	public String orderDirection;

	public int start;

	public int size;

	public String getOrderBy() {
		return orderBy;
	}

	public void setOrderBy(String orderBy) {
		this.orderBy = orderBy;
	}

	public String getOrderDirection() {
		return orderDirection;
	}

	public void setOrderDirection(String orderDirection) {
		this.orderDirection = orderDirection;
	}

	public int getStart() {
		return start;
	}

	public void setStart(int start) {
		this.start = start;
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

}
