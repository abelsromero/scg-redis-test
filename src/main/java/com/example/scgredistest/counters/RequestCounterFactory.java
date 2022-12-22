package com.example.scgredistest.counters;

import java.time.Duration;

import reactor.core.publisher.Mono;

/**
 * This is an abstraction to create a request counter for a given route id. apiKey is optional, it is also passed to {@link RequestCounter}.
 * However, some implementations need to know API key in advance, e.g. Bucket4j which takes API key in builder params.
 */
interface RequestCounterFactory {

	Mono<RequestCounter> createOfGet(String routeId, String apiKey, int limit, Duration duration);

}
