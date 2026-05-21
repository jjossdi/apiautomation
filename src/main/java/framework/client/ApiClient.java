package framework.client;

import framework.auth.AuthProvider;
import framework.config.ApiConfig;
import framework.config.ConfigManager;
import io.restassured.specification.RequestSpecification;

import java.util.LinkedHashMap;
import java.util.Map;

public final class ApiClient {

    private final ApiConfig config;
    private final AuthProvider auth;
    private final Map<String, String> defaultHeaders;

    private ApiClient(Builder b) {
        this.config = b.config;
        this.auth = b.auth == null ? AuthProvider.none() : b.auth;
        this.defaultHeaders = Map.copyOf(b.defaultHeaders);
    }

    public ApiConfig config() { return config; }

    void applyDefaults(RequestSpecification spec) {
        if (!defaultHeaders.isEmpty()) spec.headers(defaultHeaders);
        auth.apply(spec);
    }

    public ApiRequest get(String path)    { return new ApiRequest(this, ApiRequest.Method.GET, path); }
    public ApiRequest post(String path)   { return new ApiRequest(this, ApiRequest.Method.POST, path); }
    public ApiRequest put(String path)    { return new ApiRequest(this, ApiRequest.Method.PUT, path); }
    public ApiRequest patch(String path)  { return new ApiRequest(this, ApiRequest.Method.PATCH, path); }
    public ApiRequest delete(String path) { return new ApiRequest(this, ApiRequest.Method.DELETE, path); }

    public static Builder forProfile(String profile) {
        return new Builder(ConfigManager.load(profile));
    }

    public static Builder builder(ApiConfig config) {
        return new Builder(config);
    }

    public static final class Builder {
        private final ApiConfig config;
        private AuthProvider auth;
        private final Map<String, String> defaultHeaders = new LinkedHashMap<>();

        private Builder(ApiConfig config) { this.config = config; }

        public Builder auth(AuthProvider auth) { this.auth = auth; return this; }
        public Builder defaultHeader(String name, String value) { defaultHeaders.put(name, value); return this; }

        public ApiClient build() { return new ApiClient(this); }
    }
}
