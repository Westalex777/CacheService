package com.cache.server.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data
public class CacheSetRequest {

    @NotNull
    @Length(min = 1, max = 255)
    private String primaryCacheKey;

    @NotNull
    @Length(min = 1, max = 255)
    private String secondaryCacheKey;

    @Min(3600) // one minute
    @Max(2_592_000) // 30 days
    private Long expired;

    @NotNull
    private Object value;

}
