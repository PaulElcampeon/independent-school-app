package com.paulo.independentschooldata.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {

        registry.addViewController("/school-dashboard").setViewName("forward:/index.html");
        registry.addViewController("/admin-dashboard").setViewName("forward:/index.html");
        registry.addViewController("/admin/**").setViewName("forward:/index.html");
        registry.addViewController("/school-login").setViewName("forward:/index.html");
        registry.addViewController("/guides").setViewName("forward:/index.html");
        registry.addViewController("/reset-password").setViewName("forward:/index.html");
        registry.addViewController("/register-school").setViewName("forward:/index.html");
        registry.addViewController("/advisor-discovery").setViewName("forward:/index.html");
    }
}
