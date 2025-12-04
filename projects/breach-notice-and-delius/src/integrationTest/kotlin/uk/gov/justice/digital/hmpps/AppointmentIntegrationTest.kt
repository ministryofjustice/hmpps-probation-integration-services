package uk.gov.justice.digital.hmpps

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.test.web.servlet.get
import uk.gov.justice.digital.hmpps.data.generator.AppointmentGenerator.FUTURE_APPOINTMENTS
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator.DEFAULT_PERSON_MANAGER
import uk.gov.justice.digital.hmpps.model.NextAppointmentDetails
import uk.gov.justice.digital.hmpps.service.asAppointment
import uk.gov.justice.digital.hmpps.service.asResponsibleOfficer
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.contentAsJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken

internal class AppointmentIntegrationTest : BaseIntegrationTest() {
    @Test
    fun `can retrieve future appointments successfully`() {
        val person = PersonGenerator.DEFAULT_PERSON
        val response = mockMvc.get("/next-appointment-details/${person.crn}") {
            withToken()
        }
            .andExpect { status { is2xxSuccessful() } }
            .andReturn().response.contentAsJson<NextAppointmentDetails>()

        assertThat(response).isEqualTo(
            NextAppointmentDetails(
                DEFAULT_PERSON_MANAGER.asResponsibleOfficer().copy(telephoneNumber = "07247764536"),
                FUTURE_APPOINTMENTS.map { it.asAppointment() },
            )
        )
    }
}