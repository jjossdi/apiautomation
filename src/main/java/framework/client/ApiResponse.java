package framework.client;

import framework.reporting.ReportManager;
import io.restassured.module.jsv.JsonSchemaValidator;
import io.restassured.response.Response;
import org.assertj.core.api.SoftAssertions;
import org.testng.Assert;

import java.io.File;
import java.util.regex.Pattern;

public class ApiResponse {

    private final Response response;
    private final long elapsedMs;

    ApiResponse(Response response, long elapsedMs) {
        this.response = response;
        this.elapsedMs = elapsedMs;
    }

    public Response raw() { return response; }
    public int statusCode() { return response.statusCode(); }
    public long elapsedMs() { return elapsedMs; }
    public String body() { return response.getBody().asString(); }
    public <T> T as(Class<T> type) { return response.as(type); }
    public <T> T jsonPath(String path) { return response.jsonPath().get(path); }

    public ApiResponse assertStatus(int expected) {
        Assert.assertEquals(response.statusCode(), expected,
                "Unexpected status. Body: " + response.getBody().asString());
        return this;
    }

    public ApiResponse assertResponseTimeUnder(long thresholdMs) {
        Assert.assertTrue(elapsedMs <= thresholdMs,
                "Response time " + elapsedMs + "ms exceeded SLA " + thresholdMs + "ms");
        return this;
    }

    public ApiResponse assertSchema(File schemaFile) {
        response.then().assertThat().body(JsonSchemaValidator.matchesJsonSchema(schemaFile));
        ReportManager.info("Schema validated: " + schemaFile.getName());
        return this;
    }

    public ApiResponse assertJsonPathEquals(String path, Object expected) {
        Object actual = response.jsonPath().get(path);
        Assert.assertEquals(actual, expected, "JSON path '" + path + "' mismatch");
        return this;
    }

    public ApiResponse assertJsonPathNotNull(String path) {
        Object value = response.jsonPath().get(path);
        Assert.assertNotNull(value, "JSON path '" + path + "' was null");
        return this;
    }

    public ApiResponse assertJsonPathMatches(String path, String regex) {
        Object value = response.jsonPath().get(path);
        Assert.assertNotNull(value, "JSON path '" + path + "' was null");
        Assert.assertTrue(Pattern.matches(regex, value.toString()),
                "JSON path '" + path + "'=" + value + " does not match " + regex);
        return this;
    }

    public ApiResponse softCheck(java.util.function.Consumer<SoftAssertions> block) {
        SoftAssertions softly = new SoftAssertions();
        block.accept(softly);
        softly.assertAll();
        return this;
    }
}
