package com.mylibrary.actuator;

import io.micrometer.core.instrument.config.MeterFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("prod")
public class ProdMetricsConfig {

    @Bean
    public MeterFilter productionMeterFilter() {
        return MeterFilter.deny(id -> {
            String name = id.getName();
            return !name.equals("jvm.memory.used")
                    && !name.equals("http.server.requests");
        });
    }
}
