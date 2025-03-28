package com.expensetracker.config;

import io.micrometer.cloudwatch2.CloudWatchConfig;
import io.micrometer.cloudwatch2.CloudWatchMeterRegistry;
import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.time.Duration;

@Configuration
@EnableScheduling
public class MonitoringConfig {

    @Value("${spring.application.name}")
    private String applicationName;

    @Value("${aws.region}")
    private String awsRegion;

    @Bean
    public CloudWatchConfig cloudWatchConfig() {
        return new CloudWatchConfig() {
            @Override
            public String get(String key) {
                return switch (key) {
                    case "cloudwatch.namespace" -> applicationName;
                    case "cloudwatch.step" -> "PT1M";
                    case "cloudwatch.batchSize" -> "20";
                    default -> null;
                };
            }
        };
    }

    @Bean
    public MeterRegistry cloudWatchMeterRegistry(CloudWatchConfig config) {
        return new CloudWatchMeterRegistry(config, Clock.SYSTEM);
    }

    @Bean
    public CloudWatchMetricsPublisher cloudWatchMetricsPublisher(MeterRegistry meterRegistry) {
        return new CloudWatchMetricsPublisher(meterRegistry);
    }
} 