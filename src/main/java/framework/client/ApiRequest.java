package framework.client;

import framework.reporting.CurlBuilder;
import framework.reporting.ReportManager;
import io.restassured.config.HttpClientConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;

public class ApiRequest {

    enum Method { GET, POST, PUT, PATCH, DELETE }

    private final ApiClient client;
    private final Method method;
    private final String path;
    private final Map<String, String> headers = new LinkedHashMap<>();
    private final Map<String, Object> pathParams = new HashMap<>();
    private final Map<String, Object> queryParams = new HashMap<>();
    private Object body;
    private ContentType contentType = ContentType.JSON;

    ApiRequest(ApiClient client, Method method, String path) {
        this.client = client;
        this.method = method;
        this.path = path;
    }

    public ApiRequest header(String name, String value) { headers.put(name, value); return this; }
    public ApiRequest headers(Map<String, String> h) { if (h != null) headers.putAll(h); return this; }
    public ApiRequest pathParam(String name, Object value) { pathParams.put(name, value); return this; }
    public ApiRequest queryParam(String name, Object value) { queryParams.put(name, value); return this; }
    public ApiRequest body(Object body) { this.body = body; return this; }
    public ApiRequest contentType(ContentType ct) { this.contentType = ct; return this; }

    public ApiResponse execute() {
        CurlBuilder curl = new CurlBuilder();
        RequestSpecification spec = given()
                .config(RestAssuredConfig.config().httpClient(
                        HttpClientConfig.httpClientConfig()
                                .setParam("http.connection.timeout", client.config().connectTimeoutMs())
                                .setParam("http.socket.timeout", client.config().readTimeoutMs())))
                .baseUri(client.config().baseUrl())
                .basePath(path)
                .contentType(contentType)
                .filter(curl);

        client.applyDefaults(spec);
        spec.headers(headers);
        if (!pathParams.isEmpty()) spec.pathParams(pathParams);
        if (!queryParams.isEmpty()) spec.queryParams(queryParams);
        if (body != null) spec.body(body);

        logRequest(spec);

        long start = System.currentTimeMillis();
        Response raw = switch (method) {
            case GET -> spec.get();
            case POST -> spec.post();
            case PUT -> spec.put();
            case PATCH -> spec.patch();
            case DELETE -> spec.delete();
        };
        long elapsed = System.currentTimeMillis() - start;

        logResponse(raw, elapsed, curl.getLastCapturedCurl());
        return new ApiResponse(raw, elapsed);
    }

    private void logRequest(RequestSpecification spec) {
        ReportManager.info(method + " " + client.config().baseUrl() + path);
        if (client.config().logRequestBody() && body != null) {
            ReportManager.json(body.toString());
        }
    }

    private void logResponse(Response raw, long elapsed, String curl) {
        ReportManager.info("Status: " + raw.statusCode() + "  |  Time: " + elapsed + "ms");
        ReportManager.headers(raw.getHeaders().asList());
        if (client.config().logResponseBody()) {
            ReportManager.json(raw.getBody().prettyPrint());
        }
        ReportManager.curl(curl);
    }
}
