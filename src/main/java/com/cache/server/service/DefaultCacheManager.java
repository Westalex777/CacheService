package com.cache.server.service;

import com.cache.server.dto.CacheGetRequest;
import com.cache.server.dto.CacheResponse;
import com.cache.server.dto.CacheSetRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
@RequiredArgsConstructor
public class DefaultCacheManager implements CacheManager {

    private final CacheService<Object> cacheService;
    private final CacheSynchronization cacheSynchronization;

    @Override
    public void set(CacheSetRequest request) {
        requestSettingAdapter(request)
                .subscribeOn(Schedulers.boundedElastic())
                .subscribe();
    }

    @Override
    public void setAndSynchronization(CacheSetRequest request) {
        set(request);
        cacheSynchronization.writeReplicas(request);
    }

    @Override
    public Mono<CacheResponse> get(CacheGetRequest request) {
        return cacheService.get(request.getPrimaryCacheKey(), request.getSecondaryCacheKey())
                .map(CacheResponse::new);
    }

    /**
     * Адаптирует запрос на установку данных в кеш с учетом времени истечения.
     * Если время истечения не указано, данные сохраняются без срока действия.
     *
     * @param request объект, содержащий данные для установки в кеш.
     * @return асинхронная операция по установке данных в кеш.
     */
    private Mono<Void> requestSettingAdapter(CacheSetRequest request) {
        if (request.getExpired() == null) {
            return cacheService.set(request.getPrimaryCacheKey(), request.getSecondaryCacheKey(), request.getValue());
        }
        return cacheService.set(request.getPrimaryCacheKey(), request.getSecondaryCacheKey(), request.getExpired(), request.getValue());
    }
}
