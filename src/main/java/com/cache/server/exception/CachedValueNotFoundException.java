package com.cache.server.exception;

public class CachedValueNotFoundException extends RuntimeException {

    public CachedValueNotFoundException(String message) {
        super(message);
    }

}
