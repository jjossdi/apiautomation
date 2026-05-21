package framework.auth;

import io.restassured.specification.RequestSpecification;

public interface AuthProvider {
    void apply(RequestSpecification spec);

    static AuthProvider none() {
        return spec -> { /* no-op */ };
    }

    static AuthProvider bearer(java.util.function.Supplier<String> tokenSupplier) {
        return spec -> spec.header("Authorization", "Bearer " + tokenSupplier.get());
    }

    static AuthProvider apiKey(String headerName, String value) {
        return spec -> spec.header(headerName, value);
    }
}
