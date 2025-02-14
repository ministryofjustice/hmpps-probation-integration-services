package uk.gov.justice.digital.hmpps

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.data.generator.WarningGenerator
import uk.gov.justice.digital.hmpps.data.generator.WarningGenerator.DEFAULT_ENFORCEABLE_CONTACT
import uk.gov.justice.digital.hmpps.data.generator.WarningGenerator.NOTICE_TYPES
import uk.gov.justice.digital.hmpps.integrations.delius.codedDescriptions
import uk.gov.justice.digital.hmpps.integrations.delius.sentenceTypes
import uk.gov.justice.digital.hmpps.model.CodedDescription
import uk.gov.justice.digital.hmpps.model.WarningDetails
import uk.gov.justice.digital.hmpps.model.WarningTypes
import uk.gov.justice.digital.hmpps.service.toEnforceableContact
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.contentAsJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken

internal class WarningIntegrationTest : BaseIntegrationTest() {

    @Test
    fun `can retrieve all warning types`() {
        val response = mockMvc
            .perform(get("/warning-types").withToken())
            .andExpect(status().is2xxSuccessful)
            .andReturn().response.contentAsJson<WarningTypes>()

        assertThat(response.content)
            .containsExactlyElementsOf(
                NOTICE_TYPES.filter { it.selectable }
                    .map { CodedDescription(it.code, it.description) }
                    .sortedBy { it.description }
            )
    }

    @Test
    fun `can retrieve warning details`() {
        val person = PersonGenerator.DEFAULT_PERSON
        val response = mockMvc
            .perform(get("/warning-details/${person.crn}").withToken())
            .andExpect(status().is2xxSuccessful)
            .andReturn().response.contentAsJson<WarningDetails>()

        assertThat(response).isEqualTo(
            WarningDetails(
                breachReasons = WarningGenerator.BREACH_REASONS.filter { it.selectable }.codedDescriptions(),
                sentenceTypes = WarningGenerator.SENTENCE_TYPES.sentenceTypes(),
                enforceableContacts = listOf(DEFAULT_ENFORCEABLE_CONTACT.toEnforceableContact()),
            )
        )
        assertThat(response.enforceableContacts.first()).isNotNull
    }
}