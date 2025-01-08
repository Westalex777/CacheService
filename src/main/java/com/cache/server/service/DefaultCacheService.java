package com.cache.server.service;

import com.cache.server.exception.CachedValueNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class DefaultCacheService implements CacheService<Object> {

    private final CacheManager<Object> memoryCache;
    private final CacheManager<String> dataBaseCache;

    @Override
    public Mono<Void> set(String key1, String key2, Object value) {
        return Mono.just(value)
                .doOnNext(o -> memoryCache.set(key1, key2, o))
                .map(o -> dataBaseCache.set(key1, key2, String.valueOf(o)))
                .then();
    }

    @Override
    public Mono<Void> set(String key1, String key2, Long expired, Object value) {
        return Mono.just(value)
                .doOnNext(o -> memoryCache.set(key1, key2, o, expired))
                .map(o -> dataBaseCache.set(key1, key2, String.valueOf(o), expired))
                .then();
    }

    @Override
    public Mono<Object> get(String key1, String key2) {
        var result = memoryCache.get(key1, key2);
        if (result == null) {
            var message = String.format("Value from cached key1=%s, key2=%s not found", key1, key2);
            throw new CachedValueNotFoundException(message);
        }
        return Mono.just(result)
                .thenReturn(result);
    }
}
