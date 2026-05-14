package uk.gov.justice.digital.hmpps

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.containsString
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.notNullValue
import org.junit.jupiter.api.Test
import org.springframework.test.web.servlet.put
import uk.gov.justice.digital.hmpps.api.model.contact.UpdateContactOutcome
import uk.gov.justice.digital.hmpps.data.generator.IdGenerator
import uk.gov.justice.digital.hmpps.data.generator.UpdateContactOutcomeGenerator
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.json
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken
import java.time.LocalDate
import java.time.LocalTime

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
        }.andExpect { status { isNotFound() } }
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
}
