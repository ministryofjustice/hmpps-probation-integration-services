package uk.gov.justice.digital.hmpps

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.test.web.servlet.get
import uk.gov.justice.digital.hmpps.api.model.appointment.OverdueOutcome
import uk.gov.justice.digital.hmpps.api.model.appointment.OverdueOutcomeAppointments
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.contentAsJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken
import java.time.LocalDate

class GetOverdueOutcomeTest : IntegrationTestBase() {
    @Test
    fun `can retrieve appointments with an overdue outcome`() {
        val person = PersonGenerator.OVERVIEW
        val response = mockMvc.get("/appointment/${person.crn}/overdue-outcomes") { withToken() }
            .andExpect { status { isOk() } }
            .andReturn().response.contentAsJson<OverdueOutcomeAppointments>()

        assertThat(response.content).hasSize(2)
        with(response.content.first()) {
            assertThat(type).isEqualTo(OverdueOutcome.Type("COAP", "Description for COAP"))
            assertThat(date).isEqualTo(LocalDate.of(2024, 11, 27))
        }
    }
}