package uk.gov.justice.digital.hmpps

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
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
        val username = "J0nSm17h"
        val result = mockMvc.get("/basic-details/$crn/$username") { withToken() }
            .andExpect { status { isOk() } }
            .andReturn().response.contentAsJson<PersonDetails>()
        assertThat(result.addresses.size == 1)
        assertThat(result.name.surname == "Jones")
        assertThat(result.title == "Mr")
        assertThat(result.name.middleName == "Tom Billy")
    }

    @Test
    fun `person with no home area throws bad request`() {
        val crn = PersonGenerator.PERSON_NO_MAIN_ADDRESS.crn
        val username = "NoHomeArea"
        mockMvc.get("/basic-details/$crn/$username") { withToken() }
            .andExpect { status { isBadRequest() } }
    }

    @Test
    fun `person with crn not found throws not found`() {
        val crn = "X123458"
        val username = "J0nSm17h"
        mockMvc.get("/basic-details/$crn") { withToken() }
            .andExpect { status { isNotFound() } }
    }
}
