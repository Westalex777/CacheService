package com.cache.server.service;

import com.cache.server.config.CacheProperties;
import com.cache.server.repository.CacheEntity;
import com.cache.server.repository.CacheRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
public class DataBaseCache implements CacheManager<String> {

    private final long lifeTime;
    private final CacheRepository cacheRepository;

    public DataBaseCache(CacheRepository cacheRepository, CacheProperties cacheProperties) {
        this.lifeTime = cacheProperties.getDefaultLifeTime() != null ? cacheProperties.getDefaultLifeTime() : 604_800L;
        this.cacheRepository = cacheRepository;
    }

    @Override
    @Transactional
    public boolean set(String key1, String key2, String value) {
        return set(key1, key2, value, lifeTime);
    }

    @Override
    @Transactional
    public boolean set(String key1, String key2, String value, Long lifeTime) {
        log.info("Setting value: key1={}, key2={}, lifeTime={} seconds", key1, key2, lifeTime);
        try {
            var count = cacheRepository.countAllByPrimaryCacheKeyAndSecondaryCacheKey(key1, key2);
            if (count > 0) {
                remove(key1, key2);
            }
            cacheRepository.save(cacheEntityBuild(key1, key2, value, lifeTime));
            return true;
        } catch (DataAccessException e) {
            log.error(e.getMessage());
            return false;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public String get(String key1, String key2) {
        return cacheRepository.findByPrimaryCacheKeyAndSecondaryCacheKeyOrderByIdDesc(key1, key2).getCacheValue();
    }

    @Override
    @Transactional
    public void remove(String key1) {
        cacheRepository.deleteAllByPrimaryCacheKey(key1);
    }

    @Override
    @Transactional
    public void remove(String key1, String key2) {
        cacheRepository.deleteAllByPrimaryCacheKeyAndSecondaryCacheKey(key1, key2);
    }

    private CacheEntity cacheEntityBuild(String key1, String key2, String value, Long lifeTime) {
        var now = LocalDateTime.now();
        return CacheEntity.builder()
                .primaryCacheKey(key1)
                .secondaryCacheKey(key2)
                .expired(now.plusSeconds(lifeTime))
                .created(now)
                .cacheValue(value)
                .build();
    }
}
