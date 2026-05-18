# Proposal: Chuan hoa tieng Viet co dau toan bo JSP

## Context

Toan bo 60 file JSP trong `web/` deu viet tieng Viet khong dau: "Trang chu", "San pham", "Dang nhap", "Don gia", v.v. Can chuyen sang co dau chuan: "Trang chủ", "Sản phẩm", "Đăng nhập", "Đơn giá".

Phat hien tu scan:
- `web/layouts/`: header.jsp, footer.jsp — navigation, filter, footer text
- `web/pages/`: 14 file — login, register, profile, cart, checkout, contact, about, home, product-detail, order-history, wishlist, forgot-password, reset-password, verify-email, 404, 500
- `web/admin/`: 10 file — dashboard, product-list/form, user-list/form, order-list/detail, brand-list/form, voucher-list/form
- `web/views/`: 30 file — ban sao cua `web/pages/` va `web/admin/` (parallel routing structure)

## Bang chuyen doi — Toan bo text khong dau → co dau

### Layout files

| File | Khong dau | Co dau |
|------|-----------|--------|
| header.jsp | Trang chu | Trang chủ |
| header.jsp | Tim SSD, HDD, NAS, router... | Tìm SSD, HDD, NAS, router... |
| header.jsp | Tim | Tìm |
| header.jsp | Mo bo loc | Mở bộ lọc |
| header.jsp | Chuyen dark mode | Chuyển dark mode |
| header.jsp | Gio hang | Giỏ hàng |
| header.jsp | Tai khoan | Tài khoản |
| header.jsp | Dang xuat | Đăng xuất |
| header.jsp | Dang nhap | Đăng nhập |
| header.jsp | Dieu huong chinh | Điều hướng chính |
| header.jsp | San pham | Sản phẩm |
| header.jsp | Lien he | Liên hệ |
| header.jsp | Gioi thieu | Giới thiệu |
| header.jsp | Don hang | Đơn hàng |
| header.jsp | Quan tri | Quản trị |
| header.jsp | Bo loc san pham | Bộ lọc sản phẩm |
| header.jsp | Danh muc | Danh mục |
| header.jsp | Thuong hieu | Thương hiệu |
| header.jsp | Khoang gia | Khoảng giá |
| header.jsp | Duoi 1 trieu | Dưới 1 triệu |
| header.jsp | 1-3 trieu | 1-3 triệu |
| header.jsp | 3-5 trieu | 3-5 triệu |
| header.jsp | 5-10 trieu | 5-10 triệu |
| header.jsp | Tren 10 trieu | Trên 10 triệu |
| header.jsp | Trang thai | Trạng thái |
| header.jsp | Dang ban | Đang bán |
| header.jsp | Het hang | Hết hàng |
| header.jsp | Sap xep | Sắp xếp |
| header.jsp | Moi nhat | Mới nhất |
| header.jsp | Gia tang | Giá tăng |
| header.jsp | Gia giam | Giá giảm |
| header.jsp | Ban chay | Bán chạy |
| header.jsp | Ap dung | Áp dụng |
| footer.jsp | Thiet bi luu tru, NAS va network cho hoc tap, lam viec va van hanh cua hang nho. | Thiết bị lưu trữ, NAS và network cho học tập, làm việc và vận hành cửa hàng nhỏ. |
| footer.jsp | Link nhanh | Link nhanh |
| footer.jsp | Chinh sach | Chính sách |
| footer.jsp | Nhom thuc hien | Nhóm thực hiện |
| footer.jsp | Thanh vien | Thành viên |

### Public pages

| File | Khong dau | Co dau |
|------|-----------|--------|
| login.jsp | Dang nhap | Đăng nhập |
| login.jsp | Doi mat khau thanh cong. Vui long dang nhap lai. | Đổi mật khẩu thành công. Vui lòng đăng nhập lại. |
| login.jsp | Mat khau | Mật khẩu |
| login.jsp | Ghi nho dang nhap | Ghi nhớ đăng nhập |
| login.jsp | Quen mat khau? | Quên mật khẩu? |
| login.jsp | Dang ky | Đăng ký |
| register.jsp | Dang ky tai khoan | Đăng ký tài khoản |
| register.jsp | Ten tai khoan | Tên tài khoản |
| register.jsp | Ten dang nhap | Tên đăng nhập |
| register.jsp | Ngay sinh | Ngày sinh |
| register.jsp | So dien thoai | Số điện thoại |
| register.jsp | Dia chi | Địa chỉ |
| register.jsp | Nhap lai mat khau | Nhập lại mật khẩu |
| register.jsp | Da co tai khoan? | Đã có tài khoản? |
| profile.jsp | Ho so ca nhan | Hồ sơ cá nhân |
| profile.jsp | Ten hien thi | Tên hiển thị |
| profile.jsp | Cap nhat | Cập nhật |
| profile.jsp | Doi mat khau | Đổi mật khẩu |
| profile.jsp | Mat khau hien tai | Mật khẩu hiện tại |
| profile.jsp | Mat khau moi | Mật khẩu mới |
| profile.jsp | Nhap lai | Nhập lại |
| profile.jsp | Dang xuat thiet bi khac | Đăng xuất thiết bị khác |
| profile.jsp | Thong tin tai khoan | Thông tin tài khoản |
| cart.jsp | San pham | Sản phẩm |
| cart.jsp | Don gia | Đơn giá |
| cart.jsp | So luong | Số lượng |
| cart.jsp | Thanh tien | Thành tiền |
| cart.jsp | Xoa | Xóa |
| cart.jsp | Tong tien | Tổng tiền |
| cart.jsp | Tiep tuc mua sam | Tiếp tục mua sắm |
| cart.jsp | Thanh toan | Thanh toán |
| checkout.jsp | Ho ten | Họ tên |
| checkout.jsp | Voucher | Voucher (giu nguyen) |
| checkout.jsp | Khong dung voucher | Không dùng voucher |
| checkout.jsp | Giam gia | Giảm giá |
| checkout.jsp | Dat hang | Đặt hàng |
| checkout.jsp | Review don hang | Review đơn hàng |
| checkout.jsp | Ghi chu | Ghi chú |
| contact.jsp | Lien he LinhNamStore | Liên hệ LinhNamStore |
| contact.jsp | Ho ten | Họ tên |
| contact.jsp | Noi dung | Nội dung |
| contact.jsp | Gui lien he | Gửi liên hệ |
| contact.jsp | Thong tin ho tro | Thông tin hỗ trợ |
| contact.jsp | Dia chi: Quan 1, TP. Ho Chi Minh | Địa chỉ: Quận 1, TP. Hồ Chí Minh |
| about.jsp | Gioi thieu LinhNamStore | Giới thiệu LinhNamStore |
| about.jsp | Du an e-commerce thiet bi luu tru va network | Dự án e-commerce thiết bị lưu trữ và network |
| about.jsp | LinhNamStore la ung dung Jakarta EE JSP/Servlet dung MySQL, tap trung vao catalog san pham, gio hang, thanh toan va quan tri. | LinhNamStore là ứng dụng Jakarta EE JSP/Servlet dùng MySQL, tập trung vào catalog sản phẩm, giỏ hàng, thanh toán và quản trị. |
| about.jsp | Ho ten | Họ tên |
| about.jsp | Ngay sinh | Ngày sinh |
| about.jsp | Vai tro | Vai trò |
| home.jsp | SSD, HDD, NAS, router va phu kien mang duoc sap xep ro rang de de tim, de so sanh va de mua. | SSD, HDD, NAS, router và phụ kiện mạng được sắp xếp rõ ràng để dễ tìm, dễ so sánh và dễ mua. |
| home.jsp | Xem san pham | Xem sản phẩm |
| home.jsp | Can tu van | Cần tư vấn |
| home.jsp | Hang moi ve | Hàng mới về |
| home.jsp | Cap nhat theo ngay tao san pham. | Cập nhật theo ngày tạo sản phẩm. |
| home.jsp | Moi | Mới |
| home.jsp | Ma: | Mã: |
| home.jsp | San pham co so luong don hang cao. | Sản phẩm có số lượng đơn hàng cao. |
| home.jsp | Tat ca san pham | Tất cả sản phẩm |
| home.jsp | Controller co the truyen products de render du lieu that tu database. | Controller có thể truyền products để render dữ liệu thật từ database. |
| product-detail.jsp | Chi tiet san pham | Chi tiết sản phẩm |
| product-detail.jsp | Anh san pham | Ảnh sản phẩm |
| product-detail.jsp | Ma SP | Mã SP |
| product-detail.jsp | Thuong hieu | Thương hiệu |
| product-detail.jsp | Chon variant | Chọn variant |
| product-detail.jsp | Con ... san pham | Còn ... sản phẩm |
| product-detail.jsp | Them vao gio | Thêm vào giỏ |
| product-detail.jsp | Mua ngay | Mua ngay |
| product-detail.jsp | Luu san pham | Lưu sản phẩm |
| product-detail.jsp | Mo ta chi tiet | Mô tả chi tiết |
| product-detail.jsp | Thong so ky thuat | Thông số kỹ thuật |
| product-detail.jsp | Danh gia | Đánh giá |
| product-detail.jsp | San pham duoc toi uu cho toc do doc ghi cao, do ben tot va bao hanh chinh hang. | Sản phẩm được tối ưu cho tốc độ đọc ghi cao, độ bền tốt và bảo hành chính hãng. |
| product-detail.jsp | Giao tiep | Giao tiếp |
| product-detail.jsp | Dung luong | Dung lượng |
| product-detail.jsp | Bao hanh | Bảo hành |
| product-detail.jsp | Ten | Tên |
| product-detail.jsp | Nhan xet | Nhận xét |
| product-detail.jsp | Gui danh gia | Gửi đánh giá |
| order-history.jsp | Lich su don hang | Lịch sử đơn hàng |
| order-history.jsp | Tat ca trang thai | Tất cả trạng thái |
| order-history.jsp | Ma don | Mã đơn |
| order-history.jsp | Ngay dat | Ngày đặt |
| order-history.jsp | Hanh dong | Hành động |
| order-history.jsp | Xem chi tiet | Xem chi tiết |
| wishlist.jsp | San pham yeu thich | Sản phẩm yêu thích |
| wishlist.jsp | Xoa tat ca | Xóa tất cả |
| wishlist.jsp | Them vao gio | Thêm vào giỏ |
| forgot-password.jsp | Quen mat khau | Quên mật khẩu |
| forgot-password.jsp | Gui ma xac thuc | Gửi mã xác thực |
| reset-password.jsp | Dat lai mat khau | Đặt lại mật khẩu |
| reset-password.jsp | Ma xac thuc da duoc gui ve email. | Mã xác thực đã được gửi về email. |
| reset-password.jsp | Ma xac thuc | Mã xác thực |
| reset-password.jsp | Nhap lai mat khau moi | Nhập lại mật khẩu mới |
| verify-email.jsp | Xac thuc email | Xác thực email |
| verify-email.jsp | Xac thuc | Xác thực |
| 404.jsp | Trang khong tim thay | Trang không tìm thấy |
| 404.jsp | URL khong ton tai hoac da bi thay doi. | URL không tồn tại hoặc đã bị thay đổi. |
| 404.jsp | Quay lai trang chu | Quay lại trang chủ |
| 500.jsp | Da xay ra loi he thong | Đã xảy ra lỗi hệ thống |
| 500.jsp | Vui long thu lai sau. Trang nay khong hien thi stack trace. | Vui lòng thử lại sau. Trang này không hiển thị stack trace. |
| 500.jsp | Thu lai | Thử lại |

### Admin pages

| File | Khong dau | Co dau |
|------|-----------|--------|
| dashboard.jsp | Tong user | Tổng user |
| dashboard.jsp | Tong don hang | Tổng đơn hàng |
| dashboard.jsp | Doanh thu | Doanh thu |
| dashboard.jsp | San pham | Sản phẩm |
| dashboard.jsp | Doanh thu 7 ngay | Doanh thu 7 ngày |
| dashboard.jsp | Ma don | Mã đơn |
| dashboard.jsp | Tong tien | Tổng tiền |
| dashboard.jsp | Trang thai | Trạng thái |
| product-list.jsp | Quan ly san pham | Quản lý sản phẩm |
| product-list.jsp | Them san pham | Thêm sản phẩm |
| product-list.jsp | Sua | Sửa |
| product-list.jsp | Xoa | Xóa |
| product-form.jsp | Form san pham | Form sản phẩm |
| product-form.jsp | Them / sua san pham | Thêm / sửa sản phẩm |
| product-form.jsp | Ten san pham | Tên sản phẩm |
| product-form.jsp | Gia | Giá |
| product-form.jsp | Luu | Lưu |
| user-list.jsp | Quan ly user | Quản lý user |
| user-list.jsp | Them user | Thêm user |
| user-list.jsp | Ten | Tên |
| user-list.jsp | Ngay tao | Ngày tạo |
| user-list.jsp | Hanh dong | Hành động |
| user-form.jsp | Form user | Form user |
| user-form.jsp | Them / sua user | Thêm / sửa user |
| order-list.jsp | Quan ly don hang | Quản lý đơn hàng |
| order-list.jsp | Don hang | Đơn hàng |
| order-list.jsp | Tat ca | Tất cả |
| order-list.jsp | Ngay | Ngày |
| order-list.jsp | Chi tiet | Chi tiết |
| order-detail.jsp | Chi tiet don hang | Chi tiết đơn hàng |
| order-detail.jsp | Thong tin don | Thông tin đơn |
| order-detail.jsp | Cap nhat status | Cập nhật status |
| brand-list.jsp | Quan ly brand | Quản lý brand |
| brand-list.jsp | Thuong hieu | Thương hiệu |
| brand-list.jsp | Them brand | Thêm brand |
| brand-form.jsp | Form brand | Form brand |
| brand-form.jsp | Them / sua brand | Thêm / sửa brand |
| voucher-list.jsp | Quan ly voucher | Quản lý voucher |
| voucher-list.jsp | Them voucher | Thêm voucher |
| voucher-list.jsp | Han | Hạn |
| voucher-list.jsp | SL | SL (giữ nguyên) |
| voucher-list.jsp | Ngay tao | Ngày tạo |
| voucher-form.jsp | Form voucher | Form voucher |
| voucher-form.jsp | Them / sua voucher | Thêm / sửa voucher |

## Scope

**Total: 60 JSP files**, nhung thuc te chi ~30 file doc lap (30 file trong `web/views/` la ban sao cua `web/pages/` va `web/admin/`).

## Implementation Strategy

### Option 1: Manual Edit — tung file mot
Doc tung file, dung Edit tool de replace. Dam bao chinh xac 100%. Cham.

### Option 2: Batch script — dung regex replace
Viet script tim-kiet-replace hang loat. Nhanh, co the bo sot.

### Option 3: Group by content cluster
Gom cac file co content giong nhau (pages vs views mirrors), edit 1 lan, copy sang file kia.

**Khuyen nghi:** Option 3 — edit 30 file doc lap, copy content sang 30 file mirror.

## Thu tu trien khai

1. **Layout** (2 files): header.jsp, footer.jsp
2. **Public pages** (14 files): login, register, profile, cart, checkout, contact, about, home, product-detail, order-history, wishlist, forgot-password, reset-password, verify-email, 404, 500
3. **Admin pages** (10 files): dashboard, product-list/form, user-list/form, order-list/detail, brand-list/form, voucher-list/form
4. **Views mirrors** (30 files): copy tu pages/admin sang views

## Verification

1. Kiem tra moi JSP — khong con text khong dau (trừ "Voucher", "user", "brand" — tien Anh giu nguyen)
2. Deploy GlassFish — trang hien thi dung tieng Viet co dau, khong loi encoding
3. Kiem tra encoding: file luu UTF-8, `<%@page pageEncoding="UTF-8"%>` da co
4. Test flow: home → product → cart → checkout → login → register → admin — tat ca text co dau
