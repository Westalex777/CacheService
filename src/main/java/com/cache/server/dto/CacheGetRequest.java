package com.cache.server.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data
public class CacheGetRequest {

    @NotNull
    @Length(min = 1, max = 255)
    private String primaryCacheKey;

    @NotNull
    @Length(min = 1, max = 255)
    private String secondaryCacheKey;

}
