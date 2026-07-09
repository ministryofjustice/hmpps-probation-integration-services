package uk.gov.justice.digital.hmpps

import jakarta.servlet.ServletException
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.containsString
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.test.web.servlet.post
import uk.gov.justice.digital.hmpps.api.model.contact.EnforcementActionForUpdate
import uk.gov.justice.digital.hmpps.api.model.contact.UpdateEnforcementActions
import uk.gov.justice.digital.hmpps.data.generator.IdGenerator
import uk.gov.justice.digital.hmpps.data.generator.UpdateContactOutcomeGenerator
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.json
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken

class UpdateEnforcementActionsIntegrationTest : IntegrationTestBase() {

    @Test
    fun `unknown contact id returns not found`() {
        mockMvc.post("/contact/${IdGenerator.getAndIncrement()}/enforcement-actions") {
            withToken()
            json = UpdateEnforcementActions(
                enforcementActions = listOf(
                    EnforcementActionForUpdate(
                        code = UpdateContactOutcomeGenerator.ENFORCEMENT_ACTION.code
                    )
                )
            )
        }.andExpect { status { isNotFound() } }
    }

    @Test
    fun `contact without outcome returns bad request`() {
        mockMvc.post("/contact/${UpdateContactOutcomeGenerator.CONTACT_NO_OUTCOME.id}/enforcement-actions") {
            withToken()
            json = UpdateEnforcementActions(
                enforcementActions = listOf(
                    EnforcementActionForUpdate(
                        code = UpdateContactOutcomeGenerator.ENFORCEMENT_ACTION.code
                    )
                )
            )
        }.andExpect { status { isBadRequest() } }
    }

    @Test
    fun `contact with compliant outcome returns bad request`() {
        mockMvc.post("/contact/${UpdateContactOutcomeGenerator.CONTACT_5.id}/enforcement-actions") {
            withToken()
            json = UpdateEnforcementActions(
                enforcementActions = listOf(
                    EnforcementActionForUpdate(
                        code = UpdateContactOutcomeGenerator.PERSON_LEVEL_ENFORCEMENT_ACTION.code
                    )
                )
            )
        }.andExpect { status { isBadRequest() } }
    }

    @Test
    fun `enforcement action not valid for outcome returns internal server error`() {
        assertThrows<ServletException> {
            mockMvc.post("/contact/${UpdateContactOutcomeGenerator.CONTACT_6.id}/enforcement-actions") {
                withToken()
                json = UpdateEnforcementActions(
                    enforcementActions = listOf(
                        EnforcementActionForUpdate(
                            code = UpdateContactOutcomeGenerator.PERSON_LEVEL_ENFORCEMENT_ACTION.code
                        )
                    )
                )
            }
        }
    }

    @Test
    fun `valid enforcement actions creates enforcements updates latest action and creates linked contacts`() {
        val contactId = UpdateContactOutcomeGenerator.CONTACT_6.id
        val firstActionCode = UpdateContactOutcomeGenerator.ENFORCEMENT_ACTION.code
        val secondActionCode = UpdateContactOutcomeGenerator.ENFORCEMENT_ACTION_2.code
        val expectedContactTypeCode = UpdateContactOutcomeGenerator.CONTACT_TYPE.code

        val enforcementCountBefore = enforcementRepository.findAll().count { it.contact.id == contactId }
        val linkedContactCountBefore = contactRepository.findByLinkedContactIdOrderByDateDesc(contactId)
            .count { it.type.code == expectedContactTypeCode }

        mockMvc.post("/contact/$contactId/enforcement-actions") {
            withToken()
            json = UpdateEnforcementActions(
                enforcementActions = listOf(
                    EnforcementActionForUpdate(code = firstActionCode),
                    EnforcementActionForUpdate(code = secondActionCode)
                )
            )
        }.andExpect { status { isOk() } }

        val updatedContact = contactRepository.findById(contactId).get()
        val enforcementCountAfter = enforcementRepository.findAll().count { it.contact.id == contactId }
        val enforcement = enforcementRepository.findAll().find { it.contact.id == contactId }
        val linkedContactCountAfter = contactRepository.findByLinkedContactIdOrderByDateDesc(contactId)
            .count { it.type.code == expectedContactTypeCode }

        assertThat(enforcementCountAfter, equalTo(1))
        assertThat(enforcement?.action?.code, equalTo(secondActionCode))
        assertThat(linkedContactCountAfter, equalTo(linkedContactCountBefore + 2))
        assertThat(updatedContact.enforcementFlag, equalTo(true))
        assertThat(updatedContact.latestEnforcementAction?.code, equalTo(secondActionCode))
        assertThat(
            updatedContact.notes ?: "",
            containsString("Enforcement Action: ${UpdateContactOutcomeGenerator.ENFORCEMENT_ACTION.description}")
        )
        assertThat(
            updatedContact.notes ?: "",
            containsString("Enforcement Action: ${UpdateContactOutcomeGenerator.ENFORCEMENT_ACTION_2.description}")
        )
    }

    @Test
    fun `existing enforcement is updated when enforcement actions endpoint is called on contact with existing enforcement`() {
        val contactId = UpdateContactOutcomeGenerator.CONTACT_12.id
        val actionCode = UpdateContactOutcomeGenerator.ENFORCEMENT_ACTION_2.code

        val enforcementsBefore = enforcementRepository.findAll().filter { it.contact.id == contactId }
        assertThat(enforcementsBefore.size, equalTo(1))
        assertThat(enforcementsBefore[0].action?.code, equalTo(UpdateContactOutcomeGenerator.ENFORCEMENT_ACTION.code))

        mockMvc.post("/contact/$contactId/enforcement-actions") {
            withToken()
            json = UpdateEnforcementActions(
                enforcementActions = listOf(
                    EnforcementActionForUpdate(code = actionCode)
                )
            )
        }.andExpect { status { isOk() } }

        val enforcementsAfter = transactionTemplate.execute {
            entityManager.clear()
            enforcementRepository.findAll().filter { it.contact.id == contactId }
        }
        assertThat(enforcementsAfter?.size, equalTo(1))
        assertThat(enforcementsAfter?.get(0)?.action?.code, equalTo(actionCode))

        val updatedContact = contactRepository.findById(contactId).get()
        assertThat(updatedContact.latestEnforcementAction?.code, equalTo(actionCode))
    }

    @Test
    fun `new enforcement is created when enforcement actions endpoint is called on contact without existing enforcement`() {
        val contactId = UpdateContactOutcomeGenerator.CONTACT_13.id

        val enforcementsBefore = enforcementRepository.findAll().filter { it.contact.id == contactId }
        assertThat(enforcementsBefore.size, equalTo(0))

        mockMvc.post("/contact/$contactId/enforcement-actions") {
            withToken()
            json = UpdateEnforcementActions(
                enforcementActions = listOf(
                    EnforcementActionForUpdate(code = UpdateContactOutcomeGenerator.ENFORCEMENT_ACTION.code)
                )
            )
        }.andExpect { status { isOk() } }

        val enforcementsAfter = transactionTemplate.execute {
            entityManager.clear()
            enforcementRepository.findAll().filter { it.contact.id == contactId }
        }
        assertThat(enforcementsAfter?.size, equalTo(1))
        assertThat(
            enforcementsAfter?.get(0)?.action?.code,
            equalTo(UpdateContactOutcomeGenerator.ENFORCEMENT_ACTION.code)
        )
    }
}
