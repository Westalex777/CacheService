package com.cache.server.config;

import com.cache.server.service.CacheManager;
import com.cache.server.service.MemoryCache;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class CacheManagerConfig {

    private final CacheProperties cacheProperties;

    @Bean
    public CacheManager<Object> memoryCache() {
        MemoryCache<Object> memoryCache = new MemoryCache<>();
        var propertiesMemory = cacheProperties.getMemory();
        if (propertiesMemory.getCapacity() != null) {
            memoryCache.setCapacity(propertiesMemory.getCapacity());
        }
        if (propertiesMemory.getThresholdGC() != null) {
            memoryCache.setThresholdGC(propertiesMemory.getThresholdGC());
        }
        if (propertiesMemory.getThresholdPermissionActive() != null) {
            memoryCache.setThresholdPermissionActive(propertiesMemory.getThresholdPermissionActive());
        }
        if (cacheProperties.getDefaultLifeTime() != null) {
            memoryCache.setLifeTime(cacheProperties.getDefaultLifeTime());
        }
        memoryCache.selfCleanerStart(
                propertiesMemory.getSelfCleaner().getInitialDelay(),
                propertiesMemory.getSelfCleaner().getPeriod()
        );
        return memoryCache;
    }
}
