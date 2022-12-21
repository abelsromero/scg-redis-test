package com.example.scgredistest.wrapper;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.cloud.gateway.filter.factory.RequestRateLimiterGatewayFilterFactory;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;

public class RedisRequestRateLimiterGatewayFilterFactory
		extends AbstractGatewayFilterFactory<RequestRateLimiterGatewayFilterFactory.Config> {

	final RedisRateLimiter redisRateLimiter;
	final KeyResolver keyResolver;

	public RedisRequestRateLimiterGatewayFilterFactory(RedisRateLimiter redisRateLimiter, KeyResolver keyResolver) {
		this.redisRateLimiter = redisRateLimiter;
		this.keyResolver = keyResolver;
	}

	@Override
	public GatewayFilter apply(RequestRateLimiterGatewayFilterFactory.Config config) {

		// Here we can transform reqs-over-time configuration to Redis configurations
		final RedisRateLimiter.Config redisConfig = new RedisRateLimiter.Config();
		redisConfig.setReplenishRate(1);
		redisConfig.setRequestedTokens(2);
		redisConfig.setBurstCapacity(2);
		redisRateLimiter.getConfig().put(config.getRouteId(), redisConfig);

		// Here we can also customize the keyResolver if necessary

		var requestRateLimiterGatewayFilterFactory = new RequestRateLimiterGatewayFilterFactory(redisRateLimiter, keyResolver);

		return requestRateLimiterGatewayFilterFactory.apply(config);
	}

	@Override
	public Class<RequestRateLimiterGatewayFilterFactory.Config> getConfigClass() {
		return RequestRateLimiterGatewayFilterFactory.Config.class;
	}
}
