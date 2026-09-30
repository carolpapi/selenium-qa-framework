package io.github.carolpapi.core.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Single source of configuration for the whole framework.
 *
 * Resolution order (first match wins):
 *   1. JVM system property      -> mvn test -Dbrowser=firefox
 *   2. Environment variable     -> BROWSER=firefox  (dots become underscores, upper case)
 *   3. config/{env}.properties  -> env chosen with -Denv=qa (default) or -Denv=staging
 *
 * Why: the same code runs locally, in CI and against different environments
 * without editing any file. Secrets never live in the repo - CI injects them
 * as environment variables (e.g. SAUCE_PASSWORD from GitHub Secrets).
 */
public final class Config {

    private static final Properties PROPS = load();

    private Config() {
    }

    private static Properties load() {
        String env = System.getProperty("env", System.getenv().getOrDefault("ENV", "qa"));
        String file = "config/" + env + ".properties";
        Properties props = new Properties();
        try (InputStream in = Config.class.getClassLoader().getResourceAsStream(file)) {
            if (in == null) {
                throw new IllegalStateException("Config file not found on classpath: " + file);
            }
            props.load(in);
        } catch (IOException e) {
            throw new IllegalStateException("Could not read " + file, e);
        }
        return props;
    }

    public static String get(String key) {
        String value = System.getProperty(key);
        if (value == null || value.isBlank()) {
            value = System.getenv(key.toUpperCase().replace('.', '_'));
        }
        if (value == null || value.isBlank()) {
            value = PROPS.getProperty(key);
        }
        if (value == null) {
            throw new IllegalStateException("Missing configuration key: " + key);
        }
        return value.trim();
    }

    public static String get(String key, String defaultValue) {
        try {
            return get(key);
        } catch (IllegalStateException e) {
            return defaultValue;
        }
    }

    public static int getInt(String key) {
        return Integer.parseInt(get(key));
    }

    public static int getInt(String key, int defaultValue) {
        return Integer.parseInt(get(key, String.valueOf(defaultValue)));
    }

    public static boolean getBoolean(String key) {
        return Boolean.parseBoolean(get(key));
    }
}
