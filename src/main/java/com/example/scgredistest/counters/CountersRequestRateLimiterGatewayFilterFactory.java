package com.example.scgredistest.counters;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

import com.example.scgredistest.shared.RateLimiterProperties;
import reactor.blockhound.BlockHound;
import reactor.core.publisher.Mono;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.cloud.gateway.filter.factory.RequestRateLimiterGatewayFilterFactory;

public class CountersRequestRateLimiterGatewayFilterFactory
		extends AbstractGatewayFilterFactory<RateLimiterProperties> {

	final CustomRateLimiter rateLimiter;

	private final RequestRateLimiterGatewayFilterFactory requestRateLimiterGatewayFilterFactory;

	static final String DEFAULT_RATE_LIMIT_MAP = "GLOBAL_RATE_LIMIT";

	CountersRequestRateLimiterGatewayFilterFactory(CustomRateLimiter defaultRateLimiter) {
		this.rateLimiter = defaultRateLimiter;
		this.requestRateLimiterGatewayFilterFactory =
				new RequestRateLimiterGatewayFilterFactory(defaultRateLimiter,
						exchange -> Mono.just(DEFAULT_RATE_LIMIT_MAP));
	}

	@Override
	public GatewayFilter apply(RateLimiterProperties config) {
		BlockHound.install();

		rateLimiter.getConfig().put(config.getRouteId(), config);
		RequestRateLimiterGatewayFilterFactory.Config requestRateLimiterConfig = new RequestRateLimiterGatewayFilterFactory.Config();
		requestRateLimiterConfig.setRouteId(config.getRouteId());

		return requestRateLimiterGatewayFilterFactory.apply(requestRateLimiterConfig);
	}

	@Override
	public List<String> shortcutFieldOrder() {
		return Arrays.asList("limit", "duration", "keyLocation");
	}

	@Override
	public Class<RateLimiterProperties> getConfigClass() {
		return RateLimiterProperties.class;
	}
}
