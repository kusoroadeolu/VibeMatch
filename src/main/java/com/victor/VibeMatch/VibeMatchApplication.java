package com.victor.VibeMatch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class VibeMatchApplication {

	public static void main(String[] args) {
		SpringApplication.run(VibeMatchApplication.class, args);
	}

}
