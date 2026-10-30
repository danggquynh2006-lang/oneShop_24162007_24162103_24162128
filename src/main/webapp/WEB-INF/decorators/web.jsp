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
    <link href="/css/beauty-assistant.css" rel="stylesheet">
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
<footer class="os-footer mt-auto">
    <div class="container os-footer-main">
        <div class="os-footer-brand">
            <a class="os-brand" href="/">OneShop</a>
            <p>Tìm và tỏa sáng với vẻ đẹp của riêng bạn.</p>
            <div class="os-footer-social" aria-label="Mạng xã hội">
                <a href="https://www.facebook.com/" aria-label="Facebook"><i class="bi bi-facebook"></i></a>
                <a href="https://www.instagram.com/" aria-label="Instagram"><i class="bi bi-instagram"></i></a>
                <a href="https://www.tiktok.com/" aria-label="TikTok"><i class="bi bi-tiktok"></i></a>
                <a href="https://www.youtube.com/" aria-label="YouTube"><i class="bi bi-youtube"></i></a>
            </div>
        </div>
        <div class="os-footer-column">
            <h2>Mua sắm</h2>
            <a href="/">Trang chủ</a>
            <a href="/cart">Giỏ hàng</a>
            <a href="/user/orders">Đơn hàng của tôi</a>
            <a href="/top20">Yêu thích</a>
        </div>
        <div class="os-footer-column">
            <h2>Tài khoản</h2>
            <a href="/user/profile">Tài khoản của tôi</a>
            <a href="/user/addresses">Địa chỉ nhận hàng</a>
            <a href="/shop/register">Đăng ký bán hàng cùng OneShop</a>
        </div>
        <div class="os-footer-column os-footer-contact">
            <h2>Liên hệ</h2>
            <a href="mailto:hello@oneshop.vn"><i class="bi bi-envelope-heart"></i> hello@oneshop.vn</a>
            <a href="tel:19000000"><i class="bi bi-telephone"></i> 1900 0000</a>
            <span><i class="bi bi-geo-alt"></i> TP. Hồ Chí Minh</span>
        </div>
    </div>
    <div class="os-footer-bottom">
        <div class="container os-footer-bottom-inner">
            <span>© 2026 OneShop. Đồ án môn Lập trình Web.</span>
            <span>Giao diện dựa trên template Aura, HTML Template by: <a href="https://themeforest.net/user/templatesjungle" target="_blank" rel="noopener noreferrer">TemplatesJungle</a></span>
        </div>
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
</body>
</html>
