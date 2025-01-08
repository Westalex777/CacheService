package com.cache.server.controller;

import com.cache.server.dto.CacheGetRequest;
import com.cache.server.dto.CacheSetRequest;
import com.cache.server.dto.CacheResponse;
import com.cache.server.dto.ErrorResponse;
import com.cache.server.exception.CachedValueNotFoundException;
import com.cache.server.service.CacheService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * REST контроллер для управления кешированием данных.
 * Предоставляет конечные точки для добавления и извлечения данных в/из кеша.
 */
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/cache")
public class CacheController {

    private final CacheService<Object> cacheService;

    /**
     * Добавляет данные в кеш.
     * Если время истечения не указано в запросе, данные сохраняются без срока действия.
     * Данный метод выполняет асинхронную операцию через `Mono` и возвращает статус 202 (ACCEPTED) после завершения.
     *
     * @param request объект, содержащий данные для установки в кеш.
     * @return ответ с HTTP статусом 202 (ACCEPTED).
     */
    @PostMapping("/set")
    public Mono<ResponseEntity<?>> set(@Valid @RequestBody CacheSetRequest request) {
        requestSettingAdapter(request)
                .subscribeOn(Schedulers.boundedElastic())
                .subscribe();
        return Mono.just(ResponseEntity.status(HttpStatus.ACCEPTED).build());
    }

    /**
     * Извлекает данные из кеша по указанным ключам.
     * Если данные не найдены, возвращается ошибка с соответствующим статусом.
     *
     * @param request объект, содержащий ключи для поиска в кеше.
     * @return объект с данными из кеша или ошибка 404, если данные не найдены.
     */
    @PostMapping("/get")
    public Mono<ResponseEntity<CacheResponse>> get(@Valid @RequestBody CacheGetRequest request) {
        return cacheService.get(request.getPrimaryCacheKey(), request.getSecondaryCacheKey())
                .map(CacheResponse::new)
                .map(ResponseEntity::ok);
    }

    /**
     * Обработчик исключения, когда запрашиваемое значение не найдено в кеше.
     * Возвращает ошибку 404 с сообщением, полученным из исключения.
     *
     * @param e исключение, которое будет обработано.
     * @return ответ с HTTP статусом 404 (NOT_FOUND) и подробным сообщением об ошибке.
     */
    @ExceptionHandler(CachedValueNotFoundException.class)
    public ResponseEntity<ErrorResponse> cachedValueNotFoundExceptionHandler(CachedValueNotFoundException e) {
        ErrorResponse response = new ErrorResponse(e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
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
