package com.flip.skateshop.config

import org.springframework.boot.autoconfigure.mail.MailProperties
import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("spring")
class SpringProperties(
    val mail: MailProperties,
)
