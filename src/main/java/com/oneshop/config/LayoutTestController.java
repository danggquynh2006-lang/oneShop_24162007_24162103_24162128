package com.oneshop.config;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LayoutTestController {
    @GetMapping("/layout-test")         public String web()     { return "layout-test"; }
    @GetMapping("/admin/layout-test")   public String admin()   { return "layout-test"; }
    @GetMapping("/vendor/layout-test")  public String vendor()  { return "layout-test"; }
    @GetMapping("/shipper/layout-test") public String shipper() { return "layout-test"; }
}