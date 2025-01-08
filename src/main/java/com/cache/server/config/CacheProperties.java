package com.cache.server.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties("cache")
public class CacheProperties {

    private Long defaultLifeTime;
    private Memory memory = new Memory();
    private DB db = new DB();

    @Data
    public static class Memory {
        private Integer capacity;
        private Integer thresholdGC;
        private Integer thresholdPermissionActive;
        private SelfCleaner selfCleaner = new SelfCleaner();
    }

    @Data
    public static class SelfCleaner {
        private Long initialDelay = 3600L;
        private Long period = 3600L;
    }

    @Data
    public static class DB {
        private Scheduled scheduled = new Scheduled();
    }

    @Data
    public static class Scheduled {
        private boolean enable;
    }

}
