package com.cache.server.component;

import com.cache.server.repository.CacheEntity;
import com.cache.server.repository.CacheRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

/**
 * A scheduled component that cleans up expired entries from the database cache.
 *
 * <p>This cleaner is activated only when the property {@code cache.db.scheduled.enable=true}.
 * It runs periodically based on the cron expression defined in the property {@code cache.db.scheduled.cleaner-crone}.
 * The cleaner retrieves expired cache entries from the database and removes them from the in-memory cache.</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "cache.db.scheduled.enable", havingValue = "true")
public class DataBaseCacheSelfCleaner {

    private final EntityManager entityManager;
    private final CacheRepository cacheRepository;

    /**
     * Scheduled method that cleans expired entries from the database cache.
     *
     * <p>This method retrieves expired entries from the database using a Hibernate query and
     * removes the corresponding entries from the in-memory cache. If an exception occurs during
     * execution, it retries up to 5 times before giving up.</p>
     *
     * <p>It runs based on the cron expression specified in {@code cache.db.scheduled.cleaner-crone}.
     * The method is transactional, ensuring proper handling of database operations.</p>
     */
    @Scheduled(cron = "${cache.db.scheduled.cleaner-crone}")
    @Transactional
    public void cleanExpiredCache() {
        boolean retry;
        int countRetry = 0;
        do {
            try {
                log.info("Starting database cache cleanup process");
                AtomicInteger counter = new AtomicInteger(0);
                Session session = entityManager.unwrap(Session.class);
                Query<CacheEntity> query = session.createQuery(
                        "FROM CacheEntity c where c.expired <= :timestamp", CacheEntity.class
                );
                query.setParameter("timestamp", LocalDateTime.now());
                try (Stream<CacheEntity> userStream = query.stream()) {
                    userStream.forEach(o -> {
                        cacheRepository.deleteByPrimaryCacheKeyAndSecondaryCacheKeyTransactional(
                                o.getPrimaryCacheKey(), o.getSecondaryCacheKey()
                        );
                        counter.incrementAndGet();
                    });
                }
                session.close();
                retry = false;
                log.info("Database cache cleanup completed. Total records removed: {}", counter.get());
            } catch (Exception e) {
                log.error("Error during database cache cleanup attempt {}/5. Retrying...", countRetry, e);
                retry = true;
            }
            countRetry++;
        } while (retry && countRetry <= 5);
    }
}
