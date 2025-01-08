package com.cache.server.component;

import com.cache.server.repository.CacheEntity;
import com.cache.server.repository.CacheRepository;
import com.cache.server.service.CacheManager;
import com.cache.server.service.MemoryCache;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@SpringBootTest
@ExtendWith(SpringExtension.class)
public class MemoryCacheInitializationTest {

    @Autowired
    private CacheManager<Object> memoryCache;

    @Autowired
    private MemoryCacheInitialization memoryCacheInitialization;

    @Autowired
    private CacheRepository cacheRepository;

    private MemoryCache<Object> memoryCacheOriginal;

    @BeforeEach
    @Transactional
    void setUp() {
        memoryCacheOriginal = (MemoryCache<Object>) memoryCache;

        List<CacheEntity> entities = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            var cacheEntityValid = CacheEntity.builder()
                    .primaryCacheKey(String.valueOf(i))
                    .secondaryCacheKey(String.valueOf(i * 2))
                    .created(LocalDateTime.now().minusDays(i))
                    .expired(LocalDateTime.now().plusDays(i))
                    .build();
           entities.add(cacheEntityValid);
        }

        var cacheEntityExpired = CacheEntity.builder()
                .primaryCacheKey(String.valueOf(1))
                .secondaryCacheKey("expired")
                .created(LocalDateTime.now().minusDays(1))
                .expired(LocalDateTime.now().minusDays(2))
                .build();
        entities.add(cacheEntityExpired);

        cacheRepository.saveAll(entities);
    }

    @Test
    @Transactional(readOnly = true)
    void testCacheInitialization() {
        int startSize = memoryCacheOriginal.size();
        memoryCacheInitialization.init();
        int initialSize = memoryCacheOriginal.size();
        Assertions.assertEquals(0, startSize);
        Assertions.assertEquals(10, initialSize);
    }
}
