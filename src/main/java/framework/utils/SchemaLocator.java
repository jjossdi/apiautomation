package framework.utils;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class SchemaLocator {

    private static final Path TEST_RESOURCES =
            Paths.get(System.getProperty("user.dir"), "src", "test", "resources");

    private SchemaLocator() {}

    public static File of(String module, String schemaName) {
        Path full = TEST_RESOURCES.resolve(module).resolve("schema").resolve(schemaName + ".json");
        return full.toFile();
    }
}
