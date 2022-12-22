package com.example.scgredistest.counters;

import org.springframework.cloud.gateway.support.ConfigurationService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;

@Configuration
public class CountersRateLimiterConfiguration {

	@Bean
	RedisRequestCounterFactory redisRequestCounterFactory(
			ReactiveStringRedisTemplate reactiveRedisTemplate
	) {
		return new RedisRequestCounterFactory(reactiveRedisTemplate);
	}

	@Bean
	CustomRateLimiter customRateLimiter(
			RedisRequestCounterFactory requestCounterFactory,
			ConfigurationService configurationService
	) {
		return new CustomRateLimiter(requestCounterFactory, configurationService);
	}

	@Bean
	CountersRequestRateLimiterGatewayFilterFactory countersRequestRateLimiterGatewayFilterFactory(
			CustomRateLimiter rateLimiter
	) {
		return new CountersRequestRateLimiterGatewayFilterFactory(rateLimiter);
	}
}
