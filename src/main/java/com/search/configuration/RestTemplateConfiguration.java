package com.search.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfiguration {

	@Bean
	RestTemplate restTemplate(){
	    SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
	    requestFactory.setConnectTimeout(1000);
	    requestFactory.setReadTimeout(1000);
	    RestTemplate restTemplate = new RestTemplate(requestFactory);
	    return restTemplate;
	}


}
