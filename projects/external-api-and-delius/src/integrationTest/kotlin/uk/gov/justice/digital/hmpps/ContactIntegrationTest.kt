package uk.gov.justice.digital.hmpps

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.advice.ErrorResponse
import uk.gov.justice.digital.hmpps.data.generator.ContactGenerator
import uk.gov.justice.digital.hmpps.data.generator.ContactGenerator.CONTACT_OUTCOME_TYPE
import uk.gov.justice.digital.hmpps.data.generator.ContactGenerator.CONTACT_TYPE
import uk.gov.justice.digital.hmpps.data.generator.ContactGenerator.MAPPA_CONTACT
import uk.gov.justice.digital.hmpps.data.generator.DataGenerator.DEFAULT_PDU
import uk.gov.justice.digital.hmpps.data.generator.DataGenerator.DEFAULT_PROVIDER
import uk.gov.justice.digital.hmpps.data.generator.DataGenerator.DEFAULT_TEAM
import uk.gov.justice.digital.hmpps.data.generator.DataGenerator.JOHN_SMITH
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.model.*
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.contentAsJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken

internal class ContactIntegrationTest : BaseIntegrationTest() {
    @Test
    fun `can retrieve contact by id when mappa category matches`() {
        val response = mockMvc
            .perform(get("/case/${PersonGenerator.DEFAULT.crn}/contacts/${ContactGenerator.CONTACT.id}?mappaCategories=2").withToken())
            .andExpect(status().is2xxSuccessful)
            .andReturn().response.contentAsJson<ContactLogged>()

        assertThat(response.type).isEqualTo(CodedValue(CONTACT_TYPE.code, CONTACT_TYPE.description))
        assertThat(response.outcome).isEqualTo(CodedValue(CONTACT_OUTCOME_TYPE.code, CONTACT_OUTCOME_TYPE.description))
        assertThat(response.description).isEqualTo(ContactGenerator.CONTACT.description)
        assertThat(response.notes).isEqualTo(ContactGenerator.CONTACT.notes)
        assertThat(response.officer).isEqualTo(
            Officer(
                JOHN_SMITH.code, JOHN_SMITH.name(),
                OfficerTeam(
                    DEFAULT_TEAM.code, DEFAULT_TEAM.description,
                    OfficerPdu(
                        DEFAULT_PDU.code,
                        DEFAULT_PDU.description,
                        Provider(DEFAULT_PROVIDER.code, DEFAULT_PROVIDER.description)
                    )
                )
            )
        )
    }

    @Test
    fun `no contact when mappa category does not match`() {
        val response = mockMvc
            .perform(get("/case/${PersonGenerator.DEFAULT.crn}/contacts/${ContactGenerator.CONTACT.id}?mappaCategories=1").withToken())
            .andExpect(status().isNotFound)
            .andReturn().response.contentAsJson<ErrorResponse>()

        assertThat(response.status).isEqualTo(404)
        assertThat(response.message).isEqualTo("Person with mappa cat in [M1] with crn of A000001 not found")
    }

    @Test
    fun `can retrieve visor contacts when mappa category matches`() {
        val response = mockMvc
            .perform(get("/case/${PersonGenerator.DEFAULT.crn}/contacts?mappaCategories=2").withToken())
            .andExpect(status().is2xxSuccessful)
            .andReturn().response.contentAsJson<ContactsLogged>()

        assertThat(response.totalPages).isEqualTo(1)
        assertThat(response.totalResults).isEqualTo(1)
        assertThat(response.content.first().description).isEqualTo(MAPPA_CONTACT.description)
    }

    @Test
    fun `no contacts when mappa category does not match`() {
        val response = mockMvc
            .perform(get("/case/${PersonGenerator.DEFAULT.crn}/contacts?mappaCategories=4").withToken())
            .andExpect(status().isNotFound)
            .andReturn().response.contentAsJson<ErrorResponse>()

        assertThat(response.status).isEqualTo(404)
        assertThat(response.message).isEqualTo("Person with mappa cat in [M4] with crn of A000001 not found")
    }
}