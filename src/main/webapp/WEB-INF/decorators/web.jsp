<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="org.springframework.web.util.HtmlUtils" %>
<%
    boolean loggedIn = request.getUserPrincipal() != null;
    boolean isAdmin = request.isUserInRole("ADMIN") || request.isUserInRole("MANAGER");
    boolean isVendor = request.isUserInRole("VENDOR");
    boolean isShipper = request.isUserInRole("SHIPPER");
    // Khách hàng mua sắm: user thường hoặc vendor (vendor cũng là khách)
    boolean isCustomer = loggedIn && !isAdmin && !isShipper;
    String roleLabel = request.isUserInRole("ADMIN") ? "Admin"
            : request.isUserInRole("MANAGER") ? "Manager"
            : isVendor ? "Vendor"
            : isShipper ? "Shipper" : "";
%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title><sitemesh:write property="title"/> | OneShop</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Dancing+Script:wght@600&family=Playfair+Display:wght@500;600&family=Quicksand:wght@400;500;600;700&display=swap" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.css" rel="stylesheet">
    <link href="/css/oneshop.css" rel="stylesheet">
    <link href="/css/beauty-assistant.css" rel="stylesheet">
    <sitemesh:write property="head"/>
</head>
<body class="d-flex flex-column min-vh-100">
    <nav class="navbar navbar-expand-lg os-navbar sticky-top">
        <div class="container">
            <a class="navbar-brand os-brand" href="/">OneShop</a>
            <button class="navbar-toggler" type="button" data-bs-toggle="collapse"
                    data-bs-target="#mainNav" aria-controls="mainNav" aria-expanded="false"
                    aria-label="Mở menu">
                <span class="navbar-toggler-icon"></span>
            </button>

            <div class="collapse navbar-collapse" id="mainNav">
                <form class="os-search flex-grow-1 my-2 my-lg-0 mx-lg-4" action="/search" method="get" role="search">
                    <div class="input-group">
                        <input class="form-control" type="search" name="keyword"
                               placeholder="Tìm son, kem dưỡng, serum..." aria-label="Tìm kiếm sản phẩm">
                        <button class="btn btn-danger" type="submit" aria-label="Tìm">
                            <i class="bi bi-search"></i>
                        </button>
                    </div>
                </form>

                <ul class="navbar-nav align-items-lg-center gap-lg-2">
                    <% if (!loggedIn || isCustomer) { %>
                    <li class="nav-item">
                        <a class="nav-link position-relative" href="/cart"><i class="bi bi-bag-heart"></i> Giỏ hàng
                            <span id="cartCount" class="badge bg-danger rounded-pill ms-1 d-none" aria-label="Số sản phẩm trong giỏ">0</span>
                        </a>
                    </li>
                    <% } %>
                    <% if (loggedIn) { %>
                    <li class="nav-item dropdown">
                        <a class="nav-link dropdown-toggle" href="#" role="button"
                           data-bs-toggle="dropdown" aria-expanded="false">
                            <i class="bi bi-person-heart"></i>
                            <%= HtmlUtils.htmlEscape(request.getUserPrincipal().getName()) %>
                            <% if (!roleLabel.isEmpty()) { %>
                            <span class="badge bg-danger ms-1"><%= roleLabel %></span>
                            <% } %>
                        </a>
                        <ul class="dropdown-menu dropdown-menu-end">
                            <% if (isAdmin) { %>
                            <li><a class="dropdown-item fw-semibold" href="/admin/users"><i class="bi bi-speedometer2"></i> Trang quản trị</a></li>
                            <li><hr class="dropdown-divider"></li>
                            <% } %>
                            <% if (isVendor) { %>
                            <li><a class="dropdown-item fw-semibold" href="/vendor/shop"><i class="bi bi-shop"></i> Quản lý shop của tôi</a></li>
                            <li><hr class="dropdown-divider"></li>
                            <% } %>
                            <% if (isShipper) { %>
                            <li><a class="dropdown-item fw-semibold" href="/shipper/orders"><i class="bi bi-truck"></i> Trang giao hàng</a></li>
                            <li><hr class="dropdown-divider"></li>
                            <% } %>
                            <li><a class="dropdown-item" href="/user/profile"><i class="bi bi-person"></i> Tài khoản của tôi</a></li>
                            <% if (isCustomer) { %>
                            <li><a class="dropdown-item" href="/user/orders"><i class="bi bi-receipt"></i> Đơn hàng của tôi</a></li>
                            <li><a class="dropdown-item" href="/user/favorites"><i class="bi bi-heart"></i> Yêu thích</a></li>
                            <li><a class="dropdown-item" href="/user/viewed"><i class="bi bi-clock-history"></i> Đã xem</a></li>
                            <li><a class="dropdown-item" href="/user/addresses"><i class="bi bi-geo-alt"></i> Địa chỉ nhận hàng</a></li>
                            <% } %>
                            <% if (isCustomer && !isVendor) { %>
                            <li><a class="dropdown-item" href="/shop/register"><i class="bi bi-shop-window"></i> Đăng ký bán hàng</a></li>
                            <% } %>
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
                        <a class="nav-link" href="/login"><i class="bi bi-person"></i> Đăng nhập</a>
                    </li>
                    <li class="nav-item">
                        <a class="btn btn-outline-danger btn-sm" href="/register">Đăng ký</a>
                    </li>
                    <% } %>
                </ul>
            </div>
        </div>
    </nav>

    <main class="container my-4 flex-grow-1">
        <sitemesh:write property="body"/>
    </main>

    <footer class="os-footer mt-auto">
        <div class="container py-5">
            <div class="row g-4">
                <div class="col-md-4">
                    <div class="os-brand mb-2">OneShop</div>
                    <p class="mb-3">Tìm và tỏa sáng với vẻ đẹp của riêng bạn.</p>
                    <div class="d-flex gap-3 fs-5">
                        <a href="#" aria-label="Facebook"><i class="bi bi-facebook"></i></a>
                        <a href="#" aria-label="Instagram"><i class="bi bi-instagram"></i></a>
                        <a href="#" aria-label="TikTok"><i class="bi bi-tiktok"></i></a>
                        <a href="#" aria-label="YouTube"><i class="bi bi-youtube"></i></a>
                    </div>
                </div>
                <div class="col-6 col-md-2">
                    <h6>Mua sắm</h6>
                    <ul>
                        <li><a href="/">Trang chủ</a></li>
                        <li><a href="/cart">Giỏ hàng</a></li>
                        <li><a href="/user/orders">Đơn hàng của tôi</a></li>
                        <li><a href="/user/favorites">Yêu thích</a></li>
                    </ul>
                </div>
                <div class="col-6 col-md-3">
                    <h6>Tài khoản</h6>
                    <ul>
                        <li><a href="/user/profile">Tài khoản của tôi</a></li>
                        <li><a href="/user/addresses">Địa chỉ nhận hàng</a></li>
                        <li><a href="/shop/register">Đăng ký bán hàng cùng OneShop</a></li>
                    </ul>
                </div>
                <div class="col-md-3">
                    <h6>Liên hệ</h6>
                    <ul>
                        <li><i class="bi bi-envelope-heart"></i> hello@oneshop.vn</li>
                        <li><i class="bi bi-telephone"></i> 1900 0000</li>
                        <li><i class="bi bi-geo-alt"></i> TP. Hồ Chí Minh</li>
                    </ul>
                </div>
            </div>
        </div>
        <div class="os-footer-bottom">
        </div>
    </footer>
<div class="beauty-assistant" data-beauty-assistant>
    <section class="beauty-assistant-panel" data-assistant-panel hidden aria-label="Trợ lý làm đẹp OneShop">
        <header class="beauty-assistant-head">
            <div class="beauty-assistant-avatar"><i class="bi bi-stars" aria-hidden="true"></i></div>
            <div><div class="beauty-assistant-title">Trợ lý làm đẹp OneShop</div><div class="beauty-assistant-subtitle">Tư vấn sản phẩm theo nhu cầu của bạn</div></div>
            <button class="beauty-assistant-close" type="button" data-assistant-close aria-label="Đóng chat"><i class="bi bi-x-lg"></i></button>
        </header>
        <div class="beauty-assistant-messages" data-assistant-messages aria-live="polite">
            <div class="beauty-assistant-message bot">Chào bạn! Bạn đang quan tâm loại da hoặc nhu cầu làm đẹp nào? Mình sẽ tìm gợi ý trong các sản phẩm đang bán ở OneShop.</div>
        </div>
        <div class="beauty-assistant-suggestions" aria-label="Câu hỏi gợi ý">
            <button type="button" data-assistant-prompt="Da khô thì nên dùng sản phẩm nào của shop?">Da khô</button>
            <button type="button" data-assistant-prompt="Gợi ý sản phẩm dưỡng ẩm đang còn hàng với giá dưới 500.000 đồng.">Dưỡng ẩm</button>
            <button type="button" data-assistant-prompt="Da nhạy cảm nên chọn sản phẩm nào trong shop?">Da nhạy cảm</button>
        </div>
        <div class="beauty-assistant-status" data-assistant-status>Đang kiểm tra kết nối…</div>
        <form class="beauty-assistant-composer" data-assistant-form>
            <textarea class="form-control" data-assistant-input rows="1" maxlength="1000" placeholder="Nhập câu hỏi của bạn…" aria-label="Nhập câu hỏi cho trợ lý"></textarea>
            <button type="submit" data-assistant-send aria-label="Gửi câu hỏi"><i class="bi bi-send-fill" aria-hidden="true"></i></button>
        </form>
    </section>
    <button class="beauty-assistant-toggle" type="button" data-assistant-toggle aria-label="Mở trợ lý làm đẹp" aria-expanded="false">
        <i class="bi bi-chat-heart-fill" aria-hidden="true"></i>
    </button>
</div>
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
<script src="/js/beauty-assistant.js" defer></script>
    <% if (isCustomer) { %>
    <script>
        // Cập nhật số lượng giỏ hàng trên header.
        // Trang khác (vd nút "Thêm vào giỏ" AJAX) gọi window.updateCartCount() để làm mới số này.
        window.updateCartCount = function () {
            var badge = document.getElementById('cartCount');
            if (!badge) return;
            fetch('/api/cart/count', { credentials: 'same-origin', headers: { 'Accept': 'application/json' } })
                .then(function (res) { return res.ok ? res.json() : null; })
                .then(function (data) {
                    if (!data || data.ok !== true) return;
                    var n = Number(data.count) || 0;
                    badge.textContent = n > 99 ? '99+' : n;
                    badge.classList.toggle('d-none', n <= 0);
                })
                .catch(function () { /* lỗi mạng: giữ nguyên, không làm hỏng trang */ });
        };
        document.addEventListener('DOMContentLoaded', window.updateCartCount);
        // Quay lại bằng nút Back cũng cập nhật lại số.
        window.addEventListener('pageshow', function (e) { if (e.persisted) window.updateCartCount(); });
    </script>
    <% } %>
</body>
</html>