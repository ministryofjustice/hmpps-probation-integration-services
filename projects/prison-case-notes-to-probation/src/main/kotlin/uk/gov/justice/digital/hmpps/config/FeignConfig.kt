package uk.gov.justice.digital.hmpps.config

import feign.RequestInterceptor
import feign.Response
import feign.codec.ErrorDecoder
import org.springframework.cloud.openfeign.EnableFeignClients
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.security.authentication.AnonymousAuthenticationToken
import org.springframework.security.core.authority.AuthorityUtils
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager
import org.springframework.security.oauth2.core.OAuth2AccessToken
import org.springframework.web.server.ResponseStatusException
import uk.gov.justice.digital.hmpps.config.security.ServicePrincipal
import uk.gov.justice.digital.hmpps.integrations.nomis.NomisClient
import java.nio.charset.StandardCharsets

@Configuration
@EnableFeignClients(clients = [NomisClient::class])
class FeignConfig(
    private val authorizedClientManager: OAuth2AuthorizedClientManager,
    private val servicePrincipal: ServicePrincipal
) {
    companion object {
        const val REGISTRATION_ID = "case-notes"
    }

    @Bean
    fun errorDecoder(): ErrorDecoder = FeignErrorDecoder()

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

class FeignErrorDecoder : ErrorDecoder {
    override fun decode(methodKey: String?, response: Response?): Exception =
        ResponseStatusException(
            HttpStatus.valueOf(response?.status() ?: 500),
            response?.body()?.asReader(StandardCharsets.UTF_8)?.readText()
        )
}
