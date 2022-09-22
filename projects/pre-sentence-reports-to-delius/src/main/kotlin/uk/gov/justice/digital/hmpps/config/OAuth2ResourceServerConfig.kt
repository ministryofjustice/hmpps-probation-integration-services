package uk.gov.justice.digital.hmpps.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.web.SecurityFilterChain
import uk.gov.justice.digital.hmpps.config.security.OAuth2ClientSecurityConfig

@Configuration
class OAuth2ResourceServerConfig : OAuth2ClientSecurityConfig() {
    @Bean
    fun configureResourceServer(http: HttpSecurity): SecurityFilterChain {
        filterChain(http)
            .oauth2ResourceServer {
                it.jwt().jwtAuthenticationConverter(AuthAwareTokenConverter())
            }
        return http.build()
    }
}
