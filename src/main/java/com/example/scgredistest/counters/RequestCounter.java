package com.example.scgredistest.counters;

import reactor.core.publisher.Mono;

/**
 * A request counter associated with a particular route id. Instances of this counter can be cached if required.
 */
interface RequestCounter {
	Mono<ConsumeResponse> consume(String apiKey);
}
