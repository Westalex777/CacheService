package com.cache.server.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Implementation of a memory-based cache manager with features such as expiration handling,
 * garbage collection (GC) when memory limits are reached, and multithreaded operations.
 * <p>
 * The cache stores data in a hierarchical structure where the first key maps to a nested map
 * of secondary keys and their associated values. Expired entries are automatically removed.
 * </p>
 *
 * @param <T> the type of values stored in the cache.
 */
public class MemoryCache<T> implements CacheProvider<T> {
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private int capacity = (int) (Runtime.getRuntime().maxMemory() * 0.75f);
    private long lifeTime = 604_800L;
    private final AtomicBoolean gcRunning = new AtomicBoolean(false);
    private final AtomicBoolean permission = new AtomicBoolean(true);
    private int thresholdGC = 200;
    private int thresholdPermissionActive = 5;
    private final Cache<T> cache = new Cache<>();

    /**
     * Default constructor. Uses 75% of the JVM's max memory as the cache capacity
     * and sets a default lifetime of 604,800 seconds (7 days) for cached items.
     */
    public MemoryCache() {
        log.debug("Initializing MemoryCache with default settings: capacity={}, lifeTimeDefault={} seconds", capacity, lifeTime);
    }

    /**
     * Stores a value in the cache using a default lifetime.
     *
     * @param key1  the primary key.
     * @param key2  the secondary key.
     * @param value the value to store.
     * @return {@code true} if the value was successfully stored, {@code false} otherwise.
     */
    @Override
    public boolean set(String key1, String key2, T value) {
        return set(key1, key2, value, lifeTime);
    }

    /**
     * Stores a value in the cache with a specified lifetime.
     *
     * @param key1     the primary key.
     * @param key2     the secondary key.
     * @param value    the value to store.
     * @param lifeTime the lifetime of the cache entry in seconds.
     * @return {@code true} if the value was successfully stored, {@code false} otherwise.
     */
    @Override
    public boolean set(String key1, String key2, T value, Long lifeTime) {
        log.info("Setting value: key1={}, key2={}, lifeTime={} seconds", key1, key2, lifeTime);
        Value<T> v = new Value<>(value, lifeTime, key1, key2);
        return set(key1, key2, v);
    }

    /**
     * Retrieves a value from the cache.
     *
     * @param key1 the primary key.
     * @param key2 the secondary key.
     * @return the cached value, or {@code null} if the key does not exist or the value has expired.
     */
    @Override
    public T get(String key1, String key2) {
        log.info("Retrieving value: key1={}, key2={}", key1, key2);
        var value1 = cache.get(key1);
        if (value1 != null) {
            var value2 = value1.get(key2);
            if (value2 != null) {
                return value2.getValue();
            }
        }
        log.warn("Value not found: key1={}, key2={}", key1, key2);
        return null;
    }

    /**
     * Removes a secondary cache from the primary cache.
     *
     * @param key1 the primary key.
     */
    @Override
    public void remove(String key1) {
        log.debug("Removing primary cache: key1={}", key1);
        cache.map.remove(key1);
    }

    /**
     * Removes a value from the cache.
     *
     * @param key1 the primary key.
     * @param key2 the secondary key.
     */
    @Override
    public void remove(String key1, String key2) {
        log.debug("Removing value: key1={}, key2={}", key1, key2);
        var value1 = cache.get(key1);
        if (value1 != null) {
            value1.remove(key2);
        }
    }

    /**
     * Sets the cache capacity.
     *
     * @param capacity the maximum memory usage for the cache, in mBytes.
     */
    public void setCapacity(int capacity) {
        log.debug("Setting cache capacity: capacity={} bytes", capacity);
        this.capacity = (int) (capacity * Math.pow(1024, 2) * 0.75f);
    }

    /**
     * Sets the lifetime for cached entries.
     *
     * @param lifeTime the lifetime of cache entries, in seconds.
     */
    public void setLifeTime(long lifeTime) {
        log.debug("Setting cache lifetime: lifeTime={} seconds", lifeTime);
        this.lifeTime = lifeTime;
    }

    /**
     * Sets the threshold for triggering garbage collection.
     *
     * @param thresholdGC the number of iterations before triggering GC. Higher values delay GC.
     */
    public void setThresholdGC(int thresholdGC) {
        log.debug("Setting GC threshold: thresholdGC={}", thresholdGC);
        this.thresholdGC = thresholdGC;
    }

    /**
     * Sets the threshold for denying cache write permissions when memory is low.
     *
     * @param thresholdPermissionActive the threshold to disable write access.
     */
    public void setThresholdPermissionActive(int thresholdPermissionActive) {
        log.debug("Setting permission threshold: thresholdPermissionActive={}", thresholdPermissionActive);
        this.thresholdPermissionActive = thresholdPermissionActive;
    }

    /**
     * Starts a background thread to periodically find and remove expired entries from the cache.
     */
    public void selfCleanerStart(long initialDelay, long period) {
        ScheduledExecutorService scheduledExecutor = Executors.newSingleThreadScheduledExecutor();
        Runnable task = () -> {
            log.trace("Expired entry removal thread started");
            try {
                for (var entry : cache.map.entrySet()) {
                    if (entry.getValue().isEmpty()) {
                        log.trace("Removing empty primary cache: key1={}", entry.getKey());
                        remove(entry.getKey());
                    } else {
                        entry.getValue().values().removeIf(value -> {
                            boolean expired = value.isExpired();
                            if (expired) {
                                log.trace("Removing expired value: key1={}, key2={}", value.getKey1(), value.getKey2());
                            }
                            return expired;
                        });
                    }
                }
            } catch (Exception e) {
                log.error("Failed to remove expired entries", e);
            } finally {
                System.gc();
            }
        };
        scheduledExecutor.scheduleAtFixedRate(task, initialDelay, period, TimeUnit.SECONDS);
    }

    /**
     * Returns the total size of all entries in the cache.
     *
     * @return the total size of the cache entries
     */
    public int size() {
        int size = 0;
        for (var entry : cache.map.entrySet()) {
            size += entry.getValue().size();
        }
        return size;
    }

    private boolean set(String key1, String key2, Value<T> value) {
        if (!permission.get()) {
            log.warn("Permission denied. No resources to set value: key1={}, key2={}", key1, key2);
            return false;
        }
        var valueOfKey1 = cache.get(key1);
        if (valueOfKey1 == null) {
            log.debug("Creating new primary key entry: key1={}", key1);
            valueOfKey1 = new ConcurrentHashMap<>();
            valueOfKey1.put(key2, value);
            cache.put(key1, valueOfKey1);
        } else {
            log.debug("Adding value to existing primary key: key1={}, key2={}", key1, key2);
            valueOfKey1.put(key2, value);
        }
        addValue(value);
        gcRun();
        return true;
    }

    private void addValue(Value<T> value) {
        log.trace("Adding value to queue: key1={}, key2={}", value.getKey1(), value.getKey2());
        Workers.worker2.submit(() -> cache.add(value));
    }

    private void gcRun() {
        if (!gcRunning.get() && isNotFreeMemory()) {
            log.warn("Starting garbage collection process");
            gcRunning.set(true);
            Workers.worker.submit(this::gc);
        }
    }

    private void gc() {
        log.debug("Garbage collection started");
        int i = 0, j = 0;
        while (isNotFreeMemory()) {
            var value = cache.getAndRemove();
            if (value != null) {
                log.debug("Removing value during GC: key1={}, key2={}", value.getKey1(), value.getKey2());
                remove(value.getKey1(), value.getKey2());
            }
            if (j == thresholdPermissionActive) {
                log.warn("Permission disabled during GC");
                permission.set(false);
            }
            if (i == thresholdGC) {
                log.debug("Triggering JVM garbage collection");
                Workers.worker3.submit(System::gc);
                i = 0;
                j++;
            }
            i++;
        }
        permission.set(true);
        gcRunning.set(false);
        Workers.worker3.submit(System::gc);
        log.info("Garbage collection completed");
    }

    private boolean isNotFreeMemory() {
        boolean notFree = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory() > capacity;
        log.debug("Memory check: isNotFreeMemory={} (used={}, capacity={})", notFree, Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory(), capacity);
        return notFree;
    }

    /**
     * Internal class representing the cache structure and its associated operations.
     *
     * @param <T> the type of values stored in the cache.
     */
    private static class Cache<T> {
        private final ConcurrentHashMap<String, ConcurrentHashMap<String, Value<T>>> map = new ConcurrentHashMap<>();
        private final LinkedBlockingDeque<Value<T>> queue = new LinkedBlockingDeque<>();

        public void put(String key1, ConcurrentHashMap<String, Value<T>> valueOfKey1) {
            map.put(key1, valueOfKey1);
        }

        public ConcurrentHashMap<String, Value<T>> get(String key1) {
            return map.get(key1);
        }

        public void add(Value<T> v) {
            queue.addLast(v);
        }

        public Value<T> getAndRemove() {
            return queue.pollFirst();
        }
    }

    /**
     * Utility class containing thread pools for handling asynchronous tasks.
     */
    private static class Workers {
        public static final ExecutorService worker = Executors.newSingleThreadExecutor();
        public static final ExecutorService worker2 = Executors.newSingleThreadExecutor();
        public static final ExecutorService worker3 = Executors.newSingleThreadExecutor();
    }

    /**
     * Wrapper class for storing cached values along with their metadata.
     *
     * @param <T> the type of the cached value.
     */
    private static class Value<T> {
        private final T value;
        private final LocalDateTime expiration;
        private final String key1;
        private final String key2;

        protected T getValue() {
            return value;
        }

        protected String getKey1() {
            return key1;
        }

        protected String getKey2() {
            return key2;
        }

        public Value(T value, Long lifeTime, String key1, String key2) {
            this.key1 = key1;
            this.key2 = key2;
            this.value = value;
            this.expiration = LocalDateTime.now().plusSeconds(lifeTime);
        }

        public boolean isExpired() {
            return expiration.isBefore(LocalDateTime.now());
        }

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) return false;
            @SuppressWarnings("unchecked") Value<T> value1 = (Value<T>) o;
            return Objects.equals(value, value1.value)
                    && Objects.equals(expiration, value1.expiration)
                    && Objects.equals(key1, value1.key1)
                    && Objects.equals(key2, value1.key2);
        }

        @Override
        public int hashCode() {
            return Objects.hash(value, expiration, key1, key2);
        }
    }
}
