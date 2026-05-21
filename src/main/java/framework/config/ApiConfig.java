package framework.config;

public record ApiConfig(
        String baseUrl,
        String appName,
        String authType,
        int connectTimeoutMs,
        int readTimeoutMs,
        boolean logRequestBody,
        boolean logResponseBody,
        int defaultRetryCount,
        long responseTimeSlaMs
) {
    public boolean hasResponseTimeSla() {
        return responseTimeSlaMs > 0;
    }
}
