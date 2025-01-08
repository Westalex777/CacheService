package com.cache.server.manager;

import com.cache.server.service.MemoryCache;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class MemoryCacheTest {

    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private MemoryCache<Object> cacheManager;

    @BeforeEach
    void setUp() {
        cacheManager = new MemoryCache<>();
        cacheManager.selfCleanerStart(3600, 3600);
        cacheManager.setCapacity(512);
        cacheManager.setThresholdGC(300);
        cacheManager.setThresholdPermissionActive(3);
    }

    @Test
    @Disabled
        // только для ручного тестирования, так как долгое выполнение
    void setTest() throws InterruptedException {
        // параметры выделения ОЗУ, использоваашиеся при тестировании -Xms256m -Xmx750m. capacity 512
        AtomicInteger counter = new AtomicInteger(0);
        int rps = 10_000;
        for (int i = 0; i < rps * 100; i++) {
            var b = cacheManager.set("1", String.valueOf(i), UUID.randomUUID());
            if (!b) {
                counter.incrementAndGet();
            }
            if (i >= rps && i % rps == 0) {
                Thread.sleep(1000);
            }
        }
        log.info("Не выполнено {} записей", counter.get());
    }

    @Test
    void getMethodPerformanceTest() throws InterruptedException {
        for (int i = 0; i < 400_000; i++) {
            cacheManager.set(String.valueOf(i / 2), String.valueOf(i), UUID.randomUUID());
        }
        Thread.sleep(1000);
        List<Long> r = new ArrayList<>();
        List<UUID> m = new ArrayList<>();
        for (int i = 0; i < 400_000; i++) {
            if (i % 500 == 0) {
                long start = System.currentTimeMillis();
                UUID v = (UUID) cacheManager.get(String.valueOf(i / 2), String.valueOf(i));
                if (v != null) {
                    m.add(v);
                }
                r.add(System.currentTimeMillis() - start);
            }
        }
        if (r.isEmpty() && m.isEmpty()) {
            throw new AssertionError();
        }
        var max = r.stream().sorted().limit(1).findAny().orElseThrow();
        Assertions.assertEquals(0L, max);
    }


    @Test
    void removeExpiredTest() throws InterruptedException {
        cacheManager = new MemoryCache<>();
        cacheManager.selfCleanerStart(1, 1);
        cacheManager.setLifeTime(4);
        int mSize = 0;
        for (int i = 0; i < 10_000; i++) {
            if (i == 500) {
                mSize = cacheManager.size();
            }
            if (i == 555 || i == 3456) {
                cacheManager.set(String.valueOf(i), String.valueOf(i), UUID.randomUUID(), 3600L);
            } else {
                cacheManager.set(String.valueOf(i), String.valueOf(i), UUID.randomUUID());
            }
        }
        Thread.sleep(5000);
        Assertions.assertEquals(2, cacheManager.size());
        Assertions.assertEquals(500, mSize);
    }

    @Test
    void removeTest() {
        cacheManager.set("1", "2", UUID.randomUUID());
        cacheManager.remove("1", "2");
        int size = cacheManager.size();
        Assertions.assertEquals(0, size);
        cacheManager.set("1", "2", UUID.randomUUID());
        cacheManager.set("1", "3", UUID.randomUUID());
        cacheManager.set("2", "3", UUID.randomUUID());
        cacheManager.remove("1");
        size = cacheManager.size();
        Assertions.assertEquals(1, size);
    }

}
