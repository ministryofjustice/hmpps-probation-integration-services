package uk.gov.justice.digital.hmpps

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.test.web.servlet.patch
import org.springframework.test.web.servlet.put
import uk.gov.justice.digital.hmpps.api.model.contact.UpdateContact
import uk.gov.justice.digital.hmpps.api.model.contact.UpdateContactOutcome
import uk.gov.justice.digital.hmpps.data.generator.EnforcementFlagGenerator
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.json
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZonedDateTime

class EnforcementFlagIntegrationTest : IntegrationTestBase() {

    /**
     * When updating a contact outcome where the contact has no enforcement action,
     * the enforcement flag should not be set.
     */
    @Test
    fun `enforcement flag is not set when contact has no outstanding enforcement action`() {
        mockMvc.put("/contact/${EnforcementFlagGenerator.CONTACT_NO_OUTCOME.id}") {
            withToken()
            json = UpdateContactOutcome(
                date = LocalDate.now().plusDays(1),
                time = LocalTime.of(10, 0),
                outcomeCode = EnforcementFlagGenerator.OUTCOME_MATCHING_CONTACT_TYPE.code,
                enforcementActionCode = null,
                notes = "Setting outcome on contact with no enforcement action",
                alert = false,
                sensitive = false
            )
        }.andExpect { status { isOk() } }

        val updated = transactionTemplate.execute {
            entityManager.clear()
            contactRepository.findById(EnforcementFlagGenerator.CONTACT_NO_OUTCOME.id).get()
        }

        // Contact has no action, so neither condition in setEnforcementFlag is met — enforcementFlag remains null.
        assertThat(updated.enforcementFlag).isNull()
        assertThat(updated.outcome?.code).isEqualTo(EnforcementFlagGenerator.OUTCOME_MATCHING_CONTACT_TYPE.code)
    }

    /**
     * When updating a contact outcome where the contact has an existing enforcement action with
     * outstandingContactAction=true, the enforcement flag should be set to true.
     */
    @Test
    fun `enforcement flag is set when contact has outstanding enforcement action`() {
        mockMvc.put("/contact/${EnforcementFlagGenerator.CONTACT_WITH_OUTSTANDING_ACTION.id}") {
            withToken()
            json = UpdateContactOutcome(
                date = LocalDate.now().plusDays(1),
                time = LocalTime.of(11, 0),
                outcomeCode = EnforcementFlagGenerator.OUTCOME_MATCHING_CONTACT_TYPE.code,
                enforcementActionCode = null,
                notes = "Setting outcome on contact with outstanding enforcement action",
                alert = false,
                sensitive = false
            )
        }.andExpect { status { isOk() } }

        val updated = transactionTemplate.execute {
            entityManager.clear()
            contactRepository.findById(EnforcementFlagGenerator.CONTACT_WITH_OUTSTANDING_ACTION.id).get()
        }

        // The contact has an action with outstandingContactAction=true, so enforcementFlag is set to true.
        assertThat(updated.enforcementFlag).isTrue
        assertThat(updated.outcome?.code).isEqualTo(EnforcementFlagGenerator.OUTCOME_MATCHING_CONTACT_TYPE.code)
    }

    /**
     * When calling updateContact (PATCH) on a contact that has no outcome but has an outstanding
     * enforcement action, the enforcement flag should still be set to true. This verifies the fix
     * where the early-return on null outcome previously prevented the outstanding action check.
     */
    @Test
    fun `enforcement flag is set via updateContact when contact has outstanding action and no outcome`() {
        mockMvc.patch("/contact/${EnforcementFlagGenerator.CONTACT_WITH_OUTSTANDING_ACTION_NO_OUTCOME.id}") {
            withToken()
            json = UpdateContact(
                dateTime = ZonedDateTime.now(),
                notes = "Updating contact with outstanding action",
                sensitiveFlag = null
            )
        }.andExpect { status { isOk() } }

        val updated = transactionTemplate.execute {
            entityManager.clear()
            contactRepository.findById(EnforcementFlagGenerator.CONTACT_WITH_OUTSTANDING_ACTION_NO_OUTCOME.id).get()
        }

        // Despite having no outcome, the outstanding action should still trigger enforcementFlag=true.
        assertThat(updated.enforcementFlag).isTrue
    }
}

