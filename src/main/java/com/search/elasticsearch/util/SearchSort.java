package com.search.elasticsearch.util;

import java.util.Set;

import org.springframework.data.domain.Sort;

import com.google.common.collect.Sets;

/**
 * 排序生成器
 */
public class SearchSort {

    public static final String DEFAULT_SORT_KEY = "lastUpdateTime";
    public Set<String> SORT_KEYS  = null;

    
   public  SearchSort(){
    	
    }
   
   public SearchSort(String... sortStrings) {
	   SORT_KEYS = Sets.newHashSet(sortStrings);
	   SORT_KEYS.add(DEFAULT_SORT_KEY);
   }


    public  Sort generateSort(String key, String directionKey) {
    	
        key = getSortKey(key);

        Sort.Direction direction = Sort.Direction.fromStringOrNull(directionKey);
        if (direction == null) {
            direction = Sort.Direction.DESC;
        }

        return new Sort(direction, key);
    }

    public  String getSortKey(String key) {
        if (!SORT_KEYS.contains(key)) {
            key = DEFAULT_SORT_KEY;
        }

        return key;
    }
}
