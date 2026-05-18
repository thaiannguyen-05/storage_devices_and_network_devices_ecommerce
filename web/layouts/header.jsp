<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><c:out value="${empty pageTitle ? 'LinhNamStore' : pageTitle}" /></title>
    <link rel="stylesheet" href="${ctx}/assets/css/style.css">
    <script defer src="${ctx}/assets/js/app.js"></script>
</head>
<body>
<header class="site-header">
    <div class="banner">
        <a class="brand-logo" href="${ctx}/home" aria-label="LinhNamStore home">
            <span class="logo-mark">LN</span>
            <span>LinhNamStore</span>
        </a>
        <form class="search-form" action="${ctx}/home" method="get">
            <input data-search type="search" name="q" placeholder="Tìm SSD, HDD, NAS, router..." value="${param.q}" autocomplete="off">
            <button class="button" type="submit">Tìm</button>
            <div class="search-suggestions" data-search-suggestions></div>
        </form>
        <div class="header-actions">
            <button class="hamburger" type="button" data-filter-toggle aria-label="Mở bộ lọc">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
                    <path d="M3 6h18M3 12h18M3 18h18"></path>
                </svg>
            </button>
            <a class="icon-button cart-link" href="${ctx}/cart" aria-label="Giỏ hàng">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
                    <circle cx="9" cy="20" r="1"></circle>
                    <circle cx="18" cy="20" r="1"></circle>
                    <path d="M2 3h3l2.2 11.4a2 2 0 0 0 2 1.6h8.6a2 2 0 0 0 2-1.6L22 7H7"></path>
                </svg>
                <span class="cart-badge"><c:out value="${empty sessionScope.cartCount ? 0 : sessionScope.cartCount}" /></span>
            </a>
            <c:choose>
                <c:when test="${not empty sessionScope.currentUser}">
                    <a class="icon-button" href="${ctx}/profile" aria-label="Tài khoản">
                        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
                            <path d="M20 21a8 8 0 1 0-16 0"></path>
                            <circle cx="12" cy="7" r="4"></circle>
                        </svg>
                    </a>
                    <form class="icon-form" action="${ctx}/auth" method="post">
                        <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
                        <input type="hidden" name="action" value="logout">
                        <button class="icon-button" type="submit" aria-label="Đăng xuất" title="Đăng xuất">
                            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
                                <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"></path>
                                <path d="M16 17l5-5-5-5"></path>
                                <path d="M21 12H9"></path>
                            </svg>
                        </button>
                    </form>
                </c:when>
                <c:otherwise>
                    <a class="icon-button" href="${ctx}/auth?action=login" aria-label="Đăng nhập">
                        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
                            <path d="M15 3h4a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2h-4"></path>
                            <path d="M10 17l5-5-5-5"></path>
                            <path d="M15 12H3"></path>
                        </svg>
                    </a>
                </c:otherwise>
            </c:choose>
        </div>
    </div>
    <nav class="top-menu" aria-label="Điều hướng chính">
        <div class="top-menu-inner">
            <a class="${activePage == 'home' ? 'active' : ''}" href="${ctx}/home">Trang chủ</a>
            <a class="${activePage == 'products' ? 'active' : ''}" href="${ctx}/home#products">Sản phẩm</a>
            <a class="${activePage == 'contact' ? 'active' : ''}" href="${ctx}/contact">Liên hệ</a>
            <a class="${activePage == 'about' ? 'active' : ''}" href="${ctx}/about">Giới thiệu</a>
            <a class="${activePage == 'orders' ? 'active' : ''}" href="${ctx}/orders">Đơn hàng</a>
            <c:if test="${sessionScope.currentUser.role == 'ADMIN' || sessionScope.role == 'ADMIN'}">
                <a class="${activePage == 'admin' ? 'active' : ''}" href="${ctx}/admin/dashboard">Quản trị</a>
            </c:if>
        </div>
    </nav>
    <div class="mobile-overlay"></div>
</header>
<div class="shell${hideFilters ? ' shell-auth' : ''}">
    <c:if test="${!hideFilters}">
        <aside class="left-menu" aria-label="Bộ lọc sản phẩm">
            <form action="${ctx}/home" method="get">
                <div class="filter-section">
                    <h3>Danh mục</h3>
                    <ul class="filter-list">
                        <li><label><input type="checkbox" name="category" value="STORAGE_DEVICE"> Storage device</label></li>
                        <li><label><input type="checkbox" name="category" value="NETWORK_DEVICE"> Network device</label></li>
                        <li><label><input type="checkbox" name="category" value="ACCESSORY"> Accessory</label></li>
                    </ul>
                </div>
                <div class="filter-section">
                    <h3>Thương hiệu</h3>
                    <ul class="filter-list">
                        <li><label><input type="checkbox" name="brand" value="Samsung"> Samsung</label></li>
                        <li><label><input type="checkbox" name="brand" value="Western Digital"> Western Digital</label></li>
                        <li><label><input type="checkbox" name="brand" value="Synology"> Synology</label></li>
                        <li><label><input type="checkbox" name="brand" value="TP-Link"> TP-Link</label></li>
                        <li><label><input type="checkbox" name="brand" value="SanDisk"> SanDisk</label></li>
                    </ul>
                </div>
                <div class="filter-section">
                    <h3>Khoảng giá</h3>
                    <ul class="filter-list">
                        <li><label><input type="radio" name="price" value="0-1000000"> Dưới 1 triệu</label></li>
                        <li><label><input type="radio" name="price" value="1000000-3000000"> 1-3 triệu</label></li>
                        <li><label><input type="radio" name="price" value="3000000-5000000"> 3-5 triệu</label></li>
                        <li><label><input type="radio" name="price" value="5000000-10000000"> 5-10 triệu</label></li>
                        <li><label><input type="radio" name="price" value="10000000-"> Trên 10 triệu</label></li>
                    </ul>
                </div>
                <div class="filter-section">
                    <h3>Trạng thái</h3>
                    <ul class="filter-list">
                        <li><label><input type="checkbox" name="status" value="ACTIVE"> Đang bán</label></li>
                        <li><label><input type="checkbox" name="status" value="OUT_OF_STOCK"> Hết hàng</label></li>
                    </ul>
                </div>
                <div class="filter-section">
                    <h3>Sắp xếp</h3>
                    <div class="field">
                        <select name="sort">
                            <option value="newest">Mới nhất</option>
                            <option value="priceAsc">Giá tăng</option>
                            <option value="priceDesc">Giá giảm</option>
                            <option value="bestSeller">Bán chạy</option>
                        </select>
                    </div>
                </div>
                <button class="button mt-4" type="submit">Áp dụng</button>
            </form>
        </aside>
    </c:if>
    <main class="content">

