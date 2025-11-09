package com.example.analyzer.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.*;

@Configuration
public class WebConfig implements WebMvcConfigurer {
  @Override
  public void addCorsMappings(CorsRegistry registry) {
    registry.addMapping("/api/**")
      .allowedOrigins(
        "http://localhost:5500",
        "http://127.0.0.1:5500",
        "https://beastsavager.github.io"
      )
      .allowedMethods("GET","POST","PUT","DELETE","OPTIONS")
      .allowedHeaders("*");
  }
}
