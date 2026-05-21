package framework.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

public final class ConfigManager {

    private static final ConcurrentHashMap<String, ApiConfig> CACHE = new ConcurrentHashMap<>();
    private static final String DEFAULTS = "config/default.properties";

    private ConfigManager() {}

    public static ApiConfig load(String profile) {
        return CACHE.computeIfAbsent(profile, ConfigManager::buildConfig);
    }

    public static void clearCache() {
        CACHE.clear();
    }

    private static ApiConfig buildConfig(String profile) {
        Properties merged = new Properties();
        loadInto(merged, DEFAULTS, true);
        loadInto(merged, "config/" + profile + ".properties", true);
        overrideFromSystemProps(merged);

        return new ApiConfig(
                required(merged, "baseUrl"),
                merged.getProperty("appName", profile),
                merged.getProperty("authType", "none"),
                intOr(merged, "connectTimeoutMs", 10_000),
                intOr(merged, "readTimeoutMs", 30_000),
                boolOr(merged, "logRequestBody", true),
                boolOr(merged, "logResponseBody", true),
                intOr(merged, "defaultRetryCount", 0),
                longOr(merged, "responseTimeSlaMs", 0L)
        );
    }

    private static void loadInto(Properties props, String resourcePath, boolean required) {
        try (InputStream in = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream(resourcePath)) {
            if (in == null) {
                if (required) {
                    throw new IllegalStateException("Config resource not found: " + resourcePath);
                }
                return;
            }
            props.load(in);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load " + resourcePath, e);
        }
    }

    private static void overrideFromSystemProps(Properties merged) {
        for (String name : merged.stringPropertyNames()) {
            String sys = System.getProperty(name);
            if (sys != null && !sys.isBlank()) {
                merged.setProperty(name, sys);
            }
        }
    }

    private static String required(Properties props, String key) {
        String value = props.getProperty(key);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Missing required config: " + key);
        }
        return value.trim();
    }

    private static int intOr(Properties props, String key, int fallback) {
        String value = props.getProperty(key);
        return value == null ? fallback : Integer.parseInt(value.trim());
    }

    private static long longOr(Properties props, String key, long fallback) {
        String value = props.getProperty(key);
        return value == null ? fallback : Long.parseLong(value.trim());
    }

    private static boolean boolOr(Properties props, String key, boolean fallback) {
        String value = props.getProperty(key);
        return value == null ? fallback : Boolean.parseBoolean(value.trim());
    }
}
