package com.bookstore.api;

import com.bookstore.core.Config;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Client API mỏng dùng java.net.http, chỉ phục vụ việc chuẩn bị dữ liệu cho UI test
 * (seed sách, tạo khách hàng, thêm vào giỏ, đọc đơn hàng để đối chiếu).
 * Đường dẫn truyền vào không có "/" ở đầu, ví dụ: api.get("cart").
 */
public class ApiClient {

    private static final HttpClient HTTP = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    public static final ObjectMapper JSON = new ObjectMapper();

    private final String baseUrl;
    private String token;
    private String refreshToken;
    private JsonNode user;

    public ApiClient() {
        this.baseUrl = Config.apiUrl();
    }

    /** Đăng nhập và trả về client đã gắn Bearer token */
    public static ApiClient login(String username, String password) {
        ApiClient api = new ApiClient();
        ApiResponse res = api.post("auth/login", Map.of("username", username, "password", password));
        if (!res.isSuccess() || res.result().path("token").isMissingNode()) {
            throw new IllegalStateException("Đăng nhập API thất bại cho " + username + ": " + res);
        }
        api.token = res.result().path("token").asText();
        api.refreshToken = res.result().path("refreshToken").asText(null);
        api.user = res.result().path("user");
        return api;
    }

    public String token() { return token; }
    public String refreshToken() { return refreshToken; }
    public JsonNode user() { return user; }

    public ApiResponse get(String path) {
        return get(path, Map.of());
    }

    public ApiResponse get(String path, Map<String, ?> params) {
        return send(request(path, params).GET());
    }

    public ApiResponse post(String path, Object body) {
        return send(request(path, Map.of())
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(toJson(body), StandardCharsets.UTF_8)));
    }

    private HttpRequest.Builder request(String path, Map<String, ?> params) {
        String query = params.isEmpty() ? "" : "?" + params.entrySet().stream()
                .map(e -> encode(e.getKey()) + "=" + encode(String.valueOf(e.getValue())))
                .collect(Collectors.joining("&"));
        HttpRequest.Builder b = HttpRequest.newBuilder(URI.create(baseUrl + "/" + path + query))
                .timeout(Duration.ofSeconds(30))
                .header("Accept", "application/json");
        if (token != null) b.header("Authorization", "Bearer " + token);
        return b;
    }

    private ApiResponse send(HttpRequest.Builder builder) {
        try {
            HttpResponse<String> res = HTTP.send(builder.build(), HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            JsonNode body;
            try {
                body = res.body() == null || res.body().isBlank()
                        ? JSON.createObjectNode()
                        : JSON.readTree(res.body());
            } catch (JsonProcessingException notJson) {
                body = JSON.createObjectNode().put("code", -1).put("message", res.body());
            }
            return new ApiResponse(res.statusCode(), body);
        } catch (IOException e) {
            throw new IllegalStateException("Không gọi được API " + baseUrl + ": " + e.getMessage(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(e);
        }
    }

    private static String toJson(Object body) {
        try {
            return JSON.writeValueAsString(body);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private static String encode(String s) {
        return URLEncoder.encode(s, StandardCharsets.UTF_8);
    }
}
