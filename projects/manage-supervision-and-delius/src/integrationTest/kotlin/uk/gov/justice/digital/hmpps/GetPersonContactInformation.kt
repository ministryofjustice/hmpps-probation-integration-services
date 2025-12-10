package uk.gov.justice.digital.hmpps

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.test.web.servlet.get
import uk.gov.justice.digital.hmpps.api.model.personalDetails.PersonContactInformation
import uk.gov.justice.digital.hmpps.data.generator.personalDetails.PersonDetailsGenerator.PERSONAL_DETAILS
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.contentAsJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken

class GetPersonContactInformation : IntegrationTestBase() {

    @Test
    fun `401 if not token`() {
        val person = PERSONAL_DETAILS
        mockMvc.get(URL_TO_TEST, person.crn)
            .andExpect { status { isUnauthorized() } }
    }

    @Test
    fun `404 if crn does not exist`() {
        mockMvc.get(URL_TO_TEST, "Z999999") { withToken() }
            .andExpect { status { isNotFound() } }
    }

    @Test
    fun `retrieve contact information for a case`() {
        val person = PERSONAL_DETAILS
        val response = mockMvc.get(URL_TO_TEST, person.crn) { withToken() }
            .andExpect { status { isOk() } }
            .andReturn().response.contentAsJson<PersonContactInformation>()

        assertThat(response).isEqualTo(
            PersonContactInformation(
                person.crn,
                person.telephoneNumber,
                person.mobileNumber,
                person.emailAddress
            )
        )
    }

    companion object {
        val URL_TO_TEST = "/case/{crn}/contact-information"
    }
}