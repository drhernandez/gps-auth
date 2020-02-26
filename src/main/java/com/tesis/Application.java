package com.tesis;

import com.tesis.config.SpringConfig;
import com.tesis.utils.ScopeUtils;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        ScopeUtils.calculateScopeSuffix();
        new SpringApplicationBuilder(SpringConfig.class).registerShutdownHook(true)
                .run(args);
    }
}
