package com.example.scgredistest;

import org.springframework.cloud.gateway.filter.ratelimit.RateLimiter;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
public class AppConfiguration {

	// Avoid issue with SCG initialization:
	// Parameter 0 of method requestRateLimiterGatewayFilterFactory in org.springframework.cloud.gateway.config.GatewayAutoConfiguration required a single bean, but 2 were found:
	// TODO Final solution should define a neutral rate limiter
	@Primary
	@Bean
	public RateLimiter primaryRateLimiter(RedisRateLimiter defaultRateLimiter) {
		return defaultRateLimiter;
	}
}
