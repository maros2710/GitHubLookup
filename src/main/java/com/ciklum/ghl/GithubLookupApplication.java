package com.ciklum.ghl;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@SpringBootApplication
@EnableAsync
public class GithubLookupApplication {

	public static void main(String[] args) {
		SpringApplication.run(GithubLookupApplication.class, args);
	}

	@Bean
	@Qualifier("githubExecutor")
	public Executor taskExecutor() {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize(20);
		executor.setMaxPoolSize(20);
		executor.setQueueCapacity(1000);
		executor.setThreadNamePrefix("GithubLookupExecutor-");
		executor.initialize();
		return executor;
	}
}
