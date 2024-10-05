package com.flip.skateshop.config

import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class WebConfig(
    val skateshopProperties: SkateshopProperties,
) : WebMvcConfigurer {
    override fun addCorsMappings(registry: CorsRegistry) {
        registry.addMapping("/api/**")
            .allowedOrigins(skateshopProperties.cors.allowed.origin)
            .allowedMethods("POST", "GET", "PUT", "DELETE", "OPTIONS", "PATCH")
            .allowedHeaders("Origin", "Content-Type", "Accept", "Authorization")
    }
}