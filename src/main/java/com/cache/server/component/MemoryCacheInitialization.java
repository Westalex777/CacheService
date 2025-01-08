package com.cache.server.component;

import com.cache.server.repository.CacheEntity;
import com.cache.server.repository.CacheRepository;
import com.cache.server.service.CacheProvider;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

/**
 * Component for initializing the in-memory cache with data from the database.
 * This class retrieves entities from the database, checks their expiration times,
 * and loads valid entities into the memory cache.
 * If an error occurs during initialization, it will retry up to 5 times.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MemoryCacheInitialization {

    private final EntityManager entityManager;
    private final CacheProvider<Object> memoryCache;
    private final CacheRepository cacheRepository;

    /**
     * Initializes the in-memory cache by loading entities from the database.
     * The method is scheduled to run once after a 1-second delay upon application startup.
     * <p>
     * It processes all {@link CacheEntity} objects in the database, calculates their
     * remaining expiration time, and loads valid entities into the cache.
     * Entities that have already expired are skipped.
     * <p>
     * If an error occurs, the initialization process retries up to 5 times.
     * The number of successfully loaded and skipped (expired) entities is logged.
     */
    @Scheduled(initialDelay = 1000)
    @Transactional
    public void init() {
        boolean retry;
        int countRetry = 0;
        do {
            try {
                log.info("Initializing cache {}", countRetry > 0 ? "Retrying " + countRetry : "");
                cacheRepository.deleteDuplicateEntries();
                AtomicInteger loadCount = new AtomicInteger(0);
                AtomicInteger expiredCount = new AtomicInteger(0);
                Session session = entityManager.unwrap(Session.class);
                Query<CacheEntity> query = session.createQuery("FROM CacheEntity", CacheEntity.class);
                try (Stream<CacheEntity> userStream = query.stream()) {
                    userStream.forEach(o -> {
                        var expiredSeconds = Duration.between(LocalDateTime.now(), o.getExpired()).toSeconds();
                        if (expiredSeconds > 1) {
                            memoryCache.set(o.getPrimaryCacheKey(), o.getSecondaryCacheKey(), o.getCacheValue(), expiredSeconds);
                            loadCount.incrementAndGet();
                        } else {
                            expiredCount.incrementAndGet();
                        }
                    });
                }
                session.close();
                log.info("Cache initialization completed. Loaded {}, expired {}", loadCount.get(), expiredCount.get());
                retry = false;
            } catch (Exception e) {
                log.error("Error initializing cache", e);
                retry = true;
            }
            countRetry++;
        } while (retry && countRetry <= 5);
    }
}
