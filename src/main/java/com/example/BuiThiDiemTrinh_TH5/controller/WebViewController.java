package com.example.BuiThiDiemTrinh_TH5.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebViewController {

    @GetMapping(value = {
            "/",
            "/pos",
            "/orders",
            "/tables",
            "/products",
            "/categories",
            "/customers",
            "/users",
            "/dashboard",
            "/reports",
            "/inventory",
            "/promotions",
            "/login"
    })
    public String forwardClientRoutes() {
        return "forward:/index.html";
    }
}
