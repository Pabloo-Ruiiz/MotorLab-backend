package com.motorlab;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class MotorlabBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(MotorlabBackendApplication.class, args);
	}

}
