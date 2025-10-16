package uk.gov.justice.digital.hmpps

import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.api.model.*
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.data.generator.PersonManagerGenerator
import uk.gov.justice.digital.hmpps.data.generator.TeamGenerator
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.andExpectJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class FindPersonIntegrationTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Test
    fun `get person unauthorised`() {
        mockMvc.perform(get("/person/X123456?type=CRN"))
            .andExpect(status().isUnauthorized)
    }

    @ParameterizedTest
    @MethodSource("notFound")
    fun `get person no matching crn`(value: String, type: String) {
        mockMvc.perform(get("/person/$value?type=$type").withToken())
            .andExpect(status().isNotFound)
    }

    @ParameterizedTest
    @MethodSource("searchCriteria")
    fun `find person`(value: String, type: String) {
        val wanted = PersonGenerator.DEFAULT
        mockMvc.perform(get("/person/$value?type=$type").withToken())
            .andExpect(status().is2xxSuccessful)
            .andExpectJson(Person(wanted.crn, wanted.name(), CaseType.CUSTODY))
    }

    @Test
    fun `find reallocation details`() {
        val person = PersonGenerator.DEFAULT
        mockMvc.perform(get("/person/${person.crn}/reallocation-details").withToken())
            .andExpect(status().is2xxSuccessful)
            .andExpectJson(
                ReallocationDetails(
                    person.crn, person.name(), person.dateOfBirth, PersonManagerGenerator.DEFAULT.staff.toManager(
                        TeamGenerator.DEFAULT.code
                    ), true
                )
            )
    }

    companion object {
        @JvmStatic
        fun searchCriteria() = listOf(
            Arguments.of("X123456", "CRN"),
            Arguments.of("A1234YZ", "NOMS")
        )

        @JvmStatic
        fun notFound() = listOf(
            Arguments.of("Z999999", "CRN"),
            Arguments.of("Z9999AB", "NOMS")
        )
    }
}
