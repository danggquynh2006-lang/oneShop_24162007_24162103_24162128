package com.oneshop.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

import com.oneshop.auth.CustomUserDetailsService;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            // Form Thymeleaf dùng CSRF token.
            // /api/** (AJAX) và /logout được miễn: cookie JWT đã đặt SameSite=Lax,
            // logout chỉ xóa cookie nên không có rủi ro đáng kể.
            .csrf(csrf -> csrf
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                // Không đổi CSRF token ở mỗi request (JWT stateless không có "phiên đăng nhập" để đổi).
                .sessionAuthenticationStrategy((authentication, request, response) -> { })
                .ignoringRequestMatchers("/api/**", "/logout"))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/admin/**", "/manager/**").hasAnyRole("ADMIN", "MANAGER")
                .requestMatchers("/vendor/**").hasRole("VENDOR")
                .requestMatchers("/shipper/**").hasRole("SHIPPER")
                .requestMatchers("/user/**", "/cart/**", "/shop/register", "/payment/vnpay/create", "/api/me").authenticated()
                .anyRequest().permitAll())
            // Chưa đăng nhập mà vào trang cần quyền: chuyển tới /login. Thiếu quyền: trả 403.
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((req, res, e) -> res.sendRedirect("/login")))
            .logout(logout -> logout
                .logoutUrl("/logout")
                .deleteCookies(JwtAuthenticationFilter.COOKIE_NAME)
                .logoutSuccessUrl("/login?logout"))
            .addFilterBefore(new JwtAuthenticationFilter(jwtService, userDetailsService),
                    UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}