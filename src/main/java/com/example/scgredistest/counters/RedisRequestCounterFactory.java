package com.example.scgredistest.counters;

import java.time.Duration;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.distributed.AsyncBucketProxy;
import io.github.bucket4j.distributed.ExpirationAfterWriteStrategy;
import io.github.bucket4j.distributed.proxy.RecoveryStrategy;
import io.github.bucket4j.redis.lettuce.cas.LettuceBasedProxyManager;
import io.lettuce.core.RedisClient;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;


public class RedisRequestCounterFactory implements RequestCounterFactory {

	final ReactiveRedisTemplate redisTemplate;

	public RedisRequestCounterFactory(ReactiveRedisTemplate redisTemplate) {
		this.redisTemplate = redisTemplate;
	}

	@Override
	public Mono<RequestCounter> createOfGet(String routeId, String apiKey, int limit, Duration duration) {
		long seconds = duration.getSeconds();
		Bandwidth bandwidth = Bandwidth.simple(limit, duration);
		return Mono.defer(() -> this.createBucket(routeId, apiKey, bandwidth))
				.map(Bucket4JRequestCounter::new);
	}

	private Mono<AsyncBucketProxy> createBucket(String routeId, String mapName, Bandwidth bandwidth) {
		// Hazelcast organizes data in 'maps' there isn't that in redis
		return Mono.just(redisTemplate)
				.publishOn(Schedulers.boundedElastic())
				.map(map -> {
					LettuceConnectionFactory connectionFactory = (LettuceConnectionFactory) redisTemplate.getConnectionFactory();
					RedisClient nativeClient = (RedisClient) connectionFactory.getNativeClient();
					long refillPeriodInSeconds = Duration.ofNanos(bandwidth.getRefillPeriodNanos()).toSeconds();
					var time = refillPeriodInSeconds / bandwidth.getRefillTokens();
					return LettuceBasedProxyManager.builderFor(nativeClient)
							// This seems important!!
//							.withExpirationStrategy(ExpirationAfterWriteStrategy.basedOnTimeForRefillingBucketUpToMax(Duration.ofSeconds(time)))
							.withExpirationStrategy(ExpirationAfterWriteStrategy.none())
							.build()
							.asAsync();
				})
				.map(proxy -> proxy.builder()
						.withRecoveryStrategy(RecoveryStrategy.RECONSTRUCT)
						.build(routeId.getBytes(), bucketConfiguration(bandwidth)));
	}

	private BucketConfiguration bucketConfiguration(Bandwidth bandwidth) {
		return BucketConfiguration.builder()
				.addLimit(bandwidth)
				.build();
	}

}
