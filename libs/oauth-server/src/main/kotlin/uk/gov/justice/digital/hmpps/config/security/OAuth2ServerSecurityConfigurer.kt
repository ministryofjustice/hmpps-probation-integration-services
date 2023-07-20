package uk.gov.justice.digital.hmpps.config.security

import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity

@Configuration
class OAuth2ServerSecurityConfigurer : SecurityConfigurer {
    override fun configure(http: HttpSecurity): HttpSecurity {
        http.oauth2ResourceServer {
            it.jwt { jwt -> jwt.jwtAuthenticationConverter(AuthAwareTokenConverter()) }
        }
        return http
    }
}
