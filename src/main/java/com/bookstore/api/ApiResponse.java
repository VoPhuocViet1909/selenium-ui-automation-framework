package com.bookstore.api;

import com.fasterxml.jackson.databind.JsonNode;

/** Response chuẩn của backend: { code, message, result } */
public class ApiResponse {

    private final int status;
    private final JsonNode body;

    public ApiResponse(int status, JsonNode body) {
        this.status = status;
        this.body = body;
    }

    public int status() { return status; }
    public JsonNode body() { return body; }
    public int code() { return body.path("code").asInt(-1); }
    public JsonNode result() { return body.path("result"); }

    public boolean isSuccess() {
        return status == 200 && code() == 1000;
    }

    /** Ném lỗi kèm nội dung response nếu gọi API thất bại (dùng khi seed dữ liệu) */
    public ApiResponse ensureSuccess(String action) {
        if (!isSuccess()) {
            throw new IllegalStateException(action + " thất bại: HTTP " + status + " " + body);
        }
        return this;
    }

    @Override
    public String toString() {
        return "HTTP " + status + " " + body;
    }
}
