package uk.gov.justice.digital.hmpps

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import uk.gov.justice.digital.hmpps.advice.ErrorResponse
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.data.generator.RequirementGenerator
import uk.gov.justice.digital.hmpps.model.AccreditedProgramme
import uk.gov.justice.digital.hmpps.model.PersonResponse
import uk.gov.justice.digital.hmpps.model.ProbationDeliveryUnit
import uk.gov.justice.digital.hmpps.service.toAccreditedProgramme
import uk.gov.justice.digital.hmpps.service.toPersonResponse
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.contentAsJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = RANDOM_PORT)
internal class PersonIntegrationTest @Autowired constructor(
    private val mockMvc: MockMvc
) {

    @Test
    fun `find person with valid crn returns a success response`() {
        val response = mockMvc
            .get("/person/find/X123456") { withToken() }
            .andExpect { status { isOk() } }
            .andReturn().response.contentAsJson<PersonResponse>()

        assertThat(
            response,
            equalTo(
                PersonGenerator.PERSON_1.toPersonResponse(
                    ProbationDeliveryUnit(
                        code = "A",
                        description = "Default Test PDU"
                    ), "Custody"
                )
            )
        )
    }

    @Test
    fun `find person with valid prisoner number returns a success response`() {
        val response = mockMvc
            .get("/person/find/A4321BA") { withToken() }
            .andExpect { status { isOk() } }
            .andReturn().response.contentAsJson<PersonResponse>()

        assertThat(response, equalTo(PersonGenerator.PERSON_2.toPersonResponse(null, "Community")))
    }

    @Test
    fun `find person with invalid crn or noms number returns a failure response`() {
        val response = mockMvc
            .get("/person/find/65145A") { withToken() }
            .andExpect { status { isBadRequest() } }
            .andReturn().response.contentAsJson<ErrorResponse>()

        assertThat(response.message, equalTo("65145A is not a valid crn or prisoner number"))
    }

    @Test
    fun `find person with valid crn returns a not found response`() {
        val response = mockMvc
            .get("/person/find/X754321") { withToken() }
            .andExpect { status { isNotFound() } }
            .andReturn().response.contentAsJson<ErrorResponse>()

        assertThat(response.message, equalTo("Person with crn of X754321 not found"))
    }

    @Test
    fun `find person with valid prisoner number returns a not found response`() {
        val response = mockMvc
            .get("/person/find/A5456AX") { withToken() }
            .andExpect { status { isNotFound() } }
            .andReturn().response.contentAsJson<ErrorResponse>()

        assertThat(response.message, equalTo("Person with prisoner number of A5456AX not found"))
    }

    @Test
    fun `find a persons accredited programme history`() {
        val response = mockMvc
            .get("/person/X123456/accredited-programme-history") { withToken() }
            .andExpect { status { isOk() } }
            .andReturn().response.contentAsJson<List<AccreditedProgramme>>()
        assertThat(response.get(0), equalTo(RequirementGenerator.ACC_PROG_6.toAccreditedProgramme()))
        assertThat(response.get(1), equalTo(RequirementGenerator.ACC_PROG_5.toAccreditedProgramme()))
        assertThat(response.get(2), equalTo(RequirementGenerator.ACC_PROG_1.toAccreditedProgramme()))
        assertThat(response.get(3), equalTo(RequirementGenerator.ACC_PROG_2.toAccreditedProgramme()))
        assertThat(response.get(4), equalTo(RequirementGenerator.ACC_PROG_3.toAccreditedProgramme()))
        assertThat(response.get(5), equalTo(RequirementGenerator.ACC_PROG_4.toAccreditedProgramme()))
        assertThat(response.size, equalTo(6))
    }

    @Test
    fun `find a persons accredited programme history returns person not found`() {
        val response = mockMvc
            .get("/person/X123457/accredited-programme-history") { withToken() }
            .andExpect { status { isNotFound() } }
            .andReturn().response.contentAsJson<ErrorResponse>()
        assertThat(response.message, equalTo("Person with crn of X123457 not found"))
    }
}
