package app;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/**
 * Reads settings from environment variables first, then from a local
 * fuelapp.properties file in the working directory (never committed - see
 * fuelapp.properties.example). Secrets must never be hardcoded in source.
 */
public final class AppConfig {
    private static final String FILE_NAME = "fuelapp.properties";
    private static final Properties fileProps = loadFile();

    private AppConfig() {}

    private static Properties loadFile() {
        Properties props = new Properties();
        Path path = Path.of(FILE_NAME);
        if (Files.exists(path)) {
            try (InputStream in = Files.newInputStream(path)) {
                props.load(in);
            } catch (IOException e) {
                System.err.println("❌ Could not read " + FILE_NAME + ": " + e.getMessage());
            }
        }
        return props;
    }

    /** Returns the setting, or null when it is missing or blank. */
    public static String get(String key) {
        String value = System.getenv(key);
        if (value == null || value.isBlank()) {
            value = fileProps.getProperty(key);
        }
        return (value == null || value.isBlank()) ? null : value.trim();
    }

    public static String get(String key, String defaultValue) {
        String value = get(key);
        return value != null ? value : defaultValue;
    }
}
