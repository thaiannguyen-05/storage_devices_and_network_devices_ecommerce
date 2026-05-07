<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="entity.UserEntity"%>
<%@page import="java.time.format.DateTimeFormatter"%>
<%@include file="../includes/layout.jsp" %>
<% String pageTitle = "Profile | LinhNamStore"; %>

<style>
    .profile-shell { width: 100%; max-width: 1120px; }
    .profile-flash { margin: 0 auto 16px; max-width: 920px; padding: 14px 18px; border-radius: 14px; border: 1px solid var(--ch-hairline); background: var(--ch-surface-card); font-size: 14px; font-weight: 600; }
    .profile-flash--error { color: #ef4444; }
    .profile-flash--success { color: #22c55e; }
    .profile-card { width: min(920px, 100%); margin: 0 auto; display: flex; align-items: stretch; border-radius: 30px; border: 1px solid var(--ch-hairline); background: linear-gradient(135deg, #1b1b1b, #121212); box-shadow: 0 26px 60px rgba(0, 0, 0, 0.3); overflow: hidden; animation: profileExpand 0.72s cubic-bezier(0.22, 1, 0.36, 1) both; transform-origin: left center; }
    .profile-card::before { content: none !important; display: none !important; }
    html[data-theme="light"] .profile-card { background: linear-gradient(135deg, #ffffff, #eef4fb); box-shadow: 0 26px 60px rgba(15, 23, 42, 0.12); }
    .profile-avatar-pane { width: 280px; flex-shrink: 0; padding: 36px 28px; display: flex; flex-direction: column; align-items: center; justify-content: center; text-align: center; background: radial-gradient(circle at center, rgba(250, 255, 105, 0.08), transparent 68%), linear-gradient(180deg, rgba(22, 28, 40, 0.92), rgba(18, 18, 18, 0.92)); border-right: 1px solid rgba(255, 255, 255, 0.04); }
    html[data-theme="light"] .profile-avatar-pane { background: radial-gradient(circle at center, rgba(29, 78, 216, 0.1), transparent 72%), linear-gradient(180deg, rgba(255, 255, 255, 0.94), rgba(238, 244, 251, 0.88)); }
    .profile-avatar-ring { width: 156px; height: 156px; border-radius: 50%; padding: 10px; background: linear-gradient(135deg, rgba(250, 255, 105, 0.82), rgba(250, 255, 105, 0.32)); box-shadow: 0 0 0 1px rgba(250, 255, 105, 0.14), 0 20px 38px rgba(0, 0, 0, 0.24); animation: profileAvatarPop 0.55s cubic-bezier(0.22, 1, 0.36, 1) both; }
    html[data-theme="light"] .profile-avatar-ring { background: linear-gradient(135deg, #60a5fa, #dbeafe); box-shadow: 0 18px 34px rgba(37, 99, 235, 0.16); }
    .profile-avatar { width: 100%; height: 100%; border-radius: 50%; display: flex; align-items: center; justify-content: center; background: linear-gradient(145deg, #12326f, #0a0f1f); color: #faff69; font-size: 48px; font-weight: 800; letter-spacing: 0.08em; }
    html[data-theme="light"] .profile-avatar { background: linear-gradient(145deg, #1d4ed8, #0f172a); color: #ffffff; }
    .profile-avatar-meta { margin-top: 20px; }
    .profile-badge { display: inline-flex; align-items: center; padding: 7px 12px; border-radius: 999px; background: rgba(250, 255, 105, 0.14); color: var(--ch-primary); font-size: 11px; font-weight: 700; letter-spacing: 0.16em; text-transform: uppercase; }
    html[data-theme="light"] .profile-badge { background: rgba(29, 78, 216, 0.1); color: #1d4ed8; }
    .profile-avatar-meta h2 { margin: 14px 0 8px; font-size: 30px; line-height: 1.1; color: var(--ch-ink); }
    .profile-avatar-meta p { margin: 0; color: var(--ch-muted); font-size: 14px; word-break: break-word; }
    .profile-info-pane { flex: 1; padding: 38px 38px 34px 16px; display: flex; flex-direction: column; justify-content: center; animation: profileInfoFade 0.45s ease 0.18s both; }
    .profile-kicker { display: inline-block; margin-bottom: 10px; color: var(--ch-primary); font-size: 12px; font-weight: 700; letter-spacing: 0.16em; text-transform: uppercase; }
    .profile-intro { margin-bottom: 22px; }
    .profile-intro h3 { margin: 0 0 8px; font-size: 31px; line-height: 1.08; color: var(--ch-ink); }
    .profile-grid { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 14px; }
    .profile-field { padding: 16px 18px; border: 1px solid var(--ch-hairline); border-radius: 18px; background: rgba(255, 255, 255, 0.03); box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.02); }
    html[data-theme="light"] .profile-field { background: rgba(255, 255, 255, 0.84); border-color: #cfd9e7; box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.75); }
    .profile-field span { display: block; margin-bottom: 8px; color: var(--ch-muted); font-size: 12px; font-weight: 600; letter-spacing: 0.08em; text-transform: uppercase; }
    .profile-field strong { display: block; color: var(--ch-ink); font-size: 15px; line-height: 1.5; word-break: break-word; }
    .profile-actions { margin-top: 22px; display: flex; flex-wrap: wrap; gap: 12px; }
    .profile-actions a { min-height: 42px; padding: 0 16px; border-radius: 12px; border: 1px solid var(--ch-hairline); background: rgba(255, 255, 255, 0.02); color: var(--ch-ink); text-decoration: none; font-size: 14px; font-weight: 700; display: inline-flex; align-items: center; justify-content: center; }
    html[data-theme="light"] .profile-actions a { background: #f8fbff; border-color: #d3ddec; }
    .profile-actions a:hover { border-color: var(--ch-primary); color: var(--ch-primary); background: rgba(29, 78, 216, 0.06); }
    .profile-actions .profile-action-danger { color: #ef4444; }
    .profile-actions .profile-action-danger:hover { border-color: rgba(239, 68, 68, 0.35); color: #ef4444; background: rgba(239, 68, 68, 0.06); }
    @keyframes profileExpand { from { opacity: 0; transform: scaleX(0.2) translateY(12px); } to { opacity: 1; transform: scaleX(1) translateY(0); } }
    @keyframes profileAvatarPop { from { opacity: 0; transform: scale(0.55); } to { opacity: 1; transform: scale(1); } }
    @keyframes profileInfoFade { from { opacity: 0; transform: translateX(20px); } to { opacity: 1; transform: translateX(0); } }
    @media (max-width: 860px) { .profile-card { flex-direction: column; } .profile-avatar-pane { width: 100%; padding-bottom: 24px; } .profile-info-pane { padding: 10px 22px 24px; } }
    @media (max-width: 640px) { .profile-grid { grid-template-columns: 1fr; } .profile-avatar-ring { width: 132px; height: 132px; } .profile-avatar { font-size: 40px; } .profile-avatar-meta h2, .profile-intro h3 { font-size: 26px; } }
</style>

<%
    UserEntity user = (UserEntity) request.getAttribute("profileUser");
    DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    String profileName = user != null && user.getName() != null && !user.getName().trim().isEmpty() ? user.getName().trim() : "Guest User";
    String[] profileParts = profileName.split("\\s+");
    StringBuilder initialsBuilder = new StringBuilder();
    for (String part : profileParts) {
        if (part != null && !part.isEmpty()) {
            initialsBuilder.append(Character.toUpperCase(part.charAt(0)));
        }
        if (initialsBuilder.length() == 2) break;
    }
    String profileInitials = initialsBuilder.length() > 0 ? initialsBuilder.toString() : "GU";
%>

<section class="profile-shell">
    <% if (request.getAttribute("error") != null) { %>
        <p class="profile-flash profile-flash--error"><%= request.getAttribute("error") %></p>
    <% } %>
    <% if (request.getAttribute("success") != null) { %>
        <p class="profile-flash profile-flash--success"><%= request.getAttribute("success") %></p>
    <% } %>

    <% if (user == null) { %>
        <div class="auth-card">
            <p style="color:#ef4444;">Không tìm thấy thông tin tài khoản. Vui lòng đăng nhập lại.</p>
        </div>
    <% } else { %>
        <section class="profile-card">
            <div class="profile-avatar-pane">
                <div class="profile-avatar-ring">
                    <div class="profile-avatar"><%= profileInitials %></div>
                </div>
                <div class="profile-avatar-meta">
                    <span class="profile-badge"><%= user.getRole() == null ? "USER" : user.getRole() %></span>
                    <h2><%= profileName %></h2>
                    <p><%= user.getEmail() == null ? "-" : user.getEmail() %></p>
                </div>
            </div>

            <div class="profile-info-pane">
                <div class="profile-intro">
                    <span class="profile-kicker">Tài khoản LinhNamStore</span>
                    <h3>Thông tin cá nhân</h3>
                </div>

                <div class="profile-grid">
                    <div class="profile-field"><span>Họ và tên</span><strong><%= profileName %></strong></div>
                    <div class="profile-field"><span>Email</span><strong><%= user.getEmail() == null ? "-" : user.getEmail() %></strong></div>
                    <div class="profile-field"><span>Ngày sinh</span><strong><%= user.getDateOfBirth() == null ? "-" : user.getDateOfBirth().format(dateFormatter) %></strong></div>
                    <div class="profile-field"><span>Trạng thái</span><strong><%= user.getStatus() == null ? "-" : user.getStatus() %></strong></div>
                    <div class="profile-field"><span>Vai trò</span><strong><%= user.getRole() == null ? "-" : user.getRole() %></strong></div>
                    <div class="profile-field"><span>Tạo lúc</span><strong><%= user.getCreatedAt() == null ? "-" : user.getCreatedAt().format(dateTimeFormatter) %></strong></div>
                    <div class="profile-field"><span>Cập nhật lúc</span><strong><%= user.getUpdatedAt() == null ? "-" : user.getUpdatedAt().format(dateTimeFormatter) %></strong></div>
                </div>

                <div class="profile-actions">
                    <a href="${pageContext.request.contextPath}/product">Quay lại cửa hàng</a>
                    <a href="${pageContext.request.contextPath}/auth?action=forgotPassword">Quên mật khẩu?</a>
                    <a class="profile-action-danger" href="${pageContext.request.contextPath}/auth?action=logout">Đăng xuất</a>
                </div>
            </div>
        </section>
    <% } %>
</section>

<%@include file="../includes/layout-end.jsp" %>
