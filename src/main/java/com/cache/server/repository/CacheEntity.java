package com.cache.server.repository;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Getter
@Setter
@Builder
@Cacheable(false)
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "cache_entry")
public class CacheEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "primary_cache_key", nullable = false)
    private String primaryCacheKey;

    @Column(name = "secondary_cache_key", nullable = false)
    private String secondaryCacheKey;

    @Column(name = "created", nullable = false)
    private LocalDateTime created;

    @Column(name = "expired", nullable = false)
    private LocalDateTime expired;

    @Column(name = "cache_value", nullable = false, length = 65000)
    private String cacheValue;

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        CacheEntity that = (CacheEntity) o;
        return Objects.equals(id, that.id)
                && Objects.equals(primaryCacheKey, that.primaryCacheKey)
                && Objects.equals(secondaryCacheKey, that.secondaryCacheKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, primaryCacheKey, secondaryCacheKey);
    }
}
