package com.search.configuration;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.impl.XMLResponseParser;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SearchConfiguration {
	
	private static final Logger log = LoggerFactory.getLogger(SearchConfiguration.class);
	
	@Autowired
	public SearchConfig searchConfig;
	
	@Configuration
	@ConditionalOnProperty(prefix="search",name="searchType",havingValue="elasticsearch")
	@ConditionalOnClass(TransportClient.class)
	class ElasticSearchConfiguration{
		@Bean
	    public TransportClient bulidEsClient() throws UnknownHostException {
			log.info("开始构建es客户端");
			Settings settings = Settings.builder()
	                .put("cluster.name", searchConfig.getElasticsearchClusterName())
	                .put("client.transport.sniff", true)//自动翻页节点
	                .build();

	        //此处还可以添加从节点
	        InetSocketTransportAddress master = new InetSocketTransportAddress( InetAddress.getByName(searchConfig.getElasticsearchHost()), Integer.parseInt(searchConfig.getElasticsearchPort()));
	        log.info("es主节点的host为:{},port为:{}",master.address().getHostString(),master.address().getPort());
	        //此处还可以添加从节点
	        TransportClient client = new PreBuiltTransportClient(settings)
	                .addTransportAddress(master);
	        log.info("成功构建es客户端");
	        return client;
	    }
	}
	
	
	@Configuration
	@ConditionalOnProperty(prefix="search",name="searchType",havingValue="solr")
	@ConditionalOnClass(HttpSolrServer.class)
	class SolrConfiguration{
		
		@Bean
		public HttpSolrServer buildhttpSolrServer() {
			log.info("开始构建solr客户端");
			HttpSolrServer httpSolrServer = new HttpSolrServer(searchConfig.getSoleCoreURL());
	        log.info("solr core 的地址为:{}",searchConfig.getSoleCoreURL());
			httpSolrServer.setParser(new XMLResponseParser());
			//重试次数,推荐设置为1
			httpSolrServer.setMaxRetries(searchConfig.getSolrMaxRetries());
			//建立连接的最长时间
			httpSolrServer.setConnectionTimeout(searchConfig.getSolrConnectiontimeout());
			log.info("成功构建solr客户端");
			return httpSolrServer;
		}
	}

}
