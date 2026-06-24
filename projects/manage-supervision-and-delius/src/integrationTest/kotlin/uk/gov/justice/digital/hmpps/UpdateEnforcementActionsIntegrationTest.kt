package uk.gov.justice.digital.hmpps

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.containsString
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.springframework.test.web.servlet.post
import uk.gov.justice.digital.hmpps.api.model.compliance.EnforcementAction
import uk.gov.justice.digital.hmpps.api.model.contact.UpdateEnforcementActions
import uk.gov.justice.digital.hmpps.data.generator.IdGenerator
import uk.gov.justice.digital.hmpps.data.generator.UpdateContactOutcomeGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.ContactType
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.json
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken

class UpdateEnforcementActionsIntegrationTest : IntegrationTestBase() {

    @Test
    fun `unknown contact id returns not found`() {
        mockMvc.post("/contact/${IdGenerator.getAndIncrement()}/enforcement-actions") {
            withToken()
            json = UpdateEnforcementActions(
                enforcementActions = listOf(
                    EnforcementAction(
                        code = UpdateContactOutcomeGenerator.ENFORCEMENT_ACTION.code,
                        description = UpdateContactOutcomeGenerator.ENFORCEMENT_ACTION.description,
                        responseByDate = null
                    )
                )
            )
        }.andExpect { status { isNotFound() } }
    }

    @Test
    fun `unknown enforcement action code returns not found`() {
        mockMvc.post("/contact/${UpdateContactOutcomeGenerator.CONTACT_6.id}/enforcement-actions") {
            withToken()
            json = UpdateEnforcementActions(
                enforcementActions = listOf(
                    EnforcementAction(
                        code = "INVALID",
                        description = "Invalid action",
                        responseByDate = null
                    )
                )
            )
        }.andExpect { status { isNotFound() } }
    }

    @Test
    fun `enforcement action not valid for outcome returns bad request`() {
        mockMvc.post("/contact/${UpdateContactOutcomeGenerator.CONTACT_6.id}/enforcement-actions") {
            withToken()
            json = UpdateEnforcementActions(
                enforcementActions = listOf(
                    EnforcementAction(
                        code = UpdateContactOutcomeGenerator.PERSON_LEVEL_ENFORCEMENT_ACTION.code,
                        description = UpdateContactOutcomeGenerator.PERSON_LEVEL_ENFORCEMENT_ACTION.description,
                        responseByDate = null
                    )
                )
            )
        }.andExpect { status { isBadRequest() } }
    }

    @Test
    fun `valid enforcement action creates enforcement updates latest action and creates linked contact`() {
        ensureAromContactTypeExists()

        val contactId = UpdateContactOutcomeGenerator.CONTACT_5.id
        val actionCode = UpdateContactOutcomeGenerator.PERSON_LEVEL_ENFORCEMENT_ACTION.code

        val enforcementCountBefore = enforcementRepository.findAll().count { it.contact.id == contactId }
        val linkedContactCountBefore = contactRepository.findByLinkedContactIdOrderByDateDesc(contactId)
            .count { it.type.code == "AROM" }

        mockMvc.post("/contact/$contactId/enforcement-actions") {
            withToken()
            json = UpdateEnforcementActions(
                enforcementActions = listOf(
                    EnforcementAction(
                        code = actionCode,
                        description = UpdateContactOutcomeGenerator.PERSON_LEVEL_ENFORCEMENT_ACTION.description,
                        responseByDate = null
                    )
                )
            )
        }.andExpect { status { isOk() } }

        val updatedContact = contactRepository.findById(contactId).get()
        val enforcementCountAfter = enforcementRepository.findAll().count { it.contact.id == contactId }
        val linkedContactCountAfter = contactRepository.findByLinkedContactIdOrderByDateDesc(contactId)
            .count { it.type.code == "AROM" }

        assertThat(enforcementCountAfter, equalTo(enforcementCountBefore + 1))
        assertThat(linkedContactCountAfter, equalTo(linkedContactCountBefore + 1))
        assertThat(updatedContact.enforcementFlag, equalTo(true))
        assertThat(updatedContact.latestEnforcementAction?.code, equalTo(actionCode))
        assertThat(updatedContact.notes ?: "", containsString("Enforcement Action: ${UpdateContactOutcomeGenerator.PERSON_LEVEL_ENFORCEMENT_ACTION.description}"))
    }

    private fun ensureAromContactTypeExists() {
        transactionTemplate.executeWithoutResult {
            val count = entityManager.createQuery(
                "select count(ct) from ContactType ct where ct.code = :code",
                Long::class.javaObjectType
            )
                .setParameter("code", "AROM")
                .singleResult

            if (count == 0L) {
                entityManager.persist(
                    ContactType(
                        id = IdGenerator.getAndIncrement(),
                        code = "AROM",
                        attendanceContact = false,
                        description = "Add review outcome manual",
                        locationRequired = "N",
                        editable = true,
                    )
                )
            }
        }
    }
}
