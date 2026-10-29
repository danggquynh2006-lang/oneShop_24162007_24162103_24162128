<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="org.springframework.web.util.HtmlUtils" %>
<%
    boolean loggedIn = request.getUserPrincipal() != null;
    String username = loggedIn ? HtmlUtils.htmlEscape(request.getUserPrincipal().getName()) : "";
    String keyword = request.getParameter("keyword");
    keyword = keyword == null ? "" : HtmlUtils.htmlEscape(keyword);
%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title><sitemesh:write property="title"/> | OneShop</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Dancing+Script:wght@600&family=Playfair+Display:wght@500;600;700&family=Quicksand:wght@400;500;600;700&display=swap" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.css" rel="stylesheet">
    <link href="/css/oneshop.css" rel="stylesheet">
    <sitemesh:write property="head"/>
</head>
<body class="d-flex flex-column min-vh-100">
<nav class="navbar navbar-expand-lg os-navbar sticky-top">
    <div class="container gap-3">
        <a class="navbar-brand os-brand" href="/">OneShop</a>
        <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#mainNav" aria-label="Mở menu">
            <span class="navbar-toggler-icon"></span>
        </button>
        <div class="collapse navbar-collapse" id="mainNav">
            <form class="os-search flex-grow-1 my-2 my-lg-0 mx-lg-4" action="/" method="get" role="search">
                <div class="input-group">
                    <input class="form-control" type="search" name="keyword" value="<%= keyword %>" placeholder="Tìm son, kem dưỡng, serum..." aria-label="Tìm kiếm sản phẩm">
                    <button class="btn btn-danger px-4" type="submit" aria-label="Tìm"><i class="bi bi-search"></i></button>
                </div>
            </form>
            <div class="d-flex align-items-center gap-3 ms-lg-auto">
                <a class="os-nav-link" href="/cart"><i class="bi bi-bag-heart me-1"></i> Giỏ hàng</a>
                <% if (loggedIn) { %>
                    <div class="dropdown">
                        <button class="btn btn-link os-nav-link text-decoration-none dropdown-toggle p-0" data-bs-toggle="dropdown" aria-expanded="false"><i class="bi bi-person-heart me-1"></i><%= username %></button>
                        <ul class="dropdown-menu dropdown-menu-end">
                            <li><a class="dropdown-item" href="/user/profile">Tài khoản của tôi</a></li>
                            <li><a class="dropdown-item" href="/user/orders">Đơn hàng</a></li>
                            <li><hr class="dropdown-divider"></li>
                            <li><form action="/logout" method="post" class="m-0"><button class="dropdown-item" type="submit">Đăng xuất</button></form></li>
                        </ul>
                    </div>
                <% } else { %>
                    <a class="os-nav-link" href="/login"><i class="bi bi-person me-1"></i> Đăng nhập</a>
                <% } %>
            </div>
        </div>
    </div>
</nav>
<main class="container my-4 flex-grow-1">
    <sitemesh:write property="body"/>
</main>
<footer class="os-footer mt-auto py-3 text-center"><div class="container">OneShop · Tìm và tỏa sáng với vẻ đẹp của riêng bạn.</div></footer>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
