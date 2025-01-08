package com.cache.server.kafka;

import com.cache.server.CacheServerApplication;
import com.cache.server.config.KafkaProperties;
import com.cache.server.dto.CacheSetRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProducerSynchronization {

    private final KafkaTemplate<CacheSetRequest, CacheSetRequest> kafkaTemplate;
    private final KafkaProperties kafkaProperties;
    private static final byte[] ID = CacheServerApplication.ID.toString().getBytes();
    public static final String APP_ID = "app-id";

    public void sendMessage(CacheSetRequest message) {
        ProducerRecord<CacheSetRequest, CacheSetRequest> record = new ProducerRecord<>(kafkaProperties.getTopicName(), message);
        record.headers().add(APP_ID, ID);
        kafkaTemplate.send(record);
        log.info("send message to topic {} : {}", kafkaProperties.getTopicName(), message);
    }
}
