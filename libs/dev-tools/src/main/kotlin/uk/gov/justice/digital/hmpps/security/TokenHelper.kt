package uk.gov.justice.digital.hmpps.security

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.jwt.JwtDecoder
import java.time.Duration
import java.time.Instant

@Configuration
@EnableConfigurationProperties(TokenHelper.JwtProperties::class)
class TokenHelper {
    companion object {
        const val TOKEN = "token"
    }

    @ConfigurationProperties(prefix = "jwt")
    class JwtProperties {
        var authorities: List<String> = emptyList()
    }

    @Bean
    fun jwt(jwtProperties: JwtProperties): Jwt = Jwt
        .withTokenValue(TOKEN)
        .issuedAt(Instant.now())
        .expiresAt(Instant.now().plus(Duration.ofDays(1)))
        .header("alg", "none")
        .claim("sub", "probation-integration-dev")
        .claim("authorities", jwtProperties.authorities)
        .build()

    @Bean
    @Primary
    fun jwtDecoder(jwt: Jwt) = JwtDecoder { jwt }
}
