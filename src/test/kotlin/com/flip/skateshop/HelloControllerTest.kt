package com.flip.skateshop

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient

@SpringBootTest
@AutoConfigureMockMvc
class HelloControllerTest(
    @Autowired private val webTestClient: WebTestClient,
) {
    @Test
    @Throws(Exception::class)
    fun getHello() {
        webTestClient
            .get()
            .uri("/")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk
            .expectBody(String::class.java)
            .isEqualTo("Bienvenue chez Flip Skateshop!")
    }
}
