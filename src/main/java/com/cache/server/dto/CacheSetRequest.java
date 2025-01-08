package com.cache.server.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import java.io.Serial;
import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CacheSetRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = -1604274519387749373L;

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
