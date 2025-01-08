package com.cache.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface CacheRepository extends JpaRepository<CacheEntity, Integer> {

    CacheEntity findByPrimaryCacheKeyAndSecondaryCacheKeyOrderByIdDesc(String primaryCacheKey, String secondaryCacheKey);

    void deleteAllByPrimaryCacheKey(String primaryCacheKey);

    void deleteAllByPrimaryCacheKeyAndSecondaryCacheKey(String primaryCacheKey, String secondaryCacheKey);

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    default void deleteByPrimaryCacheKeyAndSecondaryCacheKeyTransactional(String primaryCacheKey, String secondaryCacheKey) {
        deleteAllByPrimaryCacheKeyAndSecondaryCacheKey(primaryCacheKey, secondaryCacheKey);
    }

    @Modifying
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Query(nativeQuery = true, value = """ 
                        WITH ranked_rows AS (
                            SELECT id, primary_cache_key, secondary_cache_key, created,
                                   ROW_NUMBER() OVER (PARTITION BY primary_cache_key, secondary_cache_key ORDER BY created DESC) AS rn
                            FROM cache_entry)
                        DELETE FROM cache_entry
                        WHERE id IN (SELECT id FROM ranked_rows WHERE rn > 1)
            """)
    void deleteDuplicateEntries();

}
