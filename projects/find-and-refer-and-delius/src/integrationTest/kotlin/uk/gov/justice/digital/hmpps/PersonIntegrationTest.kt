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
import uk.gov.justice.digital.hmpps.advice.ErrorResponse
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.model.PersonResponse
import uk.gov.justice.digital.hmpps.model.ProbationDeliveryUnit
import uk.gov.justice.digital.hmpps.service.toPersonResponse
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.contentAsJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = RANDOM_PORT)
internal class PersonIntegrationTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Test
    fun `find person with valid crn returns a success response`() {
        val response = mockMvc
            .perform(get("/person/find/X123456").withToken())
            .andExpect(status().isOk)
            .andReturn().response.contentAsJson<PersonResponse>()

        assertThat(
            response,
            equalTo(
                PersonGenerator.PERSON_1.toPersonResponse(
                    ProbationDeliveryUnit(
                        code = "A",
                        description = "Test PDU"
                    ), "Custody"
                )
            )
        )
    }

    @Test
    fun `find person with valid prisoner number returns a success response`() {
        val response = mockMvc
            .perform(get("/person/find/A4321BA").withToken())
            .andExpect(status().isOk)
            .andReturn().response.contentAsJson<PersonResponse>()

        assertThat(response, equalTo(PersonGenerator.PERSON_2.toPersonResponse(null, "Community")))
    }

    @Test
    fun `find person with invalid crn or noms number returns a failure response`() {
        val response = mockMvc
            .perform(get("/person/find/65145A").withToken())
            .andExpect(status().isBadRequest)
            .andReturn().response.contentAsJson<ErrorResponse>()

        assertThat(response.message, equalTo("65145A is not a valid crn or prisoner number"))
    }

    @Test
    fun `find person with valid crn returns a not found response`() {
        val response = mockMvc
            .perform(get("/person/find/X754321").withToken())
            .andExpect(status().isNotFound)
            .andReturn().response.contentAsJson<ErrorResponse>()

        assertThat(response.message, equalTo("Person with crn of X754321 not found"))
    }

    @Test
    fun `find person with valid prisoner number returns a not found response`() {
        val response = mockMvc
            .perform(get("/person/find/A5456AX").withToken())
            .andExpect(status().isNotFound)
            .andReturn().response.contentAsJson<ErrorResponse>()

        assertThat(response.message, equalTo("Person with prisoner number of A5456AX not found"))
    }
}
