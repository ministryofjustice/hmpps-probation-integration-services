package uk.gov.justice.digital.hmpps

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.api.model.compliance.PersonCompliance
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator.OVERVIEW
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.contentAsJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = RANDOM_PORT)
internal class ComplianceIntegrationTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Test
    fun `compliance details are returned`() {
        val person = OVERVIEW
        val res = mockMvc
            .perform(get("/compliance/${person.crn}").withToken())
            .andExpect(status().isOk)
            .andReturn().response.contentAsJson<PersonCompliance>()
        assertThat(res.personSummary.name.forename, equalTo(person.forename))
        assertThat(
            res.previousOrders.breaches,
            equalTo(2)
        )
        assertThat(res.currentSentences[0].activeBreach?.status, equalTo("An NSI Status"))
        assertThat(res.currentSentences[0].compliance.breachStarted, equalTo(true))
        assertThat(res.currentSentences[0].compliance.currentBreaches, equalTo(1))
        assertThat(res.currentSentences[0].activity.waitingForEvidenceCount, equalTo(1))
        assertThat(res.currentSentences[0].activity.compliedAppointmentsCount, equalTo(2))
        assertThat(res.currentSentences[0].activity.outcomeNotRecordedCount, equalTo(4))
    }

    @Test
    fun `not found status returned`() {
        mockMvc
            .perform(get("/compliance/X123456").withToken())
            .andExpect(status().isNotFound)
    }

    @Test
    fun `unauthorized status returned`() {
        mockMvc
            .perform(get("/compliance/X123456"))
            .andExpect(status().isUnauthorized)
    }
}
