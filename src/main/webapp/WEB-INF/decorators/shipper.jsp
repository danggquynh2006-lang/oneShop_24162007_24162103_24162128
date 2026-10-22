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
    <title><sitemesh:write property="title"/> | OneShop Shipper</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.css" rel="stylesheet">
    <style>
        @media (max-width: 900px) {
            .shipper-shell { flex-direction: column; }
            .shipper-sidebar { width: 100% !important; }
            .shipper-sidebar .nav { flex-direction: row !important; flex-wrap: wrap; }
            .shipper-content { padding: 1rem !important; }
        }
    </style>
    <sitemesh:write property="head"/>
</head>
<body>
    <div class="d-flex min-vh-100 shipper-shell">
        <aside class="bg-dark text-white p-3 d-flex flex-column shipper-sidebar" style="width:240px">
            <h5 class="text-danger">OneShop Shipper</h5>
            <ul class="nav flex-column mt-3">
                <li class="nav-item"><a class="nav-link text-white<%= active(uri, "/shipper/orders") %>" href="/shipper/orders"><i class="bi bi-truck"></i> Đơn được giao</a></li>
                <li class="nav-item"><a class="nav-link text-white<%= active(uri, "/shipper/statistics") %>" href="/shipper/statistics"><i class="bi bi-bar-chart"></i> Thống kê</a></li>
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
        <div class="flex-grow-1 p-4 bg-light shipper-content">
            <sitemesh:write property="body"/>
        </div>
    </div>
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>