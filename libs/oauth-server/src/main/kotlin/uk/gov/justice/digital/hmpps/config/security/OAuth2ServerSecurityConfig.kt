package uk.gov.justice.digital.hmpps.config.security

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.web.SecurityFilterChain

@Configuration
class OAuth2ServerSecurityConfig : OAuth2ClientSecurityConfig() {
    @Bean
    fun configureResourceServer(http: HttpSecurity): SecurityFilterChain {
        filterChain(http)
            .oauth2ResourceServer {
                it.jwt { jwt -> jwt.jwtAuthenticationConverter(AuthAwareTokenConverter()) }
            }
        return http.build()
    }
}
