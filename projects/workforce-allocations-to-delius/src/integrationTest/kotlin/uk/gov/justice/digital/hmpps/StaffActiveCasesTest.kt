package uk.gov.justice.digital.hmpps

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.data.generator.StaffGenerator
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.json
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = RANDOM_PORT)
class StaffActiveCasesTest @Autowired constructor(
    private val mockMvc: MockMvc
) {

    @Test
    fun `successful response`() {
        val staff = StaffGenerator.DEFAULT
        val person = PersonGenerator.DEFAULT
        mockMvc.post("/staff/${StaffGenerator.DEFAULT.code}/active-cases") {
            withToken()
            json = listOf(person.crn)
        }
            .andExpect {
                status { is2xxSuccessful() }
                jsonPath("$.code") { value(staff.code) }
                jsonPath("$.name.forename") { value(staff.forename) }
                jsonPath("$.name.surname") { value(staff.surname) }
                jsonPath("$.grade") { value("PSO") }
                jsonPath("$.cases[0].crn") { value(person.crn) }
                jsonPath("$.cases[0].name.forename") { value(person.forename) }
                jsonPath("$.cases[0].name.surname") { value(person.surname) }
                jsonPath("$.cases[0].type") { value("CUSTODY") }
                jsonPath("$.cases[0].initialAllocationDate") { value("2022-06-24") }
            }
    }
}
