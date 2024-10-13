package com.flip.skateshop.security.filter

import com.flip.skateshop.security.JwtClaimer
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono

class KeyFilter(
    private val jwtDecoder: ReactiveJwtDecoder,
    private val jwtClaimer: JwtClaimer,
) : WebFilter {
    companion object {
        const val AUTHORIZATION_HEADER = "Authorization"
        const val KEY_PREFIX = "Bearer "
    }

    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
        val keyToken = extractToken(exchange.request) ?: return chain.filter(exchange)

        return jwtDecoder.decode(keyToken).map { token ->
            PreAuthenticatedAuthenticationToken(
                token.subject,
                "",
                jwtClaimer.claimAuthorities(token).map { SimpleGrantedAuthority(it) })
        }.flatMap { token ->
            chain.filter(exchange).contextWrite {
                ReactiveSecurityContextHolder.withAuthentication(token)
            }
        }
    }

    private fun extractToken(request: ServerHttpRequest): String? {
        val authorization = request.headers[AUTHORIZATION_HEADER]?.firstOrNull()

        if (authorization != null && authorization.startsWith(KEY_PREFIX)) {
            return authorization.substring(KEY_PREFIX.length, authorization.length)
        }
        return null
    }
}