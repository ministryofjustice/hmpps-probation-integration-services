package uk.gov.justice.digital.hmpps

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.test.web.servlet.get
import uk.gov.justice.digital.hmpps.data.generator.DocumentGenerator.BREACH_NOTICE_ID
import uk.gov.justice.digital.hmpps.data.generator.OfficeLocationGenerator
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.data.generator.StaffGenerator
import uk.gov.justice.digital.hmpps.model.BasicDetails
import uk.gov.justice.digital.hmpps.model.DocumentCrn
import uk.gov.justice.digital.hmpps.model.Name
import uk.gov.justice.digital.hmpps.service.toAddress
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.contentAsJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken
import java.util.*

internal class BasicDetailsIntegrationTest : BaseIntegrationTest() {

    @Test
    fun `can retrieve all basic details successfully`() {
        val person = PersonGenerator.DEFAULT_PERSON
        val username = StaffGenerator.DEFAULT_SU.username
        val response = mockMvc.get("/basic-details/${person.crn}/$username") {
            withToken()
        }
            .andExpect { status { is2xxSuccessful() } }
            .andReturn().response.contentAsJson<BasicDetails>()

        assertThat(response).isEqualTo(
            BasicDetails(
                null,
                Name(
                    person.firstName,
                    listOfNotNull(person.secondName, person.thirdName).joinToString(" "),
                    person.surname
                ),
                listOf(PersonGenerator.DEFAULT_ADDRESS.toAddress()),
                listOf(OfficeLocationGenerator.DEFAULT_LOCATION.toAddress()),
            )
        )
    }

    @Test
    fun `username not found returns 404 response`() {
        mockMvc.get("/basic-details/${PersonGenerator.DEFAULT_PERSON.crn}/nonexistent") {
            withToken()
        }
            .andExpect { status { isNotFound() } }
    }

    @Test
    fun `no home area returns 400 response`() {
        mockMvc.get("/basic-details/${PersonGenerator.DEFAULT_PERSON.crn}/NoHomeArea") {
            withToken()
        }
            .andExpect { status { isBadRequest() } }
    }

    @Test
    fun `can retrieve crn from breach notice id successfully`() {
        val person = PersonGenerator.DEFAULT_PERSON
        val response = mockMvc.get("/case/$BREACH_NOTICE_ID") {
            withToken()
        }
            .andExpect { status { is2xxSuccessful() } }
            .andReturn().response.contentAsJson<DocumentCrn>()

        assertThat(response.crn).isEqualTo(person.crn)
    }

    @Test
    fun `document not found returns 404 response`() {
        mockMvc.get("/case/${UUID.randomUUID()}") {
            withToken()
        }
            .andExpect { status { isNotFound() } }
    }
}
