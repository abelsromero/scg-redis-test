package com.example.scgredistest.counters;

import java.util.HashMap;
import java.util.Map;

import com.example.scgredistest.shared.RateLimiterProperties;
import reactor.core.publisher.Mono;

import org.springframework.cloud.gateway.filter.ratelimit.AbstractRateLimiter;
import org.springframework.cloud.gateway.support.ConfigurationService;

/**
 * Manages the different configurations for RateLimit filters as well as
 * grating access if the request has available throughput.
 * <p>
 * IMPORTANT: filter configuration is indexed ber route-id, that means
 * that ONLY 1 RateLimit filter can be applied per route.
 */

class CustomRateLimiter extends AbstractRateLimiter<RateLimiterProperties> {

	private static final String CONFIGURATION_PROPERTY_NAME = "rate-limiter";
	private static final RateLimiterProperties DEFAULT_CONFIG = new RateLimiterProperties();
	private final RequestCounterFactory requestCounterFactory;

	// Signals that no valid key was found. Workaround because returning empty in KeyResolver triggers 403 response.
	static final String MISSING_KEY = "MISSING_RATE_LIMIT_KEY";

	public CustomRateLimiter(RequestCounterFactory requestCounterFactory,
			ConfigurationService configurationService) {
		super(RateLimiterProperties.class, CONFIGURATION_PROPERTY_NAME, configurationService);
		this.requestCounterFactory = requestCounterFactory;
	}

	@Override
	public Mono<Response> isAllowed(String routeId, String id) {
		if (MISSING_KEY.equals(id)) {
			return Mono.just(new Response(false, Map.of()));
		}

		final RateLimiterProperties config = getConfig().getOrDefault(routeId, DEFAULT_CONFIG);
		return requestCounterFactory.createOfGet(routeId, id, config.getLimit(), config.getDuration())
				.flatMap(requestCounter -> requestCounter.consume(id))
				.map(this::toResponse);
	}

	private Response toResponse(ConsumeResponse consumeResponse) {
		final Map<String, String> headers = new HashMap<>();
		if (consumeResponse.isAllowed()) {
			headers.put("X-Remaining", String.valueOf(consumeResponse.remainingRequests()));
		}
		else {
			headers.put("X-Retry-In", String.valueOf(consumeResponse.retryDelayMs()));
		}

		return new Response(consumeResponse.isAllowed(), headers);
	}
}
