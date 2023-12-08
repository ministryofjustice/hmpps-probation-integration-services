package uk.gov.justice.digital.hmpps.security

import com.fasterxml.jackson.databind.JsonNode
import com.github.tomakehurst.wiremock.WireMockServer
import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder
import org.springframework.web.client.RestTemplate

class TokenHelper(
    private val wireMockServer: WireMockServer,
) {
    fun getToken(): String {
        val authResponse =
            RestTemplate()
                .postForObject("http://localhost:${wireMockServer.port()}/auth/oauth/token", null, JsonNode::class.java)!!
        return authResponse["access_token"].asText()
    }
}

fun MockHttpServletRequestBuilder.withOAuth2Token(wireMockServer: WireMockServer) =
    this.header(AUTHORIZATION, "Bearer ${TokenHelper(wireMockServer).getToken()}")
