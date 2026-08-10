package uk.gov.justice.digital.hmpps

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator.DEFAULT_PERSON
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator.NO_EVENTS_PERSON
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = RANDOM_PORT)
internal class CaseControllerIntegrationTest @Autowired constructor(private val mockMvc: MockMvc) {

    @Test
    fun `returns unauthorized when no token is supplied`() {
        mockMvc.get("/arns/${DEFAULT_PERSON.crn}")
            .andExpect { status { isUnauthorized() } }
    }

    @Test
    fun `get case data for CRN`() {
        mockMvc.get("/arns/${DEFAULT_PERSON.crn}") {
            withToken()
        }.andExpect {
            status { isOk() }
            jsonPath("$.crn") { value(DEFAULT_PERSON.crn) }
            jsonPath("$.dateOfBirth") { value("1984-05-12") }
            jsonPath("$.gender") { value("Male") }
            jsonPath("$.offences.length()") { value(2) }
            jsonPath("$.offences[0].mainOffence") { value("Murder") }
            jsonPath("$.offences[0].sentenceStartDate") { value("2026-03-01") }
            jsonPath("$.offences[0].convictionDate") { value("2026-01-10") }
            jsonPath("$.offences[1].mainOffence") { value("Second Offence") }
            jsonPath("$.offences[1].sentenceStartDate") { value("2026-04-15") }
            jsonPath("$.offences[1].convictionDate") { value("2026-02-20") }
        }
    }

    @Test
    fun `returns empty offences for a person with no events`() {
        mockMvc.get("/arns/${NO_EVENTS_PERSON.crn}") {
            withToken()
        }.andExpect {
            status { isOk() }
            jsonPath("$.crn") { value(NO_EVENTS_PERSON.crn) }
            jsonPath("$.dateOfBirth") { value("1984-05-12") }
            jsonPath("$.gender") { value("Male") }
            jsonPath("$.offences") { isArray() }
            jsonPath("$.offences.length()") { value(0) }
        }
    }

    @Test
    fun `not found when CRN does not exist`() {
        mockMvc.get("/arns/Z123456") {
            withToken()
        }.andExpect {
            status { isNotFound() }
            jsonPath("$.message") { value("Person with crn of Z123456 not found") }
        }
    }
}
