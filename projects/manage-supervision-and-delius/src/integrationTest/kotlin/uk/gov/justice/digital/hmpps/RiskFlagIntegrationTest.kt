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
import uk.gov.justice.digital.hmpps.api.model.risk.PersonRiskFlag
import uk.gov.justice.digital.hmpps.api.model.risk.PersonRiskFlags
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator.DEREGISTRATION_1
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator.OVERVIEW
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator.REGISTRATION_2
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator.REGISTRATION_3
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator.REGISTRATION_REVIEW_2
import uk.gov.justice.digital.hmpps.service.toRiskFlag
import uk.gov.justice.digital.hmpps.service.toRiskFlagRemoval
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.contentAsJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = RANDOM_PORT)
internal class RiskFlagIntegrationTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Test
    fun `all risk flags are returned`() {

        val person = OVERVIEW
        val res = mockMvc
            .perform(get("/risk-flags/${person.crn}").withToken())
            .andExpect(status().isOk)
            .andReturn().response.contentAsJson<PersonRiskFlags>()
        assertThat(res.personSummary.crn, equalTo(person.crn))
        assertThat(res.riskFlags.size, equalTo(2))
        assertThat(res.riskFlags[1].description, equalTo(REGISTRATION_2.type.description))
        assertThat(res.riskFlags[1].mostRecentReviewDate, equalTo(REGISTRATION_REVIEW_2.date))
        assertThat(res.removedRiskFlags.size, equalTo(1))
        assertThat(
            res.removedRiskFlags[0], equalTo(
                REGISTRATION_3.toRiskFlag().copy(
                    mostRecentReviewDate = REGISTRATION_REVIEW_2.date, removalHistory = listOf(
                        DEREGISTRATION_1.toRiskFlagRemoval()
                    )
                )
            )
        )
    }

    @Test
    fun `individual risk flag is returned`() {

        val person = OVERVIEW
        val res = mockMvc
            .perform(get("/risk-flags/${person.crn}/${REGISTRATION_2.id}").withToken())
            .andExpect(status().isOk)
            .andReturn().response.contentAsJson<PersonRiskFlag>()
        assertThat(res.personSummary.crn, equalTo(person.crn))
        assertThat(res.riskFlag.description, equalTo(REGISTRATION_2.type.description))
        assertThat(res.riskFlag.mostRecentReviewDate, equalTo(REGISTRATION_REVIEW_2.date))
    }

    @Test
    fun `individual risk flag not found`() {

        val person = OVERVIEW
        mockMvc.perform(get("/risk-flags/${person.crn}/9999999").withToken()).andExpect(status().isNotFound)
    }
}
