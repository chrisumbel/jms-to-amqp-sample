package com.chrisumbel.integrations.jmstoamqpsample;

import javax.jms.Session;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.transaction.RabbitTransactionManager;
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
import org.springframework.integration.dsl.jms.Jms;
import org.springframework.jms.listener.DefaultMessageListenerContainer;

@SpringBootApplication
public class App {	
	@Autowired
	private javax.jms.ConnectionFactory jmsConnectionFactory;
		
	@Bean
//	@Profile("local")
	public org.springframework.amqp.rabbit.connection.ConnectionFactory defaultConnectionFactory() {
	    CachingConnectionFactory cf = new CachingConnectionFactory("localhost");
//	    cf.setAddresses("localhost");
	    cf.setUsername("guest");
	    cf.setPassword("guest");
	    cf.setVirtualHost("/");
	    return cf;
	}	
	
	@Bean 
	public RabbitTemplate amqpTemplate(org.springframework.amqp.rabbit.connection.ConnectionFactory connectionFactory) {
		RabbitTemplate template = new RabbitTemplate(connectionFactory);
		template.setChannelTransacted(true);
		return template;
	}	
	
	@Bean 
//	@Profile("local")
	public javax.jms.ConnectionFactory activemqConnectionFactory() 
	{
		return new ActiveMQConnectionFactory("tcp://localhost:61616");
	}

	@Bean
	public  RabbitTransactionManager rabbitTransactionManager() { 
		return new RabbitTransactionManager(defaultConnectionFactory());		
	}

	@Autowired
	RabbitTransactionManager transactionManager;	
	
	@Bean(name = PollerMetadata.DEFAULT_POLLER)
	public PollerMetadata poller() {

		return Pollers.fixedRate(500).transactional(transactionManager).get();
	}	

	@Bean
	public IntegrationFlow qIntegrationFlow(RabbitTemplate amqpTemplate) {		
		DefaultMessageListenerContainer container = Jms.container(this.jmsConnectionFactory, "queue1").transactionManager(transactionManager).get();
		
		return IntegrationFlows.from(Jms.messageDrivenChannelAdapter(container))
				.handle(Amqp.outboundAdapter(amqpTemplate).routingKey("shunted")).get();
	}
		
	public static void main(String[] args) throws Exception {
		SpringApplication.run(App.class, args);
	}
}
