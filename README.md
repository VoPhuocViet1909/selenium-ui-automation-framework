# Bookstore UI Test Automation (Selenium)

![Selenium UI Tests](https://github.com/VoPhuocViet1909/selenium-ui-automation-framework/actions/workflows/selenium.yml/badge.svg)

Bộ kiểm thử giao diện tự động cho **NotFound Bookstore**, website bán sách (Spring Boot + Next.js), viết bằng **Java + Selenium WebDriver + TestNG**.

Đây là phiên bản Selenium của bộ E2E trong repo [bookstore-qa-automation](https://github.com/VoPhuocViet1909/bookstore-qa-automation) (Playwright + TypeScript): cùng hệ thống, cùng test case, khác công cụ, để so sánh hai cách tiếp cận.

## Tổng quan

| Hạng mục | Nội dung |
|---|---|
| Ngôn ngữ / Build | Java 11+, Maven |
| Công cụ | Selenium WebDriver 4 (Selenium Manager tự tải driver), TestNG |
| Thiết kế | Page Object Model, explicit wait, ThreadLocal WebDriver |
| Bộ test | `smoke` (luồng chính) và `regression` (toàn bộ) |
| Data-driven | TestNG `@DataProvider` cho các trường hợp đăng nhập không hợp lệ |
| Kỹ thuật thiết kế | Equivalence Partitioning, Boundary Value Analysis (tồn kho) |
| Chạy song song | `parallel="methods"`, mỗi test dùng một khách hàng riêng |
| Dữ liệu test | Seed qua API trước khi chạy (danh mục, sách, mã giảm giá, khách hàng) |
| Báo cáo | Allure Report, tự chụp màn hình khi test fail |
| CI | GitHub Actions: dựng backend (Docker Compose) + frontend, chạy headless, lưu Allure report |

## Test case

| Mã | Nội dung | Nhóm |
|---|---|---|
| TC-UI-LOGIN-01 | Khách hàng đăng nhập thành công -> /account | smoke |
| TC-UI-LOGIN-02 | Admin đăng nhập thành công -> /admin | smoke |
| TC-UI-LOGIN-03..05 | Bỏ trống tên đăng nhập / mật khẩu (data-driven) | regression |
| TC-UI-LOGIN-06..07 | Sai mật khẩu / tài khoản không tồn tại (data-driven) | known-bug (BUG-010) |
| TC-UI-LOGIN-08 | Mật khẩu không bị ghi ra console trình duyệt | known-bug (BUG-009) |
| TC-UI-LOGIN-09 | Đăng nhập xong quay lại đúng trang được yêu cầu | regression |
| TC-UI-SHOP-01 | Trang chi tiết hiển thị đúng tên và giá | smoke |
| TC-UI-SHOP-02 | Khách chưa đăng nhập thêm vào giỏ -> trang đăng nhập | regression |
| TC-UI-SHOP-03 | Thêm sách vào giỏ và thấy trong giỏ hàng | smoke |
| TC-UI-SHOP-04 | Tăng/giảm số lượng cập nhật thành tiền | regression |
| TC-UI-SHOP-05 | Không tăng số lượng vượt tồn kho (BVA) | regression |
| TC-UI-SHOP-06 | Xoá sản phẩm khỏi giỏ | regression |
| TC-UI-CHK-01 | Đặt hàng COD thành công | known-bug (BUG-007, BUG-008) |
| TC-UI-CHK-02 | Số tiền hiển thị bằng tổng tiền thực tế của đơn | known-bug (BUG-007) |
| TC-UI-CHK-03 | Áp mã giảm giá 10% | known-bug (BUG-011) |
| TC-UI-SEC-01 | Khách hàng không vào được /admin | security |
| TC-UI-ADM-01 | Admin thấy sách vừa tạo trong trang quản lý | smoke |

## Bug tìm được & nhóm `known-bug`

Bộ test đã phát hiện các bug thật của hệ thống, báo cáo chi tiết trong
[bookstore-qa-automation/docs/bugs](https://github.com/VoPhuocViet1909/bookstore-qa-automation/tree/main/docs/bugs):

| Bug | Nội dung | Test phát hiện |
|---|---|---|
| BUG-006 | Trang chủ / chi tiết sách sập khi `discountPrice = null` (đã vá tạm trong môi trường test để chạy tiếp) | TC-UI-SHOP-01 |
| BUG-007 | Đặt hàng COD luôn thất bại (rollback cả đơn) | TC-UI-CHK-01, CHK-02 |
| BUG-008 | Frontend báo "Đặt hàng thành công" dù backend trả lỗi | TC-UI-CHK-01 |
| BUG-009 | Mật khẩu bị ghi ra console trình duyệt | TC-UI-LOGIN-08 |
| BUG-010 | Sai mật khẩu hiện thông báo chung "Đăng nhập thất bại" | TC-UI-LOGIN-06, 07 |
| BUG-011 | Áp mã giảm giá báo thành công nhưng tổng tiền không giảm | TC-UI-CHK-03 |

Các test này được gắn group `known-bug` và chạy riêng bằng `testng-known-bugs.xml`:
- `smoke` / `regression` loại trừ `known-bug`, nên build xanh nghĩa là **không có lỗi mới (regression)**.
- CI vẫn chạy bộ `known-bug` (không chặn build) và kết quả nằm trong Allure report.
- Khi dev sửa bug, test sẽ pass -> chuyển test về lại `regression`.

```bash
mvn test -DsuiteXml=testng-known-bugs.xml   # chạy riêng các test đang bắt bug
```

## Cấu trúc thư mục

```
├── src/main/java/com/bookstore/
│   ├── core/      # Config, DriverFactory (ThreadLocal)
│   ├── api/       # ApiClient: seed dữ liệu, đăng nhập nhanh
│   ├── data/      # DataFactory, TestData (seed), Customers
│   └── pages/     # Page Object: Login, Product, Cart, Checkout, AdminBooks
├── src/test/java/com/bookstore/
│   ├── base/      # BaseTest: mở/đóng trình duyệt, loginViaApi
│   ├── listeners/ # Chụp màn hình khi fail -> Allure
│   └── tests/     # LoginTest, CartTest, CheckoutTest, AccessControlTest
├── src/test/resources/config.properties
├── testng-smoke.xml / testng-regression.xml / testng-known-bugs.xml
└── .github/workflows/selenium.yml
```

## Cách chạy (local)

Yêu cầu: JDK 11+ (khuyên dùng 17), Maven, Google Chrome, và hệ thống Bookstore đang chạy
(backend `http://localhost:8080`, frontend `http://localhost:3000`, xem repo bookstore-qa-automation).

```bash
mvn test                                                  # smoke
mvn test -DsuiteXml=testng-regression.xml -Dheadless=true # toàn bộ, headless
mvn test -Dbrowser=edge                                   # chạy bằng Edge
mvn allure:serve                                          # mở Allure report
```

Đổi URL hoặc tài khoản admin: sửa `src/test/resources/config.properties` hoặc truyền `-DbaseUrl=...`, `-DadminUser=...`.

## Selenium so với Playwright (rút ra khi làm 2 phiên bản)

- **Chờ phần tử:** Playwright tự chờ; Selenium phải dùng `WebDriverWait` + `ExpectedConditions` ở mọi thao tác, nếu không test sẽ chập chờn.
- **Locator:** Playwright có `getByRole`, `getByPlaceholder`; Selenium dùng CSS/XPath theo `aria-label`, `placeholder`, text.
- **Song song:** Playwright tách worker sẵn; Selenium cần `ThreadLocal<WebDriver>` để mỗi thread có trình duyệt riêng.
- **Đăng nhập nhanh:** Playwright dùng `addInitScript`; Selenium phải mở trang của app trước rồi mới ghi `localStorage` được.

## Kết quả và bug tìm được

Lần chạy regression gần nhất: **20 test, 14 pass, 6 fail**. Cả 6 test fail đều do bug của hệ thống (test fail đúng),
bug report nằm trong repo [bookstore-qa-automation/docs/bugs](https://github.com/VoPhuocViet1909/bookstore-qa-automation/tree/main/docs/bugs):

| Test fail | Bug |
|---|---|
| TC-UI-CHK-01, TC-UI-CHK-02 | BUG-007: Đặt hàng COD luôn thất bại (lỗi tạo vận đơn GHN làm rollback cả đơn) · BUG-008: Frontend vẫn báo "Đặt hàng thành công" |
| TC-UI-CHK-03 | BUG-011: Áp mã giảm giá báo thành công nhưng tổng tiền không giảm |
| TC-UI-LOGIN-08 | BUG-009: Mật khẩu bị ghi ra console trình duyệt |
| TC-UI-LOGIN-06, TC-UI-LOGIN-07 | BUG-010: Sai mật khẩu hiện thông báo lỗi chung |
