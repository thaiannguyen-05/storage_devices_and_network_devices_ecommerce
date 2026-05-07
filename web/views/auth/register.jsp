<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@include file="../includes/layout.jsp" %>
<%
    String pageTitle = "Đăng ký | LinhNamStore";
%>

<div class="auth-container">
    <div class="signup-shell">
        <div class="signup-brand">
            <div class="signup-brand-logo">
                <div class="signup-brand-box">L</div>
            </div>
            <h1>LinhNamStore</h1>
            <p>Hệ sinh thái lưu trữ hiệu năng cao.<br>Tạo tài khoản để mua sắm và quản lý đơn hàng.</p>
            <ul class="signup-perks">
                <li><span class="perk-icon">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/></svg>
                </span>Bảo mật thông tin tuyệt đối</li>
                <li><span class="perk-icon">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg>
                </span>Xử lý đơn hàng trong 24h</li>
                <li><span class="perk-icon">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"/></svg>
                </span>Hỗ trợ trọn đời khách hàng</li>
            </ul>
        </div>

        <div class="signup-form-wrap">
            <h2>Tạo tài khoản</h2>

            <form action="${pageContext.request.contextPath}/auth?action=signup" method="post" id="registerForm" onsubmit="return validateRegisterForm()">
                <div class="signup-row">
                    <div class="ch-form-field">
                        <label>Tên tài khoản</label>
                        <input type="text" name="accountName" id="accountName" placeholder="nguyenvana" value="<%= request.getAttribute("accountName") == null ? "" : request.getAttribute("accountName") %>">
                        <span class="form-error" id="accountNameError">Vui lòng nhập tên tài khoản</span>
                    </div>

                    <div class="ch-form-field">
                        <label>Tên đăng nhập</label>
                        <input type="text" name="username" id="username" placeholder="nguyenvana_01" value="<%= request.getAttribute("username") == null ? "" : request.getAttribute("username") %>">
                        <span class="form-error" id="usernameError">Vui lòng nhập tên đăng nhập</span>
                    </div>
                </div>

                <div class="ch-form-field">
                    <label>Họ và tên</label>
                    <input type="text" name="fullname" id="fullname" placeholder="Nguyễn Văn A" value="<%= request.getAttribute("fullname") == null ? "" : request.getAttribute("fullname") %>">
                    <span class="form-error" id="fullnameError">Vui lòng nhập họ và tên</span>
                </div>

                <div class="signup-row">
                    <div class="ch-form-field">
                        <label>Email</label>
                        <input type="email" name="email" id="email" placeholder="yourname@gmail.com" value="<%= request.getAttribute("email") == null ? "" : request.getAttribute("email") %>">
                        <span class="form-error" id="emailError">Email phải có dạng yourname@gmail.com</span>
                    </div>

                    <div class="ch-form-field">
                        <label>Số điện thoại</label>
                        <input type="tel" name="phone" id="phone" placeholder="0901234567" value="<%= request.getAttribute("phone") == null ? "" : request.getAttribute("phone") %>">
                        <span class="form-error" id="phoneError">Số điện thoại phải có 10 chữ số</span>
                    </div>
                </div>

                <div class="ch-form-field">
                    <label>Địa chỉ</label>
                    <input type="text" name="address" id="address" placeholder="Số nhà, đường, phường/xã, quận/huyện, tỉnh/thành phố" value="<%= request.getAttribute("address") == null ? "" : request.getAttribute("address") %>">
                    <span class="form-error" id="addressError">Vui lòng nhập địa chỉ</span>
                </div>

                <div class="signup-row">
                    <div class="ch-form-field">
                        <label>Ngày sinh</label>
                        <input type="date" name="dateOfBirth" id="dateOfBirth" value="<%= request.getAttribute("dateOfBirth") == null ? "" : request.getAttribute("dateOfBirth") %>">
                        <span class="form-error" id="dateOfBirthError">Bạn phải từ 18 tuổi trở lên</span>
                    </div>

                    <div class="ch-form-field"></div>
                </div>

                <div class="signup-row">
                    <div class="ch-form-field">
                        <label>Mật khẩu</label>
                        <input type="password" name="password" id="password" placeholder="••••••••">
                        <span class="form-error" id="passwordError">Tối thiểu 8 ký tự, gồm chữ hoa, chữ thường, số và ký tự đặc biệt</span>
                    </div>

                    <div class="ch-form-field">
                        <label>Xác nhận mật khẩu</label>
                        <input type="password" name="confirm_password" id="confirmPassword" placeholder="••••••••">
                        <span class="form-error" id="confirmPasswordError">Mật khẩu không khớp</span>
                    </div>
                </div>

                <div class="ch-form-field signup-terms">
                    <label style="display:flex;gap:8px;align-items:flex-start;font-size:13px;text-transform:none;letter-spacing:0;">
                        <input type="checkbox" name="acceptTerms" value="1" required style="width:auto;min-width:0;height:auto;box-shadow:none;accent-color:#a855f7;margin-top:2px;" <%= request.getAttribute("acceptTerms") != null ? "checked" : "" %>>
                        <span>Tôi đồng ý với điều khoản và quy định của LinhNamStore.</span>
                    </label>
                </div>

                <button type="submit" class="home-cta btn-full">ĐĂNG KÝ</button>
                <% if (request.getAttribute("error") != null) { %>
                    <p class="signup-msg error"><%= request.getAttribute("error") %></p>
                <% } %>
                <% if (request.getAttribute("success") != null) { %>
                    <p class="signup-msg success"><%= request.getAttribute("success") %></p>
                <% } %>

                <div class="signup-footer">
                    Đã có tài khoản? <a href="${pageContext.request.contextPath}/auth?action=signin">Đăng nhập</a>
                </div>
            </form>
        </div>
    </div>
</div>

<script>
function validateRegisterForm() {
    let valid = true;
    const fields = [
        { id: 'accountName', errorId: 'accountNameError', check: v => v.trim().length > 0 },
        { id: 'username', errorId: 'usernameError', check: v => v.trim().length > 0 },
        { id: 'fullname', errorId: 'fullnameError', check: v => v.trim().length > 0 },
        { id: 'email', errorId: 'emailError', check: v => /^[A-Za-z0-9._%+\-]+@gmail\.com$/.test(v) },
        { id: 'phone', errorId: 'phoneError', check: v => /^[0-9]{10}$/.test(v) },
        { id: 'address', errorId: 'addressError', check: v => v.trim().length > 0 },
        { id: 'password', errorId: 'passwordError', check: v => /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[^A-Za-z0-9]).{8,}$/.test(v) },
    ];

    fields.forEach(f => {
        const input = document.getElementById(f.id);
        const error = document.getElementById(f.errorId);
        if (!f.check(input.value)) {
            input.classList.add('input-error');
            error.classList.add('visible');
            valid = false;
        } else {
            input.classList.remove('input-error');
            error.classList.remove('visible');
        }
    });

    const pw = document.getElementById('password').value;
    const cpw = document.getElementById('confirmPassword');
    const cpwErr = document.getElementById('confirmPasswordError');
    if (pw !== cpw.value || cpw.value === '') {
        cpw.classList.add('input-error');
        cpwErr.classList.add('visible');
        valid = false;
    } else {
        cpw.classList.remove('input-error');
        cpwErr.classList.remove('visible');
    }

    return valid;
}
</script>

<%@include file="../includes/layout-end.jsp" %>
