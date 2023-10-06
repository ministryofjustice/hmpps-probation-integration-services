package uk.gov.justice.digital.hmpps

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.github.tomakehurst.wiremock.WireMockServer
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.data.generator.ProbationCaseGenerator
import uk.gov.justice.digital.hmpps.data.generator.ProbationCaseGenerator.COM_TEAM
import uk.gov.justice.digital.hmpps.model.CaseDetail
import uk.gov.justice.digital.hmpps.model.CaseSummaries
import uk.gov.justice.digital.hmpps.model.Ldu
import uk.gov.justice.digital.hmpps.model.Name
import uk.gov.justice.digital.hmpps.model.Team
import uk.gov.justice.digital.hmpps.security.withOAuth2Token
import java.time.LocalDate

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = RANDOM_PORT)
class ProbationCaseIntegrationTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var wireMockServer: WireMockServer

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @Test
    fun `case summaries are correctly returned`() {
        val complex = ProbationCaseGenerator.CASE_COMPLEX
        val simple = ProbationCaseGenerator.CASE_SIMPLE
        val response = mockMvc
            .perform(
                post("/probation-cases/summaries")
                    .contentType("application/json")
                    .content(objectMapper.writeValueAsString(listOf(complex.crn, simple.crn)))
                    .withOAuth2Token(wireMockServer)
            )
            .andExpect(status().isOk)
            .andReturn().response.contentAsString

        val summaries = objectMapper.readValue<CaseSummaries>(response)
        assertThat(summaries.cases.size, equalTo(2))
        assertThat(summaries.cases.map { it.crn }.sorted(), equalTo(listOf(complex.crn, simple.crn).sorted()))
        val complexCase = summaries.cases.first { it.crn == complex.crn }
        assertThat(complexCase.name, equalTo(Name("James", "Brown", listOf("John", "Jack"))))
        assertThat(complexCase.dateOfBirth, equalTo(LocalDate.of(1979, 3, 12)))
        assertTrue(complexCase.currentExclusion)
        assertTrue(complexCase.currentRestriction)
    }

    @Test
    fun `case details are correctly returned`() {
        val case = ProbationCaseGenerator.CASE_COMPLEX
        val response = mockMvc
            .perform(
                get("/probation-cases/${case.crn}/details")
                    .contentType("application/json")
                    .withOAuth2Token(wireMockServer)
            )
            .andExpect(status().isOk)
            .andReturn().response.contentAsString

        val detail = objectMapper.readValue<CaseDetail>(response)
        assertThat(detail.case.name, equalTo(Name("James", "Brown", listOf("John", "Jack"))))
        assertThat(detail.case.dateOfBirth, equalTo(LocalDate.of(1979, 3, 12)))
        assertTrue(detail.case.currentExclusion)
        assertTrue(detail.case.currentRestriction)
        assertThat(
            detail.case.manager.team,
            equalTo(Team(COM_TEAM.code, COM_TEAM.description, Ldu(COM_TEAM.ldu.code, COM_TEAM.ldu.description)))
        )
        assertThat(detail.mappaDetail?.category, equalTo(3))
        assertThat(detail.mappaDetail?.level, equalTo(2))
        assertThat(detail.registrations.map { it.description }, equalTo(listOf("Description of ARSO")))
        val mainOffence = detail.offences.first { it.main }
        assertThat(mainOffence.description, equalTo("Offence One"))
        val otherOffence = detail.offences.first { !it.main }
        assertThat(otherOffence.description, equalTo("Offence Two"))
    }
}
