# OneShop - Website bán mỹ phẩm theo mô hình chuỗi cửa hàng

Đồ án môn Lập trình Web. Nhiều shop (vendor) cùng bán trên một sàn, khách mua hàng từ nhiều shop, sàn thu chiết khấu, có nhà vận chuyển và shipper giao hàng.

## Công nghệ

- Spring Boot, Spring Security, JWT (lưu trong cookie HttpOnly)
- Thymeleaf, Bootstrap 5, Sitemesh 3 (decorator layout)
- Spring Data JPA, SQL Server
- Cloudinary (lưu ảnh), JavaMail (OTP qua email)

## Chức năng chính

- **Chung:** đăng ký có OTP email, đăng nhập, đăng xuất, quên mật khẩu có OTP, mật khẩu mã hóa BCrypt
- **Guest / User:** trang chủ, sản phẩm theo danh mục, giỏ hàng, đặt hàng, lịch sử đơn, profile, nhiều địa chỉ nhận hàng
- **Vendor:** đăng ký shop, quản lý sản phẩm, đơn hàng, khuyến mãi, doanh thu
- **Admin / Manager:** quản lý người dùng, danh mục, nhà vận chuyển, chiết khấu, khuyến mãi
- **Shipper:** quản lý và thống kê đơn được phân công

## Cài đặt và chạy

1. Cài Java 21 (hoặc 17 trở lên), SQL Server, Spring Tool Suite và plugin Lombok.
2. Tạo database trống tên `OneShop` trong SQL Server.
3. Sao chép `src/main/resources/application-example.properties` thành `application.properties` cùng thư mục, rồi điền:
   - thông tin kết nối SQL Server (username, password)
   - `jwt.secret` (chuỗi ngẫu nhiên từ 32 ký tự trở lên)
   - `cloudinary.*` (tạo tài khoản miễn phí tại cloudinary.com)
   - `spring.mail.*` (Gmail kèm App Password, cần bật xác minh 2 bước)
4. Chạy `OneShopApplication` (Run As → Spring Boot App). Bảng và dữ liệu mẫu được tạo tự động.
5. Mở http://localhost:8080

> Không commit file `application.properties` (đã nằm trong `.gitignore`).

## Tài khoản mẫu

Mật khẩu chung: `Oneshop@123`

| Vai trò | Email |
|---|---|
| Admin | admin@oneshop.com |
| Manager | manager@oneshop.com |
| Vendor | vendor@oneshop.com |
| Shipper | shipper@oneshop.com |
| User | user@oneshop.com |

## Thành viên

| Thành viên | MSSV | Phụ trách |
|---|---|---|
| Quỳnh (TV1) | 24162007 | Hạ tầng, xác thực, profile và địa chỉ, Admin/Manager, Shipper, đánh giá |
| Jung Nguyễn (TV3) | 24162103 | Vendor, khuyến mãi, thanh toán VNPAY/MoMo, doanh thu |
| Tiến (TV2) | 24162128 | Guest/User: trang chủ, tìm kiếm, giỏ hàng, đặt hàng, yêu thích |

## Quy ước làm việc

- Nhánh `main`: bản ổn định; `develop`: nhánh tích hợp; mỗi chức năng một nhánh `feature/...`, tạo Pull Request vào `develop`.
- Commit theo quy ước: `feat:`, `fix:`, `ui:`, `refactor:`, `docs:`, `chore:`.### Giảm giá sản phẩm và phiếu giao hàng

- Vendor đặt mức giảm 0-90% cho từng sản phẩm tại `/vendor/products` (ô "Giảm giá (%)"). `Product.getFinalPrice()` trả giá bán sau giảm, làm tròn đến đồng.
- Trang chủ, trang danh mục, trang shop, trang chi tiết và bảng sản phẩm của vendor hiện giá sau giảm kèm giá gốc gạch ngang. Giao diện dùng chung fragment `fragments/price`.
- Phiếu giao hàng in được tại `/vendor/orders/{id}/slip` (chỉ với đơn đã xác nhận trở đi). Phiếu có địa chỉ giao, danh sách sản phẩm, tổng tiền và số tiền thu hộ khi thanh toán COD.## OneShop - phần Vendor của Jung Nguyễn
### Giảm giá sản phẩm và phiếu giao hàng

- Vendor đặt mức giảm 0-90% cho từng sản phẩm tại `/vendor/products` (ô "Giảm giá (%)"). `Product.getFinalPrice()` trả giá bán sau giảm, làm tròn đến đồng.
- Trang chủ, trang danh mục, trang shop, trang chi tiết và bảng sản phẩm của vendor hiện giá sau giảm kèm giá gốc gạch ngang. Giao diện dùng chung fragment `fragments/price`.
- Phiếu giao hàng in được tại `/vendor/orders/{id}/slip` (chỉ với đơn đã xác nhận trở đi). Phiếu có địa chỉ giao, danh sách sản phẩm, tổng tiền và số tiền thu hộ khi thanh toán COD.## OneShop - phần Vendor của Jung Nguyễn
