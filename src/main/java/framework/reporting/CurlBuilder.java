package framework.reporting;

import io.restassured.filter.Filter;
import io.restassured.filter.FilterContext;
import io.restassured.http.Headers;
import io.restassured.response.Response;
import io.restassured.specification.FilterableRequestSpecification;
import io.restassured.specification.FilterableResponseSpecification;

public class CurlBuilder implements Filter {

    private final ThreadLocal<String> captured = new ThreadLocal<>();

    public String getLastCapturedCurl() {
        return captured.get();
    }

    public void clear() {
        captured.remove();
    }

    @Override
    public Response filter(FilterableRequestSpecification req,
                           FilterableResponseSpecification res,
                           FilterContext ctx) {
        captured.set(build(req.getMethod(), req.getURI(), req.getHeaders(), req.getBody()));
        return ctx.next(req, res);
    }

    private static String build(String method, String url, Headers headers, Object body) {
        StringBuilder curl = new StringBuilder();
        curl.append("curl --location --request ").append(method).append(" '").append(url).append("'");
        if (headers != null) {
            headers.asList().forEach(h ->
                    curl.append(" \\\n  --header '").append(h.getName()).append(": ").append(h.getValue()).append("'"));
        }
        if (body != null) {
            String bodyStr = body.toString();
            if (!bodyStr.isBlank() && !"null".equals(bodyStr)) {
                curl.append(" \\\n  --data-raw '").append(bodyStr.replace("'", "'\\''")).append("'");
            }
        }
        return curl.toString();
    }
}
