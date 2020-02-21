package com.tesis.config;

import kong.unirest.Unirest;
import kong.unirest.UnirestInstance;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RestClientConfigs {

    public static final String FAST = "fast";
    public static final String MID = "mid";
    public static final String SLOW = "slow";

    @Bean
    @Qualifier(FAST)
    public UnirestInstance fastInstance() {

        UnirestInstance fastInstance = Unirest.primaryInstance();
        fastInstance.config()
                .socketTimeout(500)
                .connectTimeout(1000)
                .concurrency(100, 20)
                .automaticRetries(true)
                .addShutdownHook(true);

        return fastInstance;
    }

    @Bean
    @Qualifier(MID)
    public UnirestInstance midInstance() {

        UnirestInstance midInstance = Unirest.primaryInstance();
        midInstance.config()
                .socketTimeout(1000)
                .connectTimeout(1500)
                .concurrency(100, 20)
                .automaticRetries(true)
                .addShutdownHook(true);

        return midInstance;
    }

    @Bean
    @Qualifier(SLOW)
    public UnirestInstance slowInstance() {

        UnirestInstance slowInstance = Unirest.primaryInstance();
        slowInstance.config()
                .socketTimeout(2000)
                .connectTimeout(2500)
                .concurrency(100, 20)
                .automaticRetries(true)
                .addShutdownHook(true);

        return slowInstance;
    }
}
