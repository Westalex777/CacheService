package com.cache.server.service;

import com.cache.server.dto.CacheGetRequest;
import com.cache.server.dto.CacheResponse;
import com.cache.server.dto.CacheSetRequest;
import reactor.core.publisher.Mono;

public interface CacheManager {

    void set(CacheSetRequest request);

    void setAndSynchronization(CacheSetRequest request);

    Mono<CacheResponse> get(CacheGetRequest request);
}
