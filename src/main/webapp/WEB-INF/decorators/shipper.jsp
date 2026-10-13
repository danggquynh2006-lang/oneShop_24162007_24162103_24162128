<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title><sitemesh:write property="title"/> | OneShop Shipper</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.css" rel="stylesheet">
    <sitemesh:write property="head"/>
</head>
<body>
    <div class="d-flex min-vh-100">
        <aside class="bg-dark text-white p-3" style="width:240px">
            <h5 class="text-danger">OneShop Shipper</h5>
            <ul class="nav flex-column mt-3">
                <li class="nav-item"><a class="nav-link text-white" href="/shipper/orders"><i class="bi bi-truck"></i> Đơn được giao</a></li>
                <li class="nav-item"><a class="nav-link text-white" href="/shipper/statistics"><i class="bi bi-bar-chart"></i> Thống kê</a></li>
            </ul>
        </aside>
        <div class="flex-grow-1 p-4 bg-light">
            <sitemesh:write property="body"/>
        </div>
    </div>
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>