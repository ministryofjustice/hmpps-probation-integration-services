package uk.gov.justice.digital.hmpps.config.security

import org.springframework.boot.autoconfigure.security.ConditionalOnDefaultWebSecurity
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.InMemoryOAuth2AuthorizedClientService
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProviderBuilder
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository

@Configuration
@ConditionalOnDefaultWebSecurity
class OAuth2ClientSecurityConfig {
    @Bean
    fun authorizedClientManager(
        clientRegistration: ClientRegistrationRepository,
    ): OAuth2AuthorizedClientManager {
        val service = InMemoryOAuth2AuthorizedClientService(clientRegistration)
        val authorizedClientManager = AuthorizedClientServiceOAuth2AuthorizedClientManager(clientRegistration, service)

        val authorizedClientProvider =
            OAuth2AuthorizedClientProviderBuilder
                .builder()
                .clientCredentials()
                .build()
        authorizedClientManager.setAuthorizedClientProvider(authorizedClientProvider)
        return authorizedClientManager
    }
}

@Configuration
class OAuthClientSecurityConfigurer : SecurityConfigurer {
    override fun configure(http: HttpSecurity): HttpSecurity {
        http.oauth2Client {}
        return http
    }
}
