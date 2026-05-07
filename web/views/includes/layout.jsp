<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%--
  Shared layout wrapper.
  Usage: include at top of each content JSP, then write content inside <div class="content-area">.
--%>
<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>${pageTitle != null ? pageTitle : "LinhNamStore"}</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&family=JetBrains+Mono:wght@400;700&display=swap" rel="stylesheet">
    <script defer src="${pageContext.request.contextPath}/assets/js/theme-toggle.js"></script>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/homepage-product.css">
</head>
<body>
    <%@include file="banner.jsp" %>
    <% if (session.getAttribute("authUserName") != null) { %>
    <%@include file="topmenu.jsp" %>

    <div class="page-layout">
        <%@include file="leftmenu.jsp" %>

        <main class="content-area">
    <% } else { %>
    <main class="content-area auth-standalone">
    <% } %>
