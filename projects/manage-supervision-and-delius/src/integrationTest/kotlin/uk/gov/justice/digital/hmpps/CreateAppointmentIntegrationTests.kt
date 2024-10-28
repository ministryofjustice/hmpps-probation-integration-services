package uk.gov.justice.digital.hmpps

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import uk.gov.justice.digital.hmpps.api.model.appointment.CreateAppointment
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken
import java.time.Duration
import java.time.ZonedDateTime

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CreateAppointmentIntegrationTests {

    @Autowired
    internal lateinit var mockMvc: MockMvc

    @Test
    fun `when offender does not exist retuns a 404 response`() {
        mockMvc.perform(
            post("/appointments/D123456")
                .withToken()
                .withJson(
                    CreateAppointment(
                        CreateAppointment.Type.HomeVisitToCaseNS,
                        ZonedDateTime.now().plusDays(1),
                        Duration.ofMinutes(30)
                    )
                )
        ).andExpect(MockMvcResultMatchers.status().isNotFound)
    }
}