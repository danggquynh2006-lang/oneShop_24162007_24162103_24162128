<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%!
    /** Trả về class tô sáng nếu đường dẫn hiện tại bắt đầu bằng prefix. */
    private String active(String uri, String prefix) {
        return uri != null && uri.startsWith(prefix) ? " bg-danger rounded" : "";
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
    <title><sitemesh:write property="title"/> | OneShop Admin</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.css" rel="stylesheet">
    <style>
        @media (max-width: 900px) {
            .admin-shell { flex-direction: column; }
            .admin-sidebar { width: 100% !important; }
            .admin-sidebar .nav { flex-direction: row !important; flex-wrap: wrap; }
            .admin-content { padding: 1rem !important; }
        }
    </style>
    <sitemesh:write property="head"/>
</head>
<body>
    <div class="d-flex min-vh-100 admin-shell">
        <aside class="bg-dark text-white p-3 d-flex flex-column admin-sidebar" style="width:240px">
            <h5 class="text-danger">OneShop Admin</h5>
            <ul class="nav flex-column mt-3">
                <li class="nav-item"><a class="nav-link text-white<%= active(uri, "/admin/users") %>" href="/admin/users"><i class="bi bi-people"></i> Người dùng</a></li>
                <li class="nav-item"><a class="nav-link text-white<%= active(uri, "/admin/categories") %>" href="/admin/categories"><i class="bi bi-tags"></i> Danh mục</a></li>
                <li class="nav-item"><a class="nav-link text-white<%= active(uri, "/admin/shops") %>" href="/admin/shops"><i class="bi bi-shop"></i> Shop</a></li>
                <li class="nav-item"><a class="nav-link text-white<%= active(uri, "/admin/products") %>" href="/admin/products"><i class="bi bi-box-seam"></i> Sản phẩm</a></li>
                <li class="nav-item"><a class="nav-link text-white<%= active(uri, "/admin/carriers") %>" href="/admin/carriers"><i class="bi bi-truck"></i> Nhà vận chuyển</a></li>
                <li class="nav-item"><a class="nav-link text-white<%= active(uri, "/admin/orders") %>" href="/admin/orders"><i class="bi bi-receipt"></i> Đơn hàng</a></li>
                <li class="nav-item"><a class="nav-link text-white<%= active(uri, "/admin/promotions") %>" href="/admin/promotions"><i class="bi bi-percent"></i> Khuyến mãi</a></li>
            </ul>
            <div class="mt-auto pt-3 border-top border-secondary small">
                <a href="/" class="btn btn-outline-light btn-sm w-100 mb-2"><i class="bi bi-house"></i> Về trang chủ</a>
                <% if (request.getUserPrincipal() != null) { %>
                    <div class="text-white-50 mb-2 text-truncate"><i class="bi bi-person-circle"></i> <%= request.getUserPrincipal().getName() %></div>
                <% } %>
                <form action="/logout" method="post" class="m-0">
                    <button type="submit" class="btn btn-outline-light btn-sm w-100"><i class="bi bi-box-arrow-right"></i> Đăng xuất</button>
                </form>
            </div>
        </aside>
        <div class="flex-grow-1 p-4 bg-light admin-content">
            <sitemesh:write property="body"/>
        </div>
    </div>
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>