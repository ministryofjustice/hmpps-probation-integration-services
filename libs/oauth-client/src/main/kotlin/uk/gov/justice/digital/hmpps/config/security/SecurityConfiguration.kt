package uk.gov.justice.digital.hmpps.config.security

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.InMemoryOAuth2AuthorizedClientService
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProviderBuilder
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.security.web.SecurityFilterChain

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
class SecurityConfiguration {

    @Bean
    fun oauthFilterChain(http: HttpSecurity): SecurityFilterChain {
        http.oauth2Client()
        return http.build()
    }

    @Bean
    fun authorizedClientManager(
        clientRegistration: ClientRegistrationRepository
    ): OAuth2AuthorizedClientManager {
        val service = InMemoryOAuth2AuthorizedClientService(clientRegistration)
        val authorizedClientManager = AuthorizedClientServiceOAuth2AuthorizedClientManager(clientRegistration, service)

        val authorizedClientProvider = OAuth2AuthorizedClientProviderBuilder
            .builder()
            .clientCredentials()
            .build()
        authorizedClientManager.setAuthorizedClientProvider(authorizedClientProvider)
        return authorizedClientManager
    }
}
