package com.example.vertxbenchmark.config;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

public class ConfigLoader {
    private final Vertx vertx;

    public ConfigLoader(Vertx vertx) {
        this.vertx = vertx;
    }

    public Future<JsonObject> loadConfig() {
        return vertx.fileSystem().readFile("config.json")
                .map(buffer -> {
                    JsonObject config = buffer.toJsonObject();
                    // Validate configuration
                    validateConfig(config);
                    return config;
                });
    }

    private void validateConfig(JsonObject config) {
        // Kiểm tra các trường bắt buộc
        if (!config.containsKey("http")) {
            throw new IllegalArgumentException("Missing 'http' configuration");
        }
        if (!config.containsKey("database")) {
            throw new IllegalArgumentException("Missing 'database' configuration");
        }
        if (!config.containsKey("vertx")) {
            throw new IllegalArgumentException("Missing 'vertx' configuration");
        }

        // Validate database config
        JsonObject dbConfig = config.getJsonObject("database");
        if (!dbConfig.containsKey("host") ||
                !dbConfig.containsKey("port") ||
                !dbConfig.containsKey("database") ||
                !dbConfig.containsKey("user") ||
                !dbConfig.containsKey("password")) {
            throw new IllegalArgumentException("Invalid database configuration");
        }
    }
}