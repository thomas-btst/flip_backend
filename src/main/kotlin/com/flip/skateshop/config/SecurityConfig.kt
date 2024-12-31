package com.flip.skateshop.config

import com.flip.skateshop.domain.RoleEnum
import com.flip.skateshop.domain.RoleEnum.ADMIN
import com.flip.skateshop.interfaces.repository.UserRepositoryInterface
import com.flip.skateshop.security.JwtClaimer
import com.nimbusds.jose.jwk.JWKSet
import com.nimbusds.jose.jwk.OctetSequenceKey
import com.nimbusds.jose.jwk.source.ImmutableJWKSet
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.convert.converter.Converter
import org.springframework.http.HttpMethod
import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.authentication.ReactiveAuthenticationManager
import org.springframework.security.authentication.UserDetailsRepositoryReactiveAuthenticationManager
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.AuthorizeExchangeDsl
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.config.web.server.invoke
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.ReactiveUserDetailsPasswordService
import org.springframework.security.core.userdetails.ReactiveUserDetailsService
import org.springframework.security.crypto.factory.PasswordEncoderFactories
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.jwt.JwtEncoder
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverterAdapter
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers.pathMatchers
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.reactive.CorsConfigurationSource
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource
import reactor.core.publisher.Mono
import javax.crypto.spec.SecretKeySpec

@Configuration
@EnableWebFluxSecurity
class SecurityConfig(
    skateshopProperties: SkateshopProperties,
) {
    private val secretKey =
        SecretKeySpec(
            skateshopProperties.security.jwt.secretKey
                .toByteArray(),
            "HMACSHA256",
        )

    @Bean
    fun springSecurityFilterChain(
        http: ServerHttpSecurity,
        jwtClaimer: JwtClaimer,
        userRepository: UserRepositoryInterface,
    ): SecurityWebFilterChain =
        http {
            securityMatcher(pathMatchers("/**"))
            cors {}
            x509 {}
            csrf { disable() }
            authorizeExchange {
                authorize(
                    pathMatchers(
                        "/v3/api-docs/**",
                        "/swagger-ui.html",
                        "/swagger-ui/**",
                        "/webjars/swagger-ui.html",
                        "/webjars/swagger-ui/**",
                        "/auth/token/refresh",
                    ),
                    permitAll,
                )
                authorize(pathMatchers(HttpMethod.GET, "/users/{id}"), hasAuthority(ADMIN))
                authorize(pathMatchers(HttpMethod.GET, "/users/limit/{limit}/page/{page}"), hasAuthority(ADMIN))
                authorize(pathMatchers(HttpMethod.GET, "/commands/users/{userId}"), hasAuthority(ADMIN))
                authorize(pathMatchers(HttpMethod.GET, "/commands/limit/{limit}/page/{page}"), hasAuthority(ADMIN))
                authorize(pathMatchers(HttpMethod.GET, "/commands/admin/{commandId}"), hasAuthority(ADMIN))
                authorize(pathMatchers(HttpMethod.PATCH, "/commands/{commandId}/status"), hasAuthority(ADMIN))
                authorize(pathMatchers(HttpMethod.GET, "/commands/stats"), hasAuthority(ADMIN))
                authorize("/payment/**", authenticated)
                authorize("/auth/**", permitAll)
                authorize("/public/**", permitAll)
                authorize("/users/**", authenticated)
                authorize("/carts/**", authenticated)
                authorize("/commands/**", authenticated)
                authorize("/products/**", hasAuthority(ADMIN))
                authorize("/", permitAll)
                authorize(anyExchange, denyAll)
            }
            oauth2ResourceServer {
                jwt {
                    jwtDecoder = jwtDecoder()
                    jwtAuthenticationConverter = grantedAuthoritiesExtractor(jwtClaimer)
                }
            }
        }

    fun AuthorizeExchangeDsl.hasAuthority(role: RoleEnum) = hasAuthority(role.name)

    fun grantedAuthoritiesExtractor(jwtClaimer: JwtClaimer): Converter<Jwt, Mono<AbstractAuthenticationToken>> {
        val jwtAuthenticationConverter = JwtAuthenticationConverter()
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter { jwt ->
            jwtClaimer.claimAuthorities(jwt).map { SimpleGrantedAuthority(it) }
        }
        return ReactiveJwtAuthenticationConverterAdapter(jwtAuthenticationConverter)
    }

    @Bean
    fun reactiveAuthenticationManager(
        reactiveUserDetailsService: ReactiveUserDetailsService,
        reactiveUserDetailsPasswordService: ReactiveUserDetailsPasswordService,
    ): ReactiveAuthenticationManager =
        UserDetailsRepositoryReactiveAuthenticationManager(reactiveUserDetailsService).apply {
            setUserDetailsPasswordService(reactiveUserDetailsPasswordService)
        }

    @Bean
    fun jwtDecoder(): ReactiveJwtDecoder = NimbusReactiveJwtDecoder.withSecretKey(secretKey).build()

    @Bean
    @Qualifier("no_expiration")
    fun jwtDecoderWithoutExp(): ReactiveJwtDecoder {
        val jwtDecoder = NimbusReactiveJwtDecoder.withSecretKey(secretKey).build()
        jwtDecoder.setJwtValidator { OAuth2TokenValidatorResult.success() }
        return jwtDecoder
    }

    @Bean
    fun jwtEncoder(): JwtEncoder = NimbusJwtEncoder(ImmutableJWKSet(JWKSet(OctetSequenceKey.Builder(secretKey).build())))

    @Bean
    fun passwordEncoder(): PasswordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder()

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource =
        UrlBasedCorsConfigurationSource().apply {
            registerCorsConfiguration(
                "/**",
                CorsConfiguration().apply {
                    allowedOrigins = listOf("*")
                    allowedMethods = listOf("POST", "GET", "PUT", "DELETE", "PATCH")
                    allowedHeaders = listOf("Origin", "Content-Type", "Accept", "Authorization")
                    addExposedHeader("Location")
                },
            )
        }
}
