package com.example.scgredistest.counters;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.cloud.gateway.event.RefreshRoutesEvent;
import org.springframework.cloud.gateway.route.RouteDefinitionLocator;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;


@Component
class RateLimitsRemover implements ApplicationListener<RefreshRoutesEvent> {

	private final Logger logger = LoggerFactory.getLogger(RateLimitsRemover.class);
	private static final String COUNTERS_RATE_LIMITER_FILTER_NAME = "CountersRequestRateLimiter";

	private final RouteDefinitionLocator routeDefinitionLocator;
	private final ApplicationEventPublisher publisher;
	private final StringRedisTemplate stringRedisTemplate;

	RateLimitsRemover(
			RouteDefinitionLocator routeDefinitionLocator,
			ApplicationEventPublisher publisher,
			StringRedisTemplate stringRedisTemplate) {
		this.routeDefinitionLocator = routeDefinitionLocator;
		this.publisher = publisher;
		this.stringRedisTemplate = stringRedisTemplate;
	}

	@Override
	public void onApplicationEvent(RefreshRoutesEvent event) {
		System.out.println("Processing event");
		routeDefinitionLocator
				.getRouteDefinitions()
				.collectList()
				.subscribe(definitions -> {
					definitions
							.stream()
							.filter(routeDefinition -> routeDefinition.getFilters()
									.stream()
									.anyMatch(filterDefinition -> filterDefinition.getName()
											.equals(COUNTERS_RATE_LIMITER_FILTER_NAME)))
							.forEach(routeDefinition -> {
								final String id = routeDefinition.getId();
								// Using a 'RedisTemplate' does not return string keys
								Set<String> keys = stringRedisTemplate.keys("test-counters-route-1");
								// just returns false if key does not exist
								stringRedisTemplate.delete(id);
								// Seems blocking. Is it safe since we don't run this to serve requests?
								logger.info("Deleted redis RateLimiter key: " + id);

								// TODO delete keys taking into account header or jwt claim
							});
				});
	}

	// Simulate events for local testing
//	@Scheduled(fixedRate = 5000)
//	public void reportCurrentTime() {
//		System.out.println("The time is now " + System.currentTimeMillis());
//		publisher.publishEvent(new RefreshRoutesEvent(this));
//	}

}
