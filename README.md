# apiautomation

Thread-safe, fluent API testing framework built on **RestAssured + TestNG + ExtentReports**.

Designed to be dropped into any Java project as a Maven dependency. No global state, no static singletons that fight each other under parallel execution, no platform-specific path hacks.

---

## Features

- **Fluent ApiClient** — builder pattern, immutable after build, thread-safe.
- **Externalised config** — `<profile>.properties` files, system property overrides, cached per profile.
- **AuthProvider** abstraction — `none()`, `bearer(supplier)`, `apiKey(name, value)`. Plug your own.
- **Listener-driven reporting** — TestNG `ITestListener` auto-creates ExtentTest per `@Test`. No boilerplate in test classes.
- **Cross-platform paths** — `java.nio.Path` everywhere; runs on macOS, Linux, Windows.
- **Assertion chain** — status, schema, JSON path, regex, response-time SLA, soft assertions.
- **cURL capture** — every request is rendered as a reproducible `curl` command in the report.

---

## Install

Clone & install to local Maven:

```bash
cd apiautomation
mvn clean install
```

This publishes `io.apiautomation:apiautomation:1.0.0-SNAPSHOT` to `~/.m2/repository`.

---

## Use it from another project

### 1. Add the dependency

```xml
<dependency>
    <groupId>io.apiautomation</groupId>
    <artifactId>apiautomation</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

### 2. Create a profile file at `src/main/resources/config/<profile>.properties`

```properties
baseUrl=https://your.api.example/v1
appName=Your API
authType=bearer
responseTimeSlaMs=3000
```

`default.properties` is shipped by the framework with sane defaults (timeouts, log flags). Any value you set in your profile overrides the default. Any `-D` system property overrides both.

### 3. Build a client

```java
ApiClient client = ApiClient.forProfile("petstore")
        .defaultHeader("Accept", "application/json")
        .auth(AuthProvider.bearer(() -> tokenStore.current()))
        .build();
```

### 4. Wrap endpoints in a service

```java
public class UserService {
    private final ApiClient client;
    public UserService(ApiClient client) { this.client = client; }

    public ApiResponse createUser(User user) {
        return client.post("/user").body(user).execute();
    }

    public ApiResponse getUser(String username) {
        return client.get("/user/{username}").pathParam("username", username).execute();
    }
}
```

### 5. Write tests

```java
@Listeners(ReportListener.class)
public class CreateUserTests {
    private UserService userService;
    private ApiConfig config;

    @BeforeClass
    public void setUp() {
        config = ConfigManager.load("petstore");
        userService = new UserService(ApiClient.forProfile("petstore").build());
    }

    @Test
    public void createUser_returns200_andMatchesSchema() {
        User user = UserFixtures.swaggerSample();

        userService.createUser(user)
                .assertStatus(200)
                .assertResponseTimeUnder(config.responseTimeSlaMs())
                .assertJsonPathEquals("username", user.getUsername())
                .assertSchema(SchemaLocator.of("petstore", "user"));
    }
}
```

### 6. Pick a profile at runtime

```bash
mvn test                              # uses whatever profile your code passes to ConfigManager.load(...)
mvn test -DbaseUrl=https://staging... # system prop overrides any profile value
```

---

## Package map

| Package                    | What lives there                                                |
|----------------------------|-----------------------------------------------------------------|
| `framework.config`         | `ApiConfig` (record) + `ConfigManager` (cached, profile-aware)  |
| `framework.client`         | `ApiClient`, `ApiRequest`, `ApiResponse`                        |
| `framework.auth`           | `AuthProvider` interface + factories                            |
| `framework.reporting`      | `ReportManager`, `ReportListener`, `CurlBuilder`                |
| `framework.utils`          | `SchemaLocator`                                                 |

---

## Soft assertions

```java
response.softCheck(soft -> {
    soft.assertThat((String) response.jsonPath("username")).isEqualTo("theUser");
    soft.assertThat((String) response.jsonPath("email")).contains("@");
    soft.assertThat((Integer) response.jsonPath("userStatus")).isEqualTo(1);
});
```

All assertions run; failures are collected and reported together.

---

## Parallel execution

The framework holds no mutable static state in the request pipeline. `ApiClient` is immutable after build; `CurlBuilder` uses `ThreadLocal`; `ReportManager` uses synchronized init + `ThreadLocal<ExtentTest>`. Put `parallel="methods"` on your TestNG suite — it just works.

```xml
<suite name="My Suite" parallel="methods" thread-count="4">
    <listeners>
        <listener class-name="framework.reporting.ReportListener"/>
    </listeners>
    ...
</suite>
```

---

## Requirements

- Java 21
- Maven 3.9+

---

## License

Internal use.
