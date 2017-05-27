package com.chrisumbel.integrations.jmstoamqpsample;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.scheduling.PollerMetadata;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.MessageSources;
import org.springframework.integration.dsl.amqp.Amqp;
import org.springframework.integration.dsl.core.Pollers;

@SpringBootApplication
public class App {	
	@Autowired
	private javax.jms.ConnectionFactory jmsConnectionFactory;
	
	@Autowired
	private RabbitTemplate amqpTemplate;	
	
	@Bean
	@Profile("local")
	public org.springframework.amqp.rabbit.connection.ConnectionFactory defaultConnectionFactory() {
	    CachingConnectionFactory cf = new CachingConnectionFactory("RABBITMQ-HOST");
	    cf.setAddresses("RABBITMQ-HOST");
	    cf.setUsername("guest");
	    cf.setPassword("guest");
	    cf.setVirtualHost("/");
	    return cf;
	}	
	
	@Bean 
	@Profile("local")
	javax.jms.ConnectionFactory activemqConnectionFactory() 
	{
		return new ActiveMQConnectionFactory("tcp://ACTIVEMQ-HOST:61616");
	}
		
	@Bean(name = PollerMetadata.DEFAULT_POLLER)
	public PollerMetadata poller() {
		return Pollers.fixedRate(500).get();
	}	

	@Bean
	public IntegrationFlow qIntegrationFlow(SampleEndpoint endpoint) {		
		return IntegrationFlows.from((MessageSources s) -> s.jms(this.jmsConnectionFactory).destination("queue1"))
				.handle(endpoint)
				.handle(Amqp.outboundAdapter(this.amqpTemplate).routingKey("shunted")).get();
	}
	
	public static void main(String[] args) throws Exception {
		SpringApplication.run(App.class, args);
	}
}
