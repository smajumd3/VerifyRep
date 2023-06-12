package com.ibm.workday.automation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;

@SpringBootApplication(exclude = HibernateJpaAutoConfiguration.class)
public class HyperloaderApplication {

	public static void main(String[] args) {
		SpringApplication.run(HyperloaderApplication.class, args);
	}

}
