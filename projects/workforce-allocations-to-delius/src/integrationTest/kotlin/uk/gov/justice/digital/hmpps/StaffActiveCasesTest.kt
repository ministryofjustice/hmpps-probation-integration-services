package uk.gov.justice.digital.hmpps

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.data.generator.StaffGenerator
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = RANDOM_PORT)
class StaffActiveCasesTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Test
    fun `successful response`() {
        val staff = StaffGenerator.DEFAULT
        val person = PersonGenerator.DEFAULT
        mockMvc
            .perform(
                post("/staff/${StaffGenerator.DEFAULT.code}/active-cases").withToken()
                    .withJson(listOf(person.crn))
            )
            .andExpect(status().is2xxSuccessful)
            .andExpect(jsonPath("$.code").value(staff.code))
            .andExpect(jsonPath("$.name.forename").value(staff.forename))
            .andExpect(jsonPath("$.name.surname").value(staff.surname))
            .andExpect(jsonPath("$.grade").value("PSO"))
            .andExpect(jsonPath("$.cases[0].crn").value(person.crn))
            .andExpect(jsonPath("$.cases[0].name.forename").value(person.forename))
            .andExpect(jsonPath("$.cases[0].name.surname").value(person.surname))
            .andExpect(jsonPath("$.cases[0].type").value("CUSTODY"))
    }
}
