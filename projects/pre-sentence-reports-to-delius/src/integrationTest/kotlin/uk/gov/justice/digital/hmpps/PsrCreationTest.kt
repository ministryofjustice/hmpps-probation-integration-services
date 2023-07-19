package uk.gov.justice.digital.hmpps

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.tomakehurst.wiremock.WireMockServer
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.data.generator.CourtReportGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.document.DocumentRepository
import uk.gov.justice.digital.hmpps.security.withOAuth2Token

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class PsrCreationTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @Autowired
    lateinit var wireMockserver: WireMockServer

    @Autowired
    lateinit var documentRepository: DocumentRepository

    @Test
    fun `create PSR unauthorised`() {
        val courtReport = CourtReportGenerator.DEFAULT
        mockMvc.perform(
            MockMvcRequestBuilders.post("/probation-cases/${courtReport.person.crn}/court-reports/${courtReport.id}/pre-sentence-reports")
                .content("{}")
        ).andExpect(status().isUnauthorized)
    }

    @Test
    fun `create PSR authorised`() {
        val courtReport = CourtReportGenerator.DEFAULT
        mockMvc.perform(
            MockMvcRequestBuilders.post("/probation-cases/${courtReport.person.crn}/court-reports/${courtReport.id}/pre-sentence-reports")
                .withOAuth2Token(wireMockserver)
                .contentType("application/json")
                .content("{\"type\": \"cr-type\"}")
        ).andExpect(status().isCreated)

        val doc = documentRepository.findAll()
            .firstOrNull { it.alfrescoId == "ebbb0d9c-1db0-4bda-918d-0cb168e323c4" && it.courtReportId == courtReport.id }
        assertNotNull(doc)
        assertThat(doc?.externalReference, equalTo("urn:uk:gov:hmpps:pre-sentence-service:report:f9b09fcf-39c0-4008-8b43-e616ddfd918c"))
    }
}
