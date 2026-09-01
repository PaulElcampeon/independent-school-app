package com.paulo.independentschooldata.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class StaticController {

    @RequestMapping({
            "/",
            "/schools",
            "/send-schools",
            "/league-table",
            "/school-map",
            "/school-login",
            "/school-dashboard",
            "/about",
            "/contact",
            "/school/{uuid}",
            "/advisor-discovery"
    })
    public String forwardToSPA() {
        return "forward:/index.html";
    }
}
