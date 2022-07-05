package uk.gov.justice.digital.hmpps.config

import feign.RequestInterceptor
import org.springframework.beans.factory.annotation.Value
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
import uk.gov.justice.digital.hmpps.integrations.delius.audit.service.UserService
import uk.gov.justice.digital.hmpps.integrations.prison.PrisonCaseNotesClient

@Configuration
@EnableFeignClients(clients = [PrisonCaseNotesClient::class])
class FeignConfig(
    private val authorizedClientManager: OAuth2AuthorizedClientManager,
    @Value("\${delius.db.username:prison-case-notes-to-probation}") val deliusDbName: String,
    private val userService: UserService
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
        val auth = SecurityContextHolder.getContext().authentication
        if (auth == null || auth.principal == null) {
            val user = userService.findUser(deliusDbName)
            SecurityContextHolder.getContext().authentication =
                AnonymousAuthenticationToken(
                    "hmpps-auth",
                    ServicePrincipal(deliusDbName, user?.id),
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
