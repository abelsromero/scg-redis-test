package com.example.scgredistest.wrapper;

import java.util.List;

import reactor.core.publisher.Mono;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.cloud.gateway.support.ConfigurationService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;

@Configuration
public class RedisRateLimiterConfiguration {

	public static final String CUSTOM_REDIS_RATE_LIMITER_BEAN = "customRedisRateLimiter";

	// copied from scg-os 'GatewayRedisAutoConfiguration'
	@Bean(CUSTOM_REDIS_RATE_LIMITER_BEAN)
	RedisRateLimiter redisRateLimiter(
			ReactiveStringRedisTemplate redisTemplate,
			@Qualifier(RedisRateLimiter.REDIS_SCRIPT_NAME) RedisScript<List<Long>> redisScript,
			ConfigurationService configurationService
	) {

		RedisRateLimiter redisRateLimiter = new RedisRateLimiter(redisTemplate, redisScript, configurationService);
		// Same header as scg-k8s RateLimit
		redisRateLimiter.setRemainingHeader("X-Remaining");
		// TODO: Missing 'X-Retry-In' header
		return redisRateLimiter;
	}

	@Bean
	RedisRequestRateLimiterGatewayFilterFactory redisRequestRateLimiterGatewayFilterFactory(
			@Qualifier(CUSTOM_REDIS_RATE_LIMITER_BEAN) RedisRateLimiter redisRateLimiter,
			KeyResolver keyResolver
	) {
		return new RedisRequestRateLimiterGatewayFilterFactory(redisRateLimiter, keyResolver);
	}

	@Bean
	KeyResolver userKeyResolver() {
		return exchange -> Mono.just("GLOBAL_KEY");
	}

}
