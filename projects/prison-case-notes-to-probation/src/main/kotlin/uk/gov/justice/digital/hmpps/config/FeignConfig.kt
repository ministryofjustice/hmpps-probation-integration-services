package uk.gov.justice.digital.hmpps.config

import feign.RequestInterceptor
import org.springframework.cloud.openfeign.EnableFeignClients
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders
import org.springframework.security.authentication.AnonymousAuthenticationToken
import org.springframework.security.core.authority.AuthorityUtils
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager
import org.springframework.security.oauth2.core.OAuth2AccessToken
import uk.gov.justice.digital.hmpps.config.security.ServicePrincipal
import uk.gov.justice.digital.hmpps.integrations.prison.PrisonCaseNotesClient

@Configuration
@EnableFeignClients(clients = [PrisonCaseNotesClient::class])
class FeignConfig(
    private val authorizedClientManager: OAuth2AuthorizedClientManager,
    private val servicePrincipal: ServicePrincipal
) {
    companion object {
        const val REGISTRATION_ID = "prison-case-notes"
    }

    @Bean
    fun requestInterceptor() = RequestInterceptor { template ->
        getAccessToken()?.tokenValue?.let {
            template.header(HttpHeaders.AUTHORIZATION, "Bearer $it")
        }
    }

    private fun getAccessToken(): OAuth2AccessToken? {
        if (SecurityContextHolder.getContext().authentication == null) {
            SecurityContextHolder.getContext().authentication =
                AnonymousAuthenticationToken(
                    "hmpps-auth",
                    servicePrincipal,
                    AuthorityUtils.createAuthorityList(ServicePrincipal.AUTHORITY)
                )
        }

        val request = OAuth2AuthorizeRequest
            .withClientRegistrationId(REGISTRATION_ID)
            .principal(SecurityContextHolder.getContext().authentication)
            .build()
        return authorizedClientManager.authorize(request)?.accessToken
    }
}
