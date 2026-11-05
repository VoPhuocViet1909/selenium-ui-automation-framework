package com.bookstore.data;

import com.bookstore.api.ApiClient;

/** Tạo khách hàng mới qua API: mỗi test có giỏ hàng trống của riêng mình. */
public final class Customers {

    private Customers() {
    }

    /** Đăng ký tài khoản mới, trả về thông tin đăng nhập */
    public static TestUser register(String prefix) {
        TestUser u = DataFactory.newUser(prefix);
        new ApiClient().post("auth/register", DataFactory.registerPayload(u))
                .ensureSuccess("Đăng ký " + u.username());
        return u;
    }

    /** Khách hàng mới + 1 địa chỉ giao hàng, trả về client đã đăng nhập */
    public static Session fresh(String prefix) {
        TestUser u = register(prefix);
        ApiClient api = ApiClient.login(u.username(), u.password());
        api.post("addresses", DataFactory.newAddress(u.fullName())).ensureSuccess("Tạo địa chỉ");
        return new Session(u, api);
    }

    public static class Session {
        public final TestUser user;
        public final ApiClient api;

        Session(TestUser user, ApiClient api) {
            this.user = user;
            this.api = api;
        }
    }
}
