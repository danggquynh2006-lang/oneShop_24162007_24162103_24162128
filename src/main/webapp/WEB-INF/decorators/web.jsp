<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title><sitemesh:write property="title"/> | OneShop</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.css" rel="stylesheet">
    <sitemesh:write property="head"/>
</head>
<body class="d-flex flex-column min-vh-100">
    <nav class="navbar bg-white shadow-sm">
        <div class="container">
            <a class="navbar-brand fw-bold text-danger" href="/">OneShop</a>
            <div class="d-flex gap-3">
                <a class="text-dark" href="/cart"><i class="bi bi-cart3"></i> Giỏ hàng</a>
                <a class="text-dark" href="/login"><i class="bi bi-person"></i> Đăng nhập</a>
            </div>
        </div>
    </nav>
    <main class="container my-4 flex-grow-1">
        <sitemesh:write property="body"/>
    </main>
    <footer class="bg-dark text-white-50 py-3 text-center">&copy; 2026 OneShop</footer>
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>