<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@include file="../includes/layout.jsp" %>
<% String pageTitle = "Liên hệ | LinhNamStore"; %>

<div class="contact-container">
    <h2>Liên hệ với chúng tôi</h2>
    <p>Gửi ý kiến, thắc mắc hoặc yêu cầu hỗ trợ. Chúng tôi sẽ phản hồi sớm nhất.</p>

    <form class="contact-form" id="contactForm" onsubmit="return validateContactForm()">
        <div class="ch-form-field">
            <label for="contactFullName">Họ và tên</label>
            <input type="text" id="contactFullName" name="fullName" placeholder="Nguyễn Văn A">
            <span class="form-error" id="fullNameError">Vui lòng nhập họ và tên</span>
        </div>

        <div class="ch-form-field">
            <label for="contactEmail">Email</label>
            <input type="email" id="contactEmail" name="email" placeholder="yourname@gmail.com">
            <span class="form-error" id="contactEmailError">Email phải có dạng yourname@gmail.com</span>
        </div>

        <div class="ch-form-field">
            <label for="contactSubject">Chủ đề</label>
            <input type="text" id="contactSubject" name="subject" placeholder="Hỗ trợ kỹ thuật, góp ý, hợp tác...">
            <span class="form-error" id="subjectError">Vui lòng nhập chủ đề</span>
        </div>

        <div class="ch-form-field">
            <label for="contactMessage">Nội dung</label>
            <textarea id="contactMessage" name="message" placeholder="Mô tả chi tiết vấn đề hoặc yêu cầu của bạn..."></textarea>
            <span class="form-error" id="messageError">Vui lòng nhập nội dung</span>
        </div>

        <button type="submit" class="home-cta">GỬI LIÊN HỆ</button>

        <% if (request.getAttribute("success") != null) { %>
            <p style="margin-top:16px;color:#22c55e;font-size:14px;"><%= request.getAttribute("success") %></p>
        <% } %>
        <% if (request.getAttribute("error") != null) { %>
            <p style="margin-top:16px;color:#ef4444;font-size:14px;"><%= request.getAttribute("error") %></p>
        <% } %>
    </form>
</div>

<script>
function validateContactForm() {
    var valid = true;
    var fields = [
        { id: 'contactFullName', errorId: 'fullNameError', check: function(v) { return v.trim().length > 0; } },
        { id: 'contactEmail', errorId: 'contactEmailError', check: function(v) { return /^[A-Za-z0-9._%+\-]+@[A-Za-z0-9.\-]+\.[A-Za-z]{2,}$/.test(v); } },
        { id: 'contactSubject', errorId: 'subjectError', check: function(v) { return v.trim().length > 0; } },
        { id: 'contactMessage', errorId: 'messageError', check: function(v) { return v.trim().length > 0; } }
    ];

    for (var i = 0; i < fields.length; i++) {
        var f = fields[i];
        var input = document.getElementById(f.id);
        var error = document.getElementById(f.errorId);
        if (!f.check(input.value)) {
            input.classList.add('input-error');
            error.classList.add('visible');
            valid = false;
        } else {
            input.classList.remove('input-error');
            error.classList.remove('visible');
        }
    }

    return valid;
}

// Clear error on input
document.querySelectorAll('.contact-form input, .contact-form textarea').forEach(function(el) {
    el.addEventListener('input', function() {
        this.classList.remove('input-error');
        var errorEl = this.parentElement.querySelector('.form-error');
        if (errorEl) errorEl.classList.remove('visible');
    });
});
</script>

<%@include file="../includes/layout-end.jsp" %>
