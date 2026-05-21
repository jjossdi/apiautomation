package framework.config;

import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertThrows;
import static org.testng.Assert.assertTrue;

public class ConfigManagerTest {

    @Test
    public void load_mergesDefaultsAndProfile() {
        ApiConfig config = ConfigManager.load("sample");

        assertEquals(config.baseUrl(), "https://example.test/api");
        assertEquals(config.appName(), "Sample");
        assertEquals(config.responseTimeSlaMs(), 2000L);
        assertEquals(config.connectTimeoutMs(), 5000);
        assertEquals(config.readTimeoutMs(), 15000);
        assertTrue(config.logRequestBody(),  "logRequestBody falls back to default true");
        assertTrue(config.logResponseBody(), "logResponseBody falls back to default true");
    }

    @Test
    public void load_failsLoudlyForMissingProfile() {
        assertThrows(IllegalStateException.class,
                () -> ConfigManager.load("does-not-exist-xyz"));
    }

    @Test
    public void load_systemPropertyOverridesFileValue() {
        System.setProperty("baseUrl", "https://overridden.test");
        try {
            ConfigManager.clearCache();
            ApiConfig config = ConfigManager.load("sample");
            assertEquals(config.baseUrl(), "https://overridden.test");
        } finally {
            System.clearProperty("baseUrl");
            ConfigManager.clearCache();
        }
    }
}
