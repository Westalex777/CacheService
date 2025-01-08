package com.cache.server.service;

import com.cache.server.dto.CacheSetRequest;
import com.cache.server.kafka.ProducerSynchronization;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CacheSynchronization {

    private final ProducerSynchronization kafkaProducerSynchronization;

    public void writeReplicas(CacheSetRequest request) {
        log.info("Write replicas. key1={}. key2={}", request.getPrimaryCacheKey(), request.getSecondaryCacheKey());
        kafkaProducerSynchronization.sendMessage(request);
    }

}
