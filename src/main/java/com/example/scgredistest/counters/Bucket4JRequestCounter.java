package com.example.scgredistest.counters;

import io.github.bucket4j.distributed.AsyncBucketProxy;
import reactor.core.publisher.Mono;

import static java.util.concurrent.TimeUnit.NANOSECONDS;

class Bucket4JRequestCounter implements RequestCounter {

	private final AsyncBucketProxy bucket;

	Bucket4JRequestCounter(AsyncBucketProxy bucket) {
		this.bucket = bucket;
	}

	@Override
	public Mono<ConsumeResponse> consume(String apiKey) {
		return Mono.fromFuture(bucket.tryConsumeAndReturnRemaining(1))
				   .map(consumptionProbe -> new ConsumeResponse(
						   consumptionProbe.isConsumed(),
						   consumptionProbe.getRemainingTokens(),
						   NANOSECONDS.toMillis(consumptionProbe.getNanosToWaitForRefill())));
	}
}
