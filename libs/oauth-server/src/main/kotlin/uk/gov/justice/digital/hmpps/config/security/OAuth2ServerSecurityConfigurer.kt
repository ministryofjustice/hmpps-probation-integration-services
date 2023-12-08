package uk.gov.justice.digital.hmpps.config.security

import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken

@Configuration
class OAuth2ServerSecurityConfigurer : SecurityConfigurer {
    override fun configure(http: HttpSecurity): HttpSecurity {
        http.oauth2ResourceServer { oauth2 ->
            oauth2.jwt { jwt -> jwt.jwtAuthenticationConverter { JwtAuthenticationToken(it, it.authorities) } }
        }
        return http
    }

    private val Jwt.authorities
        get() = getClaim<Collection<String>>("authorities")?.map(::SimpleGrantedAuthority) ?: listOf()
}
