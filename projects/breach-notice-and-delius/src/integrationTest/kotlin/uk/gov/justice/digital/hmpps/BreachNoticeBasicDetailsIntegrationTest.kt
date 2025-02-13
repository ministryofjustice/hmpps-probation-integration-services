package uk.gov.justice.digital.hmpps

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.data.generator.MessageGenerator
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.data.generator.StaffGenerator
import uk.gov.justice.digital.hmpps.data.generator.TeamGenerator
import uk.gov.justice.digital.hmpps.message.Notification
import uk.gov.justice.digital.hmpps.messaging.HmppsChannelManager
import uk.gov.justice.digital.hmpps.model.BasicDetails
import uk.gov.justice.digital.hmpps.model.Name
import uk.gov.justice.digital.hmpps.service.toAddress
import uk.gov.justice.digital.hmpps.telemetry.TelemetryMessagingExtensions.notificationReceived
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.contentAsJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = RANDOM_PORT)
internal class BreachNoticeBasicDetailsIntegrationTest {
    @Value("\${messaging.consumer.queue}")
    lateinit var queueName: String

    @Autowired
    lateinit var channelManager: HmppsChannelManager

    @Autowired
    lateinit var mockMvc: MockMvc

    @MockitoBean
    lateinit var telemetryService: TelemetryService

    @Test
    fun `message is logged to telemetry`() {
        // Given a message
        val notification = Notification(message = MessageGenerator.BREACH_NOTICE_ADDED)

        channelManager.getChannel(queueName).publishAndWait(notification)

        // Then it is logged to telemetry
        verify(telemetryService).notificationReceived(notification)
    }

    @Test
    fun `can retrieve all basic details successfully`() {
        val person = PersonGenerator.DEFAULT_PERSON
        val username = StaffGenerator.DEFAULT_SU.username
        val response = mockMvc
            .perform(get("/basic-details/${person.crn}/$username").withToken())
            .andExpect(status().is2xxSuccessful)
            .andReturn().response.contentAsJson<BasicDetails>()

        assertThat(response).isEqualTo(
            BasicDetails(
                null,
                Name(
                    person.firstName,
                    listOfNotNull(person.secondName, person.thirdName).joinToString(" "),
                    person.surname
                ),
                listOf(PersonGenerator.DEFAULT_ADDRESS.toAddress()),
                listOf(TeamGenerator.DEFAULT_LOCATION.toAddress()),
            )
        )
    }
}
