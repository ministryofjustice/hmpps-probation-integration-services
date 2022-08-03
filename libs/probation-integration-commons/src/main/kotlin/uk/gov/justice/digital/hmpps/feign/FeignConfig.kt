package uk.gov.justice.digital.hmpps.feign

import feign.RequestInterceptor
import org.springframework.context.annotation.Bean
import org.springframework.http.HttpHeaders
import org.springframework.security.authentication.AnonymousAuthenticationToken
import org.springframework.security.core.authority.AuthorityUtils
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager
import org.springframework.security.oauth2.core.OAuth2AccessToken
import uk.gov.justice.digital.hmpps.config.ServiceContext
import uk.gov.justice.digital.hmpps.config.security.ServicePrincipal

abstract class FeignConfig(
    private val authorizedClientManager: OAuth2AuthorizedClientManager
) {

    abstract fun registrationId(): String

    @Bean
    open fun requestInterceptor() = RequestInterceptor { template ->
        getAccessToken()?.tokenValue?.let {
            template.header(HttpHeaders.AUTHORIZATION, "Bearer $it")
        }
    }

    private fun getAccessToken(): OAuth2AccessToken? {
        val auth = SecurityContextHolder.getContext().authentication
        if (auth == null || auth.principal == null) {
            SecurityContextHolder.getContext().authentication =
                AnonymousAuthenticationToken(
                    "hmpps-auth",
                    ServiceContext.servicePrincipal(),
                    AuthorityUtils.createAuthorityList(ServicePrincipal.AUTHORITY)
                )
        }

        val request = OAuth2AuthorizeRequest
            .withClientRegistrationId(registrationId())
            .principal(SecurityContextHolder.getContext().authentication)
            .build()
        return authorizedClientManager.authorize(request)?.accessToken
    }
}
