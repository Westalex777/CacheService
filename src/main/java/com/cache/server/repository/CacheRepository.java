package com.cache.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface CacheRepository extends JpaRepository<CacheEntity, Integer> {

    CacheEntity findByPrimaryCacheKeyAndSecondaryCacheKeyOrderByIdDesc(String primaryCacheKey, String secondaryCacheKey);

    void deleteAllByPrimaryCacheKey(String primaryCacheKey);

    void deleteAllByPrimaryCacheKeyAndSecondaryCacheKey(String primaryCacheKey, String secondaryCacheKey);

    int countAllByPrimaryCacheKeyAndSecondaryCacheKey(String primaryCacheKey, String secondaryCacheKey);

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    default void deleteByPrimaryCacheKeyAndSecondaryCacheKeyTransactional(String primaryCacheKey, String secondaryCacheKey) {
        deleteAllByPrimaryCacheKeyAndSecondaryCacheKey(primaryCacheKey, secondaryCacheKey);
    }

}
