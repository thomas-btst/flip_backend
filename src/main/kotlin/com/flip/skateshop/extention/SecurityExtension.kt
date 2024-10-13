package com.flip.skateshop.extention

import com.flip.skateshop.domain.RoleEnum
import org.springframework.security.config.web.server.AuthorizeExchangeDsl

fun AuthorizeExchangeDsl.hasAuthority(authority: RoleEnum) = hasAuthority(authority.name)