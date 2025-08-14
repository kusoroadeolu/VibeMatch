package com.victor.VibeMatch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.retry.annotation.EnableRetry;

import java.util.HashSet;
import java.util.Set;

@SpringBootApplication
@EntityScan({
		"com.victor.VibeMatch.user",
		"com.victor.VibeMatch.userartist",
		"com.victor.VibeMatch.usergenre",
		"com.victor.VibeMatch.usertrack",
		"com.victor.VibeMatch.connections"
})
@EnableCaching
@EnableRetry
public class VibeMatchApplication {
	public static void main(String[] args) {
		SpringApplication.run(VibeMatchApplication.class, args);

}




}
