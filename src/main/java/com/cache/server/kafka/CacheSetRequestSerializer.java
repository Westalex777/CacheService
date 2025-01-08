package com.cache.server.kafka;

import com.cache.server.dto.CacheSetRequest;
import org.apache.kafka.common.serialization.Serializer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

public class CacheSetRequestSerializer implements Serializer<CacheSetRequest> {

    @Override
    public byte[] serialize(String s, CacheSetRequest cacheSetRequest) {
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
             ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream)) {
            objectOutputStream.writeObject(cacheSetRequest);
            return byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

