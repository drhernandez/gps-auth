package com.tesis.utils;

import org.apache.commons.lang3.StringUtils;

import java.util.Optional;

public class ScopeUtils {
    public static final String ENVIRONMENT = "SCOPE_SUFFIX";
    public static final String SCOPE = "SCOPE";
    public static final String DEVELOPMENT = "dev";
    public static String SCOPE_VALUE = System.getenv(SCOPE);

    public static void calculateScopeSuffix() {
        String suffix = Optional.ofNullable(SCOPE_VALUE)
                .filter(StringUtils::isNotBlank)
                .map(scope -> {
                    String[] tokens = scope.split("-");
                    return tokens[tokens.length - 1];
                })
                .orElse(DEVELOPMENT);

        System.setProperty(ENVIRONMENT, suffix);
    }
}
