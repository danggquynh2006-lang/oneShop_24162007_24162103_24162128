<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="org.springframework.web.util.HtmlUtils" %>
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
    <nav class="navbar navbar-expand-lg bg-white shadow-sm">
        <div class="container">
            <a class="navbar-brand fw-bold text-danger" href="/">OneShop</a>
            <button class="navbar-toggler" type="button" data-bs-toggle="collapse"
                    data-bs-target="#mainNav" aria-controls="mainNav" aria-expanded="false"
                    aria-label="Mở menu">
                <span class="navbar-toggler-icon"></span>
            </button>

            <div class="collapse navbar-collapse" id="mainNav">
                <form class="d-flex flex-grow-1 my-2 my-lg-0 mx-lg-4" action="/search" method="get" role="search">
                    <input class="form-control" type="search" name="keyword"
                           placeholder="Tìm sản phẩm..." aria-label="Tìm kiếm sản phẩm">
                    <button class="btn btn-danger ms-2" type="submit" aria-label="Tìm">
                        <i class="bi bi-search"></i>
                    </button>
                </form>

                <ul class="navbar-nav align-items-lg-center gap-lg-3">
                    <li class="nav-item">
                        <a class="nav-link text-dark" href="/cart"><i class="bi bi-cart3"></i> Giỏ hàng</a>
                    </li>
                    <% if (request.getUserPrincipal() != null) { %>
                    <li class="nav-item dropdown">
                        <a class="nav-link dropdown-toggle text-dark" href="#" role="button"
                           data-bs-toggle="dropdown" aria-expanded="false">
                            <i class="bi bi-person"></i>
                            <%= HtmlUtils.htmlEscape(request.getUserPrincipal().getName()) %>
                        </a>
                        <ul class="dropdown-menu dropdown-menu-end">
                            <li><a class="dropdown-item" href="/user/profile"><i class="bi bi-person"></i> Tài khoản của tôi</a></li>
                            <li><a class="dropdown-item" href="/user/orders"><i class="bi bi-receipt"></i> Đơn hàng của tôi</a></li>
                            <li><a class="dropdown-item" href="/user/favorites"><i class="bi bi-heart"></i> Yêu thích</a></li>
                            <li><a class="dropdown-item" href="/user/viewed"><i class="bi bi-clock-history"></i> Đã xem</a></li>
                            <li><a class="dropdown-item" href="/user/addresses"><i class="bi bi-geo-alt"></i> Địa chỉ nhận hàng</a></li>
                            <li><hr class="dropdown-divider"></li>
                            <li>
                                <form action="/logout" method="post" class="m-0">
                                    <button type="submit" class="dropdown-item">
                                        <i class="bi bi-box-arrow-right"></i> Đăng xuất
                                    </button>
                                </form>
                            </li>
                        </ul>
                    </li>
                    <% } else { %>
                    <li class="nav-item">
                        <a class="nav-link text-dark" href="/login"><i class="bi bi-person"></i> Đăng nhập</a>
                    </li>
                    <% } %>
                </ul>
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