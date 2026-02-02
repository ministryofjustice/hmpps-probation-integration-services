package uk.gov.justice.digital.hmpps

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.test.web.servlet.get
import uk.gov.justice.digital.hmpps.data.generator.DocumentGenerator.BREACH_NOTICE_ID
import uk.gov.justice.digital.hmpps.data.generator.DocumentGenerator.PSS_BREACH_NOTICE_ID
import uk.gov.justice.digital.hmpps.data.generator.DocumentGenerator.UNSENTENCED_BREACH_NOTICE_ID
import uk.gov.justice.digital.hmpps.data.generator.EventGenerator.DEFAULT_DISPOSAL
import uk.gov.justice.digital.hmpps.data.generator.EventGenerator.UNSENTENCED_EVENT
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.data.generator.WarningGenerator
import uk.gov.justice.digital.hmpps.data.generator.WarningGenerator.ENFORCEABLE_CONTACTS
import uk.gov.justice.digital.hmpps.data.generator.WarningGenerator.ENFORCEABLE_CONTACTS_UNPAID
import uk.gov.justice.digital.hmpps.data.generator.WarningGenerator.NOTICE_TYPES
import uk.gov.justice.digital.hmpps.data.generator.WarningGenerator.PSS_ENFORCEABLE_CONTACT
import uk.gov.justice.digital.hmpps.data.generator.WarningGenerator.SENTENCE_TYPES
import uk.gov.justice.digital.hmpps.integrations.delius.codedDescriptions
import uk.gov.justice.digital.hmpps.integrations.delius.sentenceTypes
import uk.gov.justice.digital.hmpps.model.WarningDetails
import uk.gov.justice.digital.hmpps.model.WarningTypesResponse
import uk.gov.justice.digital.hmpps.service.toEnforceableContact
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.contentAsJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken

internal class WarningIntegrationTest : BaseIntegrationTest() {

    @Test
    fun `can retrieve all warning types`() {
        val person = PersonGenerator.DEFAULT_PERSON
        val response = mockMvc.get("/warning-types/${person.crn}/$BREACH_NOTICE_ID") {
            withToken()
        }
            .andExpect { status { is2xxSuccessful() } }
            .andReturn().response.contentAsJson<WarningTypesResponse>()

        assertThat(response).isEqualTo(
            WarningTypesResponse(
                warningTypes = NOTICE_TYPES.filter { it.selectable }.codedDescriptions(),
                sentenceTypes = SENTENCE_TYPES.sentenceTypes(),
                defaultSentenceTypeCode = "PSS",
            )
        )
    }

    @Test
    fun `returns bad request error for unsentenced event`() {
        val person = PersonGenerator.DEFAULT_PERSON
        mockMvc.get("/warning-types/${person.crn}/${UNSENTENCED_BREACH_NOTICE_ID}") {
            withToken()
        }
            .andExpect {
                status { isBadRequest() }
                jsonPath("$.message") { value("Event with id ${UNSENTENCED_EVENT.id} is not sentenced") }
            }
    }

    @Test
    fun `can retrieve warning details`() {
        val person = PersonGenerator.DEFAULT_PERSON
        val response = mockMvc.get("/warning-details/${person.crn}/$BREACH_NOTICE_ID") {
            withToken()
        }
            .andExpect { status { is2xxSuccessful() } }
            .andReturn().response.contentAsJson<WarningDetails>()

        val requirements = requirementRepository.findAllByDisposalId(DEFAULT_DISPOSAL.id)
        assertThat(requirements).isNotEmpty
        assertThat(response).isEqualTo(
            WarningDetails(
                breachReasons = WarningGenerator.BREACH_REASONS.filter { it.selectable }.codedDescriptions(),
                enforceableContacts = (ENFORCEABLE_CONTACTS + ENFORCEABLE_CONTACTS_UNPAID).sortedBy { it.date }
                    .map { it.toEnforceableContact() },
            ),
        )
        assertThat(response.enforceableContacts.firstOrNull()).isNotNull()
    }

    @Test
    fun `can retrieve warning details for PSS`() {
        val person = PersonGenerator.PSS_PERSON
        val response = mockMvc.get("/warning-details/${person.crn}/$PSS_BREACH_NOTICE_ID") {
            withToken()
        }
            .andExpect { status { is2xxSuccessful() } }
            .andReturn().response.contentAsJson<WarningDetails>()

        assertThat(response).isEqualTo(
            WarningDetails(
                breachReasons = WarningGenerator.BREACH_REASONS.filter { it.selectable }.codedDescriptions(),
                enforceableContacts = listOf(PSS_ENFORCEABLE_CONTACT.toEnforceableContact()),
            ),
        )
    }

    @Test
    fun `cannot retrieve warning details if crn does not match`() {
        mockMvc.get("/warning-details/${PersonGenerator.PSS_PERSON.crn}/$BREACH_NOTICE_ID") {
            withToken()
        }
            .andExpect { status { isNotFound() } }

        mockMvc.get("/warning-details/${PersonGenerator.DEFAULT_PERSON.crn}/$PSS_BREACH_NOTICE_ID") { withToken() }
            .andExpect { status { isNotFound() } }
    }

    @Test
    fun `breach reasons are sorted case-insensitively`() {
        val person = PersonGenerator.DEFAULT_PERSON
        val response = mockMvc.get("/warning-details/${person.crn}/$BREACH_NOTICE_ID") {
            withToken()
        }
            .andExpect { status { is2xxSuccessful() } }
            .andReturn().response.contentAsJson<WarningDetails>()

        val expectedSorted =
            WarningGenerator.BREACH_REASONS.filter { it.selectable }.sortedBy { it.description.lowercase() }
        assertThat(response.breachReasons.map { it.description }).isEqualTo(expectedSorted.map { it.description })
    }
}