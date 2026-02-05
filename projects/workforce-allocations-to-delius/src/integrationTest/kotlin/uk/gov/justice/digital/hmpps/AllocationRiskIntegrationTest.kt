package uk.gov.justice.digital.hmpps

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.data.generator.RegisterTypeGenerator
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = RANDOM_PORT)
class AllocationRiskIntegrationTest @Autowired constructor(
    private val mockMvc: MockMvc
) {

    @Test
    fun `successful response`() {
        val person = PersonGenerator.DEFAULT
        mockMvc.get("/allocation-demand/${person.crn}/risk") {
            withToken()
        }
            .andExpect {
                status { is2xxSuccessful() }
                jsonPath("$.crn") { value(person.crn) }
                jsonPath("$.name.forename") { value(person.forename) }
                jsonPath("$.name.surname") { value(person.surname) }
                jsonPath("$.ogrs.score") { value(88L) }
                jsonPath("$.activeRegistrations[0].description") { value(RegisterTypeGenerator.DEFAULT.description) }
            }
    }
}
