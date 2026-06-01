package uk.gov.justice.digital.hmpps

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers
import org.hamcrest.Matchers.containsString
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.notNullValue
import org.junit.jupiter.api.Test
import org.springframework.test.web.servlet.put
import uk.gov.justice.digital.hmpps.api.model.contact.UpdateContactOutcome
import uk.gov.justice.digital.hmpps.data.generator.IdGenerator
import uk.gov.justice.digital.hmpps.data.generator.UpdateContactOutcomeGenerator
import uk.gov.justice.digital.hmpps.service.ContactLogService
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.json
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken
import java.time.LocalDate
import java.time.LocalTime
import kotlin.text.get

class UpdateContactOutcomeIntegrationTest : IntegrationTestBase() {

    @Test
    fun `unknown contact id returns not found`() {
        mockMvc.put("/contact/${IdGenerator.getAndIncrement()}") {
            withToken()
            json = UpdateContactOutcome(
                date = LocalDate.now(),
                time = LocalTime.of(10, 0),
                outcomeCode = UpdateContactOutcomeGenerator.OUTCOME.code,
                enforcementActionCode = null,
                notes = "Test notes",
                alert = false,
                sensitive = false
            )
        }.andExpect { status { isNotFound() } }
    }

    @Test
    fun `unknown outcome code returns not found`() {
        mockMvc.put("/contact/${UpdateContactOutcomeGenerator.CONTACT_1.id}") {
            withToken()
            json = UpdateContactOutcome(
                date = LocalDate.now(),
                time = LocalTime.of(10, 0),
                outcomeCode = "INVALID",
                enforcementActionCode = null,
                notes = "Test notes",
                alert = false,
                sensitive = false
            )
        }.andExpect { status { isNotFound() } }
    }

    @Test
    fun `unknown enforcement action code returns not found`() {
        mockMvc.put("/contact/${UpdateContactOutcomeGenerator.CONTACT_1.id}") {
            withToken()
            json = UpdateContactOutcome(
                date = LocalDate.now().plusDays(1),
                time = LocalTime.of(10, 0),
                outcomeCode = UpdateContactOutcomeGenerator.OUTCOME.code,
                enforcementActionCode = "INVALID",
                notes = "Test notes",
                alert = false,
                sensitive = false
            )
        }.andExpect { status { isBadRequest() } }
    }

    @Test
    fun `successfully updates contact outcome`() {
        mockMvc.put("/contact/${UpdateContactOutcomeGenerator.CONTACT_1.id}") {
            withToken()
            json = UpdateContactOutcome(
                date = LocalDate.now().plusDays(1),
                time = LocalTime.of(14, 30),
                outcomeCode = UpdateContactOutcomeGenerator.OUTCOME.code,
                enforcementActionCode = null,
                notes = "Outcome notes added",
                alert = false,
                sensitive = false
            )
        }.andExpect { status { isOk() } }

        val updated = contactRepository.findById(UpdateContactOutcomeGenerator.CONTACT_1.id).get()
        assertThat(updated.outcome?.code, equalTo(UpdateContactOutcomeGenerator.OUTCOME.code))
        assertThat(updated.notes, containsString("Outcome notes added"))
        assertThat(updated.alert, equalTo(false))
        assertThat(updated.sensitive, equalTo(false))
    }

    @Test
    fun `successfully updates contact outcome with alert and sensitive flags`() {
        mockMvc.put("/contact/${UpdateContactOutcomeGenerator.CONTACT_2.id}") {
            withToken()
            json = UpdateContactOutcome(
                date = LocalDate.now().plusDays(1),
                time = LocalTime.of(9, 0),
                outcomeCode = UpdateContactOutcomeGenerator.OUTCOME.code,
                enforcementActionCode = null,
                notes = "Sensitive outcome notes",
                alert = true,
                sensitive = true
            )
        }.andExpect { status { isOk() } }

        val updated = contactRepository.findById(UpdateContactOutcomeGenerator.CONTACT_2.id).get()
        assertThat(updated.outcome?.code, equalTo(UpdateContactOutcomeGenerator.OUTCOME.code))
        assertThat(updated.notes, containsString("Sensitive outcome notes"))
        assertThat(updated.alert, equalTo(true))
        assertThat(updated.sensitive, equalTo(true))
    }

    @Test
    fun `setting alert with no active person manager returns not found`() {
        mockMvc.put("/contact/${UpdateContactOutcomeGenerator.CONTACT_NO_MANAGER.id}") {
            withToken()
            json = UpdateContactOutcome(
                date = LocalDate.now().plusDays(1),
                time = LocalTime.of(10, 0),
                outcomeCode = UpdateContactOutcomeGenerator.OUTCOME.code,
                enforcementActionCode = null,
                notes = "Alert notes",
                alert = true,
                sensitive = false
            )
        }.andExpect { status { isNotFound() } }
    }

    @Test
    fun `successfully creates enforcement action on contact outcome update`() {
        mockMvc.put("/contact/${UpdateContactOutcomeGenerator.CONTACT_3.id}") {
            withToken()
            json = UpdateContactOutcome(
                date = LocalDate.now().plusDays(1),
                time = LocalTime.of(11, 0),
                outcomeCode = UpdateContactOutcomeGenerator.OUTCOME.code,
                enforcementActionCode = UpdateContactOutcomeGenerator.ENFORCEMENT_ACTION.code,
                notes = "Enforcement notes",
                alert = false,
                sensitive = false
            )
        }.andExpect { status { isOk() } }

        val enforcements = enforcementRepository.findAll().filter {
            it.contact.id == UpdateContactOutcomeGenerator.CONTACT_3.id
        }
        assertThat(enforcements.size, equalTo(1))
        assertThat(enforcements[0].action?.code, equalTo(UpdateContactOutcomeGenerator.ENFORCEMENT_ACTION.code))
        assertThat(enforcements[0].responseDate, notNullValue())
    }

    @Test
    fun `non-compliant outcome note contains enforcement action text followed by user note and PI footer`() {
        val enforcementText = "Case administrator will send a warning letter"
        val userNote = "Discussed non-compliance with probationer"
        val combinedNotes = "$enforcementText\n$userNote"

        mockMvc.put("/contact/${UpdateContactOutcomeGenerator.CONTACT_1.id}") {
            withToken()
            json = UpdateContactOutcome(
                date = LocalDate.now().plusDays(1),
                time = LocalTime.of(10, 0),
                outcomeCode = UpdateContactOutcomeGenerator.OUTCOME.code,
                enforcementActionCode = UpdateContactOutcomeGenerator.ENFORCEMENT_ACTION.code,
                notes = combinedNotes,
                alert = false,
                sensitive = false
            )
        }.andExpect { status { isOk() } }

        val updated = contactRepository.findById(UpdateContactOutcomeGenerator.CONTACT_1.id).get()
        val notes = updated.notes ?: ""
        val enforcementIndex = notes.indexOf(enforcementText)
        val userNoteIndex = notes.indexOf(userNote)
        val footerIndex =
            notes.indexOf("This contact/note was automatically updated via the Manage people on probation integration service on")
        assertThat("enforcement text appears before user note", enforcementIndex < userNoteIndex, equalTo(true))
        assertThat("user note appears before PI footer", userNoteIndex < footerIndex, equalTo(true))
        assertThat("PI footer is present", footerIndex > -1, equalTo(true))
    }

    @Test
    fun `ftc count is incremented when enforcement action is applied`() {
        val ftcBefore = transactionTemplate.execute {
            entityManager.clear()
            eventRepository.findById(UpdateContactOutcomeGenerator.EVENT.id).get().ftcCount ?: 0L
        }

        mockMvc.put("/contact/${UpdateContactOutcomeGenerator.CONTACT_4.id}") {
            withToken()
            json = UpdateContactOutcome(
                date = LocalDate.now().plusDays(2),
                time = LocalTime.of(12, 0),
                outcomeCode = UpdateContactOutcomeGenerator.OUTCOME.code,
                enforcementActionCode = UpdateContactOutcomeGenerator.ENFORCEMENT_ACTION.code,
                notes = "FTC increment notes",
                alert = false,
                sensitive = false
            )
        }.andExpect { status { isOk() } }

        val ftcAfter = transactionTemplate.execute {
            entityManager.clear()
            eventRepository.findById(UpdateContactOutcomeGenerator.EVENT.id).get().ftcCount ?: 0L
        }
        assertThat(ftcAfter, equalTo(ftcBefore + 1))
    }

    @Test
    fun `enforcement action creates linked contact`() {
        mockMvc.put("/contact/${UpdateContactOutcomeGenerator.CONTACT_6.id}") {
            withToken()
            json = UpdateContactOutcome(
                date = LocalDate.now().plusDays(1),
                time = LocalTime.of(11, 0),
                outcomeCode = UpdateContactOutcomeGenerator.OUTCOME.code,
                enforcementActionCode = UpdateContactOutcomeGenerator.ENFORCEMENT_ACTION.code,
                notes = "Enforcement notes",
                alert = false,
                sensitive = false
            )
        }.andExpect { status { isOk() } }

        val linkedContacts =
            contactRepository.findByLinkedContactIdOrderByDateDesc(UpdateContactOutcomeGenerator.CONTACT_6.id)
        assertThat(linkedContacts.size, equalTo(1))
        assertThat(
            linkedContacts[0].type.code,
            equalTo(UpdateContactOutcomeGenerator.ENFORCEMENT_ACTION.contactType.code)
        )
        assertThat(linkedContacts[0].event?.id, equalTo(UpdateContactOutcomeGenerator.EVENT.id))
    }

    @Test
    fun `enforcement action on contact without event creates linked contact with no event`() {
        mockMvc.put("/contact/${UpdateContactOutcomeGenerator.CONTACT_5.id}") {
            withToken()
            json = UpdateContactOutcome(
                date = LocalDate.now().plusDays(3),
                time = LocalTime.of(9, 0),
                outcomeCode = UpdateContactOutcomeGenerator.PERSON_LEVEL_OUTCOME.code,
                enforcementActionCode = UpdateContactOutcomeGenerator.PERSON_LEVEL_ENFORCEMENT_ACTION.code,
                notes = "Person level enforcement notes",
                alert = false,
                sensitive = false
            )
        }.andExpect { status { isOk() } }

        val enforcements = enforcementRepository.findAll().filter {
            it.contact.id == UpdateContactOutcomeGenerator.CONTACT_5.id
        }
        assertThat(enforcements.size, equalTo(1))
        assertThat(
            enforcements[0].action?.code,
            equalTo(UpdateContactOutcomeGenerator.PERSON_LEVEL_ENFORCEMENT_ACTION.code)
        )

        val linkedContacts =
            contactRepository.findByLinkedContactIdOrderByDateDesc(UpdateContactOutcomeGenerator.CONTACT_5.id)
        assertThat(linkedContacts.size, equalTo(1))
        assertThat(linkedContacts[0].event, Matchers.nullValue())
    }

    @Test
    fun `enforcement review contact created when ftc count exceeds limit`() {
        mockMvc.put("/contact/${UpdateContactOutcomeGenerator.CONTACT_7.id}") {
            withToken()
            json = UpdateContactOutcome(
                date = LocalDate.now().plusDays(4),
                time = LocalTime.of(10, 0),
                outcomeCode = UpdateContactOutcomeGenerator.OUTCOME.code,
                enforcementActionCode = UpdateContactOutcomeGenerator.ENFORCEMENT_ACTION.code,
                notes = "FTC review notes",
                alert = false,
                sensitive = false
            )
        }.andExpect { status { isOk() } }

        val ftcAfter = transactionTemplate.execute {
            entityManager.clear()
            eventRepository.findById(UpdateContactOutcomeGenerator.FTC_EVENT.id).get().ftcCount
        }
        assertThat(ftcAfter, equalTo(2L))

        // Enforcement action linked contact + ARWS review contact
        val linkedContacts =
            contactRepository.findByLinkedContactIdOrderByDateDesc(UpdateContactOutcomeGenerator.CONTACT_7.id)
        assertThat(linkedContacts.size, equalTo(2))
        assertThat(linkedContacts.any { it.type.code == ContactLogService.REVIEW_ENFORCEMENT_STATUS }, equalTo(true))
    }

    @Test
    fun `null outcome code leaves existing outcome unchanged`() {
        val originalOutcomeCode =
            contactRepository.findById(UpdateContactOutcomeGenerator.CONTACT_9.id).get().outcome?.code

        mockMvc.put("/contact/${UpdateContactOutcomeGenerator.CONTACT_9.id}") {
            withToken()
            json = UpdateContactOutcome(
                date = LocalDate.now().plusDays(1),
                time = LocalTime.of(10, 0),
                outcomeCode = null,
                enforcementActionCode = null,
                notes = "Notes without changing outcome",
                alert = false,
                sensitive = false
            )
        }.andExpect { status { isOk() } }

        val updated = contactRepository.findById(UpdateContactOutcomeGenerator.CONTACT_9.id).get()
        assertThat(updated.outcome?.code, equalTo(originalOutcomeCode))
        assertThat(updated.notes, containsString("Notes without changing outcome"))
    }

    @Test
    fun `null alert leaves existing alert state unchanged`() {
        val before = contactRepository.findById(UpdateContactOutcomeGenerator.CONTACT_10.id).get()
        assertThat(before.alert, equalTo(true))

        mockMvc.put("/contact/${UpdateContactOutcomeGenerator.CONTACT_10.id}") {
            withToken()
            json = UpdateContactOutcome(
                date = LocalDate.now().plusDays(1),
                time = LocalTime.of(10, 0),
                outcomeCode = UpdateContactOutcomeGenerator.OUTCOME.code,
                enforcementActionCode = null,
                notes = "Notes without changing alert",
                alert = null,
                sensitive = false
            )
        }.andExpect { status { isOk() } }

        val updated = contactRepository.findById(UpdateContactOutcomeGenerator.CONTACT_10.id).get()
        assertThat(updated.alert, equalTo(true))
        assertThat(updated.notes, containsString("Notes without changing alert"))
    }

    @Test
    fun `existing enforcement is deleted when outcome is updated to compliant`() {
        // Verify the enforcement exists before the update
        val enforcementsBefore = enforcementRepository.findAll().filter {
            it.contact.id == UpdateContactOutcomeGenerator.CONTACT_8.id
        }
        assertThat(enforcementsBefore.size, equalTo(1))

        mockMvc.put("/contact/${UpdateContactOutcomeGenerator.CONTACT_8.id}") {
            withToken()
            json = UpdateContactOutcome(
                date = LocalDate.now().plusDays(1),
                time = LocalTime.of(10, 0),
                outcomeCode = UpdateContactOutcomeGenerator.CMP_COMPLIANT_OUTCOME.code,
                enforcementActionCode = null,
                notes = "Changing outcome to compliant",
                alert = false,
                sensitive = false
            )
        }.andExpect { status { isOk() } }

        val enforcementsAfter = transactionTemplate.execute {
            entityManager.clear()
            enforcementRepository.findAll().filter {
                it.contact.id == UpdateContactOutcomeGenerator.CONTACT_8.id
            }
        }
        assertThat(enforcementsAfter?.size, equalTo(0))

        val updated = contactRepository.findById(UpdateContactOutcomeGenerator.CONTACT_8.id).get()
        assertThat(updated.outcome?.code, equalTo(UpdateContactOutcomeGenerator.CMP_COMPLIANT_OUTCOME.code))
        assertThat(updated.complied, equalTo(true))
    }
}
