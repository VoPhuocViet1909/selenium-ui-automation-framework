package com.bookstore.core;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

/**
 * Đọc cấu hình theo thứ tự ưu tiên:
 * System property (-Dkey=...) > biến môi trường (KEY) > config.properties.
 */
public final class Config {

    private static final Properties PROPS = new Properties();

    static {
        try (InputStream in = Config.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (in != null) {
                PROPS.load(new InputStreamReader(in, StandardCharsets.UTF_8));
            }
        } catch (IOException e) {
            throw new IllegalStateException("Không đọc được config.properties", e);
        }
    }

    private Config() {
    }

    public static String get(String key) {
        String sys = System.getProperty(key);
        if (sys != null && !sys.isBlank()) return sys;
        String env = System.getenv(toEnvName(key));
        if (env != null && !env.isBlank()) return env;
        String value = PROPS.getProperty(key);
        if (value == null) throw new IllegalArgumentException("Thiếu cấu hình: " + key);
        return value.trim();
    }

    /** baseUrl -> BASE_URL */
    private static String toEnvName(String key) {
        return key.replaceAll("([a-z])([A-Z])", "$1_$2").toUpperCase();
    }

    public static String baseUrl() { return trimSlash(get("baseUrl")); }
    public static String apiUrl() { return trimSlash(get("apiUrl")); }
    public static boolean headless() { return Boolean.parseBoolean(get("headless")); }
    public static int timeoutSeconds() { return Integer.parseInt(get("timeoutSeconds")); }

    private static String trimSlash(String url) {
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }
}
