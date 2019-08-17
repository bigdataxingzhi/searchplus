package com.search.operation.ealsticsearch;

import org.elasticsearch.client.transport.TransportClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class EsOperation {

	@Autowired
	TransportClient transportClient;
	
	
	
}
