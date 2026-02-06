package uk.gov.justice.digital.hmpps

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.model.PersonDetails
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.contentAsJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = RANDOM_PORT)
internal class BasicDetailsIntegrationTest @Autowired constructor(
    private val mockMvc: MockMvc
) {
    @Test
    fun `basic details returned with valid crn`() {
        val crn = PersonGenerator.DEFAULT_PERSON.crn
        val result = mockMvc.get("/basic-details/$crn") { withToken() }
            .andExpect { status { isOk() } }
            .andReturn().response.contentAsJson<PersonDetails>()
        assertThat(result.addresses.size == 1)
        assertThat(result.addresses[0].status == "MAIN")
        assertThat(result.name.surname == "Jones")
        assertThat(result.title == "Mr")
        assertThat(result.name.middleName == "Tom Billy")
    }

    @Test
    fun `person with no main address throws not found`() {
        val crn = PersonGenerator.PERSON_NO_MAIN_ADDRESS.crn
        mockMvc.get("/basic-details/$crn") { withToken() }
            .andExpect { status { isNotFound() } }
    }

    @Test
    fun `person with crn not found throws not found`() {
        val crn = "X123458"
        mockMvc.get("/basic-details/$crn") { withToken() }
            .andExpect { status { isNotFound() } }
    }
}
