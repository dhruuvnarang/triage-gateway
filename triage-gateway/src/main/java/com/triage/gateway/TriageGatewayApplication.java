package com.triage.gateway;

import com.triage.gateway.vendor.TriageProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(TriageProperties.class)
public class TriageGatewayApplication {

	public static void main(String[] args) {
		SpringApplication.run(TriageGatewayApplication.class, args);
	}

}
