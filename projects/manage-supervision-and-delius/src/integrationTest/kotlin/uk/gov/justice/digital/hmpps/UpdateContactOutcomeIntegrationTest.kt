package uk.gov.justice.digital.hmpps

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.containsString
import org.hamcrest.Matchers.equalTo
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
}
