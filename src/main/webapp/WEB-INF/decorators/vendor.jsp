<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%!
    /** Trả về class tô sáng nếu đường dẫn hiện tại bắt đầu bằng prefix. */
    private String active(String uri, String prefix) {
        return uri != null && uri.startsWith(prefix) ? " os-active" : "";
    }
%>
<%
    String uri = (String) request.getAttribute("jakarta.servlet.forward.request_uri");
    if (uri == null) {
        uri = request.getRequestURI();
    }
%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title><sitemesh:write property="title"/> | OneShop Vendor</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Dancing+Script:wght@600&family=Playfair+Display:wght@500;600&family=Quicksand:wght@400;500;600;700&display=swap" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.css" rel="stylesheet">
    <link href="/css/oneshop.css" rel="stylesheet">
    <sitemesh:write property="head"/>
</head>
<body>
    <div class="os-shell">
        <aside class="os-sidebar">
            <div class="os-brand">OneShop</div>
            <div class="os-role">Nhà bán hàng</div>
            <ul class="nav flex-column mt-3">
                <li class="nav-item"><a class="nav-link<%= active(uri, "/vendor/shop") %>" href="/vendor/shop"><i class="bi bi-shop"></i> Thông tin shop</a></li>
                <li class="nav-item"><a class="nav-link<%= active(uri, "/vendor/products") %>" href="/vendor/products"><i class="bi bi-box-seam"></i> Sản phẩm</a></li>
                <li class="nav-item"><a class="nav-link<%= active(uri, "/vendor/orders") %>" href="/vendor/orders"><i class="bi bi-receipt"></i> Đơn hàng</a></li>
                <li class="nav-item"><a class="nav-link<%= active(uri, "/vendor/promotions") %>" href="/vendor/promotions"><i class="bi bi-percent"></i> Khuyến mãi</a></li>
                <li class="nav-item"><a class="nav-link<%= active(uri, "/vendor/revenue") %>" href="/vendor/revenue"><i class="bi bi-graph-up"></i> Doanh thu</a></li>
                <li class="nav-item"><a class="nav-link<%= active(uri, "/vendor/reviews") %>" href="/vendor/reviews"><i class="bi bi-star"></i> Đánh giá</a></li>
            </ul>
            <div class="os-sidebar-footer">
                <a href="/" class="btn btn-outline-light btn-sm w-100 mb-2"><i class="bi bi-house-heart"></i> Về trang chủ</a>
                <% if (request.getUserPrincipal() != null) { %>
                    <div class="mb-2 text-truncate"><i class="bi bi-person-circle"></i> <%= request.getUserPrincipal().getName() %></div>
                <% } %>
                <form action="/logout" method="post" class="m-0">
                    <button type="submit" class="btn btn-outline-light btn-sm w-100"><i class="bi bi-box-arrow-right"></i> Đăng xuất</button>
                </form>
            </div>
        </aside>
        <div class="os-content">
            <sitemesh:write property="body"/>
        </div>
    </div>
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>