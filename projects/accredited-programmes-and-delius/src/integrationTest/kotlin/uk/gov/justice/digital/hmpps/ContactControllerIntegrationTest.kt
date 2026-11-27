package uk.gov.justice.digital.hmpps

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.delete
import com.github.tomakehurst.wiremock.client.WireMock.deleteRequestedFor
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.delete
import uk.gov.justice.digital.hmpps.data.TestData

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = RANDOM_PORT)
internal class ContactControllerIntegrationTest @Autowired constructor(
    private val mockMvc: MockMvc,
    private val wireMockServer: WireMockServer,
) {
    @BeforeEach
    fun resetWireMockRequests() {
        wireMockServer.resetRequests()
    }

    @Test
    fun `hard delete removes documents from alfresco`() {
        TestData.CONTACT_TO_HARD_DELETE_DOCUMENTS.forEach {
            wireMockServer.stubFor(
                delete(urlEqualTo("/alfresco/deletehard/${it.id}"))
                    .willReturn(aResponse().withStatus(204)),
            )
        }

        mockMvc.delete("/contacts/delete/${TestData.CONTACT_TO_HARD_DELETE.id}") {
            with(jwt().authorities(adminAuthority))
        }.andExpect { status { isOk() } }

        TestData.CONTACT_TO_HARD_DELETE_DOCUMENTS.forEach { document ->
            wireMockServer.verify(
                1,
                deleteRequestedFor(urlEqualTo("/alfresco/deletehard/${document.id}")),
            )
        }
        wireMockServer.verify(
            0,
            deleteRequestedFor(urlEqualTo("/alfresco/deletehard/${TestData.CONTACT_TO_PRESERVE_DOCUMENT.id}")),
        )
    }

    @Test
    fun `hard delete returns not found for an unknown contact`() {
        mockMvc.delete("/contacts/delete/-1") {
            with(jwt().authorities(adminAuthority))
        }.andExpect { status { isNotFound() } }
    }

    private val adminAuthority = SimpleGrantedAuthority("ROLE_PROBATION_API__ACCREDITED_PROGRAMMES__CASE_ADMIN")
}
