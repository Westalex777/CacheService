package com.cache.server.kafka;

import com.cache.server.CacheServerApplication;
import com.cache.server.dto.CacheSetRequest;
import com.cache.server.service.CacheManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConsumerSynchronization {

    private final CacheManager cacheManager;

    @KafkaListener(topics = "${kafka.topic-name}", groupId = "${kafka.group-id}")
    public void consume(CacheSetRequest message, @Header(ProducerSynchronization.APP_ID) String appId) {
        log.info("appId={}. Consumed message: {}", appId, message);
        if (!appId.equals(CacheServerApplication.ID.toString())) {
            cacheManager.set(message);
        }
    }
}
