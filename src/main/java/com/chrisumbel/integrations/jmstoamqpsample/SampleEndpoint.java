package com.chrisumbel.integrations.jmstoamqpsample;

import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.integration.annotation.ServiceActivator;

@MessageEndpoint
public class SampleEndpoint {

	public SampleEndpoint() {
	}

	@ServiceActivator
	public String hello(String input) throws Exception {
		return "SHUNTED: " + input;
	}
}
