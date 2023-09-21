package uk.gov.justice.digital.hmpps.config.feign

import feign.RequestInterceptor
import feign.Retryer
import org.springframework.context.annotation.Bean
import org.springframework.http.HttpHeaders
import org.springframework.security.authentication.AnonymousAuthenticationToken
import org.springframework.security.core.authority.AuthorityUtils
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager
import org.springframework.security.oauth2.core.OAuth2AuthenticationException

abstract class FeignConfig(
    private val authorizedClientManager: OAuth2AuthorizedClientManager
) {

    abstract fun registrationId(): String

    @Bean
    open fun retryer() = Retryer.Default()

    @Bean
    open fun requestInterceptor() = RequestInterceptor { template ->
        template.header(HttpHeaders.AUTHORIZATION, "Bearer ${getAccessToken()}")
    }

    private fun getAccessToken(): String {
        val authentication = SecurityContextHolder.getContext().authentication ?: AnonymousAuthenticationToken(
            "hmpps-auth",
            "anonymous",
            AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS")
        )
        val request = OAuth2AuthorizeRequest
            .withClientRegistrationId(registrationId())
            .principal(authentication)
            .build()
        return authorizedClientManager.authorize(request)?.accessToken?.tokenValue
            ?: throw OAuth2AuthenticationException("Unable to retrieve access token")
    }
}
