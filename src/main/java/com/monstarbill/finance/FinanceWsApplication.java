package com.monstarbill.finance;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import feign.Logger;

@SpringBootApplication
@EnableJpaAuditing
@EnableEurekaClient
@EnableAutoConfiguration
@EnableFeignClients
public class FinanceWsApplication {

	public static void main(String[] args) {
		SpringApplication.run(FinanceWsApplication.class, args);
	}
	@Bean
	Logger.Level fiegnLoggerLevel() {
		return Logger.Level.FULL;
	}
	
	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
}
