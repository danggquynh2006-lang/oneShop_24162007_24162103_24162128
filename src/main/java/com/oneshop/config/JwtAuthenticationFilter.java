package com.oneshop.config;

import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.security.web.context.RequestAttributeSecurityContextRepository;
import org.springframework.web.filter.OncePerRequestFilter;

import com.oneshop.auth.CustomUserDetailsService;
import com.oneshop.auth.UserPrincipal;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    public static final String COOKIE_NAME = "token";

    // Đánh dấu SecurityContext vào request để SessionManagementFilter không coi
    // mỗi request là "vừa đăng nhập" (nếu không sẽ đổi CSRF token ở mọi request).
    private final RequestAttributeSecurityContextRepository contextRepository =
            new RequestAttributeSecurityContextRepository();

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtService jwtService, CustomUserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String token = readToken(request);
        if (token != null) {
            String email = jwtService.extractEmail(token);
            if (email != null) {
                try {
                    UserPrincipal principal = (UserPrincipal) userDetailsService.loadUserByUsername(email);
                    // Bị khóa hoặc chưa kích hoạt thì coi như chưa đăng nhập
                    if (principal.isEnabled() && principal.isAccountNonLocked()) {
                        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                                principal, null, principal.getAuthorities());
                        auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContext context = SecurityContextHolder.createEmptyContext();
                        context.setAuthentication(auth);
                        SecurityContextHolder.setContext(context);
                        contextRepository.saveContext(context, request, response);
                    }
                } catch (UsernameNotFoundException ignored) {
                    // user đã bị xóa: bỏ qua, coi như chưa đăng nhập
                }
            }
        }
        chain.doFilter(request, response);
    }

    private String readToken(HttpServletRequest request) {
        if (request.getCookies() == null) {
            return null;
        }
        for (Cookie cookie : request.getCookies()) {
            if (COOKIE_NAME.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }
}