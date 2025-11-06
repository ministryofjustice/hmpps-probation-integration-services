package uk.gov.justice.digital.hmpps

import com.github.tomakehurst.wiremock.WireMockServer
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.data.generator.ReferenceDataGenerator
import uk.gov.justice.digital.hmpps.data.generator.UPWGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.entity.Behaviour
import uk.gov.justice.digital.hmpps.integrations.delius.entity.WorkQuality
import uk.gov.justice.digital.hmpps.model.AppointmentResponse
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.contentAsJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken
import java.util.*

@AutoConfigureMockMvc
@SpringBootTest
class AppointmentsIntegrationTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    internal lateinit var wireMockServer: WireMockServer

    @Test
    fun `can retrieve appointment details`() {
        val response = mockMvc
            .perform(get("/projects/N01DEFAULT/appointments/${UPWGenerator.DEFAULT_UPW_APPOINTMENT.id}?username=DefaultUser").withToken())
            .andExpect(status().is2xxSuccessful)
            .andReturn().response.contentAsJson<AppointmentResponse>()

        assertThat(response.version).isEqualTo(
            UUID(
                UPWGenerator.DEFAULT_UPW_APPOINTMENT.rowVersion,
                UPWGenerator.DEFAULT_CONTACT.rowVersion
            )
        )
        assertThat(response.project.name).isEqualTo("Default UPW Project")
        assertThat(response.case.crn).isEqualTo(PersonGenerator.DEFAULT_PERSON.crn)
        assertThat(response.penaltyHours).isEqualTo("01:00")
        assertThat(response.enforcementAction!!.respondBy).isEqualTo(response.date.plusDays(ReferenceDataGenerator.DEFAULT_ENFORCEMENT_ACTION.responseByPeriod))
        assertThat(response.behaviour).isEqualTo(Behaviour.EX.value)
        assertThat(response.workQuality).isEqualTo(WorkQuality.EX.value)
    }
}