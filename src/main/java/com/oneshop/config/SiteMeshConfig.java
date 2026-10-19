package com.oneshop.config;

import org.sitemesh.builder.SiteMeshFilterBuilder;
import org.sitemesh.config.ConfigurableSiteMeshFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SiteMeshConfig {

    @Bean
    public FilterRegistrationBean<OneShopSiteMeshFilter> siteMeshFilter() {
        FilterRegistrationBean<OneShopSiteMeshFilter> reg =
                new FilterRegistrationBean<>(new OneShopSiteMeshFilter());
        reg.addUrlPatterns("/*");
        return reg;
    }

    public static class OneShopSiteMeshFilter extends ConfigurableSiteMeshFilter {
        @Override
        protected void applyCustomConfiguration(SiteMeshFilterBuilder builder) {
            builder
	            .addDecoratorPath("/admin/*", "admin.jsp")
	            .addDecoratorPath("/manager/*", "admin.jsp")
	            .addDecoratorPath("/vendor/*", "vendor.jsp")
	            .addDecoratorPath("/shipper/*", "shipper.jsp")
	            .addDecoratorPath("/*", "web.jsp")
	            .addExcludedPath("/css/*")
	            .addExcludedPath("/js/*")
	            .addExcludedPath("/img/*")
	            .addExcludedPath("/api/*");
        }
    }
}