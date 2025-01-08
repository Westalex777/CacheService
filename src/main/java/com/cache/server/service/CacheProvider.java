package com.cache.server.service;

/**
 * A generic interface for managing cache operations.
 *
 * @param <T> the type of values stored in the cache.
 */
public interface CacheProvider<T> {

    /**
     * Stores a value in the cache with the default lifetime.
     *
     * @param key1  the primary key.
     * @param key2  the secondary key.
     * @param value the value to store.
     * @return {@code true} if the value was successfully stored, {@code false} otherwise.
     */
    boolean set(String key1, String key2, T value);

    /**
     * Stores a value in the cache with a specified lifetime.
     *
     * @param key1     the primary key.
     * @param key2     the secondary key.
     * @param value    the value to store.
     * @param lifeTime the lifetime of the cache entry in seconds.
     * @return {@code true} if the value was successfully stored, {@code false} otherwise.
     */
    boolean set(String key1, String key2, T value, Long lifeTime);

    /**
     * Retrieves a value from the cache.
     *
     * @param key1 the primary key.
     * @param key2 the secondary key.
     * @return the cached value, or {@code null} if the key does not exist or the value has expired.
     */
    T get(String key1, String key2);

    /**
     * Removes all values associated with the primary key from the cache.
     *
     * @param key1 the primary key.
     */
    void remove(String key1);

    /**
     * Removes a specific value associated with the primary and secondary keys from the cache.
     *
     * @param key1 the primary key.
     * @param key2 the secondary key.
     */
    void remove(String key1, String key2);
}
