package com.triage.gateway.idempotency;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.triage.gateway.error.GatewayException;
import com.triage.gateway.model.VerifyResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;

/**
 * Redis-backed idempotency. The first request claims the key. A duplicate gets the stored
 * response, or 409 while the first request is still running.
 */
@Component
public class IdempotencyStore {

	private static final String IN_PROGRESS = "IN_PROGRESS";

	private final StringRedisTemplate redis;
	private final ObjectMapper mapper;
	private final Duration ttl;
	private final Duration inProgressTtl;

	public IdempotencyStore(StringRedisTemplate redis, ObjectMapper mapper,
			@Value("${triage.idempotency.ttl}") Duration ttl,
			@Value("${triage.idempotency.in-progress-ttl}") Duration inProgressTtl) {
		this.redis = redis;
		this.mapper = mapper;
		this.ttl = ttl;
		this.inProgressTtl = inProgressTtl;
	}

	/** Empty means this request now owns the key. Present means it is a replay. */
	public Optional<VerifyResponse> claimOrReplay(String key) {
		String redisKey = redisKey(key);
		Boolean claimed = redis.opsForValue().setIfAbsent(redisKey, IN_PROGRESS, inProgressTtl);
		if (Boolean.TRUE.equals(claimed)) {
			return Optional.empty();
		}
		String stored = redis.opsForValue().get(redisKey);
		if (stored == null || IN_PROGRESS.equals(stored)) {
			throw new GatewayException(409, "IDEMPOTENCY_IN_PROGRESS",
					"a request with this Idempotency-Key is already being processed");
		}
		return Optional.of(read(stored));
	}

	public void complete(String key, VerifyResponse response) {
		redis.opsForValue().set(redisKey(key), write(response), ttl);
	}

	/** Drops the claim so the client can retry after a failure. */
	public void release(String key) {
		redis.delete(redisKey(key));
	}

	private static String redisKey(String key) {
		return "idem:" + key;
	}

	private String write(VerifyResponse response) {
		try {
			return mapper.writeValueAsString(response);
		}
		catch (JsonProcessingException e) {
			throw new IllegalStateException("cannot store response", e);
		}
	}

	private VerifyResponse read(String json) {
		try {
			return mapper.readValue(json, VerifyResponse.class);
		}
		catch (JsonProcessingException e) {
			throw new IllegalStateException("corrupt idempotency record", e);
		}
	}

}
