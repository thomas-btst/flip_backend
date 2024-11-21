package com.flip.skateshop.config

import com.flip.skateshop.domain.RoleEnum
import com.flip.skateshop.domain.RoleEnum.ADMIN
import com.flip.skateshop.repository.UserRepository
import com.flip.skateshop.security.JwtClaimer
import com.nimbusds.jose.jwk.JWKSet
import com.nimbusds.jose.jwk.OctetSequenceKey
import com.nimbusds.jose.jwk.source.ImmutableJWKSet
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.convert.converter.Converter
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
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.jwt.JwtEncoder
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverterAdapter
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers.pathMatchers
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
        userRepository: UserRepository,
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
                    ),
                    permitAll,
                )
                authorize("/auth/**", permitAll)
                authorize("/public/**", permitAll)
                authorize("/users/**", authenticated)
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
    fun jwtEncoder(): JwtEncoder = NimbusJwtEncoder(ImmutableJWKSet(JWKSet(OctetSequenceKey.Builder(secretKey).build())))

    @Bean
    fun passwordEncoder(): PasswordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder()
}
