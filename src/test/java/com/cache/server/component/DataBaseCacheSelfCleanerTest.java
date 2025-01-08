package com.cache.server.component;

import com.cache.server.repository.CacheEntity;
import com.cache.server.repository.CacheRepository;
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
public class DataBaseCacheSelfCleanerTest {

    @Autowired
    private DataBaseCacheSelfCleaner dataBaseCacheSelfCleaner;

    @Autowired
    private CacheRepository cacheRepository;

    @BeforeEach
    @Transactional
    void setUp() {
        List<CacheEntity> entities = new ArrayList<>();
        for (int i = 1; i <= 8; i++) {
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
        cacheEntityExpired = CacheEntity.builder()
                .primaryCacheKey(String.valueOf(1))
                .secondaryCacheKey("expired2")
                .created(LocalDateTime.now().minusDays(1))
                .expired(LocalDateTime.now().minusDays(2))
                .build();
        entities.add(cacheEntityExpired);

        cacheRepository.saveAll(entities);
    }

    @Test
    void cleanExpiredCacheEntriesTest() {
        dataBaseCacheSelfCleaner.cleanExpiredCache();
        var cacheEntities = cacheRepository.findAll();
        Assertions.assertEquals(8, cacheEntities.size());
    }

}
