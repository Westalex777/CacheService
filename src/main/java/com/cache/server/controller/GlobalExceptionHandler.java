package com.cache.server.controller;

import com.cache.server.dto.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * Глобальный обработчик исключений для всего приложения.
 * Перехватывает все исключения типа {@link Exception} и возвращает
 * ответ с кодом ошибки 500 (INTERNAL_SERVER_ERROR) и подробным сообщением.
 */
@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Обработчик всех исключений типа {@link Exception}.
     * Логирует исключение и возвращает клиенту ошибку 500 с сообщением.
     *
     * @param e исключение, которое произошло в приложении.
     * @return ответ с HTTP статусом 500 (INTERNAL_SERVER_ERROR) и сообщением об ошибке.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> globalExceptionHandler(Exception e) {
        // Логируем исключение
        log.error(e.getMessage(), e);

        // Формируем ответ с ошибкой
        ErrorResponse response = new ErrorResponse(e.getMessage());

        // Возвращаем ответ с кодом 500 (INTERNAL_SERVER_ERROR)
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
