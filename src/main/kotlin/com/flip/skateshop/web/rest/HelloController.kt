package com.flip.skateshop.web.rest

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class HelloController {
    @GetMapping("/")
    fun index(): String {
        return "Bienvenue chez Flip Skateshop!"
    }
}