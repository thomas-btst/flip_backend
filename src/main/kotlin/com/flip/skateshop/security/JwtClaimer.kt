package com.flip.skateshop.security

import com.flip.skateshop.config.SkateshopProperties
import com.flip.skateshop.domain.RoleEnum
import org.springframework.security.oauth2.jose.jws.MacAlgorithm
import org.springframework.security.oauth2.jwt.*
import org.springframework.stereotype.Component
import java.time.Instant

@Component
class JwtClaimer(
    private val jwtEncoder: JwtEncoder,
    properties: SkateshopProperties,
) {

    companion object {
        const val AUTHORITIES_KEY = "auth"
        const val AUTHORITIES_SEPARATOR = ","
    }

    private val tokenValidityInSecond = properties.security.jwt.tokenValidityInSeconds

    fun createToken(username: String, authorities: List<String>): String {
        val claims = JwtClaimsSet.builder()
            .subject(username)
            .claim(AUTHORITIES_KEY, authorities.joinToString(AUTHORITIES_SEPARATOR))
            .expiresAt(Instant.now().plusSeconds(tokenValidityInSecond))
            .build()
        return jwtEncoder.encode(
            JwtEncoderParameters.from(
                JwsHeader.with(MacAlgorithm.HS256).build(),
                claims
            )
        ).tokenValue
    }

    fun claimAuthorities(token: Jwt): List<String>{
        val authString = token.getClaimAsString(AUTHORITIES_KEY)
        if(authString == null || authString.isEmpty()) return emptyList()
        return authString.split(AUTHORITIES_SEPARATOR)
    }

}
