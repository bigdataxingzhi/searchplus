package com.search.configuration;

import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.HeadersExchange;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.fasterxml.jackson.databind.ObjectMapper;


/**
 * spring boot的自动配置原理,已经帮我们配置好了RabbitMQ
 * RabbitAutoConfiguration 是配置rabbit的主类.
 *    ====>RabbitProperties properties---->封装了spring对rabbitMQ配置的属性(prefix = "spring.rabbitmq")
 *    ====>①配置了 CachingConnectionFactory rabbitConnectionFactory -->rabbitMQ的连接工场
 *    ====>②配置了RabbitTemplate rabbitTemplate--->给rabbitmq发送和接收消息
 *    ====>③配置了AmqpAdmin amqpAdmin---->rabbitmq系统管理组件,可用来创建队列,交换器,绑定规则等.
 *    
 * @author 星志
 *
 */
@EnableRabbit
@Configuration
public class RabbitMQConfiguration {
	
	@Autowired
	RabbitTemplate rabbitTemplate;
	
	@Autowired
	AmqpAdmin amqpAdmin;
	
	
	/**
	 * 指定发送到队列的对象,使用哪种消息转换机制,默认为jdk的序列化
	 * @return
	 */
	@Bean
	public MessageConverter messageConverter() {
		return new Jackson2JsonMessageConverter();
	}
	
	
	/**
	 * 根据类型创建对应的交换机
	 * @param name
	 * @param clazz
	 */
	public < T extends Exchange> void createExchange(String name ,Class<T> clazz) {
		Exchange exchange = null;
		switch (clazz.getTypeName()) {
		case "org.springframework.amqp.core.DirectExchange":
			exchange = new DirectExchange(name);
			break;
		case "org.springframework.amqp.core.FanoutExchange":
			exchange = new FanoutExchange(name);
		break;
		
		case "org.springframework.amqp.core.HeadersExchange":
			exchange = new HeadersExchange(name);
		break;
		case "org.springframework.amqp.core.TopicExchange":
			exchange = new TopicExchange(name);
			break;
		default:
			exchange = new TopicExchange(name);
			break;
		}
		amqpAdmin.declareExchange(exchange);
	}
	
	
	/**
	 * 创建队列
	 * @param name
	 */
	public	void createQueue(String name) {
		amqpAdmin.deleteQueue(name);
	}
	
	/**
	 * 添加绑定规则
	 * @param queueName
	 * @param exchangeName
	 * @param routingKey
	 */
	public	void createBinding(String queueName,String exchangeName,String routingKey) {
		amqpAdmin.declareBinding(new Binding(queueName, Binding.DestinationType.QUEUE, exchangeName, routingKey, null));
	}
	
	
	/**
	 * 
	 * @param object 默认当成消息体
	 */
	public  void convertAndSend(String exchange,String routingKey,Object object)
	{
		//对象默认被以java序列化的形式发送出去
		rabbitTemplate.convertAndSend(exchange,routingKey,object);
	}
	
	/**
	 * 接收消息并转化为Object
	 * @param queueName
	 * @return
	 */
	public Object receiveAndConvert(String queueName) {
		Object object = rabbitTemplate.receiveAndConvert(queueName);
		return object;
	}
}
